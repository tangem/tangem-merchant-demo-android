package com.tangem.merchant.application.domain.charge

import com.tangem.TangemError

/**
 * Created by Anton Zhilenkov on 15/07/2020.
 */

class BlockchainInternalErrorConverter {

    companion object {
        private val stellarInternalErrors = mapOf(
            "tx_bad_seq" to "Sequence number does not match source account",
            "tx_too_late" to "The ledger closeTime was after the maxTime",
            "tx_failedop_no_destination" to "The destination account does not exist",
            "tx_no_source_account" to "Source account not found"
        )

        fun convert(throwable: Throwable?): TangemError {
            val message = throwable?.message ?: return ThrowableError(throwable)

            val customMessage = getInternalBlockchainErrorMessage(message)
            return if (customMessage == null) ThrowableError(throwable)
            else BlockchainInternalError(customMessage)
        }

        private fun getInternalBlockchainErrorMessage(message: String): String? {
            return stellarInternalErrors[message]
        }
    }
}
