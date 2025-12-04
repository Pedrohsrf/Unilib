package com.example.unilib

import DialogCallback
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.unilib.Classes.BaseActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.unilib.Login.Login
import com.example.unilib.databinding.ActivityRodapefmBinding // Assumindo este é o Binding correto
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Notificacoes.Notificacoes

class RodapeFM : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityRodapefmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRodapefmBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val bottomNavView: BottomNavigationView = findViewById(R.id.nav_view)
        val sideNavView: NavigationView = findViewById(R.id.side_nav_view_top)
        val sideNavViewBottom: NavigationView = findViewById(R.id.side_nav_view_bottom)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController


        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_inicio,
                R.id.navigation_estante,
                R.id.navigation_listas,
                R.id.navigation_perfil
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        bottomNavView.setupWithNavController(navController)

        val destinoID = intent.getIntExtra("ID_DESTINO", -1)

        if (destinoID != -1 && destinoID != R.id.navigation_inicio) {
            navController.navigate(destinoID)
        }

        val bottomNavDestinations = setOf(
            R.id.navigation_inicio,
            R.id.navigation_estante,
            R.id.navigation_listas,
            R.id.navigation_perfil
        )

        val topNavDestinations = setOf(
            R.id.navigation_inicio
        )

        sideNavView.setupWithNavController(navController)
        sideNavViewBottom.setupWithNavController(navController)

        val sharedPreferences = getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
        val administrador = sharedPreferences.getBoolean("administrador", false)

        if (!administrador) {
            val menu = sideNavView.menu
            val menuItem = menu.findItem(R.id.nav_administracao)
            menuItem?.isVisible = false
        }

        sideNavView.setNavigationItemSelectedListener { item: MenuItem ->
            val handled = item.onNavDestinationSelected(navController)
            drawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener handled
        }

        sideNavViewBottom.setNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.nav_sair) {
                val fazerLogout = Rotinas.mostrarDialogo(
                    contexto = this,
                    pLayoutDialogo = R.layout.dialogo_neutro,
                    pMensagemPrincipal = "Fazer logout",
                    pMensagemSecundaria = "Você deseja sair da sua conta no UniLib?",
                    pBtnSim = "SAIR",
                    pBtnNao = "CANCELAR",
                    pDottieAnimation = "",
                    object : DialogCallback {
                        override fun onSimClicked() {
                            Rotinas.mostrarLoading(
                                contexto = this@RodapeFM,
                                pLayoutDialogo = R.layout.activity_loading_dialog,
                                pViewMensagem = R.id.loading_mensagem,
                                pMensagem = "Saindo...",
                                pDottieAnimation = "loading_icon.json"
                            )

                            Handler(mainLooper).postDelayed({
                                val intent = Intent(this@RodapeFM, Login::class.java).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                                this@RodapeFM.overridePendingTransition(
                                    R.anim.animate_fade_enter,
                                    R.anim.animate_fade_exit
                                )
                                finish()
                            }, 1000L)
                        }

                        override fun onNaoClicked() {

                        }
                    }
                )

                drawerLayout.closeDrawers();

                return@setNavigationItemSelectedListener false;
            } else {
                val handled = item.onNavDestinationSelected(navController)
                if (handled) {
                    drawerLayout.closeDrawers()
                }
                return@setNavigationItemSelectedListener handled
            }
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id in topNavDestinations) {
                supportActionBar?.show()
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                supportActionBar?.hide()
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }

            if (destination.id in bottomNavDestinations) {
                bottomNavView.visibility = View.VISIBLE
            } else {
                bottomNavView.visibility = View.GONE
            }


        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp(appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.side_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_notificacoes) {
            val intent = Intent(this, Notificacoes::class.java)

            startActivity(intent)
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )

            return true
        }

        return super.onOptionsItemSelected(item)
    }
}