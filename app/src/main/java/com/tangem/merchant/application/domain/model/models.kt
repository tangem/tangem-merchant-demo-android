package com.tangem.merchant.application.domain.model

/**
 * Created by Anton Zhilenkov on 26/06/2020.
 */
data class Merchant(val name: String, val fiatCurrency: FiatCurrency) {
    companion object {
        fun default(): Merchant = Merchant("John Doe", FiatCurrency("", ""))
    }
}

data class FiatCurrency(val name: String, val code: String)

data class BlockchainItem(val blockchain: Blockchain, val address: String)


// Temporary enum
enum class Blockchain(
    val id: String,
    val currency: String,
    val fullName: String
) {
    Unknown("", "", ""),
    Bitcoin("BTC", "BTC", "Bitcoin"),
    BitcoinTestnet("BTC/test", "BTCt", "Bitcoin Testnet"),
    BitcoinCash("BCH", "BCH", "Bitcoin Cash"),
    Litecoin("LTC", "LTC", "Litecoin"),
    Ducatus("DUC", "DUC", "Ducatus"),
    Ethereum("ETH", "ETH", "Ethereum"),
    RSK("RSK", "RBTC", "RSK"),
    Cardano("CARDANO", "ADA", "Cardano"),
    XRP("XRP", "XRP", "XRP Ledger"),
    Binance("BINANCE", "BNB", "Binance"),
    BinanceTestnet("BINANCE/test", "BNBt", "Binance"),
    Stellar("XLM", "XLM", "Stellar"),
    Tezos("TEZOS", "XTZ", "Tezos");
}