package com.tangem.merchant.application.domain.error

/**
 * Created by Anton Zhilenkov on 03/07/2020.
 */
sealed class AppError {
    class Throwable(val throwable: kotlin.Throwable) : AppError()
    class UnsupportedConversion : AppError()
    class ConversionError : AppError()
    class CoinMarketHttpError(val errorMessage: String) : AppError()
    class NoInternetConnection: AppError()
}