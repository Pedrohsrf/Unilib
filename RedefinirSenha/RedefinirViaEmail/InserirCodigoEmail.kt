package com.example.unilib.RedefinirSenha.RedefinirViaEmail

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.unilib.Classes.BaseActivity
import com.example.unilib.R
import com.example.unilib.RedefinirSenha.AlterarSenha.AlterarSenha
import com.example.unilib.RedefinirSenha.OpcoesRedefinicao
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.ActivityInserirCodigoEmailBinding

class InserirCodigoEmail : BaseActivity() {
    private lateinit var binding: ActivityInserirCodigoEmailBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInserirCodigoEmailBinding.inflate(layoutInflater);
        setContentView(binding.root)

        val navegarOpcoesRedefinicao = Intent(this, OpcoesRedefinicao::class.java);
        val navegarAlterarSenha = Intent(this, AlterarSenha::class.java);
        var codigo = (1000..9999).random();

        binding.edcodigo.setText(codigo.toString());

        binding.btnVoltar.setOnClickListener {
            startActivity(navegarOpcoesRedefinicao);
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

        binding.btnEnviarCodigo.setOnClickListener {
            val loadingSalvando = Rotinas.mostrarLoading(
                contexto = this@InserirCodigoEmail,
                pLayoutDialogo = R.layout.activity_loading_dialog,
                pViewMensagem = R.id.loading_mensagem,
                pMensagem = "Verificando...",
                pDottieAnimation = "loading_icon.json"
            )
            Handler(mainLooper).postDelayed({
                if ((binding.edcodigo.text.toString() == codigo.toString())) {
                    startActivity(navegarAlterarSenha);
                    this.overridePendingTransition(
                        R.anim.animate_fade_enter,
                        R.anim.animate_fade_exit
                    )
                } else {
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_alerta,
                        mensagem = "Código inválido. Seu código é " + codigo,
                        dialogoAtivo = loadingSalvando
                    )
                }
            }, 1000L)


        }

        binding.btnReenviarEmail.setOnClickListener {
            val loadingGerando = Rotinas.mostrarLoading(
                contexto = this@InserirCodigoEmail,
                pLayoutDialogo = R.layout.activity_loading_dialog,
                pViewMensagem = R.id.loading_mensagem,
                pMensagem = "Enviando...",
                pDottieAnimation = "loading_icon.json"
            )

            Handler(mainLooper).postDelayed({

                codigo = (1000..9999).random()

                binding.edcodigo.setText(codigo);
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_informacao,
                    mensagem = "Código gerado com sucesso!",
                    dialogoAtivo = loadingGerando
                )

            }, 1000L)

        }
    }
}