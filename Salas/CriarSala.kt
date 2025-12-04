package com.example.unilib.Salas;

import DialogCallback
import DialogoSalaCallback
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.marginTop
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.BaseActivity
import com.example.unilib.Classes.Disponibilidade
import com.example.unilib.Classes.Horario
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Rotinas.dpToPx
import com.example.unilib.Classes.Rotinas.formatarData
import com.example.unilib.Classes.Rotinas.mostrarDialogo
import com.example.unilib.Classes.Rotinas.mostrarDialogoSala
import com.example.unilib.Classes.Rotinas.stringToDate
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.atualizarHorario
import com.example.unilib.Classes.RotinasBD.criarSala
import com.example.unilib.Classes.RotinasBD.editarSala
import com.example.unilib.Classes.RotinasBD.lerSala
import com.example.unilib.Classes.RotinasBD.salaExiste
import com.example.unilib.Classes.Sala
import com.example.unilib.R
import com.example.unilib.RodapeFM
import com.example.unilib.Salas.Fragments.DateDialog
import com.example.unilib.Salas.Fragments.TimeDialog
import com.example.unilib.Salas.RecyclerView_Adapter.DatasAdapter
import com.example.unilib.Salas.RecyclerView_Adapter.HorariosAdapter
import com.example.unilib.databinding.ActivityCriarSalaBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CriarSala : BaseActivity() {
    private lateinit var binding: ActivityCriarSalaBinding
    private val listaDisponibilidades = mutableListOf<Disponibilidade>()
    private val listaHorarios = mutableListOf<Horario>()
    private lateinit var datasAdapter: DatasAdapter
    private lateinit var horariosAdapter: HorariosAdapter
    private var dataSelecionada = 0;
    private var dataSelecionadaString = "";
    private var visualizacaoInicial = true;
    private var modoEdicao: Boolean = false
    private var dadosSala: Sala? = Sala(
        "", listOf(), ""
    );

    private fun scrollarFinal() {
        binding.scrollData.post {
            val maxScrollX = binding.scrollData.getChildAt(0).width - binding.scrollData.width
            binding.scrollData.scrollTo(maxScrollX, 0)
        }
    }

    private fun scrollarInicio() {
        binding.scrollData.post {
            binding.scrollData.scrollTo(0, 0)
        }
    }

    private fun setupAcaoBotoes(container: LinearLayout, tipo: String) {
        val iconMais = R.drawable.mais_icon
        val iconMenos = R.drawable.menos_icon
        val idBotaoAcao = if (tipo == "DATA") R.id.btnAdicionarData else R.id.btnAdicionarHorario

        for (i in 0 until container.childCount) {
            val bloco = container.getChildAt(i)
            val btnAcao = bloco.findViewById<CardView>(idBotaoAcao)
            btnAcao?.let { cardView ->
                val iconView = btnAcao.findViewById<ImageView>(
                    if (tipo == "DATA") R.id.iconAdicionarData else R.id.iconAddHorario
                )

                iconView?.let { imageView ->

                    if (i == container.childCount - 1) {
                        iconView.setImageResource(iconMais)

                        btnAcao.setOnClickListener {
                            adicionarBlocoHorario(
                                container
                            )
                        }
                    } else {
                        iconView.setImageResource(iconMenos)

                        btnAcao.setOnClickListener {
                            if (container.childCount > 1) {
                                if (tipo == "HORARIO") {
                                    val horaIniRemover =
                                        bloco.findViewById<EditText>(R.id.horaIni).text.toString()
                                    val horaFimRemover =
                                        bloco.findViewById<EditText>(R.id.horaFim).text.toString()

                                    val disponibilidadeAlvo = listaDisponibilidades[dataSelecionada]

                                    val novosHorarios =
                                        disponibilidadeAlvo.horarios.filter { horario ->
                                            !(horario.horaIni == horaIniRemover && horario.horaFim == horaFimRemover)
                                        }.toMutableList()

                                    listaDisponibilidades[dataSelecionada] =
                                        disponibilidadeAlvo.copy(horarios = novosHorarios)

                                    datasAdapter.notifyItemChanged(dataSelecionada)
                                }

                                if (listaDisponibilidades[dataSelecionada].horarios.isEmpty() && !visualizacaoInicial) {
                                    val blocoVazio = container.getChildAt(i + 1)
                                    val params = blocoVazio.layoutParams
                                    if (params is ViewGroup.MarginLayoutParams) {
                                        val marginInPixels = this.dpToPx(30)
                                        params.topMargin = marginInPixels
                                        blocoVazio.layoutParams = params
                                    }
                                }

                                container.removeView(bloco)
                                setupAcaoBotoes(container, tipo)
                            } else {
                                Rotinas.mostrarSnackbar(
                                    binding.root, R.layout.toast_alerta,
                                    "Você deve ter pelo menos um item.", null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

//    private fun adicionarBlocoData(container: LinearLayout) {
//        val inflater = LayoutInflater.from(this)
//        val novoBloco = inflater.inflate(
//            R.layout.data_item_criacao,
//            container,
//            false
//        )
//        container.addView(novoBloco)
//
//        setupAcaoBotoes(container, "DATA")
//
//        novoBloco.findViewById<EditText>(R.id.editData).requestFocus()
//    }

    private fun adicionarNovoDia(data: String, horarios: List<Horario>) {
        val dataJaExiste = listaDisponibilidades.any { disponibilidade ->
            disponibilidade.dia == data
        }

        if (dataSelecionadaString == "") {
            dataSelecionadaString = data
        }

        if (!dataJaExiste) {
            listaDisponibilidades.add(criarNovaDisponibilidade(data, horarios))

//            listaDisponibilidades.sortWith(compareBy { disponibilidade ->
//                val dateObject: Date? = stringToDate(disponibilidade.dia, "dd/MM/yyyy")
//                dateObject ?: Date(0)
//            })

            datasAdapter.notifyDataSetChanged()

//            val indexItem = listaDisponibilidades.indexOfFirst { it.dia == dataSelecionadaString }
//
//
//            if (indexItem != -1) {
//                datasAdapter.setSelected(indexItem)
//            }

            datasAdapter.setSelected(listaDisponibilidades.size - 1)

            datasAdapter.notifyItemInserted(listaDisponibilidades.size - 1)

            binding.recyclerDatas.scrollToPosition(listaDisponibilidades.size - 1)

            binding.editData.setText("")

            dataSelecionada = listaDisponibilidades.size - 1

            scrollarFinal()

            atualizarViews(listaDisponibilidades[listaDisponibilidades.size - 1].dia)
        } else {
            Rotinas.mostrarSnackbar(
                parentView = binding.root,
                layoutAlerta = R.layout.toast_alerta,
                mensagem = "Data já cadastrada para essa sala."
            )
        }

        for (disponibilidade in listaDisponibilidades) {
            Log.i("LISTAATUALIZADA Dia: ", disponibilidade.dia)
            for (horario in disponibilidade.horarios) {
                Log.i("LISTAATUALIZADA Horario: ", horario.horaIni + " - " + horario.horaFim)
            }
        }
    }

//    private fun adicionarNovoHorarioAoDia(horaIni: String, horaFim: String) {
//        if (dataSelecionada < 0 || listaDisponibilidades.isEmpty() || dataSelecionada >= listaDisponibilidades.size) {
//            Rotinas.mostrarSnackbar(binding.root, R.layout.toast_alerta, "Selecione uma data para adicionar o horário.", null)
//            return
//        }
//
//        val disponibilidadeAlvo = listaDisponibilidades[dataSelecionada]
//        val dataDoHorario = disponibilidadeAlvo.dia
//
//        val novoHorario = Horario(
//            data = dataDoHorario,
//            horaIni = horaIni,
//            horaFim = horaFim,
//            usuario = ""
//        )
//
//        val horariosAtuais = disponibilidadeAlvo.horarios.toMutableList()
//        horariosAtuais.add(novoHorario)
//
//        val novaDisponibilidade = disponibilidadeAlvo.copy(
//            horarios = horariosAtuais
//        )
//
//        listaDisponibilidades[dataSelecionada] = novaDisponibilidade
//
//        horariosAdapter.updateHorarios(novaDisponibilidade.horarios)
//
//        binding.horaIni.setText("")
//        binding.horaFim.setText("")
//    }

    private fun adicionarBlocoHorario(container: LinearLayout) {
        if (binding.nomeSala.text.toString().isNotBlank()) {
            if (dadosSala !== null || !modoEdicao) {
                val ultimoBloco = container.getChildAt(container.childCount - 1)
                val horaIni = ultimoBloco.findViewById<EditText>(R.id.horaIni).text.toString()
                val horaFim = ultimoBloco.findViewById<EditText>(R.id.horaFim).text.toString()

                if (!horaIni.isBlank()) {
                    if (!horaFim.isBlank()) {
                        if (dataSelecionada < 0 || listaDisponibilidades.isEmpty() || dataSelecionada >= listaDisponibilidades.size) {
                            Rotinas.mostrarSnackbar(
                                binding.root,
                                R.layout.toast_alerta,
                                "Selecione uma data para adicionar o horário.",
                                null
                            )
                            return
                        }

                        val disponibilidadeAlvo = listaDisponibilidades[dataSelecionada]
                        val dataDoHorario = disponibilidadeAlvo.dia


                        val novoHorario = Horario(
                            codigo = binding.nomeSala.text.toString()
                                .padStart(5, "0".first()) + dataDoHorario + horaIni + horaFim,
                            data = dataDoHorario,
                            horaIni = horaIni,
                            horaFim = horaFim,
                            usuario = ""
                        )

                        val horariosAtuais = disponibilidadeAlvo.horarios.toMutableList()
                        horariosAtuais.add(novoHorario)

                        val novaDisponibilidade = disponibilidadeAlvo.copy(
                            horarios = horariosAtuais
                        )

                        listaDisponibilidades[dataSelecionada] = novaDisponibilidade

                        val inflater = LayoutInflater.from(this)
                        val novoBloco = inflater.inflate(
                            R.layout.horario_item_criacao,
                            container,
                            false
                        )
                        container.addView(novoBloco)

                        setupPickers(novoBloco)

                        setupAcaoBotoes(container, "HORARIO")

//                        novoBloco.findViewById<EditText>(R.id.horaIni).requestFocus()

                        for (disponibilidade in listaDisponibilidades) {
                            Log.i("LISTAATUALIZADA Dia: ", disponibilidade.dia)
                            for (horario in disponibilidade.horarios) {
                                Log.i(
                                    "LISTAATUALIZADA Horario: ",
                                    horario.horaIni + " - " + horario.horaFim
                                )
                            }
                        }
                    } else {
                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_alerta,
                            mensagem = "Hora final  não pode estar vazia."
                        )
                    }
                } else {
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_alerta,
                        mensagem = "Hora inicial  não pode estar vazia."
                    )
                }
            }
        } else {
            Rotinas.mostrarSnackbar(
                parentView = binding.root,
                layoutAlerta = R.layout.toast_alerta,
                mensagem = "Informe o número da sala."
            )
        }
    }

    private fun setupPickers(blocoView: View) {
        val editData = blocoView.findViewById<EditText>(R.id.editData)
        editData?.setOnClickListener {

            DateDialog { selectedDate ->
//                val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
//
//                val dateObject = inputFormat.parse(selectedDate)
//                val formattedDate = outputFormat.format(dateObject)

                editData.setText(selectedDate)
                scrollarFinal()
            }.show(supportFragmentManager, "datePickerDialog")
        }

        val horaIni = blocoView.findViewById<EditText>(R.id.horaIni)
        horaIni?.setOnClickListener {
            TimeDialog { selectedTime ->
                horaIni.setText(selectedTime)
            }.show(supportFragmentManager, "timePickerDialog_ini")
        }

        val horaFim = blocoView.findViewById<EditText>(R.id.horaFim)
        horaFim?.setOnClickListener {
            TimeDialog { selectedTime ->
                horaFim.setText(selectedTime)
            }.show(
                supportFragmentManager,
                "timePickerDialog_fim"
            )
        }
    }

    private fun atualizarViews(data: String) {
        val container = binding.layoutHora

        container.removeAllViews()

        val disponibilidade = listaDisponibilidades.find { it.dia == data }
        val horarios = disponibilidade?.horarios ?: emptyList()

        var contador = 0;
        for (horario in horarios) {
            contador++;
            val inflater = LayoutInflater.from(this)
            val bloco = inflater.inflate(R.layout.horario_item_criacao, container, false)

            bloco.findViewById<EditText>(R.id.horaIni).setText(horario.horaIni)
            bloco.findViewById<EditText>(R.id.horaFim).setText(horario.horaFim)

            if (contador == 1) {
                val params = bloco.layoutParams
                if (params is ViewGroup.MarginLayoutParams) {
                    val marginInPixels = this.dpToPx(30)
                    params.topMargin = marginInPixels
                    bloco.layoutParams = params
                }
            }

            container.addView(bloco)
            setupPickers(bloco)
        }

        val inflater = LayoutInflater.from(this)
        val novoBlocoVazio = inflater.inflate(R.layout.horario_item_criacao, container, false)
        if (horarios.isEmpty()) {
            val params = novoBlocoVazio.layoutParams
            if (params is ViewGroup.MarginLayoutParams) {
                val marginInPixels = this.dpToPx(30)
                params.topMargin = marginInPixels
                novoBlocoVazio.layoutParams = params
            }
        }
        novoBlocoVazio.setOnClickListener {
            adicionarBlocoHorario(
                container
            )
        }
        container.addView(novoBlocoVazio)

        setupPickers(novoBlocoVazio)
        setupAcaoBotoes(container, "HORARIO")
    }

    private fun criarNovaDisponibilidade(
        data: String,
        horarios: List<Horario>
    ): Disponibilidade {
        return Disponibilidade(horarios = horarios, dia = data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        binding = ActivityCriarSalaBinding.inflate(layoutInflater)

        setContentView(binding.root);

        modoEdicao = intent.getBooleanExtra("modoEdicao", false)

        datasAdapter = DatasAdapter(this, listaDisponibilidades,
            onItemClickListener = { disponibilidade, position ->
                dataSelecionada = position
                visualizacaoInicial = false

                atualizarViews(disponibilidade.dia)

                binding.labSelecione.visibility = View.GONE
                binding.layoutHora.visibility = View.VISIBLE
            },
            onItemLongClickListener = { disponibilidade, position ->
                mostrarDialogo(contexto = this,
                    pLayoutDialogo = R.layout.dialogo_critico,
                    pMensagemPrincipal = "Excluir data",
                    pMensagemSecundaria = "Você deseja realmente excluir o dia ${disponibilidade.dia} da lista?, todos os horários desse dia serão excluidos!",
                    pBtnSim = "SIM",
                    pBtnNao = "NÃO",
                    pDottieAnimation = "",
                    object : DialogCallback {
                        override fun onSimClicked() {
                            try {
                                datasAdapter.setSelected(listaDisponibilidades.size - 1)

                                listaDisponibilidades.removeAt(position)
                                datasAdapter.notifyItemRemoved(position)

                                atualizarViews(disponibilidade.dia)

                                if (listaDisponibilidades.size > 0) {
                                    binding.labSelecione.visibility = View.VISIBLE
                                    binding.layoutHora.visibility = View.GONE
                                }

                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_informacao,
                                    mensagem = "Data removida com sucesso!"
                                )
                            } catch (E: Exception) {
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = E.message.toString()
                                )
                            }
                        }

                        override fun onNaoClicked() {

                        }
                    })
            }
        )

        horariosAdapter = HorariosAdapter(
            this,
            listaHorarios,
            onItemClickListener = { disponibilidade ->
            })

        binding.recyclerDatas.adapter = datasAdapter
        binding.recyclerDatas.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        binding.recyclerHoras.adapter = horariosAdapter;
        binding.recyclerHoras.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    override fun onStart() {
        super.onStart()

        val itemAnimatorData = binding.recyclerDatas.itemAnimator

        if (itemAnimatorData is androidx.recyclerview.widget.SimpleItemAnimator) {
            itemAnimatorData.supportsChangeAnimations = false
        }

        val navegarVisualizarSalas = Intent(this, RodapeFM::class.java)
        navegarVisualizarSalas.putExtra("ID_DESTINO", R.id.nav_salas)

        @Suppress("DEPRECATION") dadosSala = intent.getParcelableExtra("DADOS_SALA") as Sala?

        if (modoEdicao) {
            binding.btnConfiguracoes.visibility = View.VISIBLE
            binding.nomeSala.visibility = View.GONE;
            binding.txtNomeSala.visibility = View.VISIBLE;
            binding.btnCriarSala.text = "EDITAR SALA"

            val elemento1 = binding.scrollData
            val params1 = elemento1.layoutParams as ConstraintLayout.LayoutParams
            params1.topToBottom = R.id.txtNomeSala
            elemento1.layoutParams = params1
            elemento1.requestLayout()

            if (dadosSala !== null) {
                binding.nomeSala.setText(dadosSala!!.numero.padStart(2, "0".first()))
                binding.txtNomeSala.text = "Sala " + dadosSala!!.numero.padStart(2, "0".first())

                for (disponibilidade in dadosSala!!.disponibilidades) {
                    adicionarNovoDia(disponibilidade.dia, disponibilidade.horarios)

                    atualizarViews(disponibilidade.dia)
                }

                datasAdapter.setSelected(0)
                atualizarViews(listaDisponibilidades[0].dia)
                scrollarInicio()
            }
        }

        binding.btnConfiguracoes.setOnClickListener {
            if (dadosSala != null) {
                mostrarDialogoSala(
                    this,
                    "Sala " + dadosSala!!.numero.padStart(2, "0".first()),
                    dadosSala!!.status,
                    object : DialogoSalaCallback {
                        override fun onManutencaoClicked() {
                            val loadingReservando = Rotinas.mostrarLoading(
                                contexto = this@CriarSala,
                                pLayoutDialogo = R.layout.activity_loading_dialog,
                                pViewMensagem = R.id.loading_mensagem,
                                pMensagem = "Salvando alterações...",
                                pDottieAnimation = "loading_icon.json"
                            )

                            lifecycleScope.launch {
                                if (!dadosSala!!.status.equals("MANUTENCAO")) {
                                    var sucesso =
                                        editarSala(
                                            dadosSala!!
                                        )

                                    if (sucesso) {
                                        Rotinas.mostrarSnackbar(
                                            parentView = binding.root,
                                            layoutAlerta = R.layout.toast_informacao,
                                            mensagem = "Sala colocada em manutenção.",
                                            dialogoAtivo = loadingReservando
                                        )
                                        dadosSala!!.status = "MANUTENCAO"
                                    } else {
                                        Rotinas.mostrarSnackbar(
                                            parentView = binding.root,
                                            layoutAlerta = R.layout.toast_informacao,
                                            mensagem = "Erro ao colocar sala em manutenção.",
                                            dialogoAtivo = loadingReservando
                                        )
                                    }
                                } else {
                                    var sucesso =
                                        editarSala(
                                            dadosSala!!
                                        )

                                    if (sucesso) {
                                        Rotinas.mostrarSnackbar(
                                            parentView = binding.root,
                                            layoutAlerta = R.layout.toast_informacao,
                                            mensagem = "Sala retirada da manutenção.",
                                            dialogoAtivo = loadingReservando
                                        )
                                        dadosSala!!.status = "DISPONIVEL"
                                    } else {
                                        Rotinas.mostrarSnackbar(
                                            parentView = binding.root,
                                            layoutAlerta = R.layout.toast_alerta,
                                            mensagem = "Erro ao retirar sala da manutenção.",
                                            dialogoAtivo = loadingReservando
                                        )
                                    }
                                }
                            }
                        }

                        override fun onExcluirClicked() {
                            val loadingReservando = Rotinas.mostrarLoading(
                                contexto = this@CriarSala,
                                pLayoutDialogo = R.layout.activity_loading_dialog,
                                pViewMensagem = R.id.loading_mensagem,
                                pMensagem = "Deletando...",
                                pDottieAnimation = "loading_icon.json"
                            )

                            lifecycleScope.launch {
                                var podeExcluir = true;

                                var salaLida = lerSala(
                                    dadosSala!!.numero.padStart(
                                        5,
                                        "0".first()
                                    )
                                )

                                if (salaLida !== null) {
                                    for (disponibilidade in salaLida.disponibilidades) {
                                        for (horario in disponibilidade.horarios) {
                                            if (!horario.usuario?.isBlank()!!) {
                                                podeExcluir = false;
                                                break;
                                            }
                                        }

                                        if (!podeExcluir) {
                                            break;
                                        }
                                    }
                                }

                                if (podeExcluir) {

                                    Rotinas.mostrarDialogo(
                                        contexto = this@CriarSala,
                                        pLayoutDialogo = R.layout.dialogo_critico,
                                        pMensagemPrincipal = "Confirmar exclusão",
                                        pMensagemSecundaria = "Deseja realmente excluir a sala ${
                                            dadosSala!!.numero.padStart(
                                                2,
                                                "0".first()
                                            )
                                        }?",
                                        pBtnSim = "DELETAR",
                                        pBtnNao = "CANCELAR",
                                        pDottieAnimation = "",
                                        callback = object : DialogCallback {
                                            override fun onSimClicked() {

                                                lifecycleScope.launch {
                                                    var sucesso =
                                                        RotinasBD.deletarSala(
                                                            dadosSala!!.numero.padStart(
                                                                5,
                                                                "0".first()
                                                            )
                                                        )

                                                    if (sucesso) {
                                                        Rotinas.mostrarSnackbar(
                                                            parentView = binding.root,
                                                            layoutAlerta = R.layout.toast_informacao,
                                                            mensagem = "Sala excluída com sucesso!",
                                                            dialogoAtivo = loadingReservando
                                                        )

                                                        Handler(mainLooper).postDelayed({
                                                            startActivity(navegarVisualizarSalas)
                                                            this@CriarSala.overridePendingTransition(
                                                                R.anim.animate_fade_enter,
                                                                R.anim.animate_fade_exit
                                                            )
                                                        }, 1000L)
                                                    } else {
                                                        Rotinas.mostrarSnackbar(
                                                            parentView = binding.root,
                                                            layoutAlerta = R.layout.toast_alerta,
                                                            mensagem = "Erro ao excluir sala.",
                                                            dialogoAtivo = loadingReservando
                                                        )
                                                    }
                                                }
                                            }


                                            override fun onNaoClicked() {}
                                        }

                                    )

                                } else {
                                    Rotinas.mostrarSnackbar(
                                        parentView = binding.root,
                                        layoutAlerta = R.layout.toast_alerta,
                                        mensagem = "Essa sala possui horários reservados.",
                                        dialogoAtivo = loadingReservando
                                    )
                                }
                            }
                        }
                    }

                )
            }
        }

        binding.btnAdicionarData.setOnClickListener {
            if (!binding.editData.text.isBlank()) {
                val anoAtual = Calendar.getInstance().get(Calendar.YEAR)

                val dataDoItem: Date? =
                    stringToDate(binding.editData.text.toString(), "dd/MM/yyyy")

                val calendarItem = Calendar.getInstance()
                calendarItem.time = dataDoItem
                val anoDoItem = calendarItem.get(Calendar.YEAR)

                if (anoDoItem < anoAtual) {
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_alerta,
                        mensagem = "Ano não pode ser anterior ao atual."
                    )
                } else {
                    adicionarNovoDia(binding.editData.text.toString(), listOf())
                }

            } else {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Data não pode estar vazia."
                )
            }
        }

        binding.btnAdicionarHorario.setOnClickListener {
            adicionarBlocoHorario(
                binding.layoutHora
            )
        }

        binding.btnVoltar.setOnClickListener {
            startActivity(navegarVisualizarSalas)
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )

        }

        setupPickers(binding.root)

        binding.btnCriarSala.setOnClickListener {
            val loading = Rotinas.mostrarLoading(
                contexto = this@CriarSala,
                pLayoutDialogo = R.layout.activity_loading_dialog,
                pViewMensagem = R.id.loading_mensagem,
                pMensagem = "Carregando...",
                pDottieAnimation = "loading_icon.json"
            )

            if (binding.nomeSala.text.toString().isBlank()
            ) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Preencha todos os campos",
                    dialogoAtivo = loading
                )
            } else if (binding.nomeSala.text.toString().toInt() == 0) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Número da sala não pode ser zero.",
                    dialogoAtivo = loading
                )
            } else if (listaDisponibilidades.isEmpty()) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Cadastre pelo menos uma data",
                    dialogoAtivo = loading
                )
            } else {
                var exit = false;
                for (disponibilidade in listaDisponibilidades) {
                    if (disponibilidade.horarios.isEmpty()) {
                        Rotinas.mostrarSnackbar(
                            parentView = binding.root,
                            layoutAlerta = R.layout.toast_alerta,
                            mensagem = "Dia ${disponibilidade.dia} sem horários.",
                            dialogoAtivo = loading
                        )
                        exit = true;
                        break;
                    }
                }

                if (exit) {
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    if (!modoEdicao) {
                        var existe = salaExiste(
                            binding.nomeSala.text.toString().padStart(5, "0".first())
                        )

                        if (existe) {
                            Rotinas.mostrarSnackbar(
                                parentView = binding.root,
                                layoutAlerta = R.layout.toast_alerta,
                                mensagem = "Sala " + binding.nomeSala.text.toString()
                                    .padStart(5, "0".first()) + " já cadastrada.",
                                dialogoAtivo = loading
                            )
                        } else {

                            var sucesso = criarSala(
                                Sala(
                                    numero = binding.nomeSala.text.toString()
                                        .padStart(5, "0".first()),
                                    disponibilidades = listaDisponibilidades,
                                    status = "DISPONIVEL"
                                )
                            )

                            if (sucesso) {

                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_informacao,
                                    mensagem = "Sala criada com sucesso!",
                                    dialogoAtivo = loading
                                )
                                Handler(mainLooper).postDelayed({
                                    startActivity(navegarVisualizarSalas)
                                    this@CriarSala.overridePendingTransition(
                                        R.anim.animate_fade_enter,
                                        R.anim.animate_fade_exit
                                    )
                                }, 1000L)
                            } else {
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Erro ao criar sala.",
                                    dialogoAtivo = loading
                                )
                            }
                        }
                    } else {
                        if (dadosSala !== null) {
                            var sucesso = editarSala(
                                Sala(
                                    numero = binding.nomeSala.text.toString()
                                        .padStart(5, "0".first()),
                                    disponibilidades = listaDisponibilidades,
                                    status = dadosSala!!.status
                                )
                            )

                            if (sucesso) {

                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_informacao,
                                    mensagem = "Sala editada com sucesso!",
                                    dialogoAtivo = loading
                                )
                                Handler(mainLooper).postDelayed({
                                    startActivity(navegarVisualizarSalas)
                                    this@CriarSala.overridePendingTransition(
                                        R.anim.animate_fade_enter,
                                        R.anim.animate_fade_exit
                                    )
                                }, 1000L)
                            } else {
                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Erro ao editar sala.",
                                    dialogoAtivo = loading
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}