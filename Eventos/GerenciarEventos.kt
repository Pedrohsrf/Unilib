package com.example.unilib.Eventos;

import DialogCallback
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.widget.doOnTextChanged
import com.example.unilib.Classes.BaseActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Evento
import com.example.unilib.Classes.FontScaleManager
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.ConsultaPorPrefixo
import com.example.unilib.Notificacoes.RecyclerView_Adapter.EventListAdapter
import com.example.unilib.R
import com.example.unilib.RodapeFM
import com.example.unilib.databinding.ActivityGerenciarEventosBinding
import kotlinx.coroutines.launch

class GerenciarEventos : BaseActivity() {

    private lateinit var binding: ActivityGerenciarEventosBinding
    private var eventosList = mutableListOf<Evento>()
    private lateinit var eventosAdapter: EventListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("acessibilidade", MODE_PRIVATE)
        val fontPreset = prefs.getString("fontPreset", "font100") ?: "font100"
        FontScaleManager.updateActivityWithPreset(this, fontPreset)

        super.onCreate(savedInstanceState)
        binding = ActivityGerenciarEventosBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            eventosList = RotinasBD.lerEventos(true)

            val navegarAdministracao = Intent(this@GerenciarEventos, RodapeFM::class.java)
            navegarAdministracao.putExtra("ID_DESTINO", R.id.nav_administracao)

            binding.btnVoltarAdministracao.setOnClickListener {
                startActivity(navegarAdministracao)
                overridePendingTransition(R.anim.animate_fade_enter, R.anim.animate_fade_exit)
            }

