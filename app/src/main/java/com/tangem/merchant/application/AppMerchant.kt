package com.tangem.merchant.application

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tangem.merchant.BuildConfig
import com.tangem.merchant.application.network.NetworkChecker
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log
import ru.dev.gbixahue.eu4d.lib.android.global.log.TagLogger

/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class AppMerchant : Application() {

    override fun onCreate() {
        super.onCreate()

        appInstance = this
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        NetworkChecker.createInstance(this)
        Log.setLogger(AppLog())
    }

    fun sharedPreferences(name: String = APP_NAME, mode: Int = Context.MODE_PRIVATE): SharedPreferences {
        return getSharedPreferences(name, mode)
    }

    companion object {
        val APP_NAME = "MerchantApp"

        lateinit var appInstance: AppMerchant
    }
}

class AppLog : TagLogger(AppMerchant.APP_NAME)