package com.tangem.merchant.common.toggleWidget

import android.view.View
import android.view.ViewGroup


/**
 * Created by Anton Zhilenkov on 07/07/2020.
 */
interface ToggleState

interface StateModifier {
    fun stateChanged(container: ViewGroup, view: View, state: ToggleState)
}

interface ToggleView {
    val mainViewStateModifiers: MutableList<StateModifier>
    val toggleStateModifiers: MutableList<StateModifier>

    fun setState(state: ToggleState, andApply: Boolean = true)
    fun applyState()
    fun getView(): View
    fun getMainView(): View
    fun getToggleView(): View
}

class ToggleWidget : ToggleView {
    private val container: ViewGroup
    private val mainView: View
    private val toggleView: View

    private var state: ToggleState

    constructor(
        container: ViewGroup,
        mainView: View,
        toggleView: View,
        initialState: ToggleState,
        mainViewModifier: List<StateModifier> = mutableListOf(),
        loadingViewModifier: List<StateModifier> = mutableListOf()
    ) {
        this.container = container
        this.mainView = mainView
        this.toggleView = toggleView
        this.state = initialState
        this.mainViewStateModifiers.addAll(mainViewModifier)
        this.toggleStateModifiers.addAll(loadingViewModifier)
    }

    constructor(
        container: ViewGroup,
        mainViewId: Int,
        toggleViewId: Int,
        initialState: ToggleState,
        mainViewModifier: List<StateModifier> = mutableListOf(),
        loadingViewModifier: List<StateModifier> = mutableListOf()
    ) {
        this.container = container
        this.mainView = container.findViewById(mainViewId)
        this.toggleView = container.findViewById(toggleViewId)
        this.state = initialState
        this.mainViewStateModifiers.addAll(mainViewModifier)
        this.toggleStateModifiers.addAll(loadingViewModifier)
    }

    override val mainViewStateModifiers: MutableList<StateModifier> = mutableListOf()

    override val toggleStateModifiers: MutableList<StateModifier> = mutableListOf()

    override fun setState(state: ToggleState, andApply: Boolean) {
        this.state = state
        if (andApply) applyState()
    }

    override fun applyState() {
        mainViewStateModifiers.forEach { it.stateChanged(container, mainView, state) }
        toggleStateModifiers.forEach { it.stateChanged(container, toggleView, state) }
    }

    override fun getView(): View = container

    override fun getMainView(): View = mainView

    override fun getToggleView(): View = toggleView
}