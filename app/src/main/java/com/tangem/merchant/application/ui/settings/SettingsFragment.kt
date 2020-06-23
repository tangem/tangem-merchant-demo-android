package com.tangem.merchant.application.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fg_settings.*

class SettingsFragment : BaseFragment() {

    private val settingsVM: SettingsVM by viewModels()

    override fun getLayoutId() = R.layout.fg_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clAddBlc.setOnClickListener {
            navigateTo(R.id.nav_screen_settings_add_blc)
        }
    }
}