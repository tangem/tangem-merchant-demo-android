package com.tangem.merchant.application.ui.main.keyboard

/**
 * Created by Anton Zhilenkov on 31.08.17.
 */
interface KeyboardButtonClickedListener {
    fun onKeyboardClick(view: KeyboardButtonView, buttonCode: KeyboardButtonEnum, value: Any)
    fun onRippleAnimationEnd()
}
