package com.tangem.merchant.application.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tangem.merchant.application.domain.model.FiatCurrency
import com.tangem.merchant.application.domain.model.Merchant
import com.tangem.merchant.application.domain.store.MerchantStore
import com.tangem.merchant.application.ui.base.viewModel.BlockchainListVM
import com.tangem.merchant.application.ui.main.keyboard.NumberKeyboardController

/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainVM : BlockchainListVM() {
    val keyboardController = NumberKeyboardController(DECIMAL_SEPARATOR, 2)

    private val fiatValueLD = MutableLiveData<String>("0${DECIMAL_SEPARATOR}0")
    private val merchantNameLD = MutableLiveData<String>()
    private val merchantCurrencyCodeLD = MutableLiveData<String>()

    private var merchantModel: Merchant
    private val merchantStore = MerchantStore()

    fun getFiatValue(): LiveData<String> = fiatValueLD
    fun getMerchantName(): LiveData<String> = merchantNameLD
    fun getMerchantCurrencyCode(): LiveData<String> = merchantCurrencyCodeLD

    init {
        merchantModel = merchantStore.restore()
        merchantNameLD.value = merchantModel.name
        merchantCurrencyCodeLD.value = merchantModel.fiatCurrency.code

        keyboardController.onUpdate = { fiatValueLD.postValue(it) }
    }

    fun merchantNameChanged(name: String) {
        merchantNameLD.value = name
        merchantModel = merchantModel.copy(name = name)
        merchantStore.save(merchantModel)
    }

    fun fiatCurrencyCodeChanged(fiatCurrency: FiatCurrency) {
        merchantCurrencyCodeLD.value = fiatCurrency.code
        merchantModel = merchantModel.copy(fiatCurrency = fiatCurrency)
        merchantStore.save(merchantModel)
    }

    companion object {
        val DECIMAL_SEPARATOR = ","
    }
}