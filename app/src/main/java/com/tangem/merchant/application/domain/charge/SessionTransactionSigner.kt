package com.tangem.merchant.application.domain.charge

import com.tangem.CardSession
import com.tangem.blockchain.common.TransactionSigner
import com.tangem.commands.SignCommand
import com.tangem.commands.SignResponse
import com.tangem.common.CompletionResult
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log
import kotlin.coroutines.resume

/**
 * Created by Anton Zhilenkov on 12/07/2020.
 */
class SessionTransactionSigner(
    private val session: CardSession
) : TransactionSigner {
    override suspend fun sign(hashes: Array<ByteArray>, cardId: String): CompletionResult<SignResponse> =
        suspendCancellableCoroutine { continuation ->
            Log.d(this, "sign transaction...")
            SignCommand(hashes).run(session) {
                if (continuation.isActive) {
                    continuation.resume(it)
                }
            }
        }
}