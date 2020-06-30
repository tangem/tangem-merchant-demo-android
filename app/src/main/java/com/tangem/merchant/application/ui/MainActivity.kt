package com.tangem.merchant.application.ui

import android.os.Bundle
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
import ru.dev.gbixahue.eu4d.lib.android._android.components.weakReference

/**
 * Created by Anton Zhilenkov on 16/06/2020.
 */
class MainActivity : AppCompatActivity() {

    private val mainVM: MainVM by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main)

        mainVM.startFromSettingsScreen = !mainVM.isDataEnoughForLaunch()
        setupToolbar()
        setupNavController()
    }

    private fun setupToolbar() {
        toolbar.title = ""
        setSupportActionBar(toolbar)
    }

    private fun setupNavController() {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment ?: return
        val navController = navHost.navController

        val graph = navController.navInflater.inflate(R.navigation.navigation)
        graph.startDestination = if (mainVM.startFromSettingsScreen) R.id.nav_screen_settings else R.id.nav_entry_point

        navController.graph = graph

        appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener(NavDestinationLogger(this.weakReference()))
    }

    override fun onSupportNavigateUp(): Boolean = findNavigationController().navigateUp(appBarConfiguration)

    private fun findNavigationController(): NavController = findNavController(R.id.nav_host_fragment)
}