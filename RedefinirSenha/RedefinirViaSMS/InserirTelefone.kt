package com.example.unilib.RedefinirSenha.RedefinirViaSMS

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
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.databinding.ActivityInserirCodigoSmsBinding
import com.example.unilib.databinding.ActivityInserirTelefoneBinding
import kotlinx.coroutines.launch

class InserirTelefone : BaseActivity() {

    private lateinit var binding: ActivityInserirTelefoneBinding
    private val TELEFONE_REGEX = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}\$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInserirTelefoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navegarOpcoesRedefinicao = Intent(this, OpcoesRedefinicao::class.java);
        val navegarInserirCodigo = Intent(this, InserirCodigoSMS::class.java);

        binding.btnVoltar.setOnClickListener {
            startActivity(navegarOpcoesRedefinicao);
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

        binding.btnEnviarCodigo.setOnClickListener {
            val sharedPref = getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("emailUsuario", binding.edTelefone.text.toString())
            editor.apply()

            val loading = Rotinas.mostrarLoading(
                contexto = this@InserirTelefone,
                pLayoutDialogo = R.layout.activity_loading_dialog,
                pViewMensagem = R.id.loading_mensagem,
                pMensagem = "Enviando...",
                pDottieAnimation = "loading_icon.json"
            )
            if ((binding.edTelefone.text.toString() == "")) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Preencha todos os campos.",
                    dialogoAtivo = loading
                )
            } else if (!binding.edTelefone.text.toString().matches(
                    TELEFONE_REGEX.toRegex()
                )
            ) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Telefone inválido",
                    dialogoAtivo = loading
                )
            } else {
                lifecycleScope.launch {
                    val existe = RotinasBD.verificarUsuario(binding.edTelefone.text.toString())

                    if (!existe) {
                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_alerta,
                            mensagem = "Usuário não cadastrado.",
                            dialogoAtivo = loading
                        )

                        return@launch
                    } else {
                        startActivity(navegarInserirCodigo);
                        this@InserirTelefone.overridePendingTransition(
                            R.anim.animate_fade_enter,
                            R.anim.animate_fade_exit
                        )
                    }
                }
            }


        }
    }
}