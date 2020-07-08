package com.tangem.merchant.common.extensions

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

/**
 * Created by Anton Zhilenkov on 08/07/2020.
 */
fun BigDecimal.formatToCurrency(currencyCode: String): String {
    return NumberFormat.getCurrencyInstance().apply { currency = Currency.getInstance(currencyCode) }.format(this)
}