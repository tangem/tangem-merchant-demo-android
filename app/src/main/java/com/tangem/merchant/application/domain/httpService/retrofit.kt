package com.tangem.merchant.application.domain.httpService

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tangem.merchant.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by Anton Zhilenkov on 02/07/2020.
 */
fun createRetrofitInstance(
    baseUrl: String,
    interceptors: List<Interceptor>,
    converterFactory: Converter.Factory
): Retrofit {
    val okHttpBuilder = OkHttpClient.Builder()
    interceptors.forEach { okHttpBuilder.addInterceptor(it) }

    if (BuildConfig.DEBUG) okHttpBuilder.addInterceptor(createHttpLoggingInterceptor())
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(converterFactory)
        .client(okHttpBuilder.build())
        .build()
}

fun createMoshiConverterFactory(): Converter.Factory = MoshiConverterFactory.create(createMoshi())

fun createMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

private fun createHttpLoggingInterceptor(): HttpLoggingInterceptor {
    val logging = HttpLoggingInterceptor()
    logging.level = HttpLoggingInterceptor.Level.BODY
    return logging
}