package com.example.unilib.Eventos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.doOnTextChanged
import com.example.unilib.Classes.BaseFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unilib.Classes.Evento
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasBD.ConsultaPorPrefixo
import com.example.unilib.Inicio.Inicio
import com.example.unilib.Notificacoes.RecyclerView_Adapter.EventListAdapter
import com.example.unilib.Notificacoes.RecyclerView_Adapter.NotificationListAdapter
import com.example.unilib.R
import com.example.unilib.RodapeFM
import com.example.unilib.databinding.ActivityVisualizarEventosBinding
import kotlinx.coroutines.launch

class VisualizarEventos : BaseFragment() {
    private lateinit var binding: ActivityVisualizarEventosBinding
    private lateinit var btnVoltarEventos: ImageView
    private lateinit var eventosList: MutableList<Evento>
    private lateinit var eventosAdapter: EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityVisualizarEventosBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            eventosList = RotinasBD.lerEventos(false)

            configurarRecyclerView(eventosList)

            binding.btnVoltarEventos.setOnClickListener {
                parentFragmentManager.popBackStack()
//                val navegarInicio = Intent(requireContext(), RodapeFM::class.java)
//                navegarInicio.putExtra("ID_DESTINO", R.id.navigation_inicio)
//                startActivity(navegarInicio)
            }

            binding.searchInput.doOnTextChanged { text, start, before, count ->
                lifecycleScope.launch {
                    val busca = text.toString()

                    if (busca.isNotEmpty()) {

                        val listaSnapshots = ConsultaPorPrefixo(
                            "evento",
                            busca,
                            "nomeEvento",
                            "nomeEvento_lower"
                        )

                        val listaEventos = mutableListOf<Evento>()
                        for (evento in listaSnapshots) {
                            listaEventos.add(
                                Evento(
                                    codigo = evento.get("codigo") as String,
                                    nomeEvento = evento.getString("nomeEvento") ?: "",
                                    Imagem = evento.get("imagem") as String,
                                    dataEvento = evento.get("dataEvento") as String,
                                    gerenciavel = false
                                )
                            )
                        }

                        if (listaEventos.isEmpty()) {
                            binding.labNenhumEventoEncontrado3.visibility = View.VISIBLE
                            binding.RecyclerViewEventos.visibility = View.GONE
                        } else {
                            binding.labNenhumEventoEncontrado3.visibility = View.GONE
                            binding.RecyclerViewEventos.visibility = View.VISIBLE
                        }

                        eventosAdapter.atualizarListaEventos(listaEventos)

                    } else {

                        val listaCompleta = RotinasBD.lerEventos(false)

                        eventosAdapter.atualizarListaEventos(listaCompleta)
                        binding.labNenhumEventoEncontrado3.visibility = View.GONE
                        binding.RecyclerViewEventos.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun configurarRecyclerView(eventos: MutableList<Evento>) {
        eventosAdapter = EventListAdapter(
            context = requireContext(),
            eventos = eventos,
            onEditarClick = {},
            onDeletarClick = {}
        )

        binding.RecyclerViewEventos.adapter = eventosAdapter
        binding.RecyclerViewEventos.layoutManager =
            LinearLayoutManager(requireContext())
    }

}
