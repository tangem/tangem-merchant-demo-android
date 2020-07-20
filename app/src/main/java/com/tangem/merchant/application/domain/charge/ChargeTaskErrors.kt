package com.tangem.merchant.application.domain.charge

import com.tangem.TangemError

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

class NoInternetConnection : BaseError() {
    override val code: Int = 10002
    override var customMessage: String = "There is no internet connection. Please check your connection and try again"
}


class BlockchainNotSupportedByWalletManager : BaseError() {
    override val code: Int = 10010
    override var customMessage: String = "Blockchain not support"
}

class BlockchainNotProvisioned(blockchainName: String) : BaseError() {
    override val code: Int = 10011
    override var customMessage: String =
        "$blockchainName not provisioned. Please add a $blockchainName wallet in the settings page"
}

class BlockchainDoNotMatch(blockchainName: String) : BaseError() {
    override val code: Int = 10012
    override var customMessage: String = "Please choose a $blockchainName wallet to perform this transaction"
}


class SameWalletAddress(
    override val code: Int = 10020,
    override var customMessage: String = "Source and destination address is the same"
) : BaseError()

class InsufficientBalance(
    override var customMessage: String = "Insufficient balance"
) : BaseError() {
    override val code: Int = 10021
}


class BlockchainInternalError(
    override var customMessage: String
) : BaseError() {
    override val code: Int = 20000
}