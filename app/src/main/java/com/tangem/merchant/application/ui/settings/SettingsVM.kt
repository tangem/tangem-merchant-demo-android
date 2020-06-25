package com.tangem.merchant.application.ui.settings

import androidx.lifecycle.ViewModel

/**
 * Created by Anton Zhilenkov on 25/06/2020.
 */
class SettingsVM : ViewModel() {
    var fiatIndexPosition: Int = 0

    fun fiatCurrencyChanged(item: String, position: Int) {
        fiatIndexPosition = position
    }
}