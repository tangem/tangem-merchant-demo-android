package com.tangem.merchant.application.ui.settingsAddBlc

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.tangem.TangemSdk
import com.tangem.blockchain.common.Blockchain
import com.tangem.merchant.BuildConfig
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.base.BaseFragment
import com.tangem.merchant.application.ui.base.adapter.spinner.BaseHintAdapter
import com.tangem.merchant.application.ui.main.MainVM
import com.tangem.tangem_sdk_new.extensions.init
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
        if (BuildConfig.DEBUG) enableDebugMenu()
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

    // only for debug mode
    private fun enableDebugMenu() {
        setHasOptionsMenu(true)
        settingsAddBlcVM.getBlcItem().observe(viewLifecycleOwner, Observer {
            val position = settingsAddBlcVM.getBlockchainList().indexOf(it.blockchain) + 1
            if (position == settingsAddBlcVM.spinnerPosition) return@Observer

            spinner.setSelection(position)
            etBlcAddress.setText(it.address)
        })
    }

    // only for debug mode
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (BuildConfig.DEBUG) inflater.inflate(R.menu.menu_add_blc, menu)
    }

    // only for debug mode
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!BuildConfig.DEBUG) return false

        return when(item.itemId) {
            R.id.menu_action_add_blockchain -> {
                val sdk = TangemSdk.init(requireActivity())
                settingsAddBlcVM.addBlcItemFromCard(sdk)
                true
            }
            else -> false
        }
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

