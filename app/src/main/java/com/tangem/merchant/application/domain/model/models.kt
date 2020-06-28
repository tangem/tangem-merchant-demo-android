package com.tangem.merchant.application.domain.model

/**
 * Created by Anton Zhilenkov on 26/06/2020.
 */
data class Merchant(val name: String, val fiatCurrency: FiatCurrency) {
    companion object {
        fun default(): Merchant = Merchant("John Doe", FiatCurrency("", ""))
    }
}

data class FiatCurrency(val name: String, val code: String)

data class BlockchainModel(val name: String, val address: String)
