package com.example.unilib.Emprestimos // Pacote sugerido

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Emprestimo
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Usuario
import DialogCallback
import com.dotlottie.dlplayer.Mode
import com.example.unilib.Classes.BaseActivity
import com.example.unilib.Classes.Rotinas.formatarData
import com.example.unilib.Classes.RotinasBD.criarNotificacao
import com.example.unilib.Classes.RotinasBD.fb
import com.example.unilib.Emprestimos.RecyclerView_Adapter.GerenciarEmprestimosAdapter
import com.example.unilib.R
import com.example.unilib.databinding.ActivityVisualizarEmprestimosBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

// Activity para visualizar e gerenciar todos os empréstimos (área administrativa)
class GerenciarEmprestimos : BaseActivity() {

    private lateinit var binding: ActivityVisualizarEmprestimosBinding
    private lateinit var emprestimosAdapter: GerenciarEmprestimosAdapter
    private var emprestimosList: MutableList<Emprestimo> = mutableListOf()
    private var listaCompletaBackup: MutableList<Emprestimo> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisualizarEmprestimosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupPesquisa()

        // 🛑 CORREÇÃO 1: Nome do botão de voltar conforme o XML
        binding.btnVoltarAdministracao.setOnClickListener {
            finish() // Fecha a activity
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }
    }

    override fun onStart() {
        super.onStart()

        binding.recyclerview.visibility = View.GONE
        binding.loading.visibility = View.VISIBLE
        val config = Config.Builder().autoplay(true).speed(1f).loop(true)
            .source(DotLottieSource.Asset("loading_preto.json")).playMode(Mode.FORWARD)
            .useFrameInterpolation(true).build()

        val dotLottieAnimationView = binding.loading

        dotLottieAnimationView.load(config)

        val itemAnimatorData = binding.recyclerview.itemAnimator

        if (itemAnimatorData is androidx.recyclerview.widget.SimpleItemAnimator) {
            itemAnimatorData.supportsChangeAnimations = false
        }

        carregarEmprestimos()
    }

    private fun setupPesquisa() {
        // 💡 Explicação: Usamos addTextChangedListener para EditTexts
        binding.pesquisarInput.addTextChangedListener(object : android.text.TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Não precisamos fazer nada antes do texto mudar
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Chamamos a função de filtro a cada mudança de texto
                // Note que 's' é o novo texto digitado
                filtrarLista(s?.toString())
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                // Não precisamos fazer nada depois do texto mudar
            }
        })
    }

    // 🟢 FUNÇÃO QUE EXECUTA O FILTRO DE DADOS 🟢
    private fun filtrarLista(texto: String?) {
        // 1. Se o campo de busca estiver vazio, mostre todos os empréstimos (o backup)
        if (texto.isNullOrEmpty()) {
            if (listaCompletaBackup.isNotEmpty()) {
                binding.tvNenhumEcontrado.visibility = View.GONE
            } else{
                binding.tvNenhumEcontrado.visibility = View.VISIBLE
            }

            emprestimosList.clear()
            emprestimosList.addAll(listaCompletaBackup)
        } else {
            // 2. Se o texto foi digitado:
            val textoDigitado =
                texto.lowercase() // Simplifica a comparação (ignora Maiúsculas/minúsculas)

            // Filtra a lista completa, procurando pelo texto digitado no nome do livro OU no nome do usuário
            val listaFiltrada = listaCompletaBackup.filter { emprestimo ->
                emprestimo.livro.nome.lowercase().contains(textoDigitado) ||
                        emprestimo.usuario.nome.lowercase().contains(textoDigitado)
            }

            if (listaFiltrada.isNotEmpty()) {
                binding.tvNenhumEcontrado.visibility = View.GONE
            } else{
                binding.tvNenhumEcontrado.visibility = View.VISIBLE
            }
            // 3. Atualiza a lista da tela com os itens que passaram no filtro
            emprestimosList.clear()
            emprestimosList.addAll(listaFiltrada)
        }

        // 4. Avisa o Adaptador para redesenhar a lista na tela
        emprestimosAdapter.notifyDataSetChanged()
    }

    private fun carregarEmprestimos() {

        lifecycleScope.launch {
            try {
                // Busca todos os empréstimos
                val lista = lerTodosEmprestimos()

                if (lista.isNotEmpty()) {
                    listaCompletaBackup.clear()
                    listaCompletaBackup.addAll(lista)
                    emprestimosList.clear()
                    emprestimosList.addAll(lista)
                    emprestimosAdapter.notifyDataSetChanged()
                    // 🛑 CORREÇÃO 2: Usar o ID correto do RecyclerView
                    binding.recyclerview.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                } else {
                    binding.tvNenhumEcontrado.visibility = View.VISIBLE
                    Log.d("GerenciarEmprestimos", "Nenhum empréstimo encontrado.")
                    // Se não houver, o RecyclerView permanece GONE e o loading sai.
                }
            } catch (e: Exception) {
                binding.loading.visibility = View.GONE
                Log.e("GerenciarEmprestimos", "Erro ao carregar lista: ${e.message}")
                Toast.makeText(
                    this@GerenciarEmprestimos,
                    "Erro ao carregar dados.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupRecyclerView() {
        emprestimosAdapter = GerenciarEmprestimosAdapter(
            context = this,
            emprestimos = emprestimosList,
            onItemClickListener = { emprestimo, position ->

                if (emprestimo.status == "Pendente") {
                    Rotinas.mostrarDialogo(
                        contexto = this,
                        pLayoutDialogo = R.layout.dialogo_medio,
                        pMensagemPrincipal = "Aprovar empréstimo?",
                        pMensagemSecundaria = "Ao confirmar, o tempo de empréstimo será iniciado.",
                        pBtnSim = "APROVAR",
                        pBtnNao = "CANCELAR",

                        pDottieAnimation = "",

                        callback = object : DialogCallback {
                            override fun onSimClicked() {
                                atualizarStatusEmprestimo(emprestimo, "Ativo", position)
                            }

                            override fun onNaoClicked() {
                                // Não faz nada
                            }
                        }
                    )
                } else {
                    Rotinas.mostrarDialogo(
                        contexto = this,
                        pLayoutDialogo = R.layout.dialogo_medio,
                        pMensagemPrincipal = "Confirmar devolução?",
                        pMensagemSecundaria = "Ao confirmar, você declara que o livro foi devolvido.",
                        pBtnSim = "CONFIRMAR",
                        pBtnNao = "CANCELAR",

                        // --- 💡 CORREÇÃO AQUI ---
                        // Passa uma string vazia para pular a animação Lottie
                        pDottieAnimation = "",

                        callback = object : DialogCallback {
                            override fun onSimClicked() {
                                atualizarStatusEmprestimo(emprestimo, "Finalizado", position)
                            }

                            override fun onNaoClicked() {
                                // Não faz nada
                            }
                        }
                    )
                }
            }
        )

        binding.recyclerview.adapter = emprestimosAdapter
        binding.recyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    private fun atualizarStatusEmprestimo(
        emprestimo: Emprestimo,
        novoStatus: String,
        position: Int
    ) {
        val db = Firebase.firestore
        val dataAtual = com.google.firebase.Timestamp.now()

        val instant = Instant.ofEpochSecond(dataAtual.seconds, dataAtual.nanoseconds.toLong())
        val dataAtualMais14DiasInstant: Instant = instant.plus(14, ChronoUnit.DAYS)

        val dataLimite: Timestamp = Timestamp(
            dataAtualMais14DiasInstant.epochSecond,
            dataAtualMais14DiasInstant.nano
        )
        lifecycleScope.launch {
            try {
                // Busca o documento no Firestore pelo código do livro (método funcional)
                val emprestimoDoc = db.collection("Emprestimo")
                    .whereEqualTo("usuario", emprestimo.usuario.codigo)
                    .whereEqualTo("livro", emprestimo.livro.codigo)
                    .get()
                    .await()
                    .documents.firstOrNull()

                if (emprestimoDoc != null) {
                    if (novoStatus.equals("Finalizado")) {
                        db.collection("Emprestimo")
                            .document(emprestimoDoc.id)
                            .update(
                                mapOf(
                                    "status" to novoStatus, // Ex: "Finalizado"
                                    "dataDevolucao" to dataAtual // <--- ESSA LINHA É A CHAVE!
                                )
                            )
                            .await()

                        val query =
                            fb.collection("livros").whereEqualTo("codigo", emprestimo.livro.codigo)
                                .get().await()

                        val documentoLivro =
                            query.documents.firstOrNull()
                                ?.let { fb.collection("livros").document(it.id) }

                        if (documentoLivro != null) {
                            documentoLivro.update(
                                mapOf(
                                    "copiasDisponiveis" to emprestimo.livro.copiasDisponiveis + 1,
                                    "qtdLeituras" to emprestimo.livro.qtdLeituras + 1,
                                )
                            ).await()
                        }

                        emprestimosList.removeAt(position)

                        if (emprestimosList.isEmpty()) {
                            binding.tvNenhumEcontrado.visibility = View.VISIBLE
                        }
                        criarNotificacao(
                            titulo = "Empréstimo",
                            descricao = "A devolução do livro ${emprestimo.livro.nome} foi registrada com sucesso! A biblioteca agradece.",
                            usuario = emprestimo.usuario.codigo,
                            redirecionamento = ""
                        )

                    } else if (novoStatus.equals("Ativo")) {
                        db.collection("Emprestimo")
                            .document(emprestimoDoc.id)
                            .update(
                                mapOf(
                                    "status" to novoStatus, // Ex: "Finalizado"
                                    "dataDevolucao" to com.google.firebase.Timestamp(0, 0),
                                    "dataLimite" to dataLimite,
                                    "dataEmprestimo" to dataAtual,// <--- ESSA LINHA É A CHAVE!
                                )
                            )
                            .await()

                        emprestimosList[position].status = novoStatus

                        criarNotificacao(
                            titulo = "Empréstimo",
                            descricao = "O seu empréstimo do livro ${emprestimo.livro.nome} foi aprovado! Aproveite a sua experiência até ${
                                formatarData(
                                    dataLimite.toDate(),
                                    "dd/MM/yyyy"
                                )
                            }",
                            usuario = emprestimo.usuario.codigo,
                            redirecionamento = ""
                        )
                    }

                    emprestimosAdapter.notifyDataSetChanged()

                    var mensagem = ""

                    if (novoStatus.equals("Finalizado")) {
                        mensagem = "Empréstimo finalizado com sucesso!"
                    } else if (novoStatus.equals("Ativo")) {
                        mensagem = "Empréstimo aprovado com sucesso!"
                    }
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_informacao,
                        mensagem = mensagem
                    )
                }

            } catch (e: Exception) {
                Log.e("GerenciarEmprestimos", "Erro ao atualizar status: ${e.message}")
                Toast.makeText(
                    this@GerenciarEmprestimos,
                    "Falha na atualização.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


// ==============================================================================
// 🛑 FUNÇÃO DE LEITURA DO FIREBASE (Coloque em um arquivo RotinasBD.kt ou similar)
// ==============================================================================

    private suspend fun lerTodosEmprestimos(): MutableList<Emprestimo> {
        val listaEmprestimos: MutableList<Emprestimo> = mutableListOf();
        val fb = Firebase.firestore

        // Consulta TODOS os documentos de Empréstimo
        val snapshotEmprestimos =
            fb.collection("Emprestimo").whereNotEqualTo("status", "Finalizado").get().await();

        for (emprestimo in snapshotEmprestimos.documents) {

            // 1. Busca o Livro (Mantido como estava)
            val codigoLivro = emprestimo.get("livro") as String
            val snapshotLivro = fb.collection("livros")
                .whereEqualTo("codigo", codigoLivro)
                .get().await().documents.firstOrNull() ?: continue

            // ... (Criação de objLivro) ...

            val objLivro = Livro(
                codigo = snapshotLivro.get("codigo") as String,
                nome = snapshotLivro.get("nome") as String,
                autor = snapshotLivro.get("autor") as String,
                capa = snapshotLivro.get("capa") as String,
                sinopse = snapshotLivro.get("sinopse") as String,
                genero = snapshotLivro.get("genero") as String,
                qtdAvaliacoes = (snapshotLivro.get("qtdAvaliacoes") as Long).toInt(),
                qtdLeituras = (snapshotLivro.get("qtdLeituras") as Long).toInt(),
                editora = snapshotLivro.get("editora") as String,
                anoPublicacao = snapshotLivro.get("anoPublicacao") as String,
                idioma = snapshotLivro.get("idioma") as String,
                iSBN = snapshotLivro.get("ISBN") as String,
                localizacao = snapshotLivro.get("localizacao") as String,
                qtdPaginas = snapshotLivro.get("qtdPaginas") as String,
                copiasDisponiveis = (snapshotLivro.get("copiasDisponiveis") as Long).toInt(),
            );

            // 2. Busca o Usuário (Mantido como estava)
            val codigoUsuario = emprestimo.get("usuario") as String
            val snapshotUsuario = fb.collection("usuario")
                .whereEqualTo("codigo", codigoUsuario)
                .get().await().documents.firstOrNull() ?: continue

            // ... (Criação de objUsuario) ...

            val objUsuario = Usuario(
                codigo = snapshotUsuario.get("codigo") as String,
                nome = snapshotUsuario.get("nomeCompleto") as String,
                email = snapshotUsuario.get("emailTelefone") as String,
                fotoPerfil = snapshotUsuario.get("fotoPerfil") as String,
            );

            val dataDevolucaoTimestamp =
                emprestimo.get("dataDevolucao") as? com.google.firebase.Timestamp
            val dataDevolucaoDate = dataDevolucaoTimestamp?.toDate()

            val dataLimiteTimestamp = emprestimo.get("dataLimite") as? com.google.firebase.Timestamp
            val dataLimiteDate = dataLimiteTimestamp?.toDate() ?: java.util.Date()

            val objEmprestimo = Emprestimo(
                livro = objLivro,
                dataEmprestimo = (emprestimo.get("dataEmprestimo") as Timestamp).toDate(),
                dataDevolucao = dataDevolucaoDate,
                usuario = objUsuario,
                status = emprestimo.get("status") as String,
                dataLimite = dataLimiteDate // 💡 ADICIONAR ESTA LINHA
            );
            listaEmprestimos.add(objEmprestimo);
        }
        return listaEmprestimos;
    }
}
