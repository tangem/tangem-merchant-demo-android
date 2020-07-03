package com.tangem.merchant.application.domain.store

import com.google.gson.reflect.TypeToken
import com.tangem.merchant.application.AppMerchant
import com.tangem.merchant.application.domain.httpService.coinMarketCap.FiatCurrency
import com.tangem.merchant.application.domain.model.BlockchainItem
import com.tangem.merchant.application.domain.model.Merchant

/**
 * Created by Anton Zhilenkov on 28/06/2020.
 */
class MerchantStore : BaseStore<Merchant>(AppMerchant.appInstance.sharedPreferences(), "merchant") {
    override fun getDefault(): Merchant = Merchant.default()
    override fun fromJson(json: String): Merchant = gson.fromJson(json, Merchant::class.java)
}

class BlcListStore : BaseStore<List<BlockchainItem>>(AppMerchant.appInstance.sharedPreferences(), "blcList") {

    override fun getDefault(): List<BlockchainItem> = mutableListOf()

    override fun fromJson(json: String): List<BlockchainItem> {
        return gson.fromJson<List<BlockchainItem>>(json, object : TypeToken<List<BlockchainItem>>() {}.type)
    }
}

class FiatCurrencyListStore :
    BaseStore<List<FiatCurrency>>(AppMerchant.appInstance.sharedPreferences(), "fiatCurrencyList") {

    override fun getDefault(): List<FiatCurrency> = mutableListOf()

    override fun fromJson(json: String): List<FiatCurrency> {
        return gson.fromJson<List<FiatCurrency>>(json, object : TypeToken<List<FiatCurrency>>() {}.type)
    }
}