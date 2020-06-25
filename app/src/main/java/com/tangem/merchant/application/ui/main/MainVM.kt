package com.tangem.merchant.application.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tangem.merchant.application.ui.main.keyboard.NumberKeyboardController

/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainVM : ViewModel() {
    val keyboardController = NumberKeyboardController(",")
    val fiatValue = MutableLiveData<String>("0,0")

    init {
        setupNumberKeyboard()
    }

    private fun setupNumberKeyboard() {
        keyboardController.onUpdate = { fiatValue.postValue(it) }
    }
}