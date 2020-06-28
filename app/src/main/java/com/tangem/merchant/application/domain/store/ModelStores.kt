package com.tangem.merchant.application.domain.store

import com.google.gson.reflect.TypeToken
import com.tangem.merchant.application.AppMerchant
import com.tangem.merchant.application.domain.model.BlockchainModel
import com.tangem.merchant.application.domain.model.Merchant

/**
 * Created by Anton Zhilenkov on 28/06/2020.
 */
class MerchantStore : BaseStore<Merchant>(AppMerchant.appInstance.sharedPreferences(), "merchant") {
    override fun getDefault(): Merchant = Merchant.default()
    override fun fromJson(json: String): Merchant = gson.fromJson(json, Merchant::class.java)
}

class BlcListStore : BaseStore<List<BlockchainModel>>(AppMerchant.appInstance.sharedPreferences(), "blcList") {

    override fun getDefault(): List<BlockchainModel> = mutableListOf()

    override fun fromJson(json: String): List<BlockchainModel> {
        return gson.fromJson<List<BlockchainModel>>(json, object : TypeToken<List<BlockchainModel>>() {}.type)
    }
}