package com.example.unilib.Classes

import com.example.unilib.Classes.Usuario
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

    fun hideIme(view: View) {
        val context = view.context

        if (ViewCompat.isAttachedToWindow(view)) {
            ViewCompat.getWindowInsetsController(view)
                ?.hide(WindowInsetsCompat.Type.ime())
        }

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun handleFragmentDispatchTouchEvent(viewRoot: View, event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val focused = requireActivity().currentFocus
            if (focused is EditText) {
                val r = android.graphics.Rect()
                focused.getGlobalVisibleRect(r)
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()

                if (!r.contains(x, y)) {
                    focused.clearFocus()

                    viewRoot.isFocusable = true
                    viewRoot.isFocusableInTouchMode = true
                    viewRoot.requestFocus()

                    hideIme(viewRoot)
                }
            }
        }
    }
    fun getUsuarioLogado(): Usuario {
        return (activity as BaseActivity).getUsuarioLogado()
    }

    fun isUsuarioAdmin(): Boolean {
        return (activity as BaseActivity).isUsuarioAdmin()
    }
}