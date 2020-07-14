package com.tangem.merchant.application.domain.charge

import com.tangem.CardSession
import com.tangem.CardSessionRunnable
import com.tangem.TangemSdkError
import com.tangem.blockchain.common.*
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.extensions.SimpleResult
import com.tangem.blockchain.extensions.isAboveZero
import com.tangem.commands.CommandResponse
import com.tangem.common.CompletionResult
import com.tangem.merchant.application.domain.model.ChargeData
import com.tangem.merchant.application.network.NetworkChecker
import kotlinx.coroutines.*
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class ChargeSession(
    private val data: ChargeData,
    private val feeCallback: (BigDecimal?) -> Unit
) : CardSessionRunnable<CommandResponse> {

    private val parentJob = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }
    private val scope = CoroutineScope(parentJob + Dispatchers.IO + exceptionHandler)

    override val requiresPin2: Boolean = false

    override fun run(session: CardSession, callback: (result: CompletionResult<CommandResponse>) -> Unit) {
        val card = session.environment.card
        if (card == null) {
            callback(CompletionResult.Failure(TangemSdkError.CardError()))
            return
        }

        val destBlcItem = data.blcItem
        if (destBlcItem.blockchain.id != card.cardData?.blockchainName) {
            Log.e(this, "Error: Blockchain didn't match")
            callback(CompletionResult.Failure(BlockchainDidNotMatch()))
            return
        }

        val walletManager = try {
            WalletManagerFactory.makeWalletManager(card)
        } catch (ex: Exception) {
            null
        }

        if (walletManager == null) {
            Log.e(this, "Error: Blockchain not supported")
            callback(CompletionResult.Failure(BlockchainNotSupport()))
            return
        }

        if (!isDifferentWalletAddress(walletManager.wallet.address, data.blcItem.address)) {
            Log.e(this, "Error: Source and destination address is the same")
            callback(CompletionResult.Failure(SameWalletAddress()))
            return
        }

        checkNetworkAvailabilityAndNotify(callback)
        // address в Amount важен только при использовании токена
        val amount = Amount(castDecimals(data.writeOfValue, destBlcItem.blockchain), destBlcItem.blockchain, destBlcItem.address)

        scope.launch {
            try {
                Log.d(this, "Update wallet")
                walletManager.update()
            } catch (ex: Exception) {
                Log.e(this, ex)
                callback(CompletionResult.Failure(ThrowableError(ex)))
                return@launch
            }

            checkNetworkAvailabilityAndNotify(callback)
            Log.d(this, "Get fee")
            val txSender = walletManager as TransactionSender
            when (val feeResult = txSender.getFee(amount, destBlcItem.address)) {
                is Result.Success -> {
                    Log.d(this, "Getting fee success")
                    val feeAmount = if (feeResult.data.size == 3) feeResult.data[1]
                    else feeResult.data[0]

                    feeCallback(feeAmount.value)
                    val txData = walletManager.createTransaction(amount, feeAmount, destBlcItem.address)
                    val errors = validateTransaction(amount, feeAmount, walletManager)
                    if (errors.isNotEmpty()) {
                        Log.d(this, "Error: Validate transaction error")
                        callback(CompletionResult.Failure(ValidationTransactionTransaction.from(errors)))
                        return@launch
                    }
                    Log.d(this, "Start sending of a transaction")

                    checkNetworkAvailabilityAndNotify(callback)
                    when (val result = txSender.send(txData, SessionTransactionSigner(session))) {
                        is SimpleResult.Success -> {
                            Log.d(this, "Getting fee is success")
                            callback(CompletionResult.Success(SomeSuccessResponse()))
                        }
                        is SimpleResult.Failure -> {
                            Log.e(this, "Error: ${result.error}")
                            callback(CompletionResult.Failure(TransactionSendError(result.error)))
                        }
                    }
                }
                is Result.Failure -> {
                    Log.e(this, "Error: ${feeResult.error}")
                    callback(CompletionResult.Failure(ThrowableError(feeResult.error)))
                }
            }
        }
    }

    private fun castDecimals(value: BigDecimal, blockchain: Blockchain): BigDecimal {
        return value.setScale(blockchain.decimals(), RoundingMode.HALF_UP)
    }

    private fun validateTransaction(
        amount: Amount,
        fee: Amount?,
        walletManager: WalletManager
    ): EnumSet<TransactionError> {
        val errors = EnumSet.noneOf(TransactionError::class.java)

        if (!validateAmount(amount, walletManager)) errors.add(TransactionError.WrongAmount)
        if (fee == null) return errors

        if (!validateAmount(fee, walletManager)) errors.add(TransactionError.WrongFee)
        val total = (amount.value ?: BigDecimal.ZERO) + (fee.value ?: BigDecimal.ZERO)
        if (!validateAmount(Amount(amount, total), walletManager)) errors.add(TransactionError.WrongTotal)

        return errors
    }

    private fun validateAmount(amount: Amount, walletManager: WalletManager): Boolean {
        return amount.isAboveZero() && walletManager.wallet.fundsAvailable(amount.type) >= amount.value
    }

    private fun isDifferentWalletAddress(srcAddress: String, destAddress: String):Boolean = srcAddress != destAddress

    private fun checkNetworkAvailabilityAndNotify(callback: (result: CompletionResult<CommandResponse>) -> Unit) {
        val checker = NetworkChecker.getInstance()
        val isConnected = checker.activeNetworkIsConnected()
        if (!isConnected) callback(CompletionResult.Failure(NoInternetConnection()))
    }
}

class SomeSuccessResponse : CommandResponse {

}


