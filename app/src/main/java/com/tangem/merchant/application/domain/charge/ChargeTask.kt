package com.tangem.merchant.application.domain.charge

import com.tangem.CardSession
import com.tangem.CardSessionRunnable
import com.tangem.TangemSdkError
import com.tangem.blockchain.common.Amount
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.TransactionSender
import com.tangem.blockchain.common.WalletManagerFactory
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.extensions.SimpleResult
import com.tangem.commands.Card
import com.tangem.commands.CommandResponse
import com.tangem.common.CompletionResult
import com.tangem.merchant.application.domain.model.BlockchainItem
import com.tangem.merchant.application.domain.model.ChargeData
import com.tangem.merchant.application.network.NetworkChecker
import kotlinx.coroutines.*
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log
import java.math.BigDecimal
import java.math.RoundingMode

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
                callback(CompletionResult.Failure(BlockchainNotSupported(cardBlockchain.fullName)))
            } else {
                Log.e(this, "Error: Blockchain do not match")
                callback(CompletionResult.Failure(BlockchainDoNotMatch(cardBlockchain.fullName)))
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
        val amount =
            Amount(castDecimals(data.writeOfValue, destBlcItem.blockchain), destBlcItem.blockchain, destBlcItem.address)
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

            val amountError = walletManager.validateTransaction(amount, null)
            if (amountError.isNotEmpty()) {
                Log.d(this, "Error: Validate amount error")
                callback(CompletionResult.Failure(InsufficientBalance()))
                return@launch
            }
            if (!checkNetworkAvailabilityAndNotify(callback)) return@launch

            Log.d(this, "Get fee")
            val txSender = walletManager as TransactionSender
            when (val feeResult = txSender.getFee(amount, destBlcItem.address)) {
                is Result.Success -> {
                    Log.d(this, "Getting fee success")
                    val feeAmount = if (feeResult.data.size == 3) feeResult.data[1]
                    else feeResult.data[0]

                    feeCallback(feeAmount.value)
                    val validationErrors = walletManager.validateTransaction(amount, feeAmount)
                    if (validationErrors.isNotEmpty()) {
                        Log.d(this, "Error: Validate amount and feeAmount error")
                        callback(CompletionResult.Failure(InsufficientBalance()))
                    }
                    if (!checkNetworkAvailabilityAndNotify(callback)) return@launch

                    Log.d(this, "Start sending of a transaction")
                    val txData = walletManager.createTransaction(amount, feeAmount, destBlcItem.address)
                    when (val result = txSender.send(txData, SessionTransactionSigner(session))) {
                        is SimpleResult.Success -> {
                            Log.d(this, "Sending transaction is success")
                            callback(CompletionResult.Success(SomeSuccessResponse()))
                        }
                        is SimpleResult.Failure -> {
                            Log.e(this, "Error: Sending transaction: ${result.error}")
                            callback(CompletionResult.Failure(BlockchainInternalErrorConverter.convert(result.error)))
                        }
                    }
                }
                is Result.Failure -> {
                    Log.e(this, "Error: Getting fee: ${feeResult.error}")
                    callback(CompletionResult.Failure(BlockchainInternalErrorConverter.convert(feeResult.error)))
                }
            }
        }
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


