package com.example.unilib.Livros;

import DialogCallback
import android.content.Context
import android.content.Intent
import android.os.Bundle;
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast

import com.example.unilib.Classes.BaseActivity;
import androidx.cardview.widget.CardView
// !! 1. IMPORTE O 'ContextCompat' PARA MUDAR A COR DO BOTÃO !!
import androidx.core.content.ContextCompat
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Review
import com.example.unilib.Inicio.RecyclerView_Adapter.Livros1Adapter
import com.example.unilib.Inicio.RecyclerView_Adapter.ReviewsAdapter

import com.example.unilib.R;
import com.example.unilib.RodapeFM
import com.example.unilib.Classes.Rotinas
import androidx.lifecycle.lifecycleScope
import com.dotlottie.dlplayer.Mode
import com.example.unilib.Classes.Emprestimo
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.adicionarFavorito
import com.example.unilib.Classes.RotinasBD.criarEmprestimo
import com.example.unilib.Classes.RotinasBD.fb
import com.example.unilib.Classes.RotinasBD.lerEmprestimos
import com.example.unilib.Classes.RotinasBD.lerGenero
import com.example.unilib.Classes.RotinasBD.lerListas
import com.example.unilib.Classes.RotinasBD.lerLivro
import com.example.unilib.Classes.RotinasBD.lerLivros
import com.example.unilib.Classes.RotinasBD.lerUsuario
import com.example.unilib.Classes.RotinasBD.removerFavorito
import com.example.unilib.Classes.Usuario
import com.example.unilib.Login.Login
import com.example.unilib.databinding.ActivityVisualizarLivroBinding
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.StringFormat
import java.text.DecimalFormat
// !! 2. IMPORTAÇÃO CORRIGIDA PARA PEGAR A HORA ATUAL !!
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.random.Random

class VisualizarLivro : BaseActivity() {
    private lateinit var btnVoltar: ImageView;
    private lateinit var btnSalvar: ImageView;
    private lateinit var btnEmprestar: Button;
    private lateinit var btnAvaliacoes: LinearLayout;
    private lateinit var dadosLivro: Livro;
    private lateinit var codigoLivro: String;

    private lateinit var binding: ActivityVisualizarLivroBinding
    private lateinit var livrosSemelhantesAdapter: Livros1Adapter
    private lateinit var livrosRecomendadosAdapter: Livros1Adapter

    /**
     * Atualiza a aparência e o estado do botão de empréstimo.
     */
    private fun atualizarStatusBotaoEmprestar(estaEmprestado: Boolean, copiasDisponiveis: Boolean) {
        try {
            if (estaEmprestado || !copiasDisponiveis) {
                binding.btnEmprestarVL.isEnabled = false
            } else {
                binding.btnEmprestarVL.isEnabled = true
            }
        } catch (e: Exception) {
            Log.e("VisualizarLivro", "Erro ao atualizar botão: ${e.message}")
        }
    }


    fun preencherBarrasDeAvaliacao(
        percentuaisPorNota: Map<Int, Double>
    ) {
        val barrasDeProgresso: Map<Int, ProgressBar> = mapOf(
            5 to binding.barraNota5,
            4 to binding.barraNota4,
            3 to binding.barraNota3,
            2 to binding.barraNota2,
            1 to binding.barraNota1
        )

        for ((nota, percentual) in percentuaisPorNota) {
            val progressBar = barrasDeProgresso[nota]

            if (progressBar != null) {
                val progressoInt = percentual.toInt()

                progressBar.progress = progressoInt
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        binding = ActivityVisualizarLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navegarLivro = Intent(this, VisualizarLivro::class.java);

        val sharedPref = this.getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
        val codigoUsuario = sharedPref.getString("codigoUsuario", "")

        var livrosSemelhantesList: MutableList<Livro> = mutableListOf();
        var livrosRecomendadosList: MutableList<Livro> = mutableListOf();
        var emprestimosList: MutableList<Emprestimo>;
        var favoritadosList: MutableList<Livro>;
        var livrosEmprestadosList: MutableList<Livro> = mutableListOf();
        var livrosList: MutableList<Livro>;

        val config = Config.Builder().autoplay(true).speed(1f).loop(true)
            .source(DotLottieSource.Asset("loading_icon.json")).playMode(Mode.FORWARD)
            .useFrameInterpolation(true).build()

        val dotLottieAnimationView = binding.loading

        dotLottieAnimationView.load(config)
        binding.loading.visibility = View.VISIBLE;
        binding.btnSalvar.visibility = View.GONE;
        binding.cardCapaLivro.visibility = View.GONE;
        binding.layoutInfos.visibility = View.GONE;
        binding.layout1.visibility = View.GONE;
        binding.layout1.visibility = View.GONE;
        binding.divisor1.visibility = View.GONE;
        binding.layoutInfosLivro.visibility = View.GONE;
        binding.layoutSinopse.visibility = View.GONE;
        binding.divisor2.visibility = View.GONE;
        binding.divisor2.visibility = View.GONE;
        binding.labRecomendados.visibility = View.GONE;
        binding.livrosRecomendadosItem.visibility = View.GONE;
        binding.labSemelhantes.visibility = View.GONE;
        binding.livrosSemelhantesItem.visibility = View.GONE;
        binding.divisor3.visibility = View.GONE;
        binding.avaliacoes.visibility = View.GONE;
        binding.reviewsItem.visibility = View.GONE;
        binding.btnEmprestarVL.isEnabled = false;


        lifecycleScope.launch {

            btnEmprestar = binding.btnEmprestarVL;
            btnAvaliacoes = binding.avaliacoes;
            btnVoltar = binding.btnVoltar;
            btnSalvar = binding.btnSalvar;

            codigoLivro = intent.getStringExtra("CODIGO_LIVRO") ?: ""
            dadosLivro = lerLivro(codigoLivro)
            livrosList = lerLivros();


            try {
                val emprestimosAtuais = lerEmprestimos(codigoUsuario.toString())

                val estaEmprestado = emprestimosAtuais.any {
                    it.livro.codigo == codigoLivro && (it.status == "Pendente" || it.status == "Ativo" || it.status == "EM ANDAMENTO")
                }

                val temCopias =
                    if (::dadosLivro.isInitialized) dadosLivro.copiasDisponiveis > 0 else false

                atualizarStatusBotaoEmprestar(estaEmprestado, temCopias)

            } catch (e: Exception) {
                Log.e("VisualizarLivro", "Erro ao verificar status de empréstimo: ${e.message}")
                atualizarStatusBotaoEmprestar(true, false) // Desativa o botão
            }


            val itemAnimatorLivro1 = binding.livrosSemelhantesItem.itemAnimator

            if (itemAnimatorLivro1 is androidx.recyclerview.widget.SimpleItemAnimator) {
                itemAnimatorLivro1.supportsChangeAnimations = false
            }

            val itemAnimatorLivro2 = binding.livrosRecomendadosItem.itemAnimator

            if (itemAnimatorLivro2 is androidx.recyclerview.widget.SimpleItemAnimator) {
                itemAnimatorLivro2.supportsChangeAnimations = false
            }

            if (::dadosLivro.isInitialized) {

                livrosSemelhantesList = livrosList.filter { livro ->
                    return@filter livro.genero == dadosLivro.genero && livro.codigo != dadosLivro.codigo
                }.toMutableList()

                livrosSemelhantesList.shuffle()

                livrosList.removeIf {
                    it.codigo == dadosLivro.codigo
                }

                livrosList.removeAll(livrosSemelhantesList)

                binding.nomeEvento.text = dadosLivro.nome;
                binding.dataEvento.text = dadosLivro.autor;
                binding.capaLivro.setImageBitmap(Rotinas.Base64ToImage(dadosLivro.capa));
                binding.qtdLeituras.text = dadosLivro.qtdLeituras.toString() + " leituras";
                binding.qtdAvaliacoes.text = dadosLivro.qtdAvaliacoes.toString() + " avaliações";
                binding.sinopse.text = dadosLivro.sinopse;
                binding.genero.text = lerGenero(dadosLivro.genero).uppercase();
                binding.valEditora.text = dadosLivro.editora;
                binding.valAnoPublicacao.text = dadosLivro.anoPublicacao;
                binding.valIdioma.text = dadosLivro.idioma;
                binding.valISBN.text = dadosLivro.iSBN;
                binding.valLocalizacao.text = dadosLivro.localizacao;
                binding.valTamanho.text = dadosLivro.qtdPaginas + " páginas";
                binding.valCopias.text = dadosLivro.copiasDisponiveis.toString();
                binding.valEmprestimos.text = dadosLivro.qtdLeituras.toString();
                binding.qtdReviews.text = dadosLivro.qtdAvaliacoes.toString();

                val estatisticasLivro = Rotinas.calcularEstatisticasAvaliacao(dadosLivro.reviews);
                binding.mediaAvaliacoes.text = String.format("%.1f", estatisticasLivro.mediaGeral);
                Rotinas.preencherEstrelas(
                    binding.estrela1,
                    binding.estrela2,
                    binding.estrela3,
                    binding.estrela4,
                    binding.estrela5,
                    estatisticasLivro.mediaGeral
                );

                Rotinas.preencherEstrelas(
                    binding.estrelaHeader1,
                    binding.estrelaHeader2,
                    binding.estrelaHeader3,
                    binding.estrelaHeader4,
                    binding.estrelaHeader5,
                    estatisticasLivro.mediaGeral
                );
                preencherBarrasDeAvaliacao(estatisticasLivro.percentuaisPorNota);


                binding.loading.visibility = View.GONE;
                binding.btnSalvar.visibility = View.VISIBLE;
                binding.cardCapaLivro.visibility = View.VISIBLE;
                binding.layoutInfos.visibility = View.VISIBLE;
                binding.layout1.visibility = View.VISIBLE;
                binding.layout1.visibility = View.VISIBLE;
                binding.divisor1.visibility = View.VISIBLE;
                binding.layoutInfosLivro.visibility = View.VISIBLE;
                binding.layoutSinopse.visibility = View.VISIBLE;
                binding.divisor2.visibility = View.VISIBLE;
                binding.divisor2.visibility = View.VISIBLE;
                binding.labRecomendados.visibility = View.VISIBLE;
                binding.livrosRecomendadosItem.visibility = View.VISIBLE;
                binding.labSemelhantes.visibility = View.VISIBLE;
                binding.livrosSemelhantesItem.visibility = View.VISIBLE;
                binding.divisor3.visibility = View.VISIBLE;
                binding.avaliacoes.visibility = View.VISIBLE;
                binding.reviewsItem.visibility = View.VISIBLE;

                // (A nossa nova verificação no topo do 'lifecycleScope' é melhor que esta)
                // if (dadosLivro.copiasDisponiveis > 0) {
                //    binding.btnEmprestarVL.isEnabled = true;
                // }
            } else {
                binding.loading.visibility = View.GONE;
                binding.labNenhumEncontrado.visibility = View.VISIBLE;
            }


            val reviewsAdapter =
                if (::dadosLivro.isInitialized) {
                    ReviewsAdapter(this@VisualizarLivro, dadosLivro.reviews, scope = lifecycleScope)
                } else {
                    null
                }
            livrosSemelhantesAdapter = Livros1Adapter(this@VisualizarLivro,
                livrosSemelhantesList,
                onItemClickListener = { livro ->
                    navegarLivro.putExtra("CODIGO_LIVRO", livro.codigo)
                    startActivity(navegarLivro)
                    this@VisualizarLivro.overridePendingTransition(
                        R.anim.animate_fade_enter, R.anim.animate_fade_exit
                    )
                },
                onFavoritoClickListener = { livro, position ->
                    lifecycleScope.launch {
                        if (livro.favoritado == false) {
                            if (adicionarFavorito(
                                    codigoUsuario.toString(), livro.codigo
                                )
                            ) {
                                livro.favoritado = true;
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_informacao,
                                    mensagem = "Livro adicionado aos favoritos!"
                                )
                            } else {
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Erro ao favoritar livro."
                                )
                            }

                        } else {
                            if (removerFavorito(
                                    codigoUsuario.toString(), livro.codigo
                                )
                            ) {
                                livro.favoritado = false;
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_informacao,
                                    mensagem = "Livro removido dos favoritos."
                                )
                            } else {
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Erro ao desfavoritar livro."
                                )
                            }
                        }


                        livrosSemelhantesList = mutableListOf();

                        emprestimosList =
                            lerEmprestimos(codigoUsuario.toString(), "EM ANDAMENTO");

                        val (favoritosApósUpdate, _) = lerListas(codigoUsuario.toString());
                        favoritadosList = favoritosApósUpdate;
                        val codigosLivrosFavoritados =
                            favoritadosList.map { it.codigo }.toSet()

                        for (emprestimo in emprestimosList) {
                            if (emprestimo.livro.codigo in codigosLivrosFavoritados) {
                                emprestimo.livro.favoritado = true;
                            }
                            livrosEmprestadosList.add(emprestimo.livro)
                        }

                        livrosSemelhantesAdapter.notifyItemChanged(position)
                        livrosRecomendadosAdapter.atualizarListaLivros(livrosEmprestadosList)

                    }
                })
            livrosRecomendadosAdapter = Livros1Adapter(this@VisualizarLivro,
                livrosList,
                onItemClickListener = { livro ->
                    navegarLivro.putExtra("CODIGO_LIVRO", livro.codigo)
                    startActivity(navegarLivro)
                    this@VisualizarLivro.overridePendingTransition(
                        R.anim.animate_fade_enter, R.anim.animate_fade_exit
                    )
                },
                onFavoritoClickListener = { livro, position ->
                    lifecycleScope.launch {
                        if (livro.favoritado == false) {
                            if (adicionarFavorito(
                                    codigoUsuario.toString(), livro.codigo
                                )
                            ) {
                                livro.favoritado = true;
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_informacao,
                                    mensagem = "Livro adicionado aos favoritos!"
                                )
                            } else {
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Erro ao favoritar livro."
                                )
                            }

                        } else {
                            if (removerFavorito(
                                    codigoUsuario.toString(), livro.codigo
                                )
                            ) {
                                livro.favoritado = false;
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_informacao,
                                    mensagem = "Livro removido dos favoritos."
                                )
                            } else {
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Erro ao desfavoritar livro."
                                )
                            }
                        }


                        livrosRecomendadosList = mutableListOf();

                        emprestimosList =
                            lerEmprestimos(codigoUsuario.toString(), "EM ANDAMENTO");

                        val (favoritosApósUpdate, _) = lerListas(codigoUsuario.toString());
                        favoritadosList = favoritosApósUpdate;
                        val codigosLivrosFavoritados =
                            favoritadosList.map { it.codigo }.toSet()

                        for (emprestimo in emprestimosList) {
                            if (emprestimo.livro.codigo in codigosLivrosFavoritados) {
                                emprestimo.livro.favoritado = true;
                            }
                            livrosEmprestadosList.add(emprestimo.livro)
                        }

                        livrosRecomendadosAdapter.notifyItemChanged(position)
                        livrosSemelhantesAdapter.atualizarListaLivros(livrosEmprestadosList)

                    }
                })

            binding.livrosSemelhantesItem.adapter = livrosSemelhantesAdapter
            binding.livrosRecomendadosItem.adapter = livrosRecomendadosAdapter
            binding.reviewsItem.adapter = reviewsAdapter

        }
    }

    override fun onStart() {
        super.onStart()

        val navegarInicio = Intent(this, RodapeFM::class.java);
        val navegarEstante = Intent(this, RodapeFM::class.java);
        val navegarAvaliarLivro =
            Intent(this, AvaliarLivro::class.java);

        binding.btnEmprestarVL.setOnClickListener {
            lifecycleScope.launch {
                val sharedPref = getSharedPreferences("MeuAppPrefs", Context.MODE_PRIVATE)
                val UID = sharedPref.getString("UID", null)

                val loading = Rotinas.mostrarLoading(
                    contexto = this@VisualizarLivro,
                    pLayoutDialogo = R.layout.activity_loading_dialog,
                    pViewMensagem = R.id.loading_mensagem,
                    pMensagem = "Carregando...",
                    pDottieAnimation = "loading_icon.json"
                )

                var documentUsuario = lerUsuario(UID.toString())

                var objUsuario = Usuario(
                    codigo = documentUsuario.getString("codigo") ?: "",
                    nome = documentUsuario.getString("nomeCompleto") ?: "",
                    email = documentUsuario.getString("emailTelefone") ?: "",
                    fotoPerfil = documentUsuario.getString("fotoPerfil") ?: ""
                )

                var dataAtual = LocalDateTime.now()
                var dataLimite = dataAtual.plusDays(14)

                var objEmprestimo = Emprestimo(
                    livro = dadosLivro,
                    dataEmprestimo = Date.from(
                        dataAtual.atZone(ZoneId.systemDefault())
                            .toInstant()
                    ),
                    dataDevolucao = Date.from(
                        dataAtual.atZone(ZoneId.systemDefault())
                            .toInstant()
                    ),
                    usuario = objUsuario,
                    status = "Pendente",
                    dataLimite = Date.from(
                        dataLimite.atZone(ZoneId.systemDefault())
                            .toInstant()
                    )
                )

                var sucesso = criarEmprestimo(objEmprestimo)

                if (sucesso) {
                    atualizarStatusBotaoEmprestar(true, false)

                    val emprestimoDialogo = Rotinas.mostrarDialogo(contexto = this@VisualizarLivro,
                        pLayoutDialogo = R.layout.dialogo_neutro,
                        pMensagemPrincipal = "Empréstimo registrado",
                        pMensagemSecundaria = "Vá até a biblioteca para pegar o seu livro, e acompanhe o seu empréstimo na tela de Estante",
                        pBtnSim = "OK",
                        pBtnNao = "",
                        pDottieAnimation = "",
                        object : DialogCallback {
                            override fun onSimClicked() {

                            }

                            override fun onNaoClicked() {

                            }
                        })
                    loading.dismiss()
                } else {
                    Rotinas.mostrarSnackbar(
                        binding.root,
                        R.layout.toast_alerta,
                        "Erro ao solicitar empréstimo, tente novamente."
                    )
                    loading.dismiss()
                }
            }
        }

        btnVoltar.setOnClickListener() {
            finish()
//            startActivity(navegarInicio)
//            this.overridePendingTransition(
//                R.anim.animate_fade_enter, R.anim.animate_fade_exit
//            )
        }

        binding.btnAvaliar.setOnClickListener() {
            navegarAvaliarLivro.putExtra("CODIGO_LIVRO", codigoLivro)
            startActivity(navegarAvaliarLivro)
            this.overridePendingTransition(
                R.anim.animate_fade_enter, R.anim.animate_fade_exit
            )
        }

        lifecycleScope.launch {
            try {
                val usuarioCodigo = getUsuarioLogado().codigo
                if (::dadosLivro.isInitialized && usuarioCodigo.isNotEmpty()) {
                    val (favoritos, _) = RotinasBD.lerListas(usuarioCodigo)

                    val livroJaFavorito = favoritos.any { it.codigo == dadosLivro.codigo }

                    if (livroJaFavorito) {
                        btnSalvar.setImageResource(R.drawable.salvar_icon_selecionado)
                        btnSalvar.tooltipText = "salvo"
                    } else {
                        btnSalvar.setImageResource(R.drawable.salvar_icon_deselecionado)
                        btnSalvar.tooltipText = "naoSalvo"
                    }
                }
            } catch (e: Exception) {
                Log.e("VisualizarLivro", "Erro ao verificar status de favorito: ${e.message}")
            }
        }


        btnSalvar.setOnClickListener() {

            if (!::dadosLivro.isInitialized) {
                Rotinas.mostrarSnackbar(
                    binding.root,
                    R.layout.toast_alerta,
                    "Erro ao carregar livro."
                )
                return@setOnClickListener
            }
            val livroCodigo = dadosLivro.codigo
            val usuarioCodigo = getUsuarioLogado().codigo


            if (btnSalvar.tooltipText.toString() == "salvo") {

                val loadingRemovendo = Rotinas.mostrarLoading(
                    contexto = this@VisualizarLivro,
                    pLayoutDialogo = R.layout.activity_loading_dialog,
                    pViewMensagem = R.id.loading_mensagem,
                    pMensagem = "Carregando...",
                    pDottieAnimation = "loading_icon.json"
                )

                lifecycleScope.launch {
                    try {
                        val sucesso = RotinasBD.removerFavorito(usuarioCodigo, livroCodigo)

                        if (sucesso) {
                            btnSalvar.setImageResource(R.drawable.salvar_icon_deselecionado)
                            btnSalvar.tooltipText = "naoSalvo"
                            Rotinas.mostrarSnackbar(
                                parentView = binding.root,
                                layoutAlerta = R.layout.toast_informacao,
                                mensagem = "Livro removido dos salvos",
                                dialogoAtivo = loadingRemovendo
                            )
                        } else {
                            throw Exception("RotinasBD.removerFavorito falhou")
                        }

                    } catch (e: Exception) {
                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_alerta,
                            mensagem = "Erro ao remover livro",
                            dialogoAtivo = loadingRemovendo
                        )
                    }
                }

            } else {
                val dialogo = Rotinas.mostrarDialogoListas(
                    this,
                    binding.root,
                    adicionarClickFavorito = {

                        val loading = Rotinas.mostrarLoading(
                            contexto = this@VisualizarLivro,
                            pLayoutDialogo = R.layout.activity_loading_dialog,
                            pViewMensagem = R.id.loading_mensagem,
                            pMensagem = "Salvando em Favoritos...",
                            pDottieAnimation = "loading_icon.json"
                        )

                        lifecycleScope.launch {
                            try {
                                val sucesso =
                                    RotinasBD.adicionarFavorito(usuarioCodigo, livroCodigo)

                                if (sucesso) {
                                    btnSalvar.setImageResource(R.drawable.salvar_icon_selecionado)
                                    btnSalvar.tooltipText = "salvo"
                                    Rotinas.mostrarSnackbar(
                                        parentView = binding.root,
                                        layoutAlerta = R.layout.toast_informacao,
                                        mensagem = "Livro salvo com sucesso!",
                                        dialogoAtivo = loading
                                    )
                                } else {
                                    throw Exception("RotinasBD.adicionarFavorito falhou")
                                }

                            } catch (e: Exception) {
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Erro ao salvar em Favoritos",
                                    dialogoAtivo = loading
                                )
                            }
                        }
                    },
                    adicionarClickDesejo = {

                        val loading = Rotinas.mostrarLoading(
                            contexto = this@VisualizarLivro,
                            pLayoutDialogo = R.layout.activity_loading_dialog,
                            pViewMensagem = R.id.loading_mensagem,
                            pMensagem = "Salvando em Lista de Desejos...",
                            pDottieAnimation = "loading_icon.json"
                        )

                        lifecycleScope.launch {
                            try {
                                val sucesso = RotinasBD.adicionarDesejo(usuarioCodigo, livroCodigo)

                                if (sucesso) {
                                    Rotinas.mostrarSnackbar(
                                        parentView = binding.root,
                                        layoutAlerta = R.layout.toast_informacao,
                                        mensagem = "Livro salvo na Lista de Desejos!",
                                        dialogoAtivo = loading
                                    )
                                } else {
                                    throw Exception("RotinasBD.adicionarDesejo falhou")
                                }

                            } catch (e: Exception) {
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Erro ao salvar na Lista de Desejos",
                                    dialogoAtivo = loading
                                )
                            }
                        }
                    }
                ) // Fim da chamada Rotinas.mostrarDialogoListas
            }
        }
    }
}