package com.tangem.merchant.common.navigation

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import ru.dev.gbixahue.eu4d.lib.android.global.log.Log
import java.lang.ref.WeakReference

class NavDestinationLogger(
    private val wContext: WeakReference<Context>
) : NavController.OnDestinationChangedListener {

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        val dest: String = try {
            wContext.get()?.resources?.getResourceName(destination.id)!!
        } catch (e: Resources.NotFoundException) {
            destination.id.toString()
        }
        Log.d(this, "Navigated to $dest")
    }
}