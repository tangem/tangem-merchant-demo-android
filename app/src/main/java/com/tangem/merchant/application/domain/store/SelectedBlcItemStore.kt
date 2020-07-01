package com.tangem.merchant.application.domain.store

import com.tangem.merchant.application.AppMerchant
import com.tangem.merchant.application.domain.model.Blockchain
import com.tangem.merchant.application.domain.model.BlockchainItem

/**
 * Created by Anton Zhilenkov on 01/07/2020.
 */
class SelectedBlcItemStore : BaseStore<BlockchainItem>(AppMerchant.appInstance.sharedPreferences(), "selectedBlcItem") {

    override fun getDefault(): BlockchainItem = BlockchainItem(Blockchain.Unknown, "")

    override fun fromJson(json: String): BlockchainItem {
        return gson.fromJson<BlockchainItem>(json, BlockchainItem::class.java)
    }
}