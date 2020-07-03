package com.tangem.merchant.application.domain.httpService.coinMarketCap

import ru.dev.gbixahue.eu4d.lib.kotlin.stringOf
import java.math.BigDecimal

/**
 * Created by Anton Zhilenkov on 02/07/2020.
 */
open class CoinMarketResponse<T : Any> {
    lateinit var status: Status
    lateinit var data: T
}

class FiatMapResponse : CoinMarketResponse<List<FiatCurrency>>()
class PriceConversionResponse : CoinMarketResponse<ConversionPrice>()
class CoinMarketError(val status: Status)

data class ConversionPrice(
    val id: String,
    val symbol: String,
    val name: String,
    val amount: Double,
    val last_updated: String,
    val quote: Map<*, *>
) {
    fun getQuotes(): Map<String, PriceConversion> {
        val map = mutableMapOf<String, PriceConversion>()
        quote.map { entry ->
            val conversionMap = entry.value as? Map<*, *> ?: null
            if (conversionMap != null) {
                val price = conversionMap["price"].toString().toBigDecimal()
                val lastUpdated = stringOf(conversionMap["last_updated"])
                map[stringOf(entry.key)] = PriceConversion(price, lastUpdated)
            }
        }
        return map
    }
}

data class Status(
    val timestamp: String,
    val error_code: Int,
    val error_message: String?,
    val elapsed: Int,
    val credit_count: Int,
    val notice: String?
)

data class FiatCurrency(
    val id: Int,
    val name: String,
    val sign: String,
    val symbol: String
)

data class PriceConversion(
    val price: BigDecimal,
    val last_updated: String
)

data class ErrorMessage(
    val message: String? = null,
    val throwable: Throwable? = null,
    val messageId: Int? = null
)