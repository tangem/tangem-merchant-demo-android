package com.tangem.merchant.application.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.base.BaseFragment
import com.tangem.merchant.application.ui.base.adapter.spinner.DefaultItemSpinnerAdapter
import kotlinx.android.synthetic.main.fg_settings.*
import kotlinx.android.synthetic.main.w_spinner_underlined.*

/**
 * Created by Anton Zhilenkov on 25/06/2020.
 */
class SettingsFragment : BaseFragment() {

    private val settingsVM: SettingsVM by viewModels()

    override fun getLayoutId() = R.layout.fg_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clAddBlc.setOnClickListener { navigateTo(R.id.nav_screen_settings_add_blc) }

        val list = resources.getStringArray(R.array.fiat_currencies).toMutableList()
        list.add(getString(R.string.spinner_hint_fiat_currency))
        DefaultItemSpinnerAdapter(list, spinner, listener = { item, position ->
            settingsVM.fiatCurrencyChanged(item, position)
        })
    }

}