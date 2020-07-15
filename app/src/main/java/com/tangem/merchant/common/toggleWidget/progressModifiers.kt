package com.tangem.merchant.common.toggleWidget

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.dev.gbixahue.eu4d.lib.android._android.views.enableLayoutAnimation

/**
 * Created by Anton Zhilenkov on 07/07/2020.
 */

sealed class ProgressState : ToggleState {
    class Progress : ProgressState()
    class None : ProgressState()
}

class EnableDisableStateModifier(
    private val isEnableOnLoading: Boolean = false
) : StateModifier {

    override fun stateChanged(container: ViewGroup, view: View, state: ToggleState) {
        view.isEnabled = when (state) {
            is ProgressState.Progress -> isEnableOnLoading
            is ProgressState.None -> !isEnableOnLoading
            else -> return
        }
    }
}

class ReplaceTextStateModifier(
    private val initialText: String,
    private val replaceText: String = ""
) : StateModifier {

    override fun stateChanged(container: ViewGroup, view: View, state: ToggleState) {
        val tv = view as? TextView ?: return

        when (state) {
            is ProgressState.Progress -> {
                tv.text = replaceText
            }
            is ProgressState.None -> {
                tv.text = initialText
            }
        }
    }
}

class ShowHideStateModifier(
    private val isShowOnLoading: Boolean = true,
    private val typeOfHiding: Int = View.INVISIBLE
) : StateModifier {

    override fun stateChanged(container: ViewGroup, view: View, state: ToggleState) {
        container.enableLayoutAnimation()
        view.visibility = when (state) {
            is ProgressState.Progress -> if (isShowOnLoading) View.VISIBLE else typeOfHiding
            is ProgressState.None -> if (isShowOnLoading) typeOfHiding else View.VISIBLE
            else -> return
        }
    }
}

class ClickableStateModifier(
    private val isClickableOnLoading: Boolean = false
) : StateModifier {

    override fun stateChanged(container: ViewGroup, view: View, state: ToggleState) {
        view.isClickable = when (state) {
            is ProgressState.Progress -> isClickableOnLoading
            is ProgressState.None -> !isClickableOnLoading
            else -> return

        }
    }
}