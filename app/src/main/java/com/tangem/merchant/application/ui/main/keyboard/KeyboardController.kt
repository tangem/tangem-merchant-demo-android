package com.tangem.merchant.application.ui.main.keyboard

import com.tangem.merchant.application.domain.model.FiatValue
import com.tangem.merchant.application.ui.main.keyboard.KeyboardButtonEnum.*

/**
 * Created by Anton Zhilenkov on 25/06/2020.
 */
class NumberKeyboardController(
    private val currencyCode: String,
    private var fiatValue: FiatValue = FiatValue.create("0", currencyCode),
    var onUpdate: ((FiatValue) -> Unit)? = null
) : KeyboardButtonClickedListener {

    init {
        onUpdate?.invoke(fiatValue)
    }

    override fun onKeyboardClick(view: KeyboardButtonView, buttonCode: KeyboardButtonEnum, value: Any) {
        val sign = getSign(buttonCode)
        val oldStringValue = fiatValue.stringValue
        val newStringValue = if (sign == null) {
            if (oldStringValue.length == 1) oldStringValue
            else oldStringValue.substring(0, oldStringValue.length - 1)
        } else {
            if (oldStringValue == "0" && sign == "0") oldStringValue
            else oldStringValue + sign
        }
        fiatValue = FiatValue.create(newStringValue, currencyCode)
        onUpdate?.invoke(fiatValue)
    }

    override fun onRippleAnimationEnd() {}

    private fun getSign(buttonCode: KeyboardButtonEnum): String? = when (buttonCode) {
        BUTTON_0 -> "0"
        BUTTON_1 -> "1"
        BUTTON_2 -> "2"
        BUTTON_3 -> "3"
        BUTTON_4 -> "4"
        BUTTON_5 -> "5"
        BUTTON_6 -> "6"
        BUTTON_7 -> "7"
        BUTTON_8 -> "8"
        BUTTON_9 -> "9"
        else -> null
    }
}