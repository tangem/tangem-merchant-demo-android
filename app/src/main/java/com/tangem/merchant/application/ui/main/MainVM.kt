package com.tangem.merchant.application.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tangem.TangemSdk
import com.tangem.blockchain.common.Blockchain
import com.tangem.commands.CommandResponse
import com.tangem.common.CompletionResult
import com.tangem.merchant.application.domain.charge.ChargeTask
import com.tangem.merchant.application.domain.error.AppError
import com.tangem.merchant.application.domain.error.AppMessage
import com.tangem.merchant.application.domain.model.BlockchainItem
import com.tangem.merchant.application.domain.model.ChargeData
import com.tangem.merchant.application.domain.model.FiatValue
import com.tangem.merchant.application.domain.model.Merchant
import com.tangem.merchant.application.domain.store.FiatCurrencyListStore
import com.tangem.merchant.application.domain.store.MerchantStore
import com.tangem.merchant.application.domain.store.SelectedBlcItemStore
import com.tangem.merchant.application.network.NetworkChecker
import com.tangem.merchant.application.network.httpService.coinMarketCap.CoinMarket
import com.tangem.merchant.application.network.httpService.coinMarketCap.FiatCurrency
import com.tangem.merchant.application.ui.base.viewModel.BlcItemListVM
import com.tangem.merchant.application.ui.main.keyboard.NumberKeyboardController
import com.tangem.merchant.common.AppDataChecker
import com.tangem.merchant.common.FirstLaunchChecker
import com.tangem.merchant.common.toggleWidget.ProgressState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log
import ru.dev.gbixahue.eu4d.lib.android.global.threading.postUI
import java.math.BigDecimal
import java.util.*


