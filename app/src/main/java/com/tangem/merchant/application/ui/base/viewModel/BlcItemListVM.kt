package com.tangem.merchant.application.ui.base.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tangem.merchant.application.domain.model.BlockchainItem
import com.tangem.merchant.application.domain.store.BlcListStore

/**
 * Created by Anton Zhilenkov on 29/06/2020.
 */
open class BlcItemListVM : BaseVM() {
    protected val blcItemListLD = MutableLiveData<MutableList<BlockchainItem>>()
    protected val blcListStore = BlcListStore()

    init {
        restoreBlcItemList()
    }

    fun getBlcItemList(): LiveData<MutableList<BlockchainItem>> = blcItemListLD

    protected fun restoreBlcItemList() {
        blcItemListLD.value = blcListStore.restore().toMutableList()
    }

    fun addBlcItem(item: BlockchainItem) {
        val itemList = blcItemListLD.value ?: return

        itemList.add(item)
        blcItemListLD.value = itemList
        blcListStore.save(itemList)
    }

    fun deleteBlcItem(blc: BlockchainItem) {
        val itemList = blcItemListLD.value ?: return

        itemList.remove(blc)
        blcItemListLD.value = itemList
        blcListStore.save(itemList)
    }

    fun refreshBlcList() {
        restoreBlcItemList()
    }
}