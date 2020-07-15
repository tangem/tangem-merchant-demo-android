package com.tangem.merchant.application.ui.main

import android.content.Context
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
import com.tangem.merchant.R
import com.tangem.merchant.application.domain.model.BlockchainItem
import com.tangem.merchant.application.ui.base.BaseFragment
import com.tangem.merchant.common.toggleWidget.*
import com.tangem.tangem_sdk_new.extensions.init
import kotlinx.android.synthetic.main.fg_main.*
import ru.dev.gbixahue.eu4d.lib.android._android.components.stringFrom
import ru.dev.gbixahue.eu4d.lib.android._android.views.inflate
import ru.dev.gbixahue.eu4d.lib.android.global.threading.postUI


/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainFragment : BaseFragment() {

    private val mainVM: MainVM by activityViewModels()
    private lateinit var loadingButton: ToggleWidget

    override fun getLayoutId(): Int = R.layout.fg_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initChargeButton()
        setupKeyboard()
        listenBlcItemSpinnerChanges()
        listenMerchantChanges()
        listenNumberKeyboardChanges()
        listenConversionChanges()
        listenFeeCalculation()
        listenLockUiStateChanges()
    }

    private fun initChargeButton() {
        loadingButton = ToggleWidget(flTest, btnCharge, progress, ProgressState.None())
        loadingButton.setupIndeterminateProgress(requireContext())
        btnCharge.setOnClickListener { mainVM.startChargeSession(TangemSdk.init(requireActivity())) }
    }

    private fun setupKeyboard() {
        keyboard.setTextSize(37f)
        keyboard.setTextColor(R.color.textPrimary)
        keyboard.setKeyboardButtonClickedListener(mainVM.keyboardController)
    }

    private fun listenBlcItemSpinnerChanges() {
        mainVM.getBlcItemList().observe(viewLifecycleOwner, Observer { blcList ->
            spBlockchain.adapter = BlcSpinnerAdapter(blcList)
            spBlockchain.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    mainVM.blcItemChanged(blcList[position])
                    mainVM.calculateConversion()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            //restore
            mainVM.getSelectedBlcItem().observe(viewLifecycleOwner, Observer { blcItem ->
                if (blcItem.blockchain == Blockchain.Unknown) return@Observer

                spBlockchain.setSelection(blcList.indexOf(blcItem))
                tvBlockchainCurrency.text = blcItem.blockchain.currency
            })
        })
    }

    private fun listenMerchantChanges() {
        mainVM.getMerchantName().observe(viewLifecycleOwner, Observer { tvMerchantTitle.text = it })
    }

    private fun listenNumberKeyboardChanges() {
        mainVM.getFiatValue().observe(viewLifecycleOwner, Observer {
            tvFiatValue.text = it.localizedValue
            tvFeeValue.text = "0"
            mainVM.calculateConversion { postUI { loadingButton.setState(ProgressState.Progress()) } }
        })
    }

    private fun listenConversionChanges() {
        mainVM.getConvertedFiatValue().observe(viewLifecycleOwner, Observer {
            tvBlockchainValue.text = it.toString()
            loadingButton.setState(ProgressState.None())
        })
    }

    private fun listenFeeCalculation() {
        mainVM.getCalculatedFeeValue().observe(viewLifecycleOwner, Observer { tvFeeValue.text = it })
    }

    private fun listenLockUiStateChanges() {
        mainVM.getUiLockState().observe(viewLifecycleOwner, Observer {
            btnCharge.isEnabled = it
        })
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
        val view: TextView = convertView as? TextView ?: parent.inflate(android.R.layout.simple_spinner_item)

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


fun ToggleWidget.setupIndeterminateProgress(context: Context) {
    mainViewStateModifiers.clear()
    mainViewStateModifiers.add(ReplaceTextStateModifier(context.stringFrom(R.string.btn_charge), ""))
    mainViewStateModifiers.add(EnableDisableStateModifier())
    toggleStateModifiers.clear()
    toggleStateModifiers.add(ShowHideStateModifier())
}