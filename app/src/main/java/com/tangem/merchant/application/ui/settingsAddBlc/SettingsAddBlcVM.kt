package com.tangem.merchant.application.ui.settingsAddBlc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tangem.blockchain.common.Blockchain
import com.tangem.merchant.application.domain.model.BlockchainItem
import com.tangem.merchant.application.ui.base.viewModel.BlockchainListVM
import com.tangem.merchant.application.ui.main.MainVM
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log

class SettingsAddBlcVM : BlockchainListVM() {
    var spinnerPosition: Int = 0

    private var blcItem: BlockchainItem = BlockchainItem(Blockchain.Unknown, "")
    private val isAddBlcBtnEnabledLD = MutableLiveData<Boolean>(false)

    fun isAddBlcButtonEnabled(): LiveData<Boolean> = isAddBlcBtnEnabledLD

    fun blockchainChanged(blockchain: Blockchain) {
        blcItem = blcItem.copy(blockchain = blockchain)
        isAddBlcBtnEnabledLD.value = blcItemIsReady()
    }

    fun addressChanged(address: String) {
        blcItem = blcItem.copy(address = address)
        isAddBlcBtnEnabledLD.value = blcItemIsReady()
    }

    fun onAddBlcItem(mainVM: MainVM) {
        if (!blcItemIsReady()) {
            Log.e(this, "Can't add blockchain $blcItem")
            return
        }

        addBlcItem(blcItem)
        mainVM.refreshBlcList()

    }

    private fun blcItemIsReady(): Boolean = blcItem.address.isNotEmpty() && blcItem.blockchain != Blockchain.Unknown
}