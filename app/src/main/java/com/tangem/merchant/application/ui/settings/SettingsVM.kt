package com.tangem.merchant.application.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tangem.merchant.application.domain.model.FiatCurrency
import com.tangem.merchant.common.SingleLiveEvent
import ru.dev.gbixahue.eu4d.lib.kotlin.currency.CurrencyCodeConverter

/**
 * Created by Anton Zhilenkov on 25/06/2020.
 */
class SettingsVM : ViewModel() {
    var spinnerPosition: Int = 0

    private val fiatCurrencyList = SingleLiveEvent<MutableList<FiatCurrency>>()

    fun getCurrencyList(): LiveData<MutableList<FiatCurrency>> = fiatCurrencyList

    fun currencyCodesObtained(currencyCodeList: MutableList<String>) {
        val converter = CurrencyCodeConverter()
        val currencyList = currencyCodeList.map { FiatCurrency(it, converter.convert(it)) }.toMutableList()
        fiatCurrencyList.value = currencyList
    }

}