package com.tangem.merchant.application.ui.main.keyboard

import com.tangem.merchant.application.ui.main.keyboard.KeyboardButtonEnum.*

/**
 * Created by Anton Zhilenkov on 25/06/2020.
 */
class NumberKeyboardController(
    private val decimalSeparator: String,
    private val decimalCount: Int
) : KeyboardButtonClickedListener {

    var onUpdate: ((String) -> Unit)? = null
        set(value) {
            field = value
            updateValue()
        }

    private val ZERO = "0"
    private val FULL_ZERO = ZERO + decimalSeparator + ZERO

    private var numberPart: String = ZERO
    private var decimalPart: String = ZERO

    private val value: String
        get() = numberPart + decimalSeparator + decimalPart


    private var isDecimalPart = false

    fun setValue(newValue: String) {
        if (newValue.contains(decimalSeparator)) {
            val splitted = newValue.split(decimalSeparator)
            numberPart = splitted[0].leaveOnlyNumbers()
            decimalPart = splitted[1].leaveOnlyNumbers()
        } else {
            numberPart = newValue.leaveOnlyNumbers()
        }
        updateValue()
    }

    override fun onKeyboardClick(view: KeyboardButtonView, buttonCode: KeyboardButtonEnum, value: Any) {
        val digit = getDigitSign(buttonCode)
        if (digit == null) {
            when (buttonCode) {
                BUTTON_COMA -> isDecimalPart = true
                BUTTON_CLEAR -> onErase()
            }
        } else {
            onAdd(digit)
        }
        updateValue()
    }

    private fun onAdd(digit: Int) {
        if (isDecimalPart) {
            if (decimalPart.length == decimalCount) return

            decimalPart = if (decimalPart == ZERO) "$digit" else "$decimalPart$digit"
        } else {
            numberPart = if (numberPart == ZERO) "$digit" else "$numberPart$digit"
        }
    }

    private fun onErase() {
        if (value == FULL_ZERO) return

        fun deleteLastSign(raw: String): String {
            return if (raw == ZERO || raw.isEmpty()) raw
            else raw.substring(0, raw.length - 1)
        }
        if (isDecimalPart) {
            isDecimalPart = !(decimalPart == ZERO || decimalPart.length == 1)
            if (decimalPart == ZERO) return

            decimalPart = if (decimalPart.length == 1) ZERO else deleteLastSign(decimalPart)
        } else {
            numberPart = if (numberPart.length == 1) ZERO else deleteLastSign(numberPart)
        }

    }

    private fun updateValue() {
        onUpdate?.invoke(value)
    }

    override fun onRippleAnimationEnd() {}

    private fun getDigitSign(buttonCode: KeyboardButtonEnum): Int? {
        return when (buttonCode) {
            BUTTON_0 -> 0
            BUTTON_1 -> 1
            BUTTON_2 -> 2
            BUTTON_3 -> 3
            BUTTON_4 -> 4
            BUTTON_5 -> 5
            BUTTON_6 -> 6
            BUTTON_7 -> 7
            BUTTON_8 -> 8
            BUTTON_9 -> 9
            else -> null
        }
    }
}

private fun String.leaveOnlyNumbers(): String = this.replace(Regex("[^0-9]"), "")