package com.tangem.merchant.application.domain.charge

import com.tangem.*
import com.tangem.blockchain.common.*
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.extensions.SimpleResult
import com.tangem.blockchain.extensions.isAboveZero
import com.tangem.commands.CommandResponse
import com.tangem.commands.SignCommand
import com.tangem.commands.SignResponse
import com.tangem.common.CompletionResult
import com.tangem.common.apdu.CommandApdu
import com.tangem.common.apdu.ResponseApdu
import com.tangem.common.apdu.StatusWord
import com.tangem.common.apdu.toTangemSdkError
import com.tangem.common.extensions.toInt
import com.tangem.common.tlv.TlvTag
import com.tangem.merchant.application.domain.model.ChargeData
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.util.*

class ChargeSession(
    private val data: ChargeData
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

        val destBlcItem = data.blcItem
        if (destBlcItem.blockchain.id != card.cardData?.blockchainName) {
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

        // address в Amount важен только при использовании токена
        val amount = Amount(data.writeOfValue, destBlcItem.blockchain, destBlcItem.address)

        scope.launch {
            val txSender = walletManager as TransactionSender
            walletManager.update()

            when (val successFeeResult = txSender.getFee(amount, destBlcItem.address)) {
                is Result.Success -> {
                    val feeAmount = if (successFeeResult.data.size == 3) successFeeResult.data[1]
                    else successFeeResult.data[0]

                    val txData = walletManager.createTransaction(amount, feeAmount, destBlcItem.address)
                    val errors = validateTransaction(amount, feeAmount, walletManager)
                    if (errors.isNotEmpty()) {
                        callback(CompletionResult.Failure(TangemSdkError.CardError()))
                        return@launch
                    }
                    when (txSender.send(txData, SessionTransactionSigner(session))) {
                        is SimpleResult.Success -> {
                            callback(CompletionResult.Success(SomeSuccessResponse()))
                        }
                        is SimpleResult.Failure -> {
                            callback(CompletionResult.Failure(TangemSdkError.CardError()))
                        }
                    }
                }
                is Result.Failure -> {
                    callback(CompletionResult.Failure(TangemSdkError.CardError()))
                }
            }
        }
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
}

class SomeSuccessResponse : CommandResponse {

}

class SessionTransactionSigner(
    private val session: CardSession
) : TransactionSigner {
    override suspend fun sign(hashes: Array<ByteArray>, cardId: String): CompletionResult<SignResponse> {
        return suspendCancellableCoroutine { continuation ->
            val signCommand = SignCommand(hashes)
            transceiveApdu(signCommand.serialize(session.environment), session) {
                when (it) {
                    is CompletionResult.Success -> {
                        val signResponse: SignResponse = signCommand.deserialize(session.environment, it.data)
                        if (continuation.isActive) {
                            continuation.resumeWith(kotlin.Result.success(CompletionResult.Success(signResponse)))
                        }
                    }
                    is CompletionResult.Failure -> {
                        if (continuation.isActive) {
                            continuation.resumeWith(kotlin.Result.success(CompletionResult.Failure(it.error)))
                        }
                    }
                }
            }
        }
    }

    private fun transceiveApdu(
        apdu: CommandApdu,
        session: CardSession,
        callback: (result: CompletionResult<ResponseApdu>) -> Unit
    ) {
        session.send(apdu) { result ->
            when (result) {
                is CompletionResult.Success -> {
                    val responseApdu = result.data

                    when (responseApdu.statusWord) {
                        StatusWord.ProcessCompleted,
                        StatusWord.Pin1Changed, StatusWord.Pin2Changed -> {
                            callback(CompletionResult.Success(responseApdu))
                        }
                        StatusWord.NeedPause -> {
                            // NeedPause is returned from the card whenever security delay is triggered.
                            val remainingTime = deserializeSecurityDelay(responseApdu)
                            if (remainingTime != null) {
                                val totalDuration = session.environment.card?.pauseBeforePin2 ?: 0
                                session.viewDelegate.onSecurityDelay(remainingTime, totalDuration)
                            }
                            transceiveApdu(apdu, session, callback)
                        }
                        StatusWord.NeedEncryption -> {
                            when (session.environment.encryptionMode) {
                                EncryptionMode.NONE -> {
                                    session.environment.encryptionKey = null
                                    session.environment.encryptionMode = EncryptionMode.FAST
                                }
                                EncryptionMode.FAST -> {
                                    session.environment.encryptionKey = null
                                    session.environment.encryptionMode = EncryptionMode.STRONG
                                }
                                EncryptionMode.STRONG -> {
                                    Log.e(this::class.simpleName!!, "Encryption doesn't work")
                                    callback(CompletionResult.Failure(TangemSdkError.NeedEncryption()))
                                    return@send
                                }
                            }
                            transceiveApdu(apdu, session, callback)
                        }
                        else -> {
                            val error = responseApdu.statusWord.toTangemSdkError()
                            if (error != null && !tryHandleError(error)) {
                                callback(CompletionResult.Failure(error))
                            } else {
                                callback(CompletionResult.Failure(TangemSdkError.UnknownError()))
                            }
                        }
                    }
                }
                is CompletionResult.Failure ->
                    if (result.error is TangemSdkError.TagLost) {
                        session.viewDelegate.onTagLost()
                    } else {
                        callback(CompletionResult.Failure(result.error))
                    }
            }
        }
    }

    private fun deserializeSecurityDelay(responseApdu: ResponseApdu): Int? {
        val tlv = responseApdu.getTlvData()
        return tlv?.find { it.tag == TlvTag.Pause }?.value?.toInt()
    }

    private fun tryHandleError(error: TangemSdkError): Boolean {
        return false
    }
}