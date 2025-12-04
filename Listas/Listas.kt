package com.example.unilib.Listas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.BaseFragment
import com.example.unilib.Classes.Lista // Importa sua data class
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Estante.RecyclerView_Adapter.ListasAdapter
import com.example.unilib.R
import com.example.unilib.databinding.ActivityListasBinding
import kotlinx.coroutines.launch

class Listas : BaseFragment() {

    private lateinit var binding: ActivityListasBinding
    private lateinit var listasAdapter: ListasAdapter
    private val listasComputadas = mutableListOf<Lista>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityListasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        setupAdapter()
    }

    override fun onResume() {
        super.onResume()
        carregarMinhasListas()
    }

    private fun setupAdapter() {
        listasAdapter = ListasAdapter(
            requireContext(),
            listasComputadas,
            onItemClickListener = { lista ->
                val navegarLista = Intent(requireContext(), EditarLista::class.java)

                navegarLista.putExtra("NOME_DA_LISTA", lista.nomeLista)

                startActivity(navegarLista)
                this.requireActivity().overridePendingTransition(
                    R.anim.animate_fade_enter,
                    R.anim.animate_fade_exit
                )
            }
        )
        binding.listas.adapter = listasAdapter
    }

    private fun carregarMinhasListas() {
        binding.listas.visibility = View.INVISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val usuarioCodigo = getUsuarioLogado().codigo

                val (livrosFavoritos, livrosDesejos) = RotinasBD.lerListas(usuarioCodigo)

                val listaFavoritos = Lista(
                    "Favoritos",
                    livrosFavoritos,
                    R.color.lista_amarela
                )
                val listaDesejos = Lista(
                    "Lista de desejos",
                    livrosDesejos,
                    R.color.lista_azul
                )

                listasComputadas.clear()
                listasComputadas.add(listaFavoritos)
                listasComputadas.add(listaDesejos)
                listasAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("ListasFragment", "Erro ao carregar listas: ${e.message}")
            } finally {
                binding.listas.visibility = View.VISIBLE
            }
        }
    }
}