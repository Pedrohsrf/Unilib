package com.example.unilib.RedefinirSenha.RedefinirViaEmail

import DialogCallback
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.unilib.Classes.BaseActivity
import androidx.lifecycle.lifecycleScope
import com.example.unilib.R
import com.example.unilib.RedefinirSenha.OpcoesRedefinicao
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasAuth.redefinirSenha
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Login.Login
import com.example.unilib.databinding.ActivityInserirEmailBinding
import kotlinx.coroutines.launch

class InserirEmail : BaseActivity() {
    private lateinit var binding: ActivityInserirEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInserirEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navegarOpcoesRedefinicao = Intent(this, OpcoesRedefinicao::class.java);
        val navegarInserirCodigo = Intent(this, InserirCodigoEmail::class.java);
        val navegarLogin = Intent(this, Login::class.java);

        binding.btnVoltar.setOnClickListener {
            startActivity(navegarLogin);
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

        binding.btnEnviarConfirmacao.setOnClickListener {
            val sharedPref = getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("emailUsuario", binding.edemail.text.toString())
            editor.apply()



            val loading = Rotinas.mostrarLoading(
                contexto = this@InserirEmail,
                pLayoutDialogo = R.layout.activity_loading_dialog,
                pViewMensagem = R.id.loading_mensagem,
                pMensagem = "Enviando...",
                pDottieAnimation = "loading_icon.json"
            )

            if ((binding.edemail.text.toString() == "")) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Preencha todos os campos.",
                    dialogoAtivo = loading
                )
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.edemail.text.toString())
                    .matches()
            ) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Email inválido.",
                    dialogoAtivo = loading
                )
            } else {
                lifecycleScope.launch {
                    val existe = RotinasBD.verificarUsuario(binding.edemail.text.toString())

                    if (!existe) {
                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_alerta,
                            mensagem = "Usuário não cadastrado.",
                            dialogoAtivo = loading
                        )

                        return@launch
                    } else {
                        val enviado = redefinirSenha(binding.edemail.text.toString())

                        if (enviado) {
                            val enviadoDialogo = Rotinas.mostrarDialogo(contexto = this@InserirEmail,
                                pLayoutDialogo = R.layout.dialogo_neutro,
                                pMensagemPrincipal = "Email enviado",
                                pMensagemSecundaria = "Acesse sua caixa de entrada \nou spam para redefinir sua senha.",
                                pBtnSim = "OK",
                                pBtnNao = "",
                                pDottieAnimation = "",
                                object : DialogCallback {
                                    override fun onSimClicked() {
                                        loading.dismiss()
                                        finish()
//                                        startActivity(navegarLogin);
//                                        this@InserirEmail.overridePendingTransition(
//                                            R.anim.animate_fade_enter,
//                                            R.anim.animate_fade_exit
//                                        )
                                    }

                                    override fun onNaoClicked() {

                                    }
                                })
                        } else {
                            Rotinas.mostrarSnackbar(
                                parentView = binding.root,
                                layoutAlerta = R.layout.toast_alerta,
                                mensagem = "Erro ao enviar email.",
                                dialogoAtivo = loading
                            )
                        }
                    }
                }

            }


        }
    }
}