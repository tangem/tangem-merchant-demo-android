package com.tangem.merchant.application.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tangem.merchant.R
import com.tangem.merchant.application.domain.model.FiatCurrency
import com.tangem.merchant.application.ui.base.BaseFragment
import com.tangem.merchant.application.ui.base.adapter.spinner.BaseHintAdapter
import com.tangem.merchant.application.ui.main.MainVM
import com.tangem.merchant.application.ui.settingsAddBlc.BlcRvAdapter
import com.tangem.merchant.application.ui.settingsAddBlc.SpaceItemDivider
import kotlinx.android.synthetic.main.fg_settings.*
import kotlinx.android.synthetic.main.w_spinner_underlined.*
import ru.dev.gbixahue.eu4d.lib.android._android.views.afterTextChanged
import ru.dev.gbixahue.eu4d.lib.android._android.views.moveCursorToEnd
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
    }

    private fun initMerchantTitle() {
        val merchantTitleWatcher = etMerchantTitle.afterTextChanged { mainVM.merchantNameChanged(it) }

        mainVM.getMerchantName().observe(viewLifecycleOwner, Observer {
            if (etMerchantTitle.text.toString() == it) return@Observer

            etMerchantTitle.removeTextChangedListener(merchantTitleWatcher)
            etMerchantTitle.setText(it)
            if (etMerchantTitle.hasFocus()) etMerchantTitle.moveCursorToEnd()
            etMerchantTitle.addTextChangedListener(merchantTitleWatcher)
        })
    }


    private fun initSpinner() {
        settingsVM.currencyCodesObtained(resources.getStringArray(R.array.fiat_currencies).toMutableList())
        settingsVM.getCurrencyList().observe(viewLifecycleOwner, Observer { fiatCurrencyList ->
            val adapter = FiatCurrencySpinnerAdapter(requireContext(), fiatCurrencyList)
            spinner.adapter = adapter
            BaseHintAdapter.setItemSelectedListener<FiatCurrency>(spinner) { fiatCurrency, position ->
                settingsVM.spinnerPosition = position
                mainVM.fiatCurrencyCodeChanged(fiatCurrency)
            }

            mainVM.getMerchantCurrencyCode().observe(viewLifecycleOwner, Observer { code ->
                val foundFiatCurrency = fiatCurrencyList.firstOrNull { it.code == code } ?: return@Observer

                settingsVM.spinnerPosition = fiatCurrencyList.indexOf(foundFiatCurrency) + 1
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
            adapter.notifyItemRemoved(aPos)
        }
        rvBlc.addItemDecoration(SpaceItemDivider(8))
        rvBlc.adapter = adapter
        rvBlc.setHasFixedSize(true)

        mainVM.getBlcItemList().observe(viewLifecycleOwner, Observer { blcList ->
            if (isDeleting) {
                isDeleting = false
                return@Observer
            }

            Log.d(this, "getBlcItemList size: ${blcList.size}")
//            (rvBlc.parent as ViewGroup).beginDelayedTransition()
            adapter.setItemList(blcList)
            adapter.notifyDataSetChanged()
        })
    }


}

class FiatCurrencySpinnerAdapter(
    context: Context,
    itemList: MutableList<FiatCurrency>
) : BaseHintAdapter<FiatCurrency>(context, itemList, R.string.spinner_hint_fiat_currency) {
    override fun getLabelFor(item: FiatCurrency): String = "${item.code} - ${item.name}"
}

