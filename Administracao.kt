package com.example.unilib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.unilib.Classes.BaseFragment
import com.example.unilib.Inicio.Inicio
import com.example.unilib.Emprestimos.GerenciarEmprestimos
import com.example.unilib.Eventos.GerenciarEventos
import com.example.unilib.Livros.VisualizarLivros
import com.example.unilib.Reviews.GerenciarReviews
import com.example.unilib.Usuarios.GerenciarUsuarios
import com.example.unilib.databinding.ActivityAdministracaoBinding

class Administracao : BaseFragment() {

    private lateinit var binding: ActivityAdministracaoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityAdministracaoBinding.inflate(layoutInflater)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navegarInicio = Intent(requireContext(), RodapeFM::class.java)
        val navegarGerenciarLivros = Intent(requireContext(), VisualizarLivros::class.java)
        val navegarGerenciarEmprestimos = Intent(requireContext(), GerenciarEmprestimos::class.java)
        val navegarGerenciarEventos = Intent(requireContext(), GerenciarEventos::class.java)
        val navegarGerenciarReviews = Intent(requireContext(), GerenciarReviews::class.java)
        val navegarGerenciarUsuarios = Intent(requireContext(), GerenciarUsuarios::class.java)

        binding.btnVoltarAdministracao.setOnClickListener {
            parentFragmentManager.popBackStack()
//            navegarInicio.putExtra("ID_DESTINO", R.id.navigation_inicio)
//            startActivity(navegarInicio)
//            this.requireActivity().overridePendingTransition(
//                R.anim.animate_fade_enter,
//                R.anim.animate_fade_exit
//            )
        }

        binding.btnGerenciarLivros.setOnClickListener {
            startActivity(navegarGerenciarLivros)
            this.requireActivity().overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

        binding.btnGerenciarEmprestimos.setOnClickListener {
            startActivity(navegarGerenciarEmprestimos)
            this.requireActivity().overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }
        binding.btnGerenciarEventos.setOnClickListener {
            startActivity(navegarGerenciarEventos)
            this.requireActivity().overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

        binding.btnGerenciarReviews.setOnClickListener {
            startActivity(navegarGerenciarReviews)
            this.requireActivity().overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

        binding.btnGerenciarUsuarios.setOnClickListener {
            startActivity(navegarGerenciarUsuarios)
            this.requireActivity().overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }
    }
}