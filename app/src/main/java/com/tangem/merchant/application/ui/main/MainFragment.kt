package com.tangem.merchant.application.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.base.BaseFragment

/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainFragment : BaseFragment() {

    private val mainVM: MainVM by activityViewModels()

    override fun getLayoutId(): Int = R.layout.fg_main

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}