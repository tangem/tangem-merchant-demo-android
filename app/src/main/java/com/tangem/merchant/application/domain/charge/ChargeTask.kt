package com.tangem.merchant.application.domain.charge

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tangem.CardSession
import com.tangem.CardSessionRunnable
import com.tangem.TangemSdkError
import com.tangem.blockchain.common.*
import com.tangem.blockchain.extensions.Result
import com.tangem.commands.Card
import com.tangem.commands.CommandResponse
import com.tangem.common.CompletionResult
import com.tangem.merchant.application.domain.model.BlockchainItem
import com.tangem.merchant.application.domain.model.ChargeData
import com.tangem.merchant.application.network.NetworkChecker
import com.tangem.merchant.common.extensions.stripZeroPlainString
import kotlinx.coroutines.*
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class ChargeTask(
    private val data: ChargeData,
    private val blsItemList: MutableList<BlockchainItem>?,
    private val feeCallback: (BigDecimal?) -> Unit
) : CardSessionRunnable<CommandResponse> {

    private val parentJob = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }
    private val scope = CoroutineScope(parentJob + Dispatchers.IO + exceptionHandler)

    override val requiresPin2: Boolean = false

    override fun run(session: CardSession, callback: (result: CompletionResult<CommandResponse>) -> Unit) {
        val card = session.environment.card
        if (card == null) {
            callback(CompletionResult.Failure(TangemSdkError.MissingPreflightRead()))
            return
        }

        val destBlcItem = data.blcItem
        val cardBlockchain = getBlockchainFromCard(card)
        if (destBlcItem.blockchain.id != cardBlockchain.id) {
            val blockChainAlreadyAdded =
                blsItemList?.filter { it.blockchain.id == card.cardData?.blockchainName }?.isNotEmpty() ?: false
            if (blockChainAlreadyAdded) {
                Log.e(this, "Error: Please choose a ${cardBlockchain.fullName} wallet to perform this transaction")
                callback(CompletionResult.Failure(BlockchainDoNotMatch(cardBlockchain.fullName)))
            } else {
                Log.e(
                    this,
                    "Error: ${cardBlockchain.fullName} not provisioned. Please add a ${cardBlockchain.fullName} wallet in the settings page"
                )
                callback(CompletionResult.Failure(BlockchainNotProvisioned(cardBlockchain.fullName)))
            }

            return
        }

        val walletManager = try {
            WalletManagerFactory.makeWalletManager(card)
        } catch (ex: Exception) {
            null
        }
        if (walletManager == null) {
            Log.e(this, "Error: Blockchain not supported by walletManager")
            callback(CompletionResult.Failure(BlockchainNotSupportedByWalletManager()))
            return
        }

        if (!isDifferentWalletAddress(walletManager.wallet.address, data.blcItem.address)) {
            Log.e(this, "Error: Source and destination address is the same")
            callback(CompletionResult.Failure(SameWalletAddress()))
            return
        }

        // address в Amount важен только при использовании токена
        val walletAmount = walletManager.wallet.amounts[AmountType.Coin] ?: return
        val amount = Amount(walletAmount, castDecimals(data.writeOfValue, destBlcItem.blockchain))
        if (!checkNetworkAvailabilityAndNotify(callback)) return

        scope.launch {
            try {
                Log.d(this, "Update wallet")
                walletManager.update()
            } catch (ex: Exception) {
                Log.e(this, "Error: Update wallet: $ex")
                callback(CompletionResult.Failure(BlockchainInternalErrorConverter.convert(ex)))
                return@launch
            }

            if (!transactionIsValid(walletManager, amount, null, callback)) return@launch
            if (!checkNetworkAvailabilityAndNotify(callback)) return@launch

            Log.d(this, "Get fee")
            val txSender = walletManager as TransactionSender
            when (val feeResult = txSender.getFee(amount, destBlcItem.address)) {
                is Result.Success -> {
                    Log.d(this, "Getting fee success")
                    val feeAmount = if (feeResult.data.size == 3) feeResult.data[1]
                    else feeResult.data[0]

                    feeCallback(feeAmount.value)
                    if (!transactionIsValid(walletManager, amount, feeAmount, callback)) return@launch
                    if (!checkNetworkAvailabilityAndNotify(callback)) return@launch

                    Log.d(this, "Start sending of a transaction")
                    val txData = walletManager.createTransaction(amount, feeAmount, destBlcItem.address)
                    when (val result = txSender.send(txData, SessionTransactionSigner(session))) {
                        is Result.Success -> {
                            Log.d(this, "Sending transaction is success")
                            callback(CompletionResult.Success(SomeSuccessResponse()))
                        }
                        is Result.Failure -> handleFailureResult(result, callback)
                    }
                }
                is Result.Failure -> {
                    Log.e(this, "Error: Getting fee: ${feeResult.error}")
                    callback(CompletionResult.Failure(BlockchainInternalErrorConverter.convert(feeResult.error)))
                }
            }
        }
    }

    private fun handleFailureResult(result: Result.Failure, callback: (CompletionResult<CommandResponse>) -> Unit) {
        Log.e(this, "Error: Sending transaction: ${result.error}")
        when (result.error) {
            is com.tangem.blockchain.common.CreateAccountUnderfunded -> {
                val error = result.error as com.tangem.blockchain.common.CreateAccountUnderfunded
                val reserve = error.minReserve.value?.stripZeroPlainString() ?: "0"
                val symbol = error.minReserve.currencySymbol
                callback(CompletionResult.Failure(CreateAccountUnderfunded(listOf(reserve, symbol))))
            }
            is SendException -> {
                result.error?.let { FirebaseCrashlytics.getInstance().recordException(it) }
            }
            is Throwable -> {
                val message = (result.error as Throwable).message
                when {
                    message == null -> callback(CompletionResult.Failure(UnknownError()))
                    message.contains("50002") -> {
                        // user was cancelled the operation by closing the Sdk bottom sheet
                    }
                    else -> {
                        Timber.e(result.error)
                        callback(CompletionResult.Failure(BlockchainInternalErrorConverter.convert(result.error)))
                    }
                }
            }
        }
    }

    private fun transactionIsValid(
        walletManager: WalletManager,
        amount: Amount,
        feeAmount: Amount?,
        callback: (CompletionResult<CommandResponse>) -> Unit
    ): Boolean {
        val transactionError = walletManager.validateTransaction(amount, feeAmount)
        return if (transactionError.isNotEmpty()) {
            Log.d(this, "Error: Validate amount error")
            val stringError = createErrorStringFromTransactionErrors(transactionError, walletManager)
            callback(CompletionResult.Failure(CustomMessageError(stringError)))
            false
        } else {
            true
        }
    }

    private fun createErrorStringFromTransactionErrors(
        errorList: EnumSet<TransactionError>,
        walletManager: WalletManager
    ): String {
        val transactionErrors = createValidateTransactionError(errorList, walletManager)
        val messageList = transactionErrors.errorList.map {
            val args = it.args ?: listOf()
            if (args.isNotEmpty()) {
                String.format(it.customMessage, *args.toTypedArray())
            } else {
                it.customMessage
            }
        }
        return transactionErrors.builder(messageList)
    }

    private fun createValidateTransactionError(
        errorList: EnumSet<TransactionError>,
        walletManager: WalletManager
    ): ValidateTransactionErrors {
        val tapErrors = errorList.map {
            when (it) {
                TransactionError.AmountExceedsBalance -> AmountError.AmountExceedsBalance
                TransactionError.FeeExceedsBalance -> AmountError.FeeExceedsBalance
                TransactionError.TotalExceedsBalance -> AmountError.TotalExceedsBalance
                TransactionError.InvalidAmountValue -> AmountError.InvalidAmountValue
                TransactionError.InvalidFeeValue -> AmountError.InvalidFeeValue
                TransactionError.DustAmount -> {
                    AmountError.DustAmount(listOf(walletManager.dustValue?.stripZeroPlainString() ?: "0"))
                }
                TransactionError.DustChange -> AmountError.DustChange
                else -> AmountError.UnknownError
            }
        }
        return ValidateTransactionErrors(tapErrors) { it.joinToString("\r\n") }
    }

    private fun castDecimals(value: BigDecimal, blockchain: Blockchain): BigDecimal {
        return value.setScale(blockchain.decimals(), RoundingMode.HALF_UP)
    }

    private fun isDifferentWalletAddress(srcAddress: String, destAddress: String): Boolean = srcAddress != destAddress

    private fun checkNetworkAvailabilityAndNotify(callback: (result: CompletionResult<CommandResponse>) -> Unit): Boolean {
        val checker = NetworkChecker.getInstance()
        val isConnected = checker.activeNetworkIsConnected()
        if (!isConnected) callback(CompletionResult.Failure(NoInternetConnection()))
        return isConnected
    }

    private fun getBlockchainFromCard(card: Card): Blockchain = Blockchain.fromId(card.cardData?.blockchainName ?: "")
}

class SomeSuccessResponse : CommandResponse {

}