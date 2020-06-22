package com.tangem.merchant.common.extensions

/**
 * Created by Anton Zhilenkov on 31/03/2020.
 */
fun <T> List<T>.print(delimiter: String = ", ", wrap: Boolean = true): String {
    val builder = StringBuilder()
    forEach { builder.append(it).append(delimiter) }
    val length = builder.length
    if (length > delimiter.length) {
        builder.delete(length - delimiter.length, length)
    }
    val result = builder.toString()

    return if (wrap) "[$result]" else result
}