            eventosAdapter = EventListAdapter(
                this@GerenciarEventos,
                eventosList,
                onEditarClick = { evento ->
                    Rotinas.mostrarDialogoEvento(
                        contexto = this@GerenciarEventos,
                        parentView = binding.root,
                        dialogoLayout = R.layout.dialogo_editar_evento,
                        dadosEvento = evento,
                        onConfirmar = { nome, data, imagemBase64 ->
                            Rotinas.mostrarDialogo(
                                contexto = this@GerenciarEventos,
                                pLayoutDialogo = R.layout.dialogo_medio,
                                pMensagemPrincipal = "Confirmar edição",
                                pMensagemSecundaria = "Deseja salvar as alterações em '${evento.nomeEvento}'|'${evento.dataEvento}'?",
                                pBtnSim = "Salvar",
                                pBtnNao = "Cancelar",
                                pDottieAnimation = "",
                                callback = object : DialogCallback {
                                    override fun onSimClicked() {
                                        val pos = eventosList.indexOf(evento)
                                        if (pos != -1) {
                                            lifecycleScope.launch {
                                                val sucesso = RotinasBD.editarEvento(
                                                    evento,
                                                    nome,
                                                    data,
                                                    imagemBase64
                                                )
                                                if (sucesso) {
                                                    eventosList[pos].nomeEvento = nome
                                                    eventosList[pos].dataEvento = data
                                                    eventosList[pos].Imagem = imagemBase64
                                                    eventosAdapter.notifyItemChanged(pos)

                                                    Rotinas.mostrarSnackbar(
                                                        parentView = findViewById(android.R.id.content),
                                                        layoutAlerta = R.layout.toast_informacao,
                                                        mensagem = "Evento editado com sucesso!"
                                                    )
                                                } else {
                                                    Rotinas.mostrarSnackbar(
                                                        parentView = findViewById(android.R.id.content),
                                                        layoutAlerta = R.layout.toast_alerta,
                                                        mensagem = "Falha ao editar evento!"
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    override fun onNaoClicked() {}
                                }
                            )
                        }
                    )
                },
                onDeletarClick = { evento ->
                    Rotinas.mostrarDialogo(
                        contexto = this@GerenciarEventos,
                        pLayoutDialogo = R.layout.dialogo_critico,
                        pMensagemPrincipal = "Confirmar exclusão",
                        pMensagemSecundaria = "Deseja excluir o evento '${evento.nomeEvento}'\n que acontecerá dia '${evento.dataEvento}'?",
                        pBtnSim = "Deletar",
                        pBtnNao = "Cancelar",
                        pDottieAnimation = "",
                        callback = object : DialogCallback {
                            override fun onSimClicked() {
                                val pos = eventosList.indexOf(evento)
                                if (pos != -1) {
                                    lifecycleScope.launch {
                                        RotinasBD.deletarEvento(evento.codigo)
                                    }
                                    eventosList.removeAt(pos)
                                    eventosAdapter.notifyItemRemoved(pos)
                                    Rotinas.mostrarSnackbar(
                                        parentView = findViewById(android.R.id.content),
                                        layoutAlerta = R.layout.toast_informacao,
                                        mensagem = "Evento excluído!"
                                    )
                                }
                            }

                            override fun onNaoClicked() {}
                        }
                    )
                }
            )

            binding.RecyclerViewEventos.adapter = eventosAdapter
            binding.RecyclerViewEventos.layoutManager =
                LinearLayoutManager(this@GerenciarEventos, RecyclerView.VERTICAL, false)
        }

        binding.btnCriarEvento.setOnClickListener {
            Rotinas.mostrarDialogoEvento(
                contexto = this,
                parentView = findViewById(android.R.id.content),
                dialogoLayout = R.layout.dialogo_criar_evento,
                onConfirmar = { nome, data, imagemBase64 ->
                    val loading = Rotinas.mostrarLoading(
                        contexto = this,
                        pLayoutDialogo = R.layout.activity_loading_dialog,
                        pViewMensagem = R.id.loading_mensagem,
                        pMensagem = "Criando evento...",
                        pDottieAnimation = "loading_icon.json"
                    )

                    lifecycleScope.launch {
                        var objEvento = Evento(
                            codigo = RotinasBD.proximoCodigo("evento", 4, '0'),
                            nomeEvento = nome,
                            dataEvento = data,
                            Imagem = imagemBase64,
                            gerenciavel = true
                        )

                        RotinasBD.criarEvento(objEvento)

                        eventosList.add(objEvento);
                        eventosAdapter.notifyDataSetChanged()

                        Rotinas.mostrarSnackbar(
                            parentView = findViewById(android.R.id.content),
                            layoutAlerta = R.layout.toast_informacao,
                            mensagem = "Evento criado com sucesso!"
                        )

                        loading.dismiss()
                    }
                }
            )
        }


        binding.searchInput.setOnEditorActionListener { v, actionId, event ->
            Log.i("INICIO: Pesquisa Concluida", binding.searchInput.text.toString())

            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm =
                    v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                return@setOnEditorActionListener true
            }

            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                return@setOnEditorActionListener true
            }

            false
        }
        binding.searchInput.doOnTextChanged { text, start, before, count ->
            Log.i("INICIO: Edit Atualizado", binding.searchInput.text.toString())

            lifecycleScope.launch {
                if (!binding.searchInput.text.toString().equals("")) {

                    var listaSnapshots = ConsultaPorPrefixo(
                        "evento",
                        binding.searchInput.text.toString(),
                        "nomeEvento",
                        "nomeEvento_lower"
                    )

                    var listaEventos: MutableList<Evento> = mutableListOf();
                    for (evento in listaSnapshots) {
                        Log.i("Evento Encontrado", evento.get("nomeEvento") as String)

                        val objEvento = Evento(
                            codigo = evento.get("codigo") as String,
                            nomeEvento = evento.getString("nomeEvento") ?: "",
                            Imagem = evento.get("imagem") as String,
                            dataEvento = evento.get("dataEvento") as String,
                            gerenciavel = true,
                        );
                        listaEventos.add(objEvento)
                    }
                    if (listaEventos.isEmpty()) {
                        binding.labNenhumEventoEncontrado2.visibility = View.VISIBLE;
                        binding.RecyclerViewEventos.visibility = View.GONE;
                        binding.labNenhumEventoEncontrado2.text = "Nenhum evento encontrado.";
                    } else {
                        binding.labNenhumEventoEncontrado2.visibility = View.GONE;
                        binding.RecyclerViewEventos.visibility = View.VISIBLE;
                    }
                    eventosAdapter.atualizarListaEventos(listaEventos)
                } else {
                    lifecycleScope.launch {
                        val listaEventos = RotinasBD.lerEventos(true)
                        eventosAdapter.atualizarListaEventos(listaEventos)

                        binding.labNenhumEventoEncontrado2.visibility = View.GONE
                        binding.RecyclerViewEventos.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    Rotinas.onImagemSelecionada?.invoke(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    Rotinas.onImagemSelecionada = null
                }
            }
        }
    }
}
