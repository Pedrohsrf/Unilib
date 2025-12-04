package com.example.unilib.Cadastro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.unilib.Classes.BaseActivity
import androidx.lifecycle.lifecycleScope
import com.example.unilib.R
import com.example.unilib.Login.Login
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasAuth
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.criarListasVazias
import com.example.unilib.Classes.RotinasBD.lerUsuario
import com.example.unilib.Classes.RotinasBD.proximoCodigo
import com.example.unilib.databinding.ActivityCadastroBinding
import com.example.unilib.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class Cadastro : BaseActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private val TELEFONE_REGEX = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}\$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navegarLogin = Intent(this, Login::class.java);
        val navegarBoasVindas = Intent(this, BoasVindas::class.java);

        binding.btnLogin.setOnClickListener {
            startActivity(navegarLogin);
        }

        binding.btnCadastrar.setOnClickListener {
            val loadingCadastrando = Rotinas.mostrarLoading(
                contexto = this,
                pLayoutDialogo = R.layout.activity_loading_dialog,
                pViewMensagem = R.id.loading_mensagem,
                pMensagem = "Cadastrando...",
                pDottieAnimation = "loading_icon.json"

            )
            try {
                if (binding.edNome.text.toString() == ""
                    || binding.edEmailTel.text.toString() == ""
                    || binding.edSenha.text.toString() == ""
                    || binding.edConfirmaSenha.text.toString() == ""
                ) {
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_alerta,
                        mensagem = "Preencha todos os campos.",
                        dialogoAtivo = loadingCadastrando
                    )
                } else {
                    val email = binding.edEmailTel.text.toString();
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                            .matches() && !email.matches(TELEFONE_REGEX.toRegex())
                    ) {
                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_alerta,
                            mensagem = "Email inválido",
                            dialogoAtivo = loadingCadastrando
                        )
                    } else if (binding.edSenha.text.toString().length < 12 || binding.edConfirmaSenha.text.toString().length < 12) {
                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_alerta,
                            mensagem = "A senha deve ter no mínimo 12 caracteres",
                            dialogoAtivo = loadingCadastrando
                        )
                    } else if (!binding.edSenha.text.toString()
                            .equals(binding.edConfirmaSenha.text.toString())
                    ) {
                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_alerta,
                            mensagem = "As senhas não coincidem.",
                            dialogoAtivo = loadingCadastrando
                        )
                    } else {
                        lifecycleScope.launch {
                            try {
                                val codigoUsuario = proximoCodigo("usuario", 5, "0".first())

                                val UID = RotinasAuth.cadastrarUsuario(
                                    binding.edEmailTel.text.toString(),
                                    binding.edSenha.text.toString(),
                                )

                                if (UID.isNotEmpty()) {

                                    val sucessoBD = RotinasBD.cadastrarUsuario(
                                        binding.edNome.text.toString(),
                                        binding.edEmailTel.text.toString(),
                                        UID,
                                        false,
                                    )

                                    if (sucessoBD) {
                                        var listasCriadas =
                                            criarListasVazias(codigoUsuario)
                                        if (listasCriadas) {
                                            var usuarioCadastrado = binding.edNome.text;
                                            val nomeCadastrado = binding.edNome.text.toString();
                                            val emailCadastrado =
                                                binding.edEmailTel.text.toString();
                                            navegarBoasVindas.putExtra(
                                                "USUARIO_CADASTRADO",
                                                usuarioCadastrado.toString()
                                            )
                                            val dadosUsuario = lerUsuario(UID);
                                            val sharedPref =
                                                getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
                                            val editor = sharedPref.edit()
                                            editor.putString(
                                                "codigoUsuario",
                                                dadosUsuario.get("codigo") as String
                                            )
                                            editor.putString("nomeUsuario", nomeCadastrado)
                                            editor.putString("emailUsuario", emailCadastrado)
                                            editor.putString("UID", dadosUsuario.get("UID") as String)
                                            editor.putBoolean("administrador", false)
                                            editor.apply()
                                            startActivity(navegarBoasVindas);
                                            this@Cadastro.overridePendingTransition(
                                                R.anim.animate_fade_enter,
                                                R.anim.animate_fade_exit
                                            )
                                        } else {
                                            Rotinas.mostrarSnackbar(
                                                parentView = binding.root,
                                                layoutAlerta = R.layout.toast_alerta,
                                                mensagem = "Erro ao criar listas.",
                                                dialogoAtivo = loadingCadastrando
                                            )
                                        }
                                    }
                                }
                            } catch (E: Exception) {
                                var msgErro = E.message.toString()

                                if (E.message.toString().equals("The email address is already in use by another account.")) {
                                    msgErro = "Usuário já cadastrado."
                                }

                                    Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = msgErro,
                                    dialogoAtivo = loadingCadastrando
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = e.message.toString(),
                    dialogoAtivo = loadingCadastrando
                )
                this.overridePendingTransition(
                    R.anim.animate_fade_enter,
                    R.anim.animate_fade_exit
                )
            }
        }
    }
}