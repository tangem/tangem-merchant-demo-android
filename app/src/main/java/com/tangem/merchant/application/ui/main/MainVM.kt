package com.tangem.merchant.application.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tangem.blockchain.common.Blockchain
import com.tangem.merchant.application.domain.model.*
import com.tangem.merchant.application.domain.store.MerchantStore
import com.tangem.merchant.application.domain.store.SelectedBlcItemStore
import com.tangem.merchant.application.ui.base.viewModel.BlockchainListVM
import com.tangem.merchant.application.ui.main.keyboard.NumberKeyboardController
import com.tangem.merchant.common.AppDataChecker
import com.tangem.merchant.common.FirstLaunchChecker
import java.util.*

/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainVM : BlockchainListVM() {
    private val initialFiatValue: String = "0"

    var startFromSettingsScreen = false
    var chargeData = ChargeData(BlockchainItem(Blockchain.Unknown, ""), initialFiatValue.toBigDecimal())

    lateinit var keyboardController: NumberKeyboardController

    private val fiatValueLD = MutableLiveData<FiatValue>()
    private val merchantNameLD = MutableLiveData<String>()
    private val merchantCurrencySymbolLD = MutableLiveData<String>()
    private val selectedBlcItemLD = MutableLiveData<BlockchainItem>()

    private var merchantModel: Merchant
    private val merchantStore = MerchantStore()
    private val selectedBlcItemStore = SelectedBlcItemStore()

    fun getFiatValue(): LiveData<FiatValue> = fiatValueLD
    fun getMerchantName(): LiveData<String> = merchantNameLD
    fun getMerchantCurrencySymbol(): LiveData<String> = merchantCurrencySymbolLD
    fun getSelectedBlcItem(): LiveData<BlockchainItem> = selectedBlcItemLD

    init {
        merchantModel = merchantStore.restore()
        merchantNameLD.value = merchantModel.name
        merchantCurrencySymbolLD.value = merchantModel.fiatCurrency?.symbol
        selectedBlcItemLD.value = selectedBlcItemStore.restore()

        fiatValueLD.value = createFiatValue(merchantModel)
        initKeyboardController()
        keyboardController.onUpdate = { fiatValue ->
            chargeData = chargeData.copy(priceTag = fiatValue.value)
            fiatValueLD.value = fiatValue
        }
    }

    fun isDataEnoughForLaunch(): Boolean {
        if (FirstLaunchChecker().isFirstLaunch()) return false
        return AppDataChecker().isDataEnough()
    }

    fun merchantNameChanged(name: String) {
        merchantNameLD.value = name
        merchantModel = merchantModel.copy(name = name)
        merchantStore.save(merchantModel)
    }

    fun fiatCurrencyChanged(fiatCurrency: FiatCurrency) {
        merchantCurrencySymbolLD.value = fiatCurrency.symbol
        merchantModel = merchantModel.copy(fiatCurrency = fiatCurrency)
        fiatValueLD.value = createFiatValue(merchantModel, fiatValueLD.value)
        initKeyboardController()
        merchantStore.save(merchantModel)
    }

    private fun initKeyboardController() {
        val code = getCurrencyCode(merchantModel)
        val fiat = fiatValueLD.value ?: createFiatValue(merchantModel)
        if (::keyboardController.isInitialized) {
            keyboardController = NumberKeyboardController(code, fiat, keyboardController.onUpdate)
        } else {
            keyboardController = NumberKeyboardController(code, fiat)
        }
    }

    fun blcItemChanged(blcItem: BlockchainItem) {
        chargeData = chargeData.copy(blcItem = blcItem)
        if (selectedBlcItemLD.value == blcItem) return

        selectedBlcItemStore.save(blcItem)
        selectedBlcItemLD.value = blcItem
    }

    private fun createFiatValue(merchant: Merchant, oldFiatValue: FiatValue? = null): FiatValue {
        return FiatValue.create(oldFiatValue?.stringValue ?: initialFiatValue, getCurrencyCode(merchant))
    }

    private fun getCurrencyCode(merchant: Merchant): String {
        return merchant.fiatCurrency?.code ?: Currency.getInstance(Locale.getDefault()).currencyCode
    }
}