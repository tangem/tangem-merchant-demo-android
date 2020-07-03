package com.tangem.merchant.application.domain.httpService.coinMarketCap

import com.tangem.merchant.application.domain.httpService.createMoshiConverterFactory
import com.tangem.merchant.application.domain.httpService.createRetrofitInstance
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal

/**
 * Created by Anton Zhilenkov on 02/07/2020.
 */
interface CoinMarketCapService {

    @GET("v1/fiat/map")
    suspend fun getFiatMap(): FiatMapResponse

    @GET("v1/tools/price-conversion")
    suspend fun getPriceConversion(
        @Query("amount") amount: BigDecimal,
        @Query("id") cryptoId: Int,
        @Query("convert") convert: String
    ): PriceConversionResponse

    companion object {
        val baseUrl = "https://pro-api.coinmarketcap.com/"

        fun create(url: String): CoinMarketCapService {
            return createRetrofitInstance(
                url,
                listOf(createCoinMarketRequestInterceptor()),
                createMoshiConverterFactory()
            ).create(CoinMarketCapService::class.java)
        }
    }
}

private fun createCoinMarketRequestInterceptor(): Interceptor {
    return object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("X-CMC_PRO_API_KEY", "f6622117-c043-47a0-8975-9d673ce484de")
            return chain.proceed(requestBuilder.build())
        }
    }
}