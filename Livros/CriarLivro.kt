package com.example.unilib.Livros;

import android.app.Activity
import android.content.Intent
import android.os.Bundle;
import android.os.Handler
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import com.example.unilib.Classes.BaseActivity;
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD
import android.Manifest
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.unilib.Classes.Genero
import com.example.unilib.Classes.RotinasBD.lerGeneros
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope

import com.example.unilib.R;
import com.example.unilib.databinding.ActivityCriarLivroBinding
import com.example.unilib.databinding.ActivityEditarLivroBinding
import kotlinx.coroutines.launch

class CriarLivro : BaseActivity() {

    private lateinit var binding: ActivityCriarLivroBinding

    private var imageUri: Uri? = null

    private lateinit var lkpGeneros: Spinner

    private lateinit var generoSelecionado: String

    private val galeriaLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                imageUri = data?.data

                if (imageUri != null) {
                    val uri = imageUri!!

                    val takeFlags =
                        data?.flags?.and((Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))

                    try {
                        binding.capaLivro.setImageURI(uri)
                        binding.capaLivro.alpha = 1.0F;
                        binding.btnEditarFoto.visibility = View.GONE;

                    } catch (e: SecurityException) {
                        e.printStackTrace()
                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_alerta,
                            mensagem = "Erro de permissão: Acesso persistente negado."
                        )
                        binding.capaLivro.setImageURI(uri)
                    }

                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        binding = ActivityCriarLivroBinding.inflate(layoutInflater)

        setContentView(binding.root);
    }

    override fun onStart() {
        super.onStart()

        val navegarVisualizarLivros = Intent(this, VisualizarLivros::class.java)

        binding.btnVoltar.setOnClickListener {
            finish()
//            startActivity(navegarVisualizarLivros)
//            this.overridePendingTransition(
//                R.anim.animate_fade_enter,
//                R.anim.animate_fade_exit
//            )
        }

        lifecycleScope.launch {

            lkpGeneros = binding.lkpGeneros

            var generosList: MutableList<Genero>;
            generosList = lerGeneros();

            generosList.removeAll { it.codigo == "00001" }

            generosList.add(0, Genero(codigo = "hint_item", nome = "Escolha um gênero", icone = ""))

            val nomesGeneros: List<String> = generosList.map { it.nome }

            val adapter =
                ArrayAdapter(this@CriarLivro, android.R.layout.simple_spinner_item, nomesGeneros)

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            lkpGeneros.adapter = adapter

            lkpGeneros.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (position > 0) {
                        val generoSelecionado: Genero = generosList[position]

                        this@CriarLivro.generoSelecionado = generoSelecionado.codigo;
                    } else {
                        this@CriarLivro.generoSelecionado = ""
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

        }

        binding.btnEditarFoto.setOnClickListener {
            val intentGaleria = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            galeriaLauncher.launch(intentGaleria)
        }

        binding.btnCriar.setOnClickListener {
            if (binding.nomeLivro.text.isBlank() ||
                binding.nomeAutor.text.isBlank() ||
                binding.sinopse.text.isBlank() ||
                binding.valEditora.text.isBlank() ||
                binding.valAnoPublicacao.text.isBlank() ||
                binding.valIdioma.text.isBlank() ||
                binding.valISBN.text.isBlank() ||
                binding.valLocalizacao.text.isBlank() ||
                binding.valTamanho.text.isBlank()
            ) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Preencha todos os campos."
                )
            } else if (binding.capaLivro.drawable == null) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Escolha a capa do livro."
                )
            } else if (generoSelecionado == "") {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Escolha um gênero."
                )
            } else {

                val loading = Rotinas.mostrarLoading(
                    contexto = this@CriarLivro,
                    pLayoutDialogo = R.layout.activity_loading_dialog,
                    pViewMensagem = R.id.loading_mensagem,
                    pMensagem = "Carregando...",
                    pDottieAnimation = "loading_icon.json"
                )
                lifecycleScope.launch {
                    val sucesso = RotinasBD.criarLivro(
                        Livro(
                            codigo = RotinasBD.proximoCodigo("livros", 5, "0".first()),
                            nome = binding.nomeLivro.text.toString(),
                            autor = binding.nomeAutor.text.toString(),
                            capa = Rotinas.ImageToBase64(binding.capaLivro.drawable.toBitmap())
                                .toString(),
                            sinopse = binding.sinopse.text.toString(),
                            genero = generoSelecionado,
                            qtdAvaliacoes = 0,
                            qtdLeituras = 0,
                            editora = binding.valEditora.text.toString(),
                            anoPublicacao = binding.valAnoPublicacao.text.toString(),
                            idioma = binding.valIdioma.text.toString(),
                            iSBN = binding.valISBN.text.toString(),
                            localizacao = binding.valLocalizacao.text.toString(),
                            qtdPaginas = binding.valTamanho.text.toString(),
                            copiasDisponiveis = binding.valCopias.text.toString().toInt(),
                        )
                    )

                    if (sucesso) {
                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_informacao,
                            mensagem = "Livro criado com sucesso!",
                            dialogoAtivo = loading
                        )
                        Handler(mainLooper).postDelayed({
                            finish()
//                            val navegarVisualizarLivros =
//                                Intent(this@CriarLivro, VisualizarLivros::class.java);
//                            startActivity(navegarVisualizarLivros)
//                            this@CriarLivro.overridePendingTransition(
//                                R.anim.animate_fade_enter, R.anim.animate_fade_exit
//                            )
                        }, 1000L)
                    }


                }
            }
        }
    }
}