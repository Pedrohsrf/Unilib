package com.example.unilib

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.BaseFragment
import com.example.unilib.Inicio.Inicio
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.ActivityAcessibilidadeBinding
import com.example.unilib.Classes.FontScaleManager
import com.example.unilib.Classes.RotinasBD.editarAcessibilidadePrefs
import com.example.unilib.Classes.RotinasBD.lerAcessibilidadePref
import kotlinx.coroutines.launch

class Acessibilidade : BaseFragment() {

    private lateinit var btnVoltar: ImageView
    private lateinit var minusButton: CardView
    private lateinit var addButton: CardView
    private lateinit var labPorcentagem: TextView
    private lateinit var btnSalvar: Button

    private lateinit var allTexts: List<TextView>
    private lateinit var binding: ActivityAcessibilidadeBinding
    private lateinit var baseTextSizes: MutableMap<Int, Float>
    private lateinit var prefs: android.content.SharedPreferences


    private var fontPreset = "font100"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityAcessibilidadeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val sharedPref = requireContext().getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
        val UID = sharedPref.getString("UID", "")
        prefs = requireContext().getSharedPreferences("acessibilidade", Context.MODE_PRIVATE)

        btnVoltar = binding.btnVoltarAcessibilidade
        minusButton = binding.btnMenos
        addButton = binding.btnMais
        labPorcentagem = binding.labPorcentagem
        btnSalvar = binding.btnSalvar

        allTexts = listOf(
            binding.labAcessibilidade,
            binding.labTamanhoFonte,
            binding.labPorcentagem
        )

        baseTextSizes = mutableMapOf()
        allTexts.forEach { textView ->
            val baseSizeSp = textView.textSize / resources.displayMetrics.scaledDensity
            baseTextSizes[textView.id] = baseSizeSp
        }

        var acessibilidadePrefs = requireContext().getSharedPreferences("acessibilidade", Context.MODE_PRIVATE)

        fontPreset = acessibilidadePrefs.getString("fontPreset", "font100").toString()

        FontScaleManager.updateActivityWithPreset(requireActivity(), fontPreset)

        atualizarPreview()

        minusButton.setOnClickListener {

            fontPreset = when (fontPreset) {
                "font150" -> "font140"
                "font140" -> "font130"
                "font130" -> "font120"
                "font120" -> "font110"
                "font110" -> "font100"
                "font100" -> "font90"
                "font90" -> "font80"
                "font80" -> "font70"
                else -> "font70"
            }
            atualizarPreview()
        }

        addButton.setOnClickListener {

            fontPreset = when (fontPreset) {
                "font70" -> "font80"
                "font80" -> "font90"
                "font90" -> "font100"
                "font100" -> "font110"
                "font110" -> "font120"
                "font120" -> "font130"
                "font130" -> "font140"
                "font140" -> "font150"
                else -> "font150"
            }
            atualizarPreview()
        }

        btnSalvar.setOnClickListener {
            lifecycleScope.launch {

                prefs.edit().putString("fontPreset", fontPreset).apply()


                val loading = Rotinas.mostrarLoading(
                    contexto = requireContext(),
                    pLayoutDialogo = R.layout.activity_loading_dialog,
                    pViewMensagem = R.id.loading_mensagem,
                    pMensagem = "Salvando...",
                    pDottieAnimation = "loading_icon.json"
                )


                editarAcessibilidadePrefs(UID.toString(), fontPreset)

                loading.dismiss()

                FontScaleManager.updateActivityWithPreset(requireActivity(), fontPreset)
                requireActivity().recreate()

                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_informacao,
                    mensagem = "Configurações salvas com sucesso!",
                    dialogoAtivo = loading
                )

            }
        }

        btnVoltar.setOnClickListener {
            parentFragmentManager.popBackStack()
//            val navegarInicio = Intent(requireContext(), RodapeFM::class.java)
//            startActivity(navegarInicio)
//            this.requireActivity().overridePendingTransition(
//                R.anim.animate_fade_enter,
//                R.anim.animate_fade_exit
//            )
        }
    }

    private fun atualizarPreview() {

        val porcentagem = when (fontPreset) {
            "font70" -> 70
            "font80" -> 80
            "font90" -> 90
            "font100" -> 100
            "font110" -> 110
            "font120" -> 120
            "font130" -> 130
            "font140" -> 140
            "font150" -> 150
            else -> 100
        }
        labPorcentagem.text = "$porcentagem %"

        val scale = when (fontPreset) {
            "font70" -> 0.7f
            "font80" -> 0.8f
            "font90" -> 0.9f
            "font100" -> 1.0f
            "font110" -> 1.1f
            "font120" -> 1.2f
            "font130" -> 1.3f
            "font140" -> 1.4f
            "font150" -> 1.5f
            else -> 1.0f
        }

        allTexts.forEach { textView ->
            val baseSize = baseTextSizes[textView.id] ?: return@forEach
            val newSize = baseSize * scale
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
        }
    }
}

