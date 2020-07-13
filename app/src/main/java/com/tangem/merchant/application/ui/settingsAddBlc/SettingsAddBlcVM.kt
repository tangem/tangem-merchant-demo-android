package com.tangem.merchant.application.ui.settingsAddBlc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tangem.TangemSdk
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.Token
import com.tangem.blockchain.common.Wallet
import com.tangem.commands.Card
import com.tangem.common.CompletionResult
import com.tangem.merchant.application.domain.model.BlockchainItem
import com.tangem.merchant.application.ui.base.viewModel.BlcItemListVM
import com.tangem.merchant.application.ui.main.MainVM
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log
import ru.dev.gbixahue.eu4d.lib.android.global.threading.postUI

class SettingsAddBlcVM : BlcItemListVM() {
    var spinnerPosition: Int = 0

    private var blcItem: BlockchainItem = BlockchainItem(Blockchain.Unknown, "")

    private val blcItemLD = MutableLiveData<BlockchainItem>()
    private val isAddBlcBtnEnabledLD = MutableLiveData<Boolean>(false)
    private val isBlcAddressValidLD = MutableLiveData<Boolean>()

    fun getBlockchainList(): MutableList<Blockchain> {
        return Blockchain.values().filter {
            it != Blockchain.Unknown && it != Blockchain.Ducatus && !it.id.contains("test")
        }.toMutableList()
    }

    fun getBlcItem(): LiveData<BlockchainItem> = blcItemLD

    fun isAddBlcButtonEnabled(): LiveData<Boolean> = isAddBlcBtnEnabledLD

    fun isBlcAddressValid(): LiveData<Boolean> = isAddBlcBtnEnabledLD

    fun blockchainChanged(blockchain: Blockchain) {
        if (blockchain == blcItem.blockchain) return

        updateBlcItem(blcItem.copy(blockchain = blockchain))
    }

    fun addressChanged(address: String) {
        if (address == blcItem.address) return

        updateBlcItem(blcItem.copy(address = address))
    }

    fun onAddBlcItem(mainVM: MainVM) {
        if (!blcItemIsReady()) {
            Log.e(this, "Can't add blockchain $blcItem")
            return
        }

        addBlcItem(blcItem)
        mainVM.refreshBlcList()
    }

    private fun updateBlcItem(blcItem: BlockchainItem) {
        this.blcItem = blcItem
        blcItemLD.value = blcItem
        isAddBlcBtnEnabledLD.value = blcItemIsReady() && blcAddressIsValid()
        isAddBlcBtnEnabledLD.value = blcItemIsReady()
    }

    private fun blcAddressIsValid(): Boolean {
        isBlcAddressValidLD.value = blcItem.blockchain.validateAddress(blcItem.address)
        return isBlcAddressValidLD.value!!
    }

    private fun blcItemIsReady(): Boolean = blcItem.address.isNotEmpty() && blcItem.blockchain != Blockchain.Unknown

    // only for debug mode
    fun addBlcItemFromCard(sdk: TangemSdk) {
        fun getToken(card: Card): Token? {
            val symbol = card.cardData?.tokenSymbol ?: return null
            val contractAddress = card.cardData?.tokenContractAddress ?: return null
            val decimals = card.cardData?.tokenDecimal ?: return null
            return Token(symbol, contractAddress, decimals)
        }

        sdk.scanCard {
            when (it) {
                is CompletionResult.Success -> {
                    val blockchain = Blockchain.fromId(it.data.cardData?.blockchainName ?: "")
                    if (blockchain == Blockchain.Unknown) return@scanCard
                    val walletPublicKey = it.data.walletPublicKey ?: return@scanCard

                    val token = getToken(it.data)
                    val wallet = Wallet(blockchain, blockchain.makeAddress(walletPublicKey), token)
                    postUI { updateBlcItem(BlockchainItem(blockchain, wallet.address)) }

                }
                is CompletionResult.Failure -> {
                }
            }
        }
    }
}