/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainVM : BlcItemListVM() {
    var startFromSettingsScreen = false

    // the initialFiatValue must be String for properly works of the NumberKeyboardController
    val initialFiatValue: String = "0"
    lateinit var keyboardController: NumberKeyboardController

    val networkChecker: NetworkChecker = NetworkChecker.getInstance()

    private val coinMarket = CoinMarket() { errorMessageSLE.postValue(it) }

    private var chargeData = ChargeData(BlockchainItem(Blockchain.Unknown, ""), BigDecimal.ZERO)

    private var merchant: Merchant
    private val merchantStore = MerchantStore()
    private val merchantNameLD = MutableLiveData<String>()
    private val merchantFiatCurrencyLD = MutableLiveData<FiatCurrency>()

    private val fiatValueLD = MutableLiveData<FiatValue>()
    private val convertedFiatValueLD = MutableLiveData<BigDecimal>(BigDecimal.ZERO)
    private val calculatedFeeValueLD = MutableLiveData<String>("")
    private val fiatCurrencyListLD = MutableLiveData<List<FiatCurrency>>(listOf())
    private val fiatCurrencyListStore = FiatCurrencyListStore()

    private val selectedBlcItemLD = MutableLiveData<BlockchainItem>()
    private val selectedBlcItemStore = SelectedBlcItemStore()

    private val uiIsEnabledLD = MutableLiveData<Boolean>()

    fun getMerchantName(): LiveData<String> = merchantNameLD
    fun getMerchantFiatCurrency(): LiveData<FiatCurrency> = merchantFiatCurrencyLD
    fun getFiatValue(): LiveData<FiatValue> = fiatValueLD
    fun getConvertedFiatValue(): LiveData<BigDecimal> = convertedFiatValueLD
    fun getCalculatedFeeValue(): LiveData<String> = calculatedFeeValueLD
    fun getFiatCurrencyList(): LiveData<List<FiatCurrency>> = fiatCurrencyListLD
    fun getSelectedBlcItem(): LiveData<BlockchainItem> = selectedBlcItemLD
    fun getUiLockState(): LiveData<Boolean> = uiIsEnabledLD
    fun getChargeData(): ChargeData = chargeData

    init {
        startFromSettingsScreen = !isDataEnoughForLaunch()
        loadFiatCurrencyList()
        merchant = merchantStore.restore()
        merchantNameLD.value = merchant.name
        merchantFiatCurrencyLD.value = merchant.fiatCurrency
        selectedBlcItemLD.value = selectedBlcItemStore.restore()

        fiatValueLD.value = createFiatValue(merchant)
        initKeyboardController()
        keyboardController.onUpdate = { fiatValue ->
            fiatValueLD.value = fiatValue
        }
    }

    fun canNavigateUpFromSettingsScreen(): Boolean {
        if (startFromSettingsScreen) return true
        return isDataEnoughForLaunch()
    }

    private fun loadFiatCurrencyList() {
        val currencyList = fiatCurrencyListStore.restore()
        fun sortCurrencies(list: List<FiatCurrency>) {
            Collections.sort(list, kotlin.Comparator { o1, o2 -> o1.name.compareTo(o2.name) })
        }
        if (currencyList.isEmpty()) {
            if (!checkNetworkAvailabilityAndNotify()) return

            coinMarket.loadFiatMap {
                sortCurrencies(it)
                fiatCurrencyListLD.postValue(it)
                fiatCurrencyListStore.save(it)
            }
        } else {
            fiatCurrencyListLD.postValue(currencyList)
            if (!checkNetworkAvailabilityAndNotify()) return

            coinMarket.loadFiatMap {
                sortCurrencies(it)
                fiatCurrencyListLD.postValue(it)
                fiatCurrencyListStore.save(it)
            }
        }
    }

    fun isDataEnoughForLaunch(): Boolean {
        if (FirstLaunchChecker().isFirstLaunch()) return false
        return AppDataChecker().isDataEnough()
    }

    fun merchantNameChanged(name: String) {
        merchant = merchant.copy(name = name)
        merchantNameLD.value = merchant.name
        merchantStore.save(merchant)
    }

    fun fiatCurrencyChanged(fiatCurrency: FiatCurrency) {
        merchant = merchant.copy(fiatCurrency = fiatCurrency)
        merchantStore.save(merchant)
        merchantFiatCurrencyLD.value = merchant.fiatCurrency

        fiatValueLD.value = createFiatValue(merchant, fiatValueLD.value)
        initKeyboardController()
    }

    private fun initKeyboardController() {
        val code = getCurrencyCode(merchant)
        val fiat = fiatValueLD.value ?: createFiatValue(merchant)
        if (::keyboardController.isInitialized) {
            keyboardController = NumberKeyboardController(code, fiat, onUpdate = keyboardController.onUpdate)
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

    private fun chargeSessionCompleted(result: CompletionResult<CommandResponse>) {
        when (result) {
            is CompletionResult.Success -> {
                keyboardController.reset()
                messageSLE.value = AppMessage.ChargeSessionCompleted()
            }
            is CompletionResult.Failure -> {
            }
        }
    }

    private fun createFiatValue(merchant: Merchant, oldFiatValue: FiatValue? = null): FiatValue {
        return FiatValue.create(oldFiatValue?.stringValue ?: initialFiatValue, getCurrencyCode(merchant))
    }

    private fun getCurrencyCode(merchant: Merchant): String {
        return merchant.fiatCurrency?.symbol ?: Currency.getInstance(Locale.getDefault()).currencyCode
    }

    fun calculateConversion(progressStateHandler: ((ProgressState) -> Unit)? = null) {
        val fiatValue = fiatValueLD.value ?: return
        if (fiatValue.stringValue == "0") {
            convertedFiatValueLD.postValue(BigDecimal.ZERO)
            chargeData = chargeData.copy(writeOfValue = BigDecimal.ZERO)
            return
        }

        progressStateHandler?.invoke(ProgressState.Progress())
        coinMarket.scope.launch {
            delay(600)
            if (fiatValue.value != fiatValueLD.value?.value) return@launch
            // end of delay

            if (!checkNetworkAvailabilityAndNotify()) {
                convertedFiatValueLD.postValue(convertedFiatValueLD.value)
                progressStateHandler?.invoke(ProgressState.None())
                return@launch
            }

            val blcItem = selectedBlcItemLD.value ?: return@launch
            val currencyList = fiatCurrencyListLD.value ?: return@launch
            val fiatCurrency = merchantFiatCurrencyLD.value ?: return@launch

            val currency = currencyList.firstOrNull { it.symbol == fiatCurrency.symbol }
            if (currency == null) {
                errorMessageSLE.postValue(AppError.UnsupportedConversion())
                progressStateHandler?.invoke(ProgressState.None())
                return@launch
            }

            coinMarket.convertFiatValue(
                fiatValue.value,
                blcItem.blockchain,
                currency,
                progressStateHandler,
                {
                    chargeData = chargeData.copy(writeOfValue = it)
                    convertedFiatValueLD.postValue(it)
                }
            )
        }
    }

    private fun checkNetworkAvailabilityAndNotify(): Boolean {
        val isConnected = networkChecker.activeNetworkIsConnected()
        if (!isConnected) errorMessageSLE.postValue(AppError.NoInternetConnection())
        return isConnected
    }

    fun startChargeSession(sdk: TangemSdk) {
        sdk.startSessionWithRunnable(ChargeTask(getChargeData(), blcItemListLD.value) {
            Log.d(this, "Calculated fee: BigDecimal: $it")
            Log.d(this, "Calculated fee: BigDecimal.toPlainString: ${it?.toPlainString()}")
            calculatedFeeValueLD.postValue(it?.toPlainString() ?: "")
        }) {
            Log.d(this, "the charge session complete")
            postUI { chargeSessionCompleted(it) }
        }
    }
}