package com.tangem.merchant.application.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fg_main.*

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupKeyboard()

        listenMerchantChanges()
        listenNumberKeyboardChanges()
    }

    private fun setupKeyboard() {
        keyboard.setTextSize(37f)
        keyboard.setTextColor(R.color.textPrimary)
        keyboard.setKeyboardButtonClickedListener(mainVM.keyboardController)
    }

    private fun listenMerchantChanges() {
        mainVM.merchant.observe(viewLifecycleOwner, Observer {
            tvMerchantTitle.text = it.name
            tvFiatCurrency.text = it.currency.currencySymbol
        })
    }

    private fun listenNumberKeyboardChanges() {
        mainVM.fiatValue.observe(viewLifecycleOwner, Observer { tvFiatValue.text = it })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(this)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }
}