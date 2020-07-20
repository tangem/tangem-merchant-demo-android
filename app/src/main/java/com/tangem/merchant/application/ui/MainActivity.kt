package com.tangem.merchant.application.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.tangem.merchant.R
import com.tangem.merchant.application.domain.error.AppError
import com.tangem.merchant.application.domain.error.AppMessage
import com.tangem.merchant.application.ui.main.MainVM
import com.tangem.merchant.common.SnackbarHolder
import com.tangem.merchant.common.navigation.NavDestinationLogger
import kotlinx.android.synthetic.main.a_main.*
import ru.dev.gbixahue.eu4d.lib.android._android.components.toast
import ru.dev.gbixahue.eu4d.lib.android._android.components.weakReference

/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainActivity : AppCompatActivity(), SnackbarHolder {

    private val mainVM: MainVM by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main)

        setupNavController()
        listenMessages()
        listenErrors()
    }

    private fun setupNavController() {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment ?: return
        val navController = navHost.navController

        val graph = navController.navInflater.inflate(R.navigation.navigation)
        graph.startDestination = if (mainVM.startFromSettingsScreen) R.id.nav_screen_settings else R.id.nav_entry_point

        navController.graph = graph

        appBarConfiguration = AppBarConfiguration(navController.graph)
        navController.addOnDestinationChangedListener(NavDestinationLogger(this.weakReference()))
        setupToolbar(navController, appBarConfiguration)
    }

    private fun setupToolbar(navController: NavController, appBarConfiguration: AppBarConfiguration) {
        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        toolbar.setNavigationOnClickListener {
            val currentDestination = navController.currentDestination?.id ?: return@setNavigationOnClickListener

            if (currentDestination == R.id.nav_screen_settings) {
                if (mainVM.canNavigateUpFromSettingsScreen()) navController.navigateUp()
                else toast(R.string.error_not_enough_data_for_launch, Toast.LENGTH_LONG)
            } else {
                navController.navigateUp()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun showSnackbar(id: Int, length: Int) {
        showSnackbar(getString(id), length)
    }

    override fun showSnackbar(message: String, length: Int) {
        Snackbar.make(nav_host_fragment, message, length).show()
    }

    private fun listenMessages() {
        mainVM.messageSLE.observe(this, Observer {
            when (it) {
                is AppMessage.ChargeSessionCompleted -> showSnackbar(R.string.payment_transaction_complete)
            }
        })
    }

    private fun listenErrors() {
        mainVM.errorMessageSLE.observe(this, Observer {
            when (it) {
                is AppError.Throwable -> showSnackbar(it.throwable.toString())
                is AppError.UnsupportedConversion -> showSnackbar(R.string.error_unsupported_conversion)
                is AppError.ConversionError -> showSnackbar(R.string.error_coin_market_conversion_error)
                is AppError.CoinMarketHttpError -> showSnackbar(it.errorMessage)
                is AppError.NoInternetConnection -> showSnackbar(R.string.error_lost_internet_connection)
            }
        })
    }
}