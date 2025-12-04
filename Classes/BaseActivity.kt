package com.example.unilib.Classes

import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Usuario
import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

open class BaseActivity : AppCompatActivity() {

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val focused = currentFocus
            if (focused is EditText) {
                val r = Rect()
                focused.getGlobalVisibleRect(r)
                val x = ev.rawX.toInt()
                val y = ev.rawY.toInt()

                if (!r.contains(x, y)) {
                    // 1) Tira o foco do EditText
                    focused.clearFocus()

                    // 2) Passa o foco para a raiz para evitar que volte pro EditText
                    val root = window.decorView
                    root.isFocusable = true
                    root.isFocusableInTouchMode = true
                    root.requestFocus()

                    // 3) Esconde o teclado (duas estratégias)
                    hideIme(root)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideIme(anchor: View) {
        // Tenta via WindowInsets (moderno)
        ViewCompat.getWindowInsetsController(anchor)
            ?.hide(WindowInsetsCompat.Type.ime())

        // Fallback via InputMethodManager
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(anchor.windowToken, 0)
    }
    fun getUsuarioLogado(): Usuario {
        val codigo = Rotinas.lerPreferencia(this, "codigoUsuario")
        val nome = Rotinas.lerPreferencia(this, "nomeUsuario")
        val email = Rotinas.lerPreferencia(this, "emailUsuario")

        // Retorna o objeto Usuario (sem foto, pois o Login.kt não salva)
        return Usuario(
            nome = nome,
            email = email,
            fotoPerfil = "", // Ignoramos a foto
            codigo = codigo
        )
    }
    fun isUsuarioAdmin(): Boolean {
        val sharedPref = getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
        return sharedPref.getBoolean("administrador", false)
    }
}
