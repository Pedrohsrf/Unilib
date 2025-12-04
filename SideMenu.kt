package com.example.unilib

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.example.unilib.Classes.BaseActivity
import com.example.unilib.Login.Login
import com.example.unilib.databinding.ActivitySideMenuBinding
import androidx.navigation.ui.onNavDestinationSelected
import com.example.unilib.Notificacoes.Notificacoes

class SideMenu : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySideMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySideMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarSideMenu.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_side_menu)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_eventos,
                R.id.nav_salas,
                R.id.nav_chatbot,
                R.id.nav_administracao,
                R.id.nav_acessibilidade,
                R.id.nav_sair
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.nav_sair) {
                val intent = Intent(this, Login::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                this.overridePendingTransition(
                    R.anim.animate_fade_enter,
                    R.anim.animate_fade_exit
                )
                finish()

                return@setNavigationItemSelectedListener true
            } else {
                val handled = item.onNavDestinationSelected(navController)

                if (handled) {
                    binding.drawerLayout.closeDrawers()
                }
                return@setNavigationItemSelectedListener handled
            }
        }
//        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.side_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_notificacoes) {
            val navegarNotificacoes = Intent(this, Notificacoes::class.java)

            startActivity(navegarNotificacoes)
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_side_menu)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}