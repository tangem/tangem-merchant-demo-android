package com.tangem.merchant.common

import android.content.SharedPreferences
import com.tangem.merchant.application.AppMerchant

open class FirstLaunchChecker(private val sp: SharedPreferences = AppMerchant.appInstance.sharedPreferences()) {
    private val key = "lastLaunch"

    fun isFirstLaunch(): Boolean {
        val isFirst = sp.contains(key)
        if (isFirst) sp.edit().putInt(key, System.currentTimeMillis().toInt()).apply()

        return isFirst
    }
}