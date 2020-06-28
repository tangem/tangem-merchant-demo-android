package com.tangem.merchant.application.domain.model

/**
 * Created by Anton Zhilenkov on 26/06/2020.
 */
data class Merchant(val name: String, val currency: FiatCurrency) {
    companion object {
        fun default(): Merchant = Merchant("Merchant title", FiatCurrency("Dollar US", "$"))
    }
}

data class FiatCurrency(val name: String, val currencySymbol: String)

data class BlockchainModel(val name: String, val address: String)
