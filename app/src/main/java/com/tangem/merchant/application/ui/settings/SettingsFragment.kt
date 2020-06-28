package com.tangem.merchant.application.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.tangem.merchant.R
import com.tangem.merchant.application.domain.model.FiatCurrency
import com.tangem.merchant.application.ui.base.BaseFragment
import com.tangem.merchant.application.ui.base.adapter.spinner.BaseHintAdapter
import com.tangem.merchant.application.ui.main.MainVM
import kotlinx.android.synthetic.main.fg_settings.*
import kotlinx.android.synthetic.main.w_spinner_underlined.*
import ru.dev.gbixahue.eu4d.lib.kotlin.currency.CurrencyCodeConverter


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

        val currencyCodeList = resources.getStringArray(R.array.fiat_currencies).toMutableList()
        val converter = CurrencyCodeConverter()
        val fiatCurrencyList = currencyCodeList.map { FiatCurrency(converter.convert(it), it) }.toMutableList()
        val adapter = FiatCurrencySpinnerAdapter(requireContext(), fiatCurrencyList)
        spinner.adapter = adapter
        BaseHintAdapter.setItemSelectedListener<FiatCurrency>(spinner) { fiatCurrency, position ->
            settingsVM.fiatIndexPosition = position
        }
        spinner.setSelection(settingsVM.fiatIndexPosition, true)
    }
}

class FiatCurrencySpinnerAdapter(
    context: Context,
    itemList: MutableList<FiatCurrency>
) : BaseHintAdapter<FiatCurrency>(context, itemList, R.string.spinner_hint_fiat_currency) {
    override fun getLabelFor(item: FiatCurrency): String = "${item.currencySymbol} - ${item.name}"
}

