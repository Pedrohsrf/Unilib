package com.example.unilib.RedefinirSenha.AlterarSenha

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.unilib.Classes.BaseActivity
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Login.Login
import com.example.unilib.R
import com.example.unilib.RedefinirSenha.OpcoesRedefinicao
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.databinding.ActivityAlterarSenhaBinding
import kotlinx.coroutines.launch

class AlterarSenha : BaseActivity() {
    private lateinit var binding: ActivityAlterarSenhaBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlterarSenhaBinding.inflate(layoutInflater);
        setContentView(binding.root)

        val navegarLogin = Intent(this, Login::class.java);
        val navegarOpcoesRedefinicao = Intent(this, OpcoesRedefinicao::class.java);

        binding.btnVoltar.setOnClickListener {
            startActivity(navegarOpcoesRedefinicao);
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

        binding.btnContinuarBoasVindas.setOnClickListener {
            val loadingSalvando = Rotinas.mostrarLoading(
                contexto = this@AlterarSenha,
                pLayoutDialogo = R.layout.activity_loading_dialog,
                pViewMensagem = R.id.loading_mensagem,
                pMensagem = "Redefinindo...",
                pDottieAnimation = "loading_icon.json"
            )
            if ((binding.edSenha2.text.toString() == "")
                || (binding.edConfirmaSenha.text.toString() == "")
            ) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Preencha todos os campos.",
                    dialogoAtivo = loadingSalvando
                )
            } else {
                if (binding.edSenha2.text.toString() ==
                    binding.edConfirmaSenha.text.toString()
                ) {
                    val sharedPref = this.getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
                    val emailTelefone = sharedPref.getString("emailUsuario", "emailUsuário")

                    lifecycleScope.launch {
//                        val sucesso = RotinasBD.alterarSenhaUsuario(
//                            emailTelefone.toString(),
//                            binding.edSenha2.text.toString()
//                        )

//                        if (sucesso) {
//                            Rotinas.mostrarSnackbar(
//                                parentView = binding.root,
//                                layoutAlerta = R.layout.toast_informacao,
//                                mensagem = "Senha alterada com sucesso!",
//                                dialogoAtivo = loadingSalvando
//                            )
//
//                            Handler(mainLooper).postDelayed({
//                                startActivity(navegarLogin);
//                                this@AlterarSenha.overridePendingTransition(
//                                    R.anim.animate_fade_enter,
//                                    R.anim.animate_fade_exit
//                                )
//                            }, 1000L)
//                        } else {
//                            Rotinas.mostrarSnackbar(
//                                parentView = binding.root,
//                                layoutAlerta = R.layout.toast_alerta,
//                                mensagem = "Erro ao alterar senha, tente novamente.",
//                                dialogoAtivo = loadingSalvando
//                            )
//                        }
                    }


                } else {
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_alerta,
                        mensagem = "As senhas não coincidem.",
                        dialogoAtivo = loadingSalvando
                    )
                }
            }


        }
    }
}