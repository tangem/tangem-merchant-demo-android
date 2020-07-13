package com.tangem.merchant.application.ui.settingsAddBlc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tangem.blockchain.common.Blockchain
import com.tangem.merchant.application.domain.model.BlockchainItem
import com.tangem.merchant.application.ui.base.viewModel.BlcItemListVM
import com.tangem.merchant.application.ui.main.MainVM
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log

class SettingsAddBlcVM : BlcItemListVM() {
    var spinnerPosition: Int = 0

    private var blcItem: BlockchainItem = BlockchainItem(Blockchain.Unknown, "")
    private val isAddBlcBtnEnabledLD = MutableLiveData<Boolean>(false)
    private val isBlcAddressValidLD = MutableLiveData<Boolean>()

    fun getBlockchainList(): MutableList<Blockchain> {
        return Blockchain.values().filter {
            it != Blockchain.Unknown && it != Blockchain.Ducatus && !it.id.contains("test")
        }.toMutableList()
    }

    fun isAddBlcButtonEnabled(): LiveData<Boolean> = isAddBlcBtnEnabledLD
    fun isBlcAddressValid(): LiveData<Boolean> = isAddBlcBtnEnabledLD

    fun blockchainChanged(blockchain: Blockchain) {
        blcItem = blcItem.copy(blockchain = blockchain)
        changeStateOfAddBlcBtn()
    }

    fun addressChanged(address: String) {
        blcItem = blcItem.copy(address = address)
        changeStateOfAddBlcBtn()
    }

    fun onAddBlcItem(mainVM: MainVM) {
        if (!blcItemIsReady()) {
            Log.e(this, "Can't add blockchain $blcItem")
            return
        }

        addBlcItem(blcItem)
        mainVM.refreshBlcList()
    }

    private fun changeStateOfAddBlcBtn() {
        isAddBlcBtnEnabledLD.value = blcItemIsReady() && blcAddressIsValid()
    }

    private fun blcAddressIsValid(): Boolean {
        isBlcAddressValidLD.value = blcItem.blockchain.validateAddress(blcItem.address)
        return isBlcAddressValidLD.value!!
    }

    private fun blcItemIsReady(): Boolean = blcItem.address.isNotEmpty() && blcItem.blockchain != Blockchain.Unknown
}