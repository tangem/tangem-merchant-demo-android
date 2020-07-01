package com.tangem.merchant.application.ui.main

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.tangem.TangemSdk
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.extensions.Signer
import com.tangem.merchant.R
import com.tangem.merchant.application.domain.charge.ChargeSession
import com.tangem.merchant.application.domain.model.BlockchainItem
import com.tangem.merchant.application.ui.base.BaseFragment
import com.tangem.tangem_sdk_new.extensions.init
import kotlinx.android.synthetic.main.fg_main.*
import ru.dev.gbixahue.eu4d.lib.android._android.views.inflate
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log


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
        setupBlcSpinner()
        listenMerchantChanges()
        listenNumberKeyboardChanges()
        initChargeButton()
    }

    private fun setupKeyboard() {
        keyboard.setTextSize(37f)
        keyboard.setTextColor(R.color.textPrimary)
        keyboard.setKeyboardButtonClickedListener(mainVM.keyboardController)
    }

    private fun setupBlcSpinner() {
        mainVM.getBlcItemList().observe(viewLifecycleOwner, Observer { blcList ->
            spBlockchain.adapter = BlcSpinnerAdapter(blcList)
            spBlockchain.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    mainVM.blcItemChanged(blcList[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            mainVM.getSelectedBlcItem().observe(viewLifecycleOwner, Observer { blcItem ->
                if (blcItem.blockchain == Blockchain.Unknown) return@Observer

                spBlockchain.setSelection(blcList.indexOf(blcItem))
            })
        })
    }

    private fun listenMerchantChanges() {
        mainVM.getMerchantName().observe(viewLifecycleOwner, Observer { tvMerchantTitle.text = it })
        mainVM.getMerchantCurrencySymbol().observe(viewLifecycleOwner, Observer { tvFiatCurrency.text = it })
    }

    private fun listenNumberKeyboardChanges() {
        mainVM.getFiatValue().observe(viewLifecycleOwner, Observer { tvFiatValue.setText(it.localizedValue) })
    }

    private fun initChargeButton() {
        btnCharge.setOnClickListener {
            val sdk = TangemSdk.init(requireActivity())
            sdk.startSessionWithRunnable(ChargeSession(mainVM.chargeData, Signer(sdk))) {
                Log.d(this, "the charge session complete")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(this)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }
}

class BlcSpinnerAdapter(
    private val itemList: MutableList<BlockchainItem>
) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: TextView =
            convertView as? TextView ?: parent.inflate(android.R.layout.simple_spinner_item)

        view.text = itemList[position].blockchain.fullName
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: TextView = convertView as? TextView
            ?: parent.inflate(android.R.layout.simple_spinner_dropdown_item)

        view.text = itemList[position].blockchain.fullName
        return view
    }

    override fun getItem(position: Int): Any = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = itemList.size
}