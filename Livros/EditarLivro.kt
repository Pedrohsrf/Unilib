package com.example.unilib.Livros;

import DialogCallback
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle;
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import com.example.unilib.Classes.BaseActivity;
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.Genero
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.lerGeneros

import com.example.unilib.R;
import com.example.unilib.RodapeFM
import com.example.unilib.databinding.ActivityEditarLivroBinding
import com.example.unilib.databinding.ActivityEditarPerfilBinding
import kotlinx.coroutines.launch

class EditarLivro : BaseActivity() {

    private lateinit var binding: ActivityEditarLivroBinding

    private lateinit var dadosLivro: Livro;

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

        binding = ActivityEditarLivroBinding.inflate(layoutInflater)

        lifecycleScope.launch {

            dadosLivro = RotinasBD.lerLivro(intent.getStringExtra("DADOS_LIVRO").toString());

            if (dadosLivro != null) {
                binding.nomeEvento.setText(dadosLivro.nome);
                binding.dataEvento.setText(dadosLivro.autor);
                binding.capaLivro.setImageBitmap(Rotinas.Base64ToImage(dadosLivro.capa));
                binding.sinopse.setText(dadosLivro.sinopse);
                binding.valEditora.setText(dadosLivro.editora);
                binding.valAnoPublicacao.setText(dadosLivro.anoPublicacao);
                binding.valIdioma.setText(dadosLivro.idioma);
                binding.valISBN.setText(dadosLivro.iSBN);
                binding.valLocalizacao.setText(dadosLivro.localizacao);
                binding.valTamanho.setText(dadosLivro.qtdPaginas);
                binding.valCopias.setText(dadosLivro.copiasDisponiveis.toString());
                binding.valEmprestimos.setText(dadosLivro.qtdLeituras.toString());
            }

            lkpGeneros = binding.lkpGeneros

            var generosList: MutableList<Genero>;
            generosList = lerGeneros();

            generosList.removeAll { it.codigo == "00001" }

            generosList.add(0, Genero(codigo = "hint_item", nome = "Escolha um gênero", icone = ""))

            val nomesGeneros: List<String> = generosList.map { it.nome }

            val adapter =
                ArrayAdapter(this@EditarLivro, android.R.layout.simple_spinner_item, nomesGeneros)

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            lkpGeneros.adapter = adapter

            lkpGeneros.setSelection(generosList.indexOfFirst { it.codigo == dadosLivro.genero })

            lkpGeneros.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (position > 0) {
                        val generoSelecionado: Genero = generosList[position]

                        this@EditarLivro.generoSelecionado = generoSelecionado.codigo;
                    } else {
                        this@EditarLivro.generoSelecionado = ""
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            setContentView(binding.root);


        }
    }

    override fun onStart() {
        super.onStart()

        binding.btnEditarLivro.setOnClickListener {
            if (dadosLivro !== null) {
                if (binding.nomeEvento.text.isBlank() ||
                    binding.dataEvento.text.isBlank() ||
                    binding.sinopse.text.isBlank() ||
                    generoSelecionado.isBlank() ||
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
                } else {
                    val dialogo = Rotinas.mostrarDialogo(
                        contexto = this@EditarLivro,
                        pLayoutDialogo = R.layout.dialogo_medio,
                        pMensagemPrincipal = "Confirmar edição?",
                        pMensagemSecundaria = "Essa ação não pode ser revertida. Todos os dados desse livro serão atualizados.",
                        pBtnSim = "EDITAR",
                        pBtnNao = "CANCELAR",
                        pDottieAnimation = "",
                        object : DialogCallback {
                            override fun onSimClicked() {
                                lifecycleScope.launch {
                                    val loading = Rotinas.mostrarLoading(
                                        contexto = this@EditarLivro,
                                        pLayoutDialogo = R.layout.activity_loading_dialog,
                                        pViewMensagem = R.id.loading_mensagem,
                                        pMensagem = "Salvando alterações...",
                                        pDottieAnimation = "loading_icon.json"
                                    )

                                    val sucesso = RotinasBD.editarLivro(
                                        Livro(
                                            codigo = dadosLivro.codigo,
                                            nome = binding.nomeEvento.text.toString(),
                                            autor = binding.dataEvento.text.toString(),
                                            capa = Rotinas.ImageToBase64(binding.capaLivro.drawable.toBitmap())
                                                .toString(),
                                            sinopse = binding.sinopse.text.toString(),
                                            genero = lkpGeneros.selectedItem as String,
                                            qtdAvaliacoes = dadosLivro.qtdAvaliacoes,
                                            qtdLeituras = dadosLivro.qtdLeituras,
                                            editora = binding.valEditora.text.toString(),
                                            anoPublicacao = binding.valAnoPublicacao.text.toString(),
                                            idioma = binding.valIdioma.text.toString(),
                                            iSBN = binding.valISBN.text.toString(),
                                            localizacao = binding.valLocalizacao.text.toString(),
                                            qtdPaginas = binding.valTamanho.text.toString(),
                                            copiasDisponiveis = binding.valCopias.text.toString()
                                                .toInt(),
                                        )
                                    )

                                    if (sucesso) {
                                        Rotinas.mostrarSnackbar(
                                            parentView = binding.root,
                                            layoutAlerta = R.layout.toast_informacao,
                                            mensagem = "Alterações salvas com sucesso!",
                                            dialogoAtivo = loading
                                        )
                                        Handler(mainLooper).postDelayed({
                                            finish()
//                                            val navegarVisualizarLivros =
//                                                Intent(
//                                                    this@EditarLivro,
//                                                    VisualizarLivros::class.java
//                                                );
//                                            startActivity(navegarVisualizarLivros)
//                                            this@EditarLivro.overridePendingTransition(
//                                                R.anim.animate_fade_enter, R.anim.animate_fade_exit
//                                            )
                                        }, 1000L)
                                    }

                                }
                            }

                            override fun onNaoClicked() {

                            }
                        })
                }
            }
        }

        val navegarVisualizarLivros = Intent(this, VisualizarLivros::class.java)

        binding.btnEditarFoto.setOnClickListener {
            val intentGaleria = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            galeriaLauncher.launch(intentGaleria)
        }

        binding.btnVoltar.setOnClickListener {
            finish()
//            startActivity(navegarVisualizarLivros)
//            this.overridePendingTransition(
//                R.anim.animate_fade_enter,
//                R.anim.animate_fade_exit
//            )
        }
    }
}