package com.tangem.merchant.application.domain.charge

import com.tangem.CardSession
import com.tangem.EncryptionMode
import com.tangem.TangemSdkError
import com.tangem.blockchain.common.TransactionSigner
import com.tangem.commands.SignCommand
import com.tangem.commands.SignResponse
import com.tangem.common.CompletionResult
import com.tangem.common.apdu.CommandApdu
import com.tangem.common.apdu.ResponseApdu
import com.tangem.common.apdu.StatusWord
import com.tangem.common.apdu.toTangemSdkError
import com.tangem.common.extensions.toInt
import com.tangem.common.tlv.TlvTag
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log

/**
 * Created by Anton Zhilenkov on 12/07/2020.
 */
open class BaseCommand {

    protected fun transceiveApdu(apdu: CommandApdu, session: CardSession, callback: (result: CompletionResult<ResponseApdu>) -> Unit) {
        Log.d(this, "transceiveApdu... send...")
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
                                    Log.e(this, "Encryption doesn't work")
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
                is CompletionResult.Failure -> {
                    Log.e(this, "transceiveApdu... error")
                    if (result.error is TangemSdkError.TagLost) {
                        session.viewDelegate.onTagLost()
                    } else {
                        callback(CompletionResult.Failure(result.error))
                    }
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

class SessionTransactionSigner(
    private val session: CardSession
) : BaseCommand(), TransactionSigner {
    override suspend fun sign(hashes: Array<ByteArray>, cardId: String): CompletionResult<SignResponse> {
        return suspendCancellableCoroutine { continuation ->
            Log.d(this, "sign transaction...")
            val signCommand = SignCommand(hashes)
            transceiveApdu(signCommand.serialize(session.environment), session) {
                when (it) {
                    is CompletionResult.Success -> {
                        Log.d(this, "sign transaction success")
                        val signResponse: SignResponse = signCommand.deserialize(session.environment, it.data)
                        if (continuation.isActive) {
                            continuation.resumeWith(Result.success(CompletionResult.Success(signResponse)))
                        }
                    }
                    is CompletionResult.Failure -> {
                        Log.e(this, "sign transaction error: ${it.error}")
                        if (continuation.isActive) {
                            continuation.resumeWith(Result.success(CompletionResult.Failure(it.error)))
                        }
                    }
                }
            }
        }
    }
}

// было бы классно дать возможность запускать из сессии уже подготовленные команды )

class SessionReadCommand(
    private val session: CardSession,
    private val callback: (result: CompletionResult<ResponseApdu>) -> Unit
) : BaseCommand() {

}
