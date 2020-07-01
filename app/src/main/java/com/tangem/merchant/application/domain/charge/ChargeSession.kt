package com.tangem.merchant.application.domain.charge

import com.tangem.CardSession
import com.tangem.CardSessionRunnable
import com.tangem.TangemSdkError
import com.tangem.blockchain.common.Amount
import com.tangem.blockchain.common.TransactionSender
import com.tangem.blockchain.common.TransactionSigner
import com.tangem.blockchain.common.WalletManagerFactory
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.extensions.SimpleResult
import com.tangem.commands.CommandResponse
import com.tangem.common.CompletionResult
import com.tangem.merchant.application.domain.model.ChargeData
import kotlinx.coroutines.*
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log

class ChargeSession(
    private val data: ChargeData,
    private val signer: TransactionSigner
) : CardSessionRunnable<CommandResponse> {

    private val parentJob = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }
    private val scope = CoroutineScope(parentJob + Dispatchers.IO + exceptionHandler)

    override val performPreflightRead: Boolean = true

    override fun run(session: CardSession, callback: (result: CompletionResult<CommandResponse>) -> Unit) {
        val card = session.environment.card
        if (card == null) {
            callback(CompletionResult.Failure(TangemSdkError.CardError()))
            return
        }

        val srcBlcItem = data.blcItem.blockchain
        if (srcBlcItem.id != card.cardData?.blockchainName) {
            callback(CompletionResult.Failure(TangemSdkError.CardError()))
            return
        }

        val walletManager = try {
            WalletManagerFactory.makeWalletManager(card)
        } catch (ex: Exception) {
            null
        }

        if (walletManager == null) {
            callback(CompletionResult.Failure(TangemSdkError.CardError()))
            return
        }

        val srcAddress = walletManager.wallet.address
        val srcBlcAmount = Amount(data.priceTag, srcBlcItem, srcAddress)
        val txSender = walletManager as TransactionSender

        scope.launch {
            when (val successFeeResult = txSender.getFee(srcBlcAmount, srcAddress)) {
                is Result.Success -> {
                    Log.e(this, "txSender.getFee: Success")

                    val feeAmount = if (successFeeResult.data.size == 3) successFeeResult.data[2]
                    else successFeeResult.data[0]

                    val txData = walletManager.createTransaction(srcBlcAmount, feeAmount, srcAddress)
                    when (txSender.send(txData, signer)) {
                        is SimpleResult.Success -> {
                            Log.d(this, "txSender.send: Success")
                        }
                        is SimpleResult.Failure -> {
                            Log.e(this, "txSender.send: Failure")
                        }
                    }
                }
                is Result.Failure -> {
                    Log.e(this, "txSender.getFee: Failure")
                    callback(CompletionResult.Failure(TangemSdkError.CardError()))
                }
            }
        }

    }
}