package com.tangem.merchant.application.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.tangem.merchant.R
import com.tangem.merchant.application.network.httpService.coinMarketCap.FiatCurrency
import com.tangem.merchant.application.ui.MainActivity
import com.tangem.merchant.application.ui.base.BaseFragment
import com.tangem.merchant.application.ui.base.adapter.spinner.BaseHintAdapter
import com.tangem.merchant.application.ui.main.MainVM
import com.tangem.merchant.application.ui.settingsAddBlc.BlcRvAdapter
import com.tangem.merchant.application.ui.settingsAddBlc.SpaceItemDivider
import kotlinx.android.synthetic.main.fg_settings.*
import kotlinx.android.synthetic.main.w_spinner_underlined.*
import ru.dev.gbixahue.eu4d.lib.android._android.components.dimenFrom
import ru.dev.gbixahue.eu4d.lib.android._android.components.toast
import ru.dev.gbixahue.eu4d.lib.android._android.views.afterTextChanged
import ru.dev.gbixahue.eu4d.lib.android._android.views.moveCursorToEnd
import ru.dev.gbixahue.eu4d.lib.android._android.views.show
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log


/**
 * Created by Anton Zhilenkov on 25/06/2020.
 */
class SettingsFragment : BaseFragment() {

    private val settingsVM: SettingsVM by viewModels()
    private val mainVM: MainVM by activityViewModels()

    override fun getLayoutId() = R.layout.fg_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clAddBlc.setOnClickListener { navigateTo(R.id.nav_screen_settings_add_blc) }

        initMerchantTitle()
        initSpinner()
        initBlcRecycler()
        initLaunchButton()
        addOnBackPressHandler()
    }

    private fun addOnBackPressHandler() {
        val activity = activity ?: return

        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mainVM.canNavigateUpFromSettingsScreen()) {
                    if (!Navigation.findNavController(mainView).navigateUp()) requireActivity().finish()
                } else {
                    activity.toast(R.string.e_not_enough_data_for_launch, Toast.LENGTH_LONG)
                }
            }
        })
    }

    private fun initMerchantTitle() {
        val merchantTitleWatcher = etMerchantTitle.afterTextChanged {
            mainVM.merchantNameChanged(it)
            updateLaunchButtonState()
        }

        mainVM.getMerchantName().observe(viewLifecycleOwner, Observer {
            if (etMerchantTitle.text.toString() == it) return@Observer

            etMerchantTitle.removeTextChangedListener(merchantTitleWatcher)
            etMerchantTitle.setText(it)
            if (etMerchantTitle.hasFocus()) etMerchantTitle.moveCursorToEnd()
            etMerchantTitle.addTextChangedListener(merchantTitleWatcher)
        })
    }

    private fun initSpinner() {
        val adapter = FiatCurrencySpinnerAdapter(requireContext(), listOf())
        spinner.adapter = adapter
        BaseHintAdapter.setItemSelectedListener<FiatCurrency>(spinner) { fiatCurrency, position ->
            settingsVM.spinnerPosition = position
            mainVM.fiatCurrencyChanged(fiatCurrency)
            updateLaunchButtonState()
        }

        mainVM.getFiatCurrencyList().observe(viewLifecycleOwner, Observer { fiatCurrencyList ->
            adapter.setItemList(fiatCurrencyList)
            adapter.notifyDataSetChanged()

            mainVM.getMerchantFiatCurrency().observe(viewLifecycleOwner, Observer { fiatCurrency ->
                val found = fiatCurrencyList.firstOrNull { it == fiatCurrency } ?: return@Observer

                settingsVM.spinnerPosition = fiatCurrencyList.indexOf(found) + 1
                spinner.setSelection(settingsVM.spinnerPosition, true)
            })
        })
    }

    private fun initBlcRecycler() {
        var isDeleting = false
        val adapter = BlcRvAdapter { aPos, lPos, blc ->
            isDeleting = true
            mainVM.deleteBlcItem(blc)
            val adapter = rvBlc.adapter as? BlcRvAdapter ?: return@BlcRvAdapter

            adapter.removeItem(aPos)
            adapter.notifyDataSetChanged()
            rvBlc.requestLayout()
            updateLaunchButtonState()
        }
        rvBlc.addItemDecoration(SpaceItemDivider(8))
        rvBlc.adapter = adapter

        mainVM.getBlcItemList().observe(viewLifecycleOwner, Observer { blcList ->
            if (isDeleting) {
                isDeleting = false
                return@Observer
            }

            Log.d(this, "getBlcItemList size: ${blcList.size}")
            adapter.setItemList(blcList)
            adapter.notifyDataSetChanged()
            updateLaunchButtonState()
        })
    }

    private fun initLaunchButton() {
        btnLaunchApp.show(mainVM.startFromSettingsScreen)
        val bottomPadding = if (!mainVM.startFromSettingsScreen) 0
        else requireContext().dimenFrom(R.dimen.rv_blockchain_bottom_paddin).toInt()

        rvBlc.setPadding(0, 0, 0, bottomPadding)
        btnLaunchApp.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    private fun updateLaunchButtonState() {
        btnLaunchApp.isEnabled = mainVM.isDataEnoughForLaunch()
    }
}

class FiatCurrencySpinnerAdapter(
    context: Context,
    itemList: List<FiatCurrency>
) : BaseHintAdapter<FiatCurrency>(context, itemList.toMutableList(), R.string.spinner_hint_fiat_currency) {
    override fun getLabelFor(item: FiatCurrency): String = "${item.sign} - ${item.symbol}"
}

