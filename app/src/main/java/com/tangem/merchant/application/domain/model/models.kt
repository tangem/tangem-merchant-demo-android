package com.tangem.merchant.application.domain.model

import com.tangem.blockchain.common.Blockchain
import com.tangem.merchant.application.domain.httpService.coinMarketCap.FiatCurrency
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

/**
 * Created by Anton Zhilenkov on 26/06/2020.
 */
data class Merchant(val name: String, val fiatCurrency: FiatCurrency?) {
    companion object {
        fun default(): Merchant = Merchant("", null)
    }
}

data class BlockchainItem(val blockchain: Blockchain, val address: String)

data class FiatValue(
    val stringValue: String,
    val localizedValue: String,
    val value: BigDecimal
) {
    companion object {
        fun create(strValue: String, currencyCode: String): FiatValue {
            val doubleValue = toBigDecimal(strValue)
            return FiatValue(strValue, localizeValue(doubleValue, currencyCode), doubleValue)
        }

        private fun toBigDecimal(value: String): BigDecimal {
            val scaled = BigDecimal(value).setScale(2, BigDecimal.ROUND_FLOOR)
            return scaled.divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
        }

        private fun localizeValue(value: BigDecimal, currencyCode: String): String {
            val formatter = NumberFormat.getCurrencyInstance().apply { currency = Currency.getInstance(currencyCode) }
            return formatter.format(value)
        }
    }
}

data class ChargeData(
    val blcItem: BlockchainItem,
    val writeOfValue: BigDecimal
)