package com.tangem.merchant.common

import com.tangem.merchant.application.domain.store.BlcListStore
import com.tangem.merchant.application.domain.store.MerchantStore

class AppDataChecker {
    private val merchantStore = MerchantStore()
    private val blcListStore = BlcListStore()

    fun isDataEnough(): Boolean  {
        if (!blcListStore.has()) return false
        if (!merchantStore.has()) return false

        val merchant = merchantStore.restore()
        return merchant.fiatCurrency != null && merchant.name.isNotEmpty() && blcListStore.restore().isNotEmpty()
    }
}