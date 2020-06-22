package com.tangem.merchant.application.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.base.BaseFragment

/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainFragment : BaseFragment() {

    private val mainVM: MainVM by activityViewModels()

    override fun getLayoutId(): Int = R.layout.fg_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(this)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }
}