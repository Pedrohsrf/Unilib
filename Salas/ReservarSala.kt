package com.example.unilib.Salas;

import DialogCallback
import android.content.Intent
import DialogoSalaCallback
import android.content.Context
import android.os.Bundle;
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope

import com.example.unilib.Classes.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Sala
import com.example.unilib.Classes.Disponibilidade
import com.example.unilib.Classes.Horario
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Rotinas.criarDataCompleta
import com.example.unilib.Classes.Rotinas.mostrarDialogoSala
import com.example.unilib.Classes.Rotinas.stringToDate
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.atualizarHorario
//import com.example.unilib.Classes.RotinasBD.deletarSala
import com.example.unilib.Classes.RotinasBD.editarSala
import com.example.unilib.Classes.RotinasBD.lerSala

import com.example.unilib.R;
import com.example.unilib.RodapeFM
import com.example.unilib.Salas.RecyclerView_Adapter.DatasAdapter
import com.example.unilib.Salas.RecyclerView_Adapter.HorariosAdapter
import com.example.unilib.databinding.ActivityReservarSalaBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ReservarSala : BaseActivity() {

    private lateinit var binding: ActivityReservarSalaBinding
    private lateinit var horariosAdapter: HorariosAdapter
    private lateinit var disponibilidadeAtual: Disponibilidade
    private var modoEdicao: Boolean = false

    suspend fun preencherDisponibilidadesDeTeste() {
        val fb = Firebase.firestore

        val ontemDDMM = "14/11"
        val hojeDDMM = "15/11"
        val amanhaDDMM = "16/11"

        val colecao = fb.collection("disponibilidades")

        // Função auxiliar para gerar códigos de usuário de 5 posições (de 1 a 5)
        fun gerarCodigoUsuario(index: Int): String {
            return index.toString().padStart(5, '0')
        }

        val disponibilidades = listOf(
            mapOf(
                "data" to ontemDDMM,
                "horaIni" to "23:00",
                "horaFim" to "23:59",
                "sala" to "00005",
                "usuario" to ""
            ),
            // 14. SALA 00005 (Amanhã - Ocupado)
            mapOf(
                "data" to ontemDDMM,
                "horaIni" to "12:00",
                "horaFim" to "13:00",
                "sala" to "00005",
                "usuario" to gerarCodigoUsuario(1)
            ),
            // 10. HOJE (Vago, Múltiplo de 2 Horas) - Deve ser mantido
            mapOf(
                "data" to ontemDDMM,
                "horaIni" to "17:00",
                "horaFim" to "19:00",
                "sala" to "00003",
                "usuario" to ""
            ),

            // === SALAS NOVAS (Mais Vagas) ===
            // 11. SALA 00004 (Vago) - Deve ser mantido
            mapOf(
                "data" to ontemDDMM,
                "horaIni" to "19:00",
                "horaFim" to "20:00",
                "sala" to "00004",
                "usuario" to ""
            ),
            // 12. SALA 00004 (Amanhã - Ocupado)
            mapOf(
                "data" to ontemDDMM,
                "horaIni" to "10:00",
                "horaFim" to "11:00",
                "sala" to "00004",
                "usuario" to gerarCodigoUsuario(5)
            ),
            // 13. SALA 00005 (Vago) - Deve ser mantido
            mapOf(
                "data" to ontemDDMM,
                "horaIni" to "23:00",
                "horaFim" to "23:59",
                "sala" to "00005",
                "usuario" to ""
            ),
            // 14. SALA 00005 (Amanhã - Ocupado)
            mapOf(
                "data" to ontemDDMM,
                "horaIni" to "12:00",
                "horaFim" to "13:00",
                "sala" to "00005",
                "usuario" to gerarCodigoUsuario(1)
            ),
        )

        Log.d("Firestore", "Iniciando inserção de ${disponibilidades.size} documentos de teste...")

        // Inserir cada documento
        disponibilidades.forEachIndexed { index, data ->
            try {
                // Garante que o código da sala seja padronizado para 5 posições
                val codigoSala = data["sala"] as String
                val novoData = data.toMutableMap()
                novoData["sala"] = codigoSala.padStart(5, '0')

                colecao.add(novoData).await()
                Log.d(
                    "Firestore",
                    "Documento ${index + 1} inserido com sucesso (Sala ${novoData["sala"]})."
                )
            } catch (e: Exception) {
                Log.e("Firestore", "Erro ao inserir documento ${index + 1}: ${e.message}")
            }
        }
        Log.d("Firestore", "Inserção de dados de teste concluída.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        binding = ActivityReservarSalaBinding.inflate(layoutInflater)
        setContentView(binding.root);

        modoEdicao = intent.getBooleanExtra("modoEdicao", false)
    }

    override fun onStart() {
        super.onStart()

        val sharedPref = this.getSharedPreferences("MeuAppPrefs", Context.MODE_PRIVATE)
        val codUsuario = sharedPref.getString("codigoUsuario", "")
        val administrador = sharedPref.getBoolean("administrador", false)

        val navegarSalas = Intent(
            this, RodapeFM::class.java
        )
        navegarSalas.putExtra("ID_DESTINO", R.id.nav_salas)

        @Suppress("DEPRECATION") var dadosSala = intent.getParcelableExtra("DADOS_SALA") as Sala?

        dadosSala = filtrarDisponibilidades(dadosSala, codUsuario.toString());

        if (dadosSala != null) {
            binding.nomeSala.text = "Sala " + dadosSala.numero.padStart(2, "0".first())

            disponibilidadeAtual = dadosSala.disponibilidades
                .firstOrNull()
                ?: Disponibilidade(horarios = emptyList(), dia = "")

            if (dadosSala.disponibilidades.isEmpty()) {
                binding.recyclerDatas.visibility = View.GONE;
                binding.recyclerHorarios.visibility = View.GONE

                binding.layoutDataVazio.visibility = View.VISIBLE
                binding.divisor1.visibility = View.VISIBLE
                binding.layoutHorariosVazio.visibility = View.VISIBLE
            }
        }

        binding.btnVoltar.setOnClickListener {
            finish()
//            startActivity(navegarSalas)
//            this.overridePendingTransition(
//                R.anim.animate_fade_enter, R.anim.animate_fade_exit
//            )
        }

        if (dadosSala != null) {
            if (dadosSala.disponibilidades !== null) {
                horariosAdapter = HorariosAdapter(
                    this,
                    disponibilidadeAtual.horarios,
                    onItemClickListener = { disponibilidade ->
                        if (!administrador) {
                            if (disponibilidade.usuario?.isBlank() == true) {
                                val reservar = Rotinas.mostrarDialogo(contexto = this,
                                    pLayoutDialogo = R.layout.dialogo_neutro,
                                    pMensagemPrincipal = "Reservar sala",
                                    pMensagemSecundaria = "Você deseja realmente reservar \na Sala " + dadosSala?.numero?.padStart(
                                        2, "0".first()
                                    ) + " das " + disponibilidade.horaIni + "-" + disponibilidade.horaFim + "?",
                                    pBtnSim = "SIM",
                                    pBtnNao = "NÃO",
                                    pDottieAnimation = "",
                                    object : DialogCallback {
                                        override fun onSimClicked() {
                                            val loadingReservando = Rotinas.mostrarLoading(
                                                contexto = this@ReservarSala,
                                                pLayoutDialogo = R.layout.activity_loading_dialog,
                                                pViewMensagem = R.id.loading_mensagem,
                                                pMensagem = "Reservando...",
                                                pDottieAnimation = "loading_icon.json"
                                            )

                                            lifecycleScope.launch {

                                                var ocupado = atualizarHorario(
                                                    dadosSala.numero.padStart(5, "0".first()),
                                                    disponibilidade.data,
                                                    disponibilidade.horaIni,
                                                    disponibilidade.horaFim,
                                                    codUsuario.toString()
                                                )

                                                if (ocupado) {

                                                    Rotinas.mostrarSnackbar(
                                                        parentView = binding.root,
                                                        layoutAlerta = R.layout.toast_informacao,
                                                        mensagem = "Sala reservada com sucesso!",
                                                        dialogoAtivo = loadingReservando
                                                    )
                                                    disponibilidade.usuario = codUsuario
                                                    val posicao =
                                                        disponibilidadeAtual.horarios.indexOf(
                                                            disponibilidade
                                                        )
                                                    horariosAdapter.notifyItemChanged(posicao)

                                                } else {
                                                    Rotinas.mostrarSnackbar(
                                                        parentView = binding.root,
                                                        layoutAlerta = R.layout.toast_alerta,
                                                        mensagem = "Erro ao reservar sala, tente novamente.",
                                                        dialogoAtivo = loadingReservando
                                                    )
                                                }
                                            }
                                        }

                                        override fun onNaoClicked() {

                                        }
                                    })
                            } else {
                                val desreservar = Rotinas.mostrarDialogo(contexto = this,
                                    pLayoutDialogo = R.layout.dialogo_neutro,
                                    pMensagemPrincipal = "Remover reserva",
                                    pMensagemSecundaria = "Você deseja realmente remover a reserva \nda Sala " + dadosSala?.numero?.padStart(
                                        2, "0".first()
                                    ) + " das " + disponibilidade.horaIni + "-" + disponibilidade.horaFim + "?",
                                    pBtnSim = "SIM",
                                    pBtnNao = "NÃO",
                                    pDottieAnimation = "",
                                    object : DialogCallback {
                                        override fun onSimClicked() {
                                            val loadingReservando = Rotinas.mostrarLoading(
                                                contexto = this@ReservarSala,
                                                pLayoutDialogo = R.layout.activity_loading_dialog,
                                                pViewMensagem = R.id.loading_mensagem,
                                                pMensagem = "Removendo...",
                                                pDottieAnimation = "loading_icon.json"
                                            )

                                            lifecycleScope.launch {

                                                var ocupado = atualizarHorario(
                                                    dadosSala.numero.padStart(5, "0".first()),
                                                    disponibilidade.data,
                                                    disponibilidade.horaIni,
                                                    disponibilidade.horaFim,
                                                    ""
                                                )

                                                if (ocupado) {

                                                    Rotinas.mostrarSnackbar(
                                                        parentView = binding.root,
                                                        layoutAlerta = R.layout.toast_informacao,
                                                        mensagem = "Reserva removida com sucesso!",
                                                        dialogoAtivo = loadingReservando
                                                    )
                                                    disponibilidade.usuario = ""
                                                    val posicao =
                                                        disponibilidadeAtual.horarios.indexOf(
                                                            disponibilidade
                                                        )
                                                    horariosAdapter.notifyItemChanged(posicao)

                                                } else {
                                                    Rotinas.mostrarSnackbar(
                                                        parentView = binding.root,
                                                        layoutAlerta = R.layout.toast_alerta,
                                                        mensagem = "Erro ao remover reserva, tente novamente.",
                                                        dialogoAtivo = loadingReservando
                                                    )
                                                }
                                            }
                                        }

                                        override fun onNaoClicked() {

                                        }
                                    })
                            }
                        } else {
                            Rotinas.mostrarSnackbar(
                                parentView = binding.root,
                                layoutAlerta = R.layout.toast_alerta,
                                mensagem = "Ação exclusiva de alunos."
                            )
                        }
                    })


                val datasAdapter = DatasAdapter(this,
                    dadosSala.disponibilidades,
                    onItemClickListener = { disponibilidade, position ->
                        disponibilidadeAtual = disponibilidade
                        horariosAdapter.updateHorarios(disponibilidade.horarios)
                    },
                    onItemLongClickListener = { disponibilidade, position ->
                        Log.i("Deseja excluir a data: ", "${disponibilidade.dia}?")
                    })

                binding.recyclerDatas.adapter = datasAdapter;
                binding.recyclerDatas.layoutManager =
                    LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

                binding.recyclerHorarios.adapter = horariosAdapter;
                binding.recyclerHorarios.layoutManager =
                    LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            }
        }
    }

    fun normalizarParaMeiaNoite(dataOriginal: Date): Date {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))

        calendar.time = dataOriginal

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    fun filtrarDisponibilidades(dadosSala: Sala?, usuario: String): Sala? {

        if (dadosSala?.disponibilidades == null) {
            return dadosSala
        }

        val timezone = TimeZone.getTimeZone("America/Sao_Paulo")

        val formatadorCompleto =
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
                timeZone = timezone
            }

        val formatadorDia = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = timezone
        }

        val dataHoraAtualString = formatadorCompleto.format(Date())

        val momentoAtual = formatadorCompleto.parse(dataHoraAtualString)!!

        val disponibilidadesFiltradas = dadosSala.disponibilidades
            .mapNotNull { disponibilidade ->

//                var dataDisponibilidade = stringToDate(disponibilidade.dia, "dd/MM/yyyy");
//                var dataHoje = stringToDate(
//                    dataHoraAtualString,
//                    "dd/MM/yyyy"
//                )
//
//                if (dataDisponibilidade !== null && dataHoje !== null) {
//                    if (dataDisponibilidade.before(dataHoje)) {
//                        null
//                    }
//                }

                val horariosFiltrados = disponibilidade.horarios.filter { horario ->

                    val dataComAno =
                        formatadorDia.format(criarDataCompleta(horario.data)!!)

                    val dataHoraFimItemString = "$dataComAno ${horario.horaFim}"

                    val dataHoraFimItem = formatadorCompleto.parse(dataHoraFimItemString)

                    val aindaNaoPassou = dataHoraFimItem != null && !dataHoraFimItem.before(momentoAtual)

                    val pertenceAUsuario = horario.usuario == usuario

                    val estaVago = horario.usuario == ""

                    return@filter aindaNaoPassou && (pertenceAUsuario || estaVago)
                }

                if (horariosFiltrados.isNotEmpty()) {
                    disponibilidade.copy(horarios = horariosFiltrados)
                } else {
                    null
                }
            }

        return dadosSala.copy(disponibilidades = disponibilidadesFiltradas)
    }
}