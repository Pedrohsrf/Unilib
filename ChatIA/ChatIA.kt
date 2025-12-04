package com.example.unilib.ChatIA

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dotlottie.dlplayer.Mode
import com.example.unilib.ChatIA.RecyclerView_Adapter.ChatAdapter
import com.example.unilib.Classes.Genero
import com.example.unilib.Classes.Livro
import com.example.unilib.Inicio.Inicio
import com.example.unilib.Inicio.RecyclerView_Adapter.GenerosAdapter
import com.example.unilib.Livros.VisualizarLivro
import com.example.unilib.R
import com.example.unilib.RodapeFM
import com.example.unilib.databinding.ActivityChatIaBinding
import com.example.unilib.databinding.ActivityInicioBinding
import com.google.ai.client.generativeai.GenerativeModel
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class ChatIA : Fragment() {

    private lateinit var binding: ActivityChatIaBinding

    private var mensagensBot = mutableListOf<String>()
    private var mensagensUsuario = mutableListOf<String>()

    private lateinit var mensagensAdapter: ChatAdapter

    private lateinit var generativeModel: GenerativeModel

    private var prePrompt =
        "Você é uma IA integrada a um aplicativo Android. " +
                "Responda SOMENTE dúvidas relacionadas ao funcionamento do aplicativo, suas telas, funções, erros, layout, navegação e lógica interna. " +
                "Se o usuário perguntar qualquer coisa que NÃO seja sobre o app, diga educadamente: " +
                "\"Desculpe, só posso responder perguntas relacionadas ao aplicativo.\" " +
                "Sempre responda em Português: " + "Responda de acordo com funções do app, saiba que o app tem abas de inicio(onde o usuario pode ver livros), estante(onde o usuario ve os emprestimos dele), listas(Onde o usuario ve a lista de favoritos e lista de desejos dele) e perfil onde ele ve as informações dele" +
                "Seja sempre muito educado e responda com clareza todas as perguntas com no maximo 270 caracteres"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = ActivityChatIaBinding.inflate(layoutInflater)

        generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = "AIzaSyBq3wd0GHaec6PEObzcNoL-IkmKT3RQhJw"
        )

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        lifecycleScope.launch {
            val config = Config.Builder().autoplay(true).speed(1f).loop(true)
                .source(DotLottieSource.Asset("loading_icon.json")).playMode(Mode.FORWARD)
                .useFrameInterpolation(true).build()

            val dotLottieAnimationView = binding.loading

            dotLottieAnimationView.load(config)

            binding.loading.visibility = View.INVISIBLE;

            val sharedPref = requireActivity().getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
            binding.labUsuario.text = sharedPref.getString("nomeUsuario", "Usuário")

            mensagensAdapter = ChatAdapter(
                requireContext(), mensagensUsuario, mensagensBot
            )

            binding.mensagensItens.adapter = mensagensAdapter

            binding.btnEnviar.setOnClickListener {
                binding.loading.visibility = View.VISIBLE;
                binding.btnEnviar.visibility = View.GONE;

                val mensagem = binding.editMensagem.text.toString();
                binding.editMensagem.setText("")
                binding.editMensagem.isEnabled = false;

                lifecycleScope.launch {
                    binding.logoUnilib.visibility = View.GONE;
                    binding.labOla.visibility = View.GONE;
                    binding.labUsuario.visibility = View.GONE;
                    binding.labOqueQuer.visibility = View.GONE;
                    binding.mensagensItens.visibility = View.VISIBLE;
                    binding.labChatBOT.visibility = View.VISIBLE;

                    val response = generativeModel.generateContent(
                        prePrompt + mensagem
                    )

                    val params = binding.btnVoltarAVL.layoutParams as? ConstraintLayout.LayoutParams

                    if (params != null) {
                        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID

                        binding.btnVoltarAVL.layoutParams = params
                    }

                    if (mensagem.isNotEmpty()) {
                        mensagensUsuario.add(mensagem)
                        mensagensAdapter.notifyItemInserted(mensagensUsuario.size - 1)
                        binding.mensagensItens.scrollToPosition(mensagensUsuario.size - 1)
                        mensagensAdapter.refresh();

                        mensagensBot.add(response.text ?: "Sem resposta do modelo")
                        mensagensAdapter.refresh();
                    }
                    binding.loading.visibility = View.GONE;
                    binding.btnEnviar.visibility = View.VISIBLE;
                    binding.editMensagem.isEnabled = true;
                }
            }

            binding.btnVoltarAVL.setOnClickListener {
                val navegarInicio = Intent(requireContext(), RodapeFM::class.java)
                navegarInicio.putExtra("ID_DESTINO", R.id.navigation_inicio)
                startActivity(navegarInicio)
            }
        }
    }
}