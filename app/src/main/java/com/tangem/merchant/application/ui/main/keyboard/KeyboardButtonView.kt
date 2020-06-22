package com.tangem.merchant.application.ui.main.keyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.main.rippleView.RippleAnimationListener
import com.tangem.merchant.application.ui.main.rippleView.RippleView

/**
 * Created by Anton Zhilenkov on 31.08.17.
 */
class KeyboardButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), RippleAnimationListener {

    private lateinit var mRippleView: RippleView
    private lateinit var textView: TextView
    private lateinit var imageView: ImageView

    private var mKeyboardButtonClickedListener: KeyboardButtonClickedListener? = null

    init {
        initializeView(attrs, defStyleAttr)
    }

    private fun initializeView(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs != null && !isInEditMode) {
            val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.KeyboardButtonView, defStyleAttr, 0)
            val text = attributes.getString(R.styleable.KeyboardButtonView_lp_keyboard_button_text)
            val imageId = attributes.getResourceId(R.styleable.KeyboardButtonView_lp_keyboard_button_image, 0)
            val rippleEnabled = attributes.getBoolean(R.styleable.KeyboardButtonView_lp_keyboard_button_ripple_enabled, true)

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.w_keyboard_button, this) as KeyboardButtonView

            textView = view.findViewById<View>(R.id.keyboard_button_textview) as TextView
            imageView = view.findViewById<View>(R.id.keyboard_button_imageview) as ImageView

            text?.let { textView.text = it }
            if (imageId != 0) {
                val drawable = AppCompatResources.getDrawable(context, imageId)
                imageView.setImageDrawable(drawable)
                imageView.visibility = View.VISIBLE
            }

            mRippleView = view.findViewById<View>(R.id.pin_code_keyboard_button_ripple) as RippleView
            mRippleView.setRippleAnimationListener(this)
            if (!rippleEnabled) {
                mRippleView.visibility = View.INVISIBLE
            }
        }
    }

    fun setOnRippleAnimationEndListener(keyboardButtonClickedListener: KeyboardButtonClickedListener?) {
        mKeyboardButtonClickedListener = keyboardButtonClickedListener
    }

    override fun onRippleAnimationEnd() {
        mKeyboardButtonClickedListener?.onRippleAnimationEnd()
    }

    fun setText(text: String) {
        textView.text = text
    }

    fun setTextSize(size: Float) {
        textView.textSize = size
    }

    fun setTextColor(color: Int) {
        textView.setTextColor(ContextCompat.getColor(context, color))
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        onTouchEvent(event)
        return false
    }
}
