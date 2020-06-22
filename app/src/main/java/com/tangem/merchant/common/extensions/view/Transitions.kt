package com.tangem.merchant.common.extensions.view

import android.view.ViewGroup
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager

/**
 * Created by Anton Zhilenkov on 14/04/2020.
 */
fun ViewGroup.beginDelayedTransition(transition: Transition = AutoTransition()) {
    TransitionManager.beginDelayedTransition(this, transition)
}