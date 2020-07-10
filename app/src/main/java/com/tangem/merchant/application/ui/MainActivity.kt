package com.tangem.merchant.application.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.tangem.merchant.R
import com.tangem.merchant.application.ui.main.MainVM
import com.tangem.merchant.common.navigation.NavDestinationLogger
import kotlinx.android.synthetic.main.a_main.*
import ru.dev.gbixahue.eu4d.lib.android._android.components.toast
import ru.dev.gbixahue.eu4d.lib.android._android.components.weakReference

/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainActivity : AppCompatActivity() {

    private val mainVM: MainVM by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main)

        setupNavController()
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
                else toast(R.string.e_not_enough_data_for_launch, Toast.LENGTH_LONG)
            } else {
                navController.navigateUp()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}