package com.example.unilib.Salas.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.unilib.Classes.BaseFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.unilib.Classes.Disponibilidade
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.dotlottie.dlplayer.Mode

import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD.editarSala
import com.example.unilib.Classes.RotinasBD.lerSalas
import com.example.unilib.Classes.Sala
import com.example.unilib.R
import com.example.unilib.Salas.CriarSala
import com.example.unilib.Salas.RecyclerView_Adapter.SalasAdapter
import com.example.unilib.Salas.ReservarSala
import com.example.unilib.databinding.FragmentSalasOcupadasBinding
import kotlinx.coroutines.launch
import java.util.Date


class FragmentSalasOcupadas : BaseFragment() {

    private lateinit var binding: FragmentSalasOcupadasBinding;
    private var modoEdicao: Boolean = false;
    private lateinit var salasAdapter: SalasAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSalasOcupadasBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editarSala = Intent(requireContext(), CriarSala::class.java);
        val navegarSala = Intent(requireContext(), ReservarSala::class.java);

            requireParentFragment().childFragmentManager.setFragmentResultListener(
                "REQUEST_KEY_EDICAO",
                viewLifecycleOwner
            ) { key, bundle ->

                modoEdicao = bundle.getBoolean("MODO_EDICAO")

                salasAdapter.modoEdicao(modoEdicao)
            }

        var salasList: MutableList<Sala>;

        val config = Config.Builder().autoplay(true).speed(1f).loop(true)
            .source(DotLottieSource.Asset("loading_preto.json")).playMode(Mode.FORWARD)
            .useFrameInterpolation(true).build()

        val dotLottieAnimationView = binding.loading

        dotLottieAnimationView.load(config)
        binding.loading.visibility = View.VISIBLE;
        binding.recyclerSalas.visibility = View.GONE;

        lifecycleScope.launch {
            try {

                val sharedPref =
                    requireContext().getSharedPreferences("MeuAppPrefs", Context.MODE_PRIVATE)
                val codUsuario = sharedPref.getString("codigoUsuario", "")
                val administrador = sharedPref.getBoolean("administrador", false)

                salasList = lerSalas("OCUPADA", codUsuario.toString(), administrador);

                if (salasList.isNotEmpty()) {

                    salasAdapter =
                        SalasAdapter(
                            view.context,
                            salasList,
                            modoEdicao,
                            onItemClickListener = { sala ->

                                if (!modoEdicao) {
                                    Rotinas.mostrarSnackbar(
                                        parentView = binding.root,
                                        layoutAlerta = R.layout.toast_alerta,
                                        mensagem = "Sala sem disponibilidades no momento."
                                    )
                                } else {
                                    editarSala.putExtra("DADOS_SALA", sala)
                                    editarSala.putExtra("modoEdicao", modoEdicao)
                                    startActivity(editarSala)
                                    this@FragmentSalasOcupadas.requireActivity()
                                        .overridePendingTransition(
                                            R.anim.animate_fade_enter,
                                            R.anim.animate_fade_exit
                                        )
                                }
                            })

                    binding.recyclerSalas.adapter = salasAdapter
                    binding.recyclerSalas.layoutManager =
                        StaggeredGridLayoutManager(
                            3,
                            StaggeredGridLayoutManager.VERTICAL
                        )
                } else {
//                Rotinas.mostrarSnackbar(
//                    parentView = binding.root,
//                    layoutAlerta = R.layout.toast_alerta,
//                    mensagem = "Nenhuma sala ocupada encontrada."
//                )
                    binding.labNenhumEncontrado.visibility = View.VISIBLE;

                }
            } finally {
                binding.loading.visibility = View.GONE;
                binding.recyclerSalas.visibility = View.VISIBLE;
            }
        }
    }
}