package com.tangem.merchant.application.ui.settingsAddBlc

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.base.BaseFragment
import com.tangem.merchant.application.ui.base.adapter.spinner.DefaultItemSpinnerAdapter
import kotlinx.android.synthetic.main.w_spinner_underlined.*

class SettingsAddBlcFragment : BaseFragment() {

    private val settingsAddBlcVM: SettingsAddBlcVM by viewModels()

    override fun getLayoutId() = R.layout.fg_settings_add_blc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = resources.getStringArray(R.array.blc_names).toMutableList()
        list.add(getString(R.string.spinner_hint_choose_blc))
        DefaultItemSpinnerAdapter(list, spinner, listener = { item, position ->
//            settingsAddBlcVM.fiatCurrencyChanged(item, position)
        })
    }
}