package com.tangem.merchant.application.domain.charge

import com.tangem.TangemError

/**
 * Created by Anton Zhilenkov on 12/07/2020.
 */
abstract class BaseError : TangemError, ArgError {
    override val code: Int = 1000
    override val messageResId: Int? = null
    override val args: List<Any>? = null
}

interface ArgError {
    val args: List<Any>?
}

class CustomMessageError(
    override var customMessage: String,
) : BaseError()

class UnknownError : BaseError() {
    override var customMessage: String = "Unknown error"
}

open class ThrowableError(
    throwable: Throwable?
) : BaseError() {
    override var customMessage: String = throwable?.localizedMessage ?: "Unknown exception"
}

class NoInternetConnection : BaseError() {
    override var customMessage: String = "There is no internet connection. Please check your connection and try again"
}

class BlockchainNotSupportedByWalletManager : BaseError() {
    override var customMessage: String = "Blockchain not support"
}

class BlockchainNotProvisioned(blockchainName: String) : BaseError() {
    override var customMessage: String =
        "$blockchainName not provisioned. Please add a $blockchainName wallet in the settings page"
}

class BlockchainDoNotMatch(blockchainName: String) : BaseError() {
    override var customMessage: String = "Please choose a $blockchainName wallet to perform this transaction"
}


class SameWalletAddress(
    override var customMessage: String = "Source and destination address is the same"
) : BaseError()


data class ValidateTransactionErrors(
    val errorList: List<AmountError>,
    val builder: (List<String>) -> String
)

sealed class AmountError(
    override var customMessage: String,
) : BaseError() {
    object UnknownError : AmountError("Unknwon error")
    object AmountExceedsBalance : AmountError("Amount Exceeds Balance")
    object FeeExceedsBalance : AmountError("Fee Exceeds Balance")
    object TotalExceedsBalance : AmountError("Total Exceeds Balance")
    object InvalidAmountValue : AmountError("Invalid Amount")
    object InvalidFeeValue : AmountError("Invalid Fee")
    object DustChange : AmountError("Change is too small")
    data class DustAmount(override val args: List<Any>) : AmountError("Minimum amount is %s")
}

class CreateAccountUnderfunded(
    override val args: List<Any>,
    override var customMessage: String = String.format(
        "Target account is not created. Send more than %1\$s %2\$s to create", *args.toTypedArray()
    )
) : BaseError()