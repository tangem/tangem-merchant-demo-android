package com.tangem.merchant.application.ui.settingsAddBlc

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.tangem.blockchain.common.Blockchain
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.base.BaseFragment
import com.tangem.merchant.application.ui.base.adapter.spinner.BaseHintAdapter
import com.tangem.merchant.application.ui.main.MainVM
import kotlinx.android.synthetic.main.fg_settings_add_blc.*
import kotlinx.android.synthetic.main.w_spinner_underlined.*
import ru.dev.gbixahue.eu4d.lib.android._android.components.colorFrom
import ru.dev.gbixahue.eu4d.lib.android._android.views.afterTextChanged

class SettingsAddBlcFragment : BaseFragment() {

    private val settingsAddBlcVM: SettingsAddBlcVM by viewModels()
    private val mainVM: MainVM by activityViewModels()

    override fun getLayoutId() = R.layout.fg_settings_add_blc

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSpinner()
        initAddress()
        initAddBlcButton()
    }

    private fun initSpinner() {
        spinner.adapter = BlockchainSpinnerAdapter(requireContext(), settingsAddBlcVM.getBlockchainList())
        BaseHintAdapter.setItemSelectedListener<Blockchain>(spinner) { blockchain, position ->
            settingsAddBlcVM.spinnerPosition = position
            settingsAddBlcVM.blockchainChanged(blockchain)
        }
        spinner.setSelection(settingsAddBlcVM.spinnerPosition, true)
    }

    private fun initAddress() {
        etBlcAddress.afterTextChanged { settingsAddBlcVM.addressChanged(it) }
        etBlcAddress.setOnImeActionListener(EditorInfo.IME_ACTION_DONE) {
            etBlcAddress.clearFocus()
            settingsAddBlcVM.onAddBlcItem(mainVM)
            return@setOnImeActionListener false
        }

        var prevColor: Int? = null
        settingsAddBlcVM.isBlcAddressValid().observe(viewLifecycleOwner, Observer {
            if (prevColor == null) prevColor = etBlcAddress.currentTextColor
            if (it) etBlcAddress.setTextColor(prevColor!!)
            else etBlcAddress.setTextColor(requireContext().colorFrom(android.R.color.holo_red_dark))
        })
    }


    private fun initAddBlcButton() {
        btnAddBlc.setOnClickListener {
            settingsAddBlcVM.onAddBlcItem(mainVM)
            Navigation.findNavController(requireView()).popBackStack()
        }
        settingsAddBlcVM.isAddBlcButtonEnabled().observe(viewLifecycleOwner, Observer {
            btnAddBlc.isEnabled = it
        })
    }

}

class BlockchainSpinnerAdapter(
    context: Context,
    itemList: MutableList<Blockchain>
) : BaseHintAdapter<Blockchain>(context, itemList, R.string.spinner_hint_choose_blc) {

    override fun getLabelFor(item: Blockchain): String = "${item.currency} - ${item.fullName} "
}

fun EditText.setOnImeActionListener(action: Int, handler: () -> Boolean) {
    this.setOnEditorActionListener { view, actionId, event ->
        if (actionId == action) {
            return@setOnEditorActionListener handler.invoke()
        }
        return@setOnEditorActionListener false
    }
}

