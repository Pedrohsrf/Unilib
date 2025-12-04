package com.example.unilib.Livros;

import android.content.Intent
import android.content.Context
import android.os.Bundle;
import android.os.Handler
import android.widget.Button
import android.widget.ImageView

import com.example.unilib.Classes.BaseActivity;
import android.util.Log
import android.widget.EditText // Adicione esta importação
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.lerLivro
import com.example.unilib.R
import com.example.unilib.databinding.ActivityAvaliarLivroBinding
import com.example.unilib.databinding.ActivityVisualizarLivroBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AvaliarLivro : BaseActivity() {

    private lateinit var binding: ActivityAvaliarLivroBinding;
    private lateinit var ratingBar: RatingBar
    private lateinit var editTextComentario: EditText // Adicione esta variável


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        binding = ActivityAvaliarLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            var codigoLivro = intent.getStringExtra("CODIGO_LIVRO") ?: ""
            var dadosLivro = lerLivro(codigoLivro)

            ratingBar = binding.ratingBar
            editTextComentario = binding.edNome2

            // --- LOGS PARA DEPURAÇÃO ---
            Log.d("AvaliarLivro", "DadosLivro recebido no onCreate: $dadosLivro")
            if (dadosLivro != null) {
                Log.d("AvaliarLivro", "Codigo do livro recebido: ${dadosLivro.codigo}")
                Log.d("AvaliarLivro", "Nome do livro recebido: ${dadosLivro.nome}")
            } else {
                Log.e("AvaliarLivro", "ERRO: DadosLivro é NULL no onCreate!")
            }
            // --- FIM DOS LOGS ---

            if (dadosLivro != null) {
                binding.nomeLivro.text = dadosLivro.nome;
                binding.nomeAutor.text = dadosLivro.autor;
                binding.capaLivro.setImageBitmap(Rotinas.Base64ToImage(dadosLivro.capa));
            }

            val navegarVisualizarLivro = Intent(this@AvaliarLivro, VisualizarLivro::class.java);

            binding.btnAvaliar.setOnClickListener {
                val avaliacao = ratingBar.rating.toInt()
                val comentario =
                    editTextComentario.text.toString().trim() // Obtenha o texto do EditText

                if (avaliacao <= 0) {
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_informacao,
                        mensagem = "Por favor, selecione uma nota para avaliar.",
                        dialogoAtivo = null
                    )
                    return@setOnClickListener
                }

                val loading = Rotinas.mostrarLoading(
                    contexto = this@AvaliarLivro,
                    pLayoutDialogo = R.layout.activity_loading_dialog,
                    pViewMensagem = R.id.loading_mensagem,
                    pMensagem = "Enviando avaliação...",
                    pDottieAnimation = "loading_icon.json"
                )

                // --- OBTENDO E LOGANDO O codigoUsuario ---
                val sharedPref = getSharedPreferences("MeuAppPrefs", Context.MODE_PRIVATE)
                val codigoUsuario = sharedPref.getString("codigoUsuario", null) ?: ""

                Log.d("AvaliarLivro", "CodigoUsuario obtido do SharedPref: '$codigoUsuario'")

                val livroCodigo = dadosLivro?.codigo ?: ""

                Log.d("AvaliarLivro", "CodigoLivro obtido de DADOS_LIVRO: '$livroCodigo'")
                Log.d("AvaliarLivro", "Comentário obtido do EditText: '$comentario'")

                if (livroCodigo.isEmpty()) {
                    Log.e("AvaliarLivro", "ERRO: livroCodigo está vazio!")
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_informacao,
                        mensagem = "Erro: Código do livro não encontrado.",
                        dialogoAtivo = loading
                    )
                    return@setOnClickListener
                }

                if (codigoUsuario.isEmpty()) {
                    Log.e("AvaliarLivro", "ERRO: codigoUsuario está vazio!")
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_informacao,
                        mensagem = "Erro: Código do usuário não encontrado.",
                        dialogoAtivo = loading
                    )
                    return@setOnClickListener
                }


                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val sucesso = RotinasBD.salvarAvaliacao(
                            livroCodigo,
                            codigoUsuario,
                            avaliacao,
                            comentario
                        ) // <-- Chamando com comentário

                        launch(Dispatchers.Main) {
                            if (sucesso) {
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_informacao,
                                    mensagem = "Avaliação publicada com sucesso!",
                                    dialogoAtivo = loading
                                )
                                Handler(mainLooper).postDelayed({
                                    finish()
//                                    navegarVisualizarLivro.putExtra("CODIGO_LIVRO", codigoLivro)
//                                    startActivity(navegarVisualizarLivro)
//                                    this@AvaliarLivro.overridePendingTransition(
//                                        R.anim.animate_fade_enter,
//                                        R.anim.animate_fade_exit
//                                    )
                                }, 1000L)
                            } else {
                                Log.e("AvaliarLivro", "Falha ao salvar avaliação no Firebase.")
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Erro ao publicar avaliação.",
                                    dialogoAtivo = loading
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AvaliarLivro", "Erro na coroutine: ", e)
                        launch(Dispatchers.Main) {
                            Rotinas.mostrarSnackbar(
                                parentView = binding.root,
                                layoutAlerta = R.layout.toast_alerta,
                                mensagem = "Erro inesperado ao avaliar.",
                                dialogoAtivo = loading
                            )
                        }
                    }
                }
            }

            binding.btnVoltarAVL.setOnClickListener {
//                navegarVisualizarLivro.putExtra("DADOS_LIVRO", dadosLivro)
//                startActivity(navegarVisualizarLivro)
                finish()
                this@AvaliarLivro.overridePendingTransition(
                    R.anim.animate_fade_enter,
                    R.anim.animate_fade_exit
                )
            }
        }
    }
}