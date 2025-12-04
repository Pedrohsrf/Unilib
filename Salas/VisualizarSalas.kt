package com.example.unilib.Salas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.BaseFragment
import com.example.unilib.Classes.RotinasBD.lerSalas
import com.example.unilib.R
import com.example.unilib.RodapeFM
import com.example.unilib.Salas.Fragments.FragmentSalas
import com.example.unilib.Salas.Fragments.FragmentSalasDisponiveis
import com.example.unilib.Salas.Fragments.FragmentSalasOcupadas
import com.example.unilib.databinding.ActivityVisualizarSalasBinding

private lateinit var binding: ActivityVisualizarSalasBinding
private var editando: Boolean = false;

class VisualizarSalas : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityVisualizarSalasBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navegarInicio = Intent(requireContext(), RodapeFM::class.java)
        navegarInicio.putExtra("ID_DESTINO", R.id.navigation_inicio);

        val navegarCriarSala = Intent(requireContext(), CriarSala::class.java)

        val sharedPref =
            requireContext().getSharedPreferences("MeuAppPrefs", Context.MODE_PRIVATE)
        val administrador = sharedPref.getBoolean("administrador", false)

        if (!administrador) {
            binding.btnEditarSalas.visibility = View.GONE;
            binding.btnCriarSala.visibility = View.GONE
        }

        if (savedInstanceState == null) {
            replaceFragment(FragmentSalas())
        }

        binding.btnTodas.setOnClickListener {
            binding.btnTodas.setBackgroundDrawable(requireContext().getDrawable(R.drawable.botao_salas_selecionado))
            binding.btnDisponiveis.setBackgroundDrawable(requireContext().getDrawable(R.drawable.botao_salas_deselecionado))
            binding.btnOcupadas.setBackgroundDrawable(requireContext().getDrawable(R.drawable.botao_salas_deselecionado))
            binding.btnTodas.setTextColor(Rotinas.getColor(requireContext(), R.color.white))
            binding.btnDisponiveis.setTextColor(
                Rotinas.getColor(
                    requireContext(),
                    R.color.cinza_ativo
                )
            )
            binding.btnOcupadas.setTextColor(
                Rotinas.getColor(
                    requireContext(),
                    R.color.cinza_ativo
                )
            )
            binding.btnTodas.alpha = 1F
            binding.btnDisponiveis.alpha = 0.4F
            binding.btnOcupadas.alpha = 0.4F
            replaceFragment(FragmentSalas())
        }

        binding.btnDisponiveis.setOnClickListener {
            binding.btnDisponiveis.setBackgroundDrawable(requireContext().getDrawable(R.drawable.botao_salas_selecionado))
            binding.btnTodas.setBackgroundDrawable(requireContext().getDrawable(R.drawable.botao_salas_deselecionado))
            binding.btnOcupadas.setBackgroundDrawable(requireContext().getDrawable(R.drawable.botao_salas_deselecionado))
            binding.btnDisponiveis.setTextColor(
                Rotinas.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.btnTodas.setTextColor(
                Rotinas.getColor(
                    requireContext(),
                    R.color.cinza_ativo
                )
            )
            binding.btnOcupadas.setTextColor(
                Rotinas.getColor(
                    requireContext(),
                    R.color.cinza_ativo
                )
            )
            binding.btnDisponiveis.alpha = 1F
            binding.btnTodas.alpha = 0.4F
            binding.btnOcupadas.alpha = 0.4F

            replaceFragment(FragmentSalasDisponiveis())
        }

        binding.btnOcupadas.setOnClickListener {
            binding.btnOcupadas.setBackgroundDrawable(requireContext().getDrawable(R.drawable.botao_salas_selecionado))
            binding.btnTodas.setBackgroundDrawable(requireContext().getDrawable(R.drawable.botao_salas_deselecionado))
            binding.btnDisponiveis.setBackgroundDrawable(requireContext().getDrawable(R.drawable.botao_salas_deselecionado))
            binding.btnOcupadas.setTextColor(Rotinas.getColor(requireContext(), R.color.white))
            binding.btnTodas.setTextColor(
                Rotinas.getColor(
                    requireContext(),
                    R.color.cinza_ativo
                )
            )
            binding.btnDisponiveis.setTextColor(
                Rotinas.getColor(
                    requireContext(),
                    R.color.cinza_ativo
                )
            )
            binding.btnOcupadas.alpha = 1F
            binding.btnTodas.alpha = 0.4F
            binding.btnDisponiveis.alpha = 0.4F

            replaceFragment(FragmentSalasOcupadas())
        }

        binding.btnEditarSalas.setOnClickListener {
            if (!editando) {
                editando = true;

                binding.btnEditarSalas.setImageResource(R.drawable.check_icon2)
            } else {
                editando = false;

                binding.btnEditarSalas.setImageResource(R.drawable.editar_icon)
            }

            val bundle = Bundle()
            bundle.putBoolean("MODO_EDICAO", editando)

            try {
                childFragmentManager.setFragmentResult("REQUEST_KEY_EDICAO", bundle)

            } catch (E: Exception) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Aguarde, carregando salas..."
                )
                editando = false;
                binding.btnEditarSalas.setImageResource(R.drawable.editar_icon)
            }
        }

        binding.btnCriarSala.setOnClickListener {
            startActivity(navegarCriarSala)
            this.requireActivity().overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

        binding.btnVoltar.setOnClickListener {
            startActivity(navegarInicio)
            this.requireActivity().overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )

        }
    }


    private fun replaceFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()

        transaction.replace(R.id.layoutFragment, fragment)

        transaction.commit()
    }
}