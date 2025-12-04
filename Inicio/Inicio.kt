package com.example.unilib.Inicio // Mude o nome do pacote se necessário

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.doOnTextChanged
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.doOnTextChanged
import com.example.unilib.Classes.BaseFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.dotlottie.dlplayer.Mode
import com.example.unilib.Classes.Emprestimo
import com.example.unilib.Classes.Genero
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Review
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD.ConsultaPorPrefixo
import com.example.unilib.Classes.RotinasBD.adicionarFavorito
import com.example.unilib.Classes.RotinasBD.lerEmprestimos
import com.example.unilib.Classes.RotinasBD.lerListas
import com.example.unilib.Classes.RotinasBD.lerGeneros
import com.example.unilib.Classes.RotinasBD.lerLivros
import com.example.unilib.Classes.RotinasBD.removerFavorito
import com.example.unilib.Classes.Usuario
import com.example.unilib.Estante.Estante
import com.example.unilib.Inicio.RecyclerView_Adapter.GenerosAdapter
import com.example.unilib.Inicio.RecyclerView_Adapter.Livros1Adapter
import com.example.unilib.Inicio.RecyclerView_Adapter.Livros2Adapter
import com.example.unilib.Inicio.RecyclerView_Adapter.ReviewsAdapter
import com.example.unilib.Livros.VisualizarLivro
import com.example.unilib.R
import com.example.unilib.databinding.ActivityInicioBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.lottiefiles.dotlottie.core.widget.DotLottieAnimation
import kotlinx.coroutines.launch
import kotlin.random.Random

class Inicio : BaseFragment() {

    private lateinit var binding: ActivityInicioBinding

    private lateinit var generosAdapter: GenerosAdapter
    private lateinit var livros1Adapter: Livros1Adapter
    private lateinit var livros2Adapter: Livros2Adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val itemAnimatorLivro1 = binding.livroItem1.itemAnimator

        if (itemAnimatorLivro1 is androidx.recyclerview.widget.SimpleItemAnimator) {
            itemAnimatorLivro1.supportsChangeAnimations = false
        }

        val itemAnimatorLivro2 = binding.livroItem2.itemAnimator

        if (itemAnimatorLivro2 is androidx.recyclerview.widget.SimpleItemAnimator) {
            itemAnimatorLivro2.supportsChangeAnimations = false
        }

        val navegarLivro = Intent(requireContext(), VisualizarLivro::class.java)
        val navegarEstante = Intent(requireContext(), Estante::class.java);

        var generosList: MutableList<Genero>;
        var livrosList: MutableList<Livro>;
        var emprestimosList: MutableList<Emprestimo>;
        var livrosEmprestadosList: MutableList<Livro> = mutableListOf();
        var favoritadosList: MutableList<Livro>;

        Log.i("Status Tela", "Carregando");
        val config = Config.Builder().autoplay(true).speed(1f).loop(true)
            .source(DotLottieSource.Asset("loading_preto.json")).playMode(Mode.FORWARD)
            .useFrameInterpolation(true).build()

        val dotLottieAnimationView = binding.loading

        dotLottieAnimationView.load(config)
        binding.loading.visibility = View.VISIBLE;
        binding.labContinueLendo.visibility = View.GONE;
        binding.divisorGenero.visibility = View.GONE;
        binding.labContinueLendo.visibility = View.GONE;

        lifecycleScope.launch {
            try {
                val sharedPref = requireActivity().getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
                val codigoUsuario = sharedPref.getString("codigoUsuario", "")

                livrosList = lerLivros();
                generosList = lerGeneros();

                emprestimosList = lerEmprestimos(codigoUsuario.toString(), "Ativo");
                val codigosLivrosEmprestados = emprestimosList.map { it.livro.codigo }.toSet()

                val (favoritos, _) = lerListas(codigoUsuario.toString()) // Chama a nova função
                favoritadosList = favoritos // Atribui à sua variável existente
                val codigosLivrosFavoritados = favoritadosList.map { it.codigo }.toSet()

                for (emprestimo in emprestimosList) {
                    if (emprestimo.livro.codigo in codigosLivrosFavoritados) {
                        emprestimo.livro.favoritado = true;
                    }
                    livrosEmprestadosList.add(emprestimo.livro)
                }

                for (livro in livrosList) {
                    if (livro.codigo in codigosLivrosFavoritados) {
                        livro.favoritado = true;
                    }

                    if (livro.codigo in codigosLivrosEmprestados) {
                        livro.emprestado = true;
                    }
                }

//            (livrosList as? MutableList)?.removeAll { livro ->
//                livro.codigo in codigosLivrosEmprestados
//            }

                if (livrosList.isNotEmpty()) {

                    livros1Adapter = Livros1Adapter(requireContext(),
                        livrosList,
                        onItemClickListener = { livro ->
                            //navegarLivro.putExtra("DADOS_LIVRO", livro)
                            navegarLivro.putExtra("CODIGO_LIVRO", livro.codigo)
                            startActivity(navegarLivro)
                            this@Inicio.requireActivity().overridePendingTransition(
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


                                livrosEmprestadosList = mutableListOf();

                                emprestimosList =
                                    lerEmprestimos(codigoUsuario.toString(), "Ativo");

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

                                livros1Adapter.notifyItemChanged(position)
                                livros2Adapter.atualizarListaLivros(livrosEmprestadosList)

                            }
                        })
                    binding.livroItem1.adapter = livros1Adapter
                }

                if (emprestimosList.isNotEmpty()) {

                    livros2Adapter = Livros2Adapter(requireContext(),
                        emprestimosList,
                        livrosEmprestadosList,
                        onItemClickListener = {
                            val navController = findNavController()

                            val bottomNavView =
                                requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

                            val estanteItem = bottomNavView.menu.findItem(R.id.navigation_estante)

                            estanteItem.onNavDestinationSelected(navController)

                            estanteItem.isChecked = true
                        },
                        onFavoritoClickListener = { livro, position ->
                            lifecycleScope.launch {
                                if (livro.favoritado == false) {
                                    if (adicionarFavorito(codigoUsuario.toString(), livro.codigo)) {
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
                                    if (removerFavorito(codigoUsuario.toString(), livro.codigo)) {
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

                                livrosList = mutableListOf();
                                livrosList = lerLivros();

                                val (favoritosApósUpdate2, _) = lerListas(codigoUsuario.toString());
                                favoritadosList = favoritosApósUpdate2;
                                val codigosLivrosFavoritados =
                                    favoritadosList.map { it.codigo }.toSet()

                                for (livro in livrosList) {
                                    if (livro.codigo in codigosLivrosFavoritados) {
                                        livro.favoritado = true;
                                    }

                                    if (livro.codigo in codigosLivrosEmprestados) {
                                        livro.emprestado = true;
                                    }
                                }

                                livros2Adapter.notifyItemChanged(position)
                                livros1Adapter.atualizarListaLivros(livrosList)

                            }
                        })
                    binding.livroItem2.adapter = livros2Adapter
                    binding.labContinueLendo.visibility = View.VISIBLE;
                }


                generosAdapter = GenerosAdapter(requireContext(),
                    generosList,
                    onItemClickListener = { genero ->
                        val config = Config.Builder().autoplay(true).speed(1f).loop(true)
                            .source(DotLottieSource.Asset("loading_preto.json"))
                            .playMode(Mode.FORWARD).useFrameInterpolation(true).build()

                        val dotLottieAnimationView = binding.loading

                        dotLottieAnimationView.load(config)
                        binding.loading.visibility = View.VISIBLE;
                        binding.labNenhumEncontrado.visibility = View.GONE;
//                        binding.layoutLivros1.visibility = View.GONE;
                        lifecycleScope.launch {
                            try {
                                Log.i(
                                    "Gênero escolhido: ", genero.nome
                                )
                                if (genero.nome == "Todos") {
                                    livrosList = lerLivros();
                                    for (livros in livrosList) {
                                        Log.i(
                                            "Livro: ", livros.nome
                                        )
                                    }
                                } else {
                                    livrosList = lerLivros(genero.codigo);
                                    for (livros in livrosList) {
                                        Log.i(
                                            "Livro: ", livros.nome
                                        )
                                    }
                                }

                                if (livrosList.isEmpty()) {
                                    binding.labNenhumEncontrado.visibility = View.VISIBLE;
                                    binding.livroItem1.visibility = View.GONE;
                                    binding.labNenhumEncontrado.text =
                                        "Nenhum livro de " + genero.nome + " encontrado.";

                                } else {
                                    binding.labNenhumEncontrado.visibility = View.GONE;
                                    binding.livroItem1.visibility = View.VISIBLE;
//                                    binding.layoutLivros1.visibility = View.VISIBLE;
                                }

                                livros1Adapter.atualizarListaLivros(livrosList)
                            } finally {
                                binding.loading.visibility = View.GONE;
                            }
                        }
                    })

                binding.generosItem.adapter = generosAdapter
            } catch (e: Exception) {
                Log.e("Inicio", "Erro ao carregar dados: ${e.message}")
            } finally {
                Log.i("Status Tela", "Dados carregados com sucesso");
                binding.loading.visibility = View.GONE;
                binding.divisorGenero.visibility = View.VISIBLE;
            }
        }

        binding.pesquisarInput.setOnEditorActionListener { v, actionId, event ->
            Log.i("INICIO: Pesquisa Concluida", binding.pesquisarInput.text.toString())

            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {

                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                return@setOnEditorActionListener true
            }

            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                return@setOnEditorActionListener true
            }

            false
        }

        binding.pesquisarInput.doOnTextChanged { text, start, before, count ->
            Log.i("INICIO: Edit Atualizado", binding.pesquisarInput.text.toString())

            lifecycleScope.launch {

                if (!binding.pesquisarInput.text.toString().equals("")) {
                    binding.generosItem.visibility = View.GONE;
                    binding.divisorGenero.visibility = View.GONE;

                    binding.labResultados.visibility = View.VISIBLE;
                    binding.labPesquisaFeita.visibility = View.VISIBLE;
                    binding.labPesquisaFeita.text = binding.pesquisarInput.text.toString();

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
                        binding.livroItem1.visibility = View.GONE;
                        binding.labNenhumEncontrado.text = "Nenhum livro encontrado.";

                    } else {
                        binding.labNenhumEncontrado.visibility = View.GONE;
                        binding.livroItem1.visibility = View.VISIBLE;
                    }

                    livros1Adapter.atualizarListaLivros(listaLivros)

                } else {
                    binding.generosItem.visibility = View.VISIBLE;
                    binding.divisorGenero.visibility = View.VISIBLE;

                    binding.labResultados.visibility = View.GONE;
                    binding.labPesquisaFeita.visibility = View.GONE;
                }
            }
        }
    }
}