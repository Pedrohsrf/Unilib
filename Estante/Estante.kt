
package com.example.unilib.Estante

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.unilib.Classes.BaseFragment
import com.example.unilib.Classes.Emprestimo
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Usuario
// Importações Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.unilib.Estante.RecyclerView_Adapter.EmprestimosAtivosAdapter
import com.example.unilib.R
import com.example.unilib.databinding.ActivityEstanteBinding

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

// Classe principal da Fragment Estante
class Estante : BaseFragment() {

    private lateinit var binding: ActivityEstanteBinding
    private lateinit var estanteAdapter: EmprestimosAtivosAdapter
    private val emprestimosAtivosList = mutableListOf<Emprestimo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityEstanteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // 🟢 CORREÇÃO 1: Implementação do botão de histórico ATIVA
        binding.btnHistoricoEmprestimos.setOnClickListener {
            val intent = Intent(requireContext(), HistoricoEmprestimos::class.java)
            startActivity(intent)

            requireActivity().overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }
    }

    override fun onStart() {
        super.onStart()

        val firebaseAuth = Firebase.auth
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            Log.e("Estante", "Erro: Nenhum usuário logado no Firebase Auth.")
            Toast.makeText(context, "Erro: Usuário não autenticado.", Toast.LENGTH_LONG).show()
            updateUI(emptyList()) // Atualiza UI para estado vazio em caso de erro
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. OBTÉM O CÓDIGO CUSTOMIZADO
                val codigoCustomizado = obterCodigoCustomizado(currentUser.email)

                if (codigoCustomizado.isEmpty()) {
                    Log.e("Estante", "Erro: Não foi possível encontrar o código customizado do usuário.")
                    Toast.makeText(context, "Usuário não configurado corretamente.", Toast.LENGTH_LONG).show()
                    updateUI(emptyList())
                    return@launch
                }

                // 2. USA O CÓDIGO CUSTOMIZADO PARA BUSCAR DADOS
                val novaLista = lerEmprestimos(codigoCustomizado, status = "Ativo")

                // 🟢 CORREÇÃO 3: Chamada para a nova lógica de atualização da interface
                updateUI(novaLista)

            } catch (e: Exception) {
                Log.e("Estante", "Erro ao carregar empréstimos: ${e.message}")
                Toast.makeText(context, "Falha ao carregar estante.", Toast.LENGTH_SHORT).show()
                updateUI(emptyList())
            }
        }
    }

    /**
     * 🟢 CORREÇÃO 2: Lógica que alterna a visibilidade entre o RecyclerView e a mensagem de "vazio".
     */
    private fun updateUI(novaLista: List<Emprestimo>) {
        emprestimosAtivosList.clear()
        emprestimosAtivosList.addAll(novaLista)
        estanteAdapter.notifyDataSetChanged()

        // Verifica se a lista está vazia
        if (novaLista.isEmpty()) {
            // Mostra a TextView de mensagem
            binding.tvEstanteVazia.visibility = View.VISIBLE
            // Esconde o RecyclerView
            binding.emprestimosAtivos.visibility = View.GONE
        } else {
            // Esconde a TextView de mensagem
            binding.tvEstanteVazia.visibility = View.GONE
            // Mostra o RecyclerView
            binding.emprestimosAtivos.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        estanteAdapter = EmprestimosAtivosAdapter(context = requireContext(), emprestimosAtivosList)

        // Assumindo que o ID do seu RecyclerView no XML é 'emprestimosAtivos'
        binding.emprestimosAtivos.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = estanteAdapter

            val itemAnimator = itemAnimator
            if (itemAnimator is SimpleItemAnimator) {
                itemAnimator.supportsChangeAnimations = false
            }
        }
    }
}

// ==============================================================================
// FUNÇÕES DE ACESSO AO BANCO DE DADOS (Mantidas inalteradas)
// ==============================================================================

suspend fun obterCodigoCustomizado(email: String?): String {
    if (email.isNullOrEmpty()) return ""

    val db = Firebase.firestore
    val snapshot = db.collection("usuario")
        .whereEqualTo("emailTelefone", email)
        .get()
        .await()

    return snapshot.documents.firstOrNull()?.get("codigo") as? String ?: ""
}

suspend fun lerEmprestimos(usuario: String, status: String = ""): MutableList<Emprestimo> {
    val listaEmprestimos: MutableList<Emprestimo> = mutableListOf();
    var fb = Firebase.firestore
    var query = fb.collection("Emprestimo").whereEqualTo("usuario", usuario);

    if (status !== "") {
        query = query.whereEqualTo("status", status);
    }

    var snapshotEmprestimos = query.get().await();

    for (emprestimo in snapshotEmprestimos.documents) {
        var snapshotLivro =
            fb.collection("livros").whereEqualTo("codigo", emprestimo.get("livro") as String)
                .get().await();

        if (snapshotLivro.documents.isEmpty()) continue

        // ... (Criação do objLivro, que você já tem) ...
        val objLivro = Livro(
            codigo = snapshotLivro.documents[0].get("codigo") as String,
            nome = snapshotLivro.documents[0].get("nome") as String,
            autor = snapshotLivro.documents[0].get("autor") as String,
            capa = snapshotLivro.documents[0].get("capa") as String,
            sinopse = snapshotLivro.documents[0].get("sinopse") as String,
            genero = snapshotLivro.documents[0].get("genero") as String,
            qtdAvaliacoes = (snapshotLivro.documents[0].get("qtdAvaliacoes") as Long).toInt(),
            qtdLeituras = (snapshotLivro.documents[0].get("qtdLeituras") as Long).toInt(),
            editora = snapshotLivro.documents[0].get("editora") as String,
            anoPublicacao = snapshotLivro.documents[0].get("anoPublicacao") as String,
            idioma = snapshotLivro.documents[0].get("idioma") as String,
            iSBN = snapshotLivro.documents[0].get("ISBN") as String,
            localizacao = snapshotLivro.documents[0].get("localizacao") as String,
            qtdPaginas = snapshotLivro.documents[0].get("qtdPaginas") as String,
            copiasDisponiveis = (snapshotLivro.documents[0].get("copiasDisponiveis") as Long).toInt(),
        );

        var snapshotUsuario =
            fb.collection("usuario")
                .whereEqualTo("codigo", emprestimo.get("usuario") as String)
                .get().await();

        if (snapshotUsuario.documents.isEmpty()) continue

        // ... (Criação do objUsuario, que você já tem) ...
        val objUsuario = Usuario(
            nome = snapshotUsuario.documents[0].get("nomeCompleto") as String,
            email = snapshotUsuario.documents[0].get("emailTelefone") as String,
            fotoPerfil = snapshotUsuario.documents[0].get("fotoPerfil") as String,
        );

        // 🛑 NOVO TRECHO: Lendo a dataDevolucao de forma segura
        val dataDevolucaoTimestamp = emprestimo.get("dataDevolucao") as? com.google.firebase.Timestamp
        val dataDevolucaoDate = dataDevolucaoTimestamp?.toDate()
        val dataLimiteTimestamp = emprestimo.get("dataLimite") as? com.google.firebase.Timestamp
        val dataLimiteDate = dataLimiteTimestamp?.toDate() ?: Date()
        // 🎯 CORREÇÃO: Cria o objeto Emprestimo final com o novo campo
        val objEmprestimo = Emprestimo(
            livro = objLivro,
            dataEmprestimo = (emprestimo.get("dataEmprestimo") as Timestamp).toDate(),
            dataDevolucao = dataDevolucaoDate, // <--- CAMPO CORRIGIDO
            usuario = objUsuario,
            status = emprestimo.get("status") as String,
            // 💡 CAMPO ADICIONADO: dataLimite
            dataLimite = dataLimiteDate
        );
        listaEmprestimos.add(objEmprestimo);
    }
    return listaEmprestimos;
}

// Funções utilitárias, como concluirEmprestimoFirestore, podem ser mantidas aqui ou em um arquivo separado.
// Para fins de demonstração, deixaremos a implementação de conclusão de empréstimo de fora, já que não é solicitada agora. 
