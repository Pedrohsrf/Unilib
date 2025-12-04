package com.example.unilib.Livros;

import DialogCallback
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle;
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

import com.example.unilib.Classes.BaseActivity;
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.Rotinas.normalizeAndLowercase
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dotlottie.dlplayer.Mode
import com.example.unilib.Administracao
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Review
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Rotinas.normalizeAndLowercase
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.ConsultaPorPrefixo
import com.example.unilib.Classes.RotinasBD.lerLivros
import com.example.unilib.Classes.Usuario
import com.example.unilib.Livros.RecyclerView_Adapter.GerenciarLivrosAdapter

import com.example.unilib.R;
import com.example.unilib.RodapeFM
import com.example.unilib.Salas.RecyclerView_Adapter.DatasAdapter
import com.example.unilib.databinding.ActivityVisualizarLivrosBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.Normalizer
import java.util.Locale
import kotlin.random.Random

class VisualizarLivros : BaseActivity() {

    private lateinit var binding: ActivityVisualizarLivrosBinding

    private lateinit var livrosAdapter: GerenciarLivrosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        binding = ActivityVisualizarLivrosBinding.inflate(layoutInflater)
        setContentView(binding.root);
    }

    override fun onStart() {
        super.onStart()
        var livrosList: MutableList<Livro> = mutableListOf();

        binding.loading.visibility = View.VISIBLE;
        binding.recyclerview.visibility = View.GONE;
        val config = Config.Builder().autoplay(true).speed(1f).loop(true)
            .source(DotLottieSource.Asset("loading_preto.json")).playMode(Mode.FORWARD)
            .useFrameInterpolation(true).build()

        val dotLottieAnimationView = binding.loading

        dotLottieAnimationView.load(config)

        lifecycleScope.launch {
            try {
                livrosList = lerLivros();

                if (!livrosList.isEmpty()) {
                    for (livro in livrosList) {
                        livro.gerenciavel = 2;
                    }

                    livrosAdapter = GerenciarLivrosAdapter(
                        this@VisualizarLivros, livrosList, onDeletarClickListener = { livro ->
                            val deletarLivro = Rotinas.mostrarDialogo(
                                contexto = this@VisualizarLivros,
                                pLayoutDialogo = R.layout.dialogo_critico,
                                pMensagemPrincipal = "Confirmar exclusão?",
                                pMensagemSecundaria = "Essa ação não pode ser revertida. Todos os dados desse livro serão perdidos.",
                                pBtnSim = "DELETAR",
                                pBtnNao = "CANCELAR",
                                pDottieAnimation = "",
                                object : DialogCallback {
                                    override fun onSimClicked() {
                                        lifecycleScope.launch {
                                            val loadingReservando = Rotinas.mostrarLoading(
                                                contexto = this@VisualizarLivros,
                                                pLayoutDialogo = R.layout.activity_loading_dialog,
                                                pViewMensagem = R.id.loading_mensagem,
                                                pMensagem = "Deletando...",
                                                pDottieAnimation = "loading_icon.json"
                                            )

                                            val sucesso = RotinasBD.deletarLivro(
                                                livro.codigo
                                            )

                                            if (sucesso) {
                                                Rotinas.mostrarSnackbar(
                                                    parentView = binding.root,
                                                    layoutAlerta = R.layout.toast_informacao,
                                                    mensagem = "Livro deletado com sucesso!",
                                                    dialogoAtivo = loadingReservando
                                                )

                                                livrosList = lerLivros();

                                                for (livro in livrosList) {
                                                    livro.gerenciavel = 2;
                                                }

                                                livrosAdapter.atualizarLista(livrosList);
                                            }
                                        }
                                    }

                                    override fun onNaoClicked() {

                                    }
                                })
                        }, onEditarClickListener = { livro ->
                            val navegarEditarLivro =
                                Intent(this@VisualizarLivros, EditarLivro::class.java);
                            navegarEditarLivro.putExtra("DADOS_LIVRO", livro.codigo)
                            startActivity(navegarEditarLivro)
                            this@VisualizarLivros.overridePendingTransition(
                                R.anim.animate_fade_enter,
                                R.anim.animate_fade_exit
                            )
                        }

                    )

                    binding.recyclerview.adapter = livrosAdapter;
                    binding.recyclerview.layoutManager =
                        LinearLayoutManager(this@VisualizarLivros, RecyclerView.VERTICAL, false)

                    binding.recyclerview.visibility = View.VISIBLE;
                }
            } finally {
                binding.loading.visibility = View.GONE;
            }
        }

        val navegarAdministracao = Intent(this, RodapeFM::class.java)
        navegarAdministracao.putExtra("ID_DESTINO", R.id.nav_administracao)

        binding.btnVoltarAdministracao.setOnClickListener {
            startActivity(navegarAdministracao)
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )

        }

        binding.pesquisarInput.doOnTextChanged { text, start, before, count ->
            Log.i("INICIO: Edit Atualizado", binding.pesquisarInput.text.toString())

            lifecycleScope.launch {

//                if (!binding.pesquisarInput.text.toString().equals("")) {

                    var listaSnapshots = ConsultaPorPrefixo(
                        "livros", binding.pesquisarInput.text.toString(), "nome", "nome_lower"
                    )
                    var listaLivros: MutableList<Livro> = mutableListOf();

                    for (livro in listaSnapshots) {
                        Log.i(
                            "Livro lido para: " + binding.pesquisarInput.text.toString(),
                            livro.get("nome") as String
                        )

                        val objLivro = Livro(
                            codigo = livro.get("codigo") as String,
                            nome = livro.get("nome") as String,
                            autor = livro.get("autor") as String,
                            capa = livro.get("capa") as String,
                            sinopse = livro.get("sinopse") as String,
                            genero = livro.get("genero") as String,
                            qtdAvaliacoes = (livro.get("qtdAvaliacoes") as Long).toInt(),
                            qtdLeituras = (livro.get("qtdLeituras") as Long).toInt(),
                            editora = livro.get("editora") as String,
                            anoPublicacao = livro.get("anoPublicacao") as String,
                            idioma = livro.get("idioma") as String,
                            iSBN = livro.get("ISBN") as String,
                            localizacao = livro.get("localizacao") as String,
                            qtdPaginas = livro.get("qtdPaginas") as String,
                            copiasDisponiveis = (livro.get("copiasDisponiveis") as Long).toInt(),
                        );
                        listaLivros.add(objLivro)
                    }

                    if (listaLivros.isEmpty()) {
                        binding.labNenhumEncontrado.visibility = View.VISIBLE;
                        binding.recyclerview.visibility = View.GONE;
                        binding.labNenhumEncontrado.text = "Nenhum livro encontrado.";

                    } else {
                        for (livro in listaLivros) {
                            livro.gerenciavel = 2;
                        }

                        binding.labNenhumEncontrado.visibility = View.GONE;
                        binding.recyclerview.visibility = View.VISIBLE;
                    }

                    livrosAdapter.atualizarLista(listaLivros)
//                }
            }
        }

            binding.btnCriar.setOnClickListener {
                Log.i("Criar livro", "Clicado")
                val navegarCriarLivro = Intent(this, CriarLivro::class.java);
                startActivity(navegarCriarLivro)
                this.overridePendingTransition(
                    R.anim.animate_fade_enter,
                    R.anim.animate_fade_exit
                )
            }


    }
}