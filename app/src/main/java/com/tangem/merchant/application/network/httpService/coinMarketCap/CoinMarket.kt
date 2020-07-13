package com.tangem.merchant.application.network.httpService.coinMarketCap

import com.squareup.moshi.JsonAdapter
import com.tangem.blockchain.common.Blockchain
import com.tangem.merchant.application.domain.error.AppError
import com.tangem.merchant.application.network.httpService.createMoshi
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.math.BigDecimal

/**
 * Created by Anton Zhilenkov on 02/07/2020.
 */
typealias ErrorMessageHandler = (AppError) -> Unit

class CoinMarket(
    private val errorMessageHandler: ErrorMessageHandler
) {

    private val coinMarketApi = CoinMarketCapService.create(CoinMarketCapService.baseUrl)
    private val moshi = createMoshi()
    private var conversionInActiveState = false

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        conversionInActiveState = false
        when (throwable) {
            is HttpException -> {
                val stringBody = throwable.response()?.errorBody()?.string() ?: return@CoroutineExceptionHandler
                val jsonAdapter: JsonAdapter<CoinMarketErrorResponse> =
                    moshi.adapter(CoinMarketErrorResponse::class.java)
                val coinMarketError: CoinMarketErrorResponse =
                    jsonAdapter.fromJson(stringBody) ?: return@CoroutineExceptionHandler

                val status = coinMarketError.status
                val message = "code: ${status.error_code}, ${status.error_message}"
                errorMessageHandler(AppError.CoinMarketHttpError(message))
            }
            else -> errorMessageHandler(AppError.Throwable(throwable))
        }
    }

    private val job: Job = Job()
        get() = if (!field.isActive) Job() else field

    val scope: CoroutineScope
        get() = CoroutineScope(job + Dispatchers.IO + exceptionHandler)

    fun loadFiatMap(callback: (List<FiatCurrency>) -> Unit) {
        scope.launch { callback(coinMarketApi.getFiatMap().data) }
    }

    fun convertFiatValue(
        value: BigDecimal,
        blockchain: Blockchain,
        currency: FiatCurrency,
        callbackUiStateIsActive: (Boolean) -> Unit,
        callbackConversion: (BigDecimal) -> Unit
    ) {
        if (conversionInActiveState) return

        conversionInActiveState = true
        scope.launch {
            callbackUiStateIsActive(false)

            val conversion = coinMarketApi.getPriceConversion(value, currency.id, blockchain.currency)
            val conversionPrice = conversion.data.getQuotes()[blockchain.currency]
            if (conversionPrice == null) {
                conversionInActiveState = false
                errorMessageHandler(AppError.ConversionError())
                return@launch
            } else {
                callbackConversion(conversionPrice.price)
                callbackUiStateIsActive(true)
                conversionInActiveState = false
            }
        }
    }
}