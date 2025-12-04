package com.example.unilib.Login

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dotlottie.dlplayer.Mode
import com.example.unilib.Cadastro.Cadastro
import com.example.unilib.Notificacoes.Notificacoes
import com.example.unilib.RodapeFM
import com.example.unilib.R
import com.example.unilib.RedefinirSenha.OpcoesRedefinicao
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.Rotinas.normalizeAndLowercase
import com.example.unilib.Classes.RotinasAuth.logarUsuario
import com.example.unilib.Classes.RotinasBD.autorizarLogin
import com.example.unilib.Classes.RotinasBD.lerUsuario
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.lottiefiles.dotlottie.core.widget.DotLottieAnimation
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.BaseActivity
import com.example.unilib.Classes.FontScaleManager
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Rotinas.normalizeAndLowercase
import com.example.unilib.Classes.RotinasAuth
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.autorizarLogin
import com.example.unilib.Classes.RotinasBD.enviarNotificacoesEmprestimo
import com.example.unilib.Classes.RotinasBD.lerAcessibilidadePref
import com.example.unilib.RedefinirSenha.RedefinirViaEmail.InserirEmail
import com.example.unilib.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class Login : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        val prefs = getSharedPreferences("acessibilidade", Context.MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        Thread.sleep(5000);
        installSplashScreen();
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navegarRedefinicaoSenha = Intent(this, InserirEmail::class.java);
        val navegarCadastro = Intent(this, Cadastro::class.java);
        val navegarTelaInicial = Intent(this, RodapeFM::class.java);

        var mostrarSenha = false;
        binding.edSenha.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD;

        binding.edSenha2.setText("admin@gmail.com")
        binding.edSenha.setText("admin123")

        binding.btnEntrar.setOnClickListener {
            var login = binding.edSenha2.text.toString();
            val senha = binding.edSenha.text.toString();

            Log.i("Email", login)
            Log.i("Senha", senha)
            val loadingEntrando = Rotinas.mostrarLoading(
                contexto = this,
                pLayoutDialogo = R.layout.activity_loading_dialog,
                pViewMensagem = R.id.loading_mensagem,
                pMensagem = "Entrando...",
                pDottieAnimation = "loading_icon.json"
            )

            if ((login == "") || (senha == "")) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Preencha todos os campos.",
                    dialogoAtivo = loadingEntrando
                )
            } else {
                lifecycleScope.launch {
//                    val existe = RotinasBD.verificarUsuario(binding.edSenha2.text.toString())
//
//                    if (!existe) {
//                        Rotinas.mostrarSnackbar(
//                            parentView = binding.root,
//                            layoutAlerta = R.layout.toast_alerta,
//                            mensagem = "Usuário não cadastrado.",
//                            dialogoAtivo = loadingEntrando
//                        )
//
//                        return@launch
//                    }
                    try {

                        val UIDAuth = logarUsuario(
                            binding.edSenha2.text.toString(),
                            binding.edSenha.text.toString()
                        )

                        lifecycleScope.launch {

                            if (UIDAuth.isNotEmpty()) {
                                    val dadosUsuario = lerUsuario(UIDAuth);

                                    val sharedPref =
                                        getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
                                    val editor = sharedPref.edit()
                                    editor.putString(
                                        "codigoUsuario",
                                        dadosUsuario.get("codigo") as String
                                    )
                                    editor.putString(
                                        "nomeUsuario",
                                        dadosUsuario.get("nomeCompleto") as String
                                    )
                                    editor.putString("emailUsuario", login)
                                    editor.putString("UID", UIDAuth)
                                    editor.putBoolean(
                                        "administrador",
                                        dadosUsuario.get("administrador") as Boolean
                                    )
                                    editor.apply()


                                var acessibilidadePrefs = this@Login.getSharedPreferences("acessibilidade", Context.MODE_PRIVATE)

                                var fontPreset = lerAcessibilidadePref(UIDAuth)

                                enviarNotificacoesEmprestimo(dadosUsuario.get("codigo") as String)

                                acessibilidadePrefs.edit().putString("fontPreset", fontPreset).apply()
                                FontScaleManager.updateActivityWithPreset(this@Login, fontPreset)
                                startActivity(navegarTelaInicial);
                                    this@Login.overridePendingTransition(
                                        R.anim.animate_fade_enter, R.anim.animate_fade_exit
                                    )

                            } else {
                                loadingEntrando.dismiss()
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Credenciais inválidas.",
                                    dialogoAtivo = loadingEntrando
                                )
                            }
                        }
                    } catch (E: Exception) {
                        var msgErro = E.message.toString()

                        if (E is FirebaseAuthException) {
                            if (E is FirebaseAuthInvalidUserException) {
                                msgErro = "Usuário não cadastrado."
                            }

                            else if (E is FirebaseAuthInvalidCredentialsException) {
                                msgErro = "Credenciais inválidas."
                            }
                        }

                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_alerta,
                            mensagem = msgErro,
                            dialogoAtivo = loadingEntrando
                        )
                    }
                }


            }
        }

        findViewById<TextView?>(R.id.btnEsquecerSenha).setOnClickListener {
            startActivity(navegarRedefinicaoSenha);
            this.overridePendingTransition(
                R.anim.animate_fade_enter, R.anim.animate_fade_exit
            )
        }

        binding.btnContinuarBoasVindas.setOnClickListener {
            startActivity(navegarCadastro);
            this.overridePendingTransition(
                R.anim.animate_fade_enter, R.anim.animate_fade_exit
            )
        }

        binding.btnMostrarSenha.setOnClickListener {
            if (mostrarSenha) {
                mostrarSenha = false;
                binding.edSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD;
                binding.btnMostrarSenha.setImageResource(R.drawable.closed_eye_icon);
            } else {
                mostrarSenha = true;
                binding.edSenha.inputType = InputType.TYPE_CLASS_TEXT;
                binding.btnMostrarSenha.setImageResource(R.drawable.open_eye_icon);
            }
        }
    }
}