package com.tangem.merchant.application.ui.main.keyboard

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.tangem.merchant.R

/**
 * Created by Anton Zhilenkov on 31.08.17.
 */
class KeyboardView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(mContext, attrs, defStyleAttr), View.OnClickListener {

    private var buttonClickListener: KeyboardButtonClickedListener? = null
    private var buttons: MutableList<KeyboardButtonView> = mutableListOf<KeyboardButtonView>()

    private var associations: SparseArray<KeyboardButtonEnum> = SparseArray()

    init {
        initAssociations()
        initKeyboardButtons()
    }

    private fun initAssociations() {
        associations.append(R.id.pin_code_button_coma, KeyboardButtonEnum.BUTTON_COMA)
        associations.append(R.id.pin_code_button_0, KeyboardButtonEnum.BUTTON_0)
        associations.append(R.id.pin_code_button_1, KeyboardButtonEnum.BUTTON_1)
        associations.append(R.id.pin_code_button_2, KeyboardButtonEnum.BUTTON_2)
        associations.append(R.id.pin_code_button_3, KeyboardButtonEnum.BUTTON_3)
        associations.append(R.id.pin_code_button_4, KeyboardButtonEnum.BUTTON_4)
        associations.append(R.id.pin_code_button_5, KeyboardButtonEnum.BUTTON_5)
        associations.append(R.id.pin_code_button_6, KeyboardButtonEnum.BUTTON_6)
        associations.append(R.id.pin_code_button_7, KeyboardButtonEnum.BUTTON_7)
        associations.append(R.id.pin_code_button_8, KeyboardButtonEnum.BUTTON_8)
        associations.append(R.id.pin_code_button_9, KeyboardButtonEnum.BUTTON_9)
        associations.append(R.id.pin_code_button_clear, KeyboardButtonEnum.BUTTON_CLEAR)
    }

    private fun initKeyboardButtons() {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.w_keyboard, this) as KeyboardView

        for (i in 0 until associations.size()) {
            val foundButton: KeyboardButtonView? = view.findViewById<KeyboardButtonView>(associations.keyAt(i))
            foundButton?.let {
                it.setOnClickListener(this@KeyboardView)
                buttons.add(foundButton)
            }
        }
    }

    fun setKeyboardButtonClickedListener(clickedListener: KeyboardButtonClickedListener?) {
        this.buttonClickListener = clickedListener
        for (button in buttons) {
            button.setOnRippleAnimationEndListener(clickedListener)
        }
    }

    override fun onClick(v: View) {
        if (associations.get(v.id) == null) return

        val enum = associations.get(v.id)
        buttonClickListener?.onKeyboardClick(v as KeyboardButtonView, enum, enum.buttonValue)
    }

    fun setButtonText(buttonEnum: KeyboardButtonEnum, textButton: String) {
        findButton(buttonEnum)?.setText(textButton)
    }

    fun setTextSize(size: Float) {
        buttons.forEach { it.setTextSize(size) }
    }

    fun setTextColor(color: Int) {
        buttons.forEach { it.setTextColor(color) }
    }

    private fun findButton(buttonEnum: KeyboardButtonEnum): KeyboardButtonView? {
        var idToFind = -1
        for (i in 0 until associations.size()) {
            if (associations.valueAt(i) == buttonEnum) {
                idToFind = associations.keyAt(i)
                break
            }
        }
        if (idToFind == -1) return null
        for (button in buttons) {
            if (button.id == idToFind) return button
        }
        return null
    }
}
