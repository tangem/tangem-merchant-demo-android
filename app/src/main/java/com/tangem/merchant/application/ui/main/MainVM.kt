package com.tangem.merchant.application.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tangem.merchant.application.domain.model.FiatCurrency
import com.tangem.merchant.application.domain.model.Merchant
import com.tangem.merchant.application.domain.store.MerchantStore
import com.tangem.merchant.application.ui.main.keyboard.NumberKeyboardController

/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainVM : ViewModel() {
    private val decimalSeparator = ","
    val keyboardController = NumberKeyboardController(decimalSeparator, 2)
    val fiatValue = MutableLiveData<String>("0${decimalSeparator}0")

    val merchant = MutableLiveData<Merchant>()

    private val merchantStore = MerchantStore()

    init {
        setupMerchant()
        setupNumberKeyboard()
    }

    private fun setupMerchant() {
        merchant.postValue(merchantStore.restore())
    }

    private fun setupNumberKeyboard() {
        keyboardController.onUpdate = { fiatValue.postValue(it) }
    }

    fun merchantTitleChanged(title: String) {

    }

    fun merchantFiatCurrencyChanged(fiatCurrency: FiatCurrency) {

    }
}