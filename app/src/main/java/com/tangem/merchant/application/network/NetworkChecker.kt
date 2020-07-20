package com.tangem.merchant.application.network

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import ru.dev.gbixahue.eu4d.lib.android._android.components.weakReference
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL


@Suppress("DEPRECATION")
class NetworkChecker(context: Context) {

    private val wContext: WeakReference<Context> = context.weakReference()

    fun activeNetworkIsConnected(): Boolean {
        val context = wContext.get() ?: return false
        val manager = context.getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false

        return manager.activeNetworkInfo?.isConnected ?: false
    }

    fun anyNetworksIsConnected(): Boolean {
        val context = wContext.get() ?: return false
        var have_WIFI = false
        var have_MobileData = false
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        for (info in connectivityManager.allNetworkInfo) {
            if (info.typeName.equals("WIFI", ignoreCase = true)) if (info.isConnected) have_WIFI = true
            if (info.typeName.equals("MOBILE DATA", ignoreCase = true)) if (info.isConnected) have_MobileData =
                true
        }
        return have_WIFI || have_MobileData
    }

    fun serverIsAvailable(serverURL: String): Boolean {
        if (activeNetworkIsConnected()) {
            try {
                val connection = URL(serverURL).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Android")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 1500 // configurable
                connection.connect()
                Log.d(this, "hasInternetConnected: ${(connection.responseCode == 200)}")
                return connection.responseCode == 200
            } catch (e: IOException) {
                Log.e(this, "Error checking server availability", e)
            }
        }
        return false
    }

    companion object {
        private lateinit var instance: NetworkChecker

        fun createInstance(context: Context): NetworkChecker {
            instance = NetworkChecker(context)
            return instance
        }

        fun getInstance(): NetworkChecker = instance
    }
}
