package com.example.unilib.Usuarios

import DialogCallback
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unilib.Classes.BaseActivity
import com.example.unilib.Classes.Evento
import com.example.unilib.Classes.FontScaleManager
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.ConsultaPorPrefixo
import com.example.unilib.Classes.Usuario
import com.example.unilib.R
import com.example.unilib.RodapeFM
import com.example.unilib.Usuarios.RecyclerView_Adapter.GerenciarUsuariosAdapter
import com.example.unilib.databinding.ActivityGerenciarUsuariosBinding
import kotlinx.coroutines.launch

class GerenciarUsuarios : BaseActivity(), GerenciarUsuariosAdapter.OnUsuarioClickListener {

    private lateinit var binding: ActivityGerenciarUsuariosBinding
    private val usuariosList = mutableListOf<Usuario>()
    private lateinit var usuariosAdapter: GerenciarUsuariosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("acessibilidade", MODE_PRIVATE)
        val fontPreset = prefs.getString("fontPreset", "font100") ?: "font100"
        FontScaleManager.updateActivityWithPreset(this, fontPreset)

        super.onCreate(savedInstanceState)
        binding = ActivityGerenciarUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        carregarUsuarios()

        val navegarAdministracao = Intent(this, RodapeFM::class.java)
        navegarAdministracao.putExtra("ID_DESTINO", R.id.nav_administracao)

        binding.btnVoltarAdministracao.setOnClickListener {
            startActivity(navegarAdministracao)
            overridePendingTransition(R.anim.animate_fade_enter, R.anim.animate_fade_exit)
        }




        binding.pesquisarInput.doOnTextChanged { text, start, before, count ->
            lifecycleScope.launch {
                val busca = text.toString()

                if (busca.isNotEmpty()) {

                    val buscaFormatada = formatarNomeBusca(busca)

                    val listaSnapshots = ConsultaPorPrefixo(
                        "usuario",
                        buscaFormatada,
                        "nomeCompleto_lower",
                        "nomeCompleto_lower"
                    )

                    val listaUsuario = mutableListOf<Usuario>()
                    for (usuario in listaSnapshots) {
                        listaUsuario.add(
                            Usuario(
                                nome = usuario.getString("nomeCompleto") ?: "",
                                email = usuario.getString("emailTelefone") ?: "",
                                fotoPerfil = usuario.getString("fotoPerfil") ?: "",
                                codigo = usuario.getString("codigo") ?: "",
                            )
                        )
                    }

                    if (listaUsuario.isEmpty()) {
                        binding.labNenhumUsuarioEncontrado.visibility = View.VISIBLE
                        binding.recyclerview.visibility = View.GONE
                    } else {
                        binding.labNenhumUsuarioEncontrado.visibility = View.GONE
                        binding.recyclerview.visibility = View.VISIBLE
                    }

                    usuariosAdapter.atualizarListaUsuario(listaUsuario)

                } else {
                    carregarUsuarios()
                    binding.labNenhumUsuarioEncontrado.visibility = View.GONE
                    binding.recyclerview.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun formatarNomeBusca(texto: String): String {
        return texto.lowercase()
    }
    private fun carregarUsuarios() {
        lifecycleScope.launch {

            var resultado = ConsultaPorPrefixo(
                colecao = "usuario",
                prefixo = "",
                campo_normal = "nomeCompleto",
                campo_lower = "nomeCompleto"
            )


            if (resultado.isEmpty()) {
                resultado = ConsultaPorPrefixo(
                    colecao = "usuario",
                    prefixo = "",
                    campo_normal = "nomeCompleto",
                    campo_lower = "nomeCompleto"
                )
            }


            usuariosList.clear()

            for (doc in resultado) {
                val nome = doc.getString("nomeCompleto") ?: continue
                val email = doc.getString("emailTelefone") ?: ""
                val foto = doc.getString("fotoPerfil") ?: ""
                val codigo = doc.getString("codigo") ?: ""

                usuariosList.add(
                    Usuario(
                        nome = nome,
                        email = email,
                        fotoPerfil = foto,
                        codigo = codigo
                    )
                )
            }

            usuariosAdapter = GerenciarUsuariosAdapter(
                this@GerenciarUsuarios,
                usuariosList,
                this@GerenciarUsuarios
            )

            binding.recyclerview.layoutManager = LinearLayoutManager(this@GerenciarUsuarios)
            binding.recyclerview.adapter = usuariosAdapter
        }
    }

    override fun onItemClick(usuario: Usuario) {
        val intent = Intent(this, VisualizarUsuario::class.java)
        intent.putExtra("codigo", usuario.codigo)
        startActivity(intent)
    }

    override fun onDeleteClick(usuario: Usuario, position: Int) {
        Rotinas.mostrarDialogo(
            contexto = this,
            pLayoutDialogo = R.layout.dialogo_critico,
            pMensagemPrincipal = "Confirmar exclusão",
            pMensagemSecundaria = "Deseja excluir o usuário '${usuario.nome}'?\nEsta ação não pode ser revertida.",
            pBtnSim = "Deletar",
            pBtnNao = "Cancelar",
            pDottieAnimation = "",
            callback = object : DialogCallback {

                override fun onSimClicked() {
                    lifecycleScope.launch {

                        val sucesso = RotinasBD.deletarUsuario(usuario.codigo)

                        if (sucesso) {
                            usuariosAdapter.removerItem(position)

                            Rotinas.mostrarSnackbar(
                                parentView = binding.root,
                                layoutAlerta = R.layout.toast_informacao,
                                mensagem = "Usuário excluído!"
                            )
                        } else {
                            Rotinas.mostrarSnackbar(
                                parentView = binding.root,
                                layoutAlerta = R.layout.toast_alerta,
                                mensagem = "Falha ao deletar usuário!"
                            )
                        }
                    }
                }

                override fun onNaoClicked() {}
            }
        )
    }

}


