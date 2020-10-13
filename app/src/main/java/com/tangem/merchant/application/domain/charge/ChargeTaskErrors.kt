package com.tangem.merchant.application.domain.charge

import com.tangem.TangemError

/**
 * Created by Anton Zhilenkov on 12/07/2020.
 */
abstract class BaseError : TangemError {
    override val messageResId: Int? = null
}

class UnknownError : BaseError() {
    override val code: Int = 1000
    override var customMessage: String = "Unknown error"
}

open class ThrowableError(
    throwable: Throwable?
) : BaseError() {
    override val code: Int = 1001
    override var customMessage: String = throwable?.localizedMessage ?: "Unknown exception"
}

class NoInternetConnection : BaseError() {
    override val code: Int = 1002
    override var customMessage: String = "There is no internet connection. Please check your connection and try again"
}


class BlockchainNotSupportedByWalletManager : BaseError() {
    override val code: Int = 1010
    override var customMessage: String = "Blockchain not support"
}

class BlockchainNotProvisioned(blockchainName: String) : BaseError() {
    override val code: Int = 1011
    override var customMessage: String =
        "$blockchainName not provisioned. Please add a $blockchainName wallet in the settings page"
}

class BlockchainDoNotMatch(blockchainName: String) : BaseError() {
    override val code: Int = 1012
    override var customMessage: String = "Please choose a $blockchainName wallet to perform this transaction"
}


class SameWalletAddress(
    override val code: Int = 1020,
    override var customMessage: String = "Source and destination address is the same"
) : BaseError()

class InsufficientBalance(
    override var customMessage: String = "Insufficient balance"
) : BaseError() {
    override val code: Int = 1021
}


class BlockchainInternalError(
    override var customMessage: String
) : BaseError() {
    override val code: Int = 2000
}