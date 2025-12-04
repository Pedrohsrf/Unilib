package com.example.unilib.Listas

import DialogCallback
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.BaseActivity
import com.example.unilib.Classes.Lista
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Livros.EditarLivro
import com.example.unilib.Livros.RecyclerView_Adapter.GerenciarLivrosAdapter
import com.example.unilib.R
import com.example.unilib.RodapeFM
import com.example.unilib.databinding.ActivityEditarListaBinding
import kotlinx.coroutines.launch

class EditarLista : BaseActivity() {

    private lateinit var binding: ActivityEditarListaBinding
    private var editando: Boolean = false
    private lateinit var nomeDaLista: String
    private lateinit var livrosAdapter: GerenciarLivrosAdapter
    private lateinit var livrosMutaveis: MutableList<Livro>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarListaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        nomeDaLista = intent.getStringExtra("NOME_DA_LISTA") ?: ""
        if (nomeDaLista.isEmpty()) {
            Rotinas.mostrarSnackbar(binding.root, R.layout.toast_alerta, "Erro ao carregar lista")
            finish()
            return
        }

        binding.nomeLista.setText(nomeDaLista)
        livrosMutaveis = mutableListOf()

        // Configura o adapter usando a lógica de 'gerenciavel'
        setupRecyclerView()

        // Configura os cliques
        setupClickListeners()

        // Carrega os dados do Firebase
        carregarLivrosDaLista()
    }

    private fun carregarLivrosDaLista() {
        binding.recyclerview.visibility = View.INVISIBLE
        lifecycleScope.launch {
            try {
                val usuarioCodigo = getUsuarioLogado().codigo
                val (livrosFavoritos, livrosDesejos) = RotinasBD.lerListas(usuarioCodigo)

                val livrosParaMostrar = if (nomeDaLista == "Favoritos") {
                    livrosFavoritos
                } else {
                    livrosDesejos
                }

                livrosMutaveis.clear()
                livrosMutaveis.addAll(livrosParaMostrar)
                livrosAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Rotinas.mostrarSnackbar(binding.root, R.layout.toast_alerta, "Erro ao carregar livros.")
            } finally {
                binding.recyclerview.visibility = View.VISIBLE
            }
        }
    }


    private fun setupRecyclerView() {
        // !! CORREÇÃO AQUI !!
        // Instancia o SEU adapter (que usa 'on...ClickListener' nullável)
        livrosAdapter = GerenciarLivrosAdapter(
            this,
            livrosMutaveis,

            // Passa o listener para o construtor 'onEditarClickListener'
            onEditarClickListener = { livro ->
                finish()
//                val intent = Intent(this, EditarLivro::class.java)
//                intent.putExtra("DADOS_LIVRO", livro.codigo)
//                startActivity(intent)
            },

            // Passa o listener para o construtor 'onDeletarClickListener'
            onDeletarClickListener = { livro ->
                Rotinas.mostrarDialogo(
                    contexto = this,
                    pLayoutDialogo = R.layout.dialogo_neutro,
                    pMensagemPrincipal = "Confirmar exclusão?",
                    pMensagemSecundaria = "Deseja realmente remover '${livro.nome}' da sua lista?",
                    pBtnSim = "REMOVER",
                    pBtnNao = "CANCELAR",
                    pDottieAnimation = "",
                    object : DialogCallback {
                        override fun onSimClicked() {
                            removerLivroDoBanco(livro)
                        }
                        override fun onNaoClicked() {
                            // Não faz nada
                        }
                    })
            }
        )
        binding.recyclerview.adapter = livrosAdapter
    }

    // !! CORREÇÃO AQUI !!
    // Altera a função para usar a lógica 'gerenciavel' em vez de 'setEditMode'
    private fun setupClickListeners() {
        binding.btnEditar.setOnClickListener {
            if (!editando) {
                editando = true
                binding.btnEditar.setImageResource(R.drawable.check_icon2)

                // Verifica se o usuário é admin (da BaseActivity)
                val admin = isUsuarioAdmin()

                // Define o nível de gerenciamento (1=deletar, 2=deletar+editar)
                // Isto vai corresponder ao 'if (livro.gerenciavel == 1)' do seu adapter
                val nivelGerenciavel = 1

                // Altera a propriedade em CADA livro da lista
                livrosMutaveis.forEach { it.gerenciavel = nivelGerenciavel }

            } else {
                editando = false
                binding.btnEditar.setImageResource(R.drawable.editar_icon)

                // Esconde os botões (seta gerenciavel para 0)
                // Isto vai corresponder ao 'else' do seu adapter
                livrosMutaveis.forEach { it.gerenciavel = 0 }
            }

            // Notifica o adapter que os dados mudaram (para re-renderizar os botões)
            livrosAdapter.notifyDataSetChanged()
        }

        binding.btnVoltar.setOnClickListener {
            finish()
//            val navegarListas = Intent(this, RodapeFM::class.java)
//            navegarListas.putExtra("ID_DESTINO", R.id.navigation_listas)
//            startActivity(navegarListas)
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }
    }

    private fun removerLivroDoBanco(livro: Livro) {
        val loadingReservando = Rotinas.mostrarLoading(
            contexto = this@EditarLista,
            pLayoutDialogo = R.layout.activity_loading_dialog,
            pViewMensagem = R.id.loading_mensagem,
            pMensagem = "Removendo...",
            pDottieAnimation = "loading_icon.json"
        )

        lifecycleScope.launch {
            try {
                val usuarioCodigo = getUsuarioLogado().codigo
                val sucesso: Boolean

                if (nomeDaLista == "Favoritos") {
                    sucesso = RotinasBD.removerFavorito(usuarioCodigo, livro.codigo)
                } else {
                    sucesso = RotinasBD.removerDesejo(usuarioCodigo, livro.codigo)
                }

                if (sucesso) {
                    val index = livrosMutaveis.indexOf(livro)
                    if (index != -1) {
                        livrosMutaveis.removeAt(index)
                        livrosAdapter.notifyItemRemoved(index)
                    }

                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_informacao,
                        mensagem = "Livro removido da lista",
                        dialogoAtivo = loadingReservando
                    )
                } else {
                    throw Exception("Função do RotinasBD retornou false")
                }

            } catch (e: Exception) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Erro ao remover livro",
                    dialogoAtivo = loadingReservando
                )
            }
        }
    }
}