package com.tangem.merchant.application.ui.settingsAddBlc

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.base.BaseFragment

class SettingsAddBlcFragment : BaseFragment() {

    private val settingsAddBlcVM: SettingsAddBlcVM by viewModels()

    override fun getLayoutId() = R.layout.fg_settings_add_blc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
    }
}