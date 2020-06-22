package com.tangem.merchant.common

import com.google.android.material.snackbar.Snackbar

interface SnackbarHolder {
    fun showSnackbar(message: String, length: Int = Snackbar.LENGTH_SHORT)
    fun showSnackbar(id: Int, length: Int = Snackbar.LENGTH_SHORT)
}