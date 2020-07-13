package com.tangem.merchant.application.domain.charge

import com.tangem.TangemError
import com.tangem.blockchain.common.TransactionError
import java.util.*

/**
 * Created by Anton Zhilenkov on 12/07/2020.
 */
abstract class BaseError : TangemError {
}

class UnknownError : TangemError {
    override val code: Int = 10000
    override var customMessage: String = "Unknown error"
}

open class ThrowableError(
    throwable: Throwable?
) : BaseError() {
    override val code: Int = 10001
    override var customMessage: String = throwable?.localizedMessage ?: "Unknown exception"
}

class NoInternetConnection: BaseError() {
    override val code: Int = 10002
    override var customMessage: String = "There is no internet connection. Please check your connection and try again"
}

class BlockchainDidNotMatch : BaseError() {
    override val code: Int = 10010
    override var customMessage: String = "Blockchain do not match"
}

class BlockchainNotSupport : BaseError() {
    override val code: Int = 10011
    override var customMessage: String = "Blockchain not support"
}

class SameWalletAddress(
    override val code: Int = 10012,
    override var customMessage: String = "Source and destination address is the same"
): BaseError()






class ValidationTransactionTransaction(
    override var customMessage: String = "Unknown transaction validation error"
) : BaseError() {
    override val code: Int = 10020

    companion object {
        fun from(errors: EnumSet<TransactionError>): ValidationTransactionTransaction {
            if (errors.isEmpty()) return ValidationTransactionTransaction()

            val delimiter = ", "
            val builder = StringBuilder()
            errors.forEach { builder.append(it.name).append(delimiter) }
            val stringedErrors = builder.substring(0, builder.length - delimiter.length).toString()
            return ValidationTransactionTransaction(customMessage = "Validation transaction error: $stringedErrors")
        }
    }
}

class TransactionSendError(
    throwable: Throwable?
) : ThrowableError(throwable) {
    override val code: Int = 10021
}