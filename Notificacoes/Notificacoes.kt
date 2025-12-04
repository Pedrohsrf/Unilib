package com.example.unilib.Notificacoes;

import android.content.Context
import android.content.Intent
import android.os.Bundle;
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper

import com.example.unilib.Classes.BaseActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import com.dotlottie.dlplayer.Mode
import com.example.unilib.Classes.Notificacao
import com.example.unilib.Classes.RotinasBD.lerNotificacoes
import com.example.unilib.Notificacoes.RecyclerView_Adapter.NotificationListAdapter
import com.example.unilib.Notificacoes.RecyclerView_Adapter.SwipeToDeleteCallback

import com.example.unilib.R;
import com.example.unilib.RodapeFM
import com.example.unilib.databinding.ActivityInicioBinding
import com.example.unilib.databinding.ActivityNotificacoesBinding
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Notificacoes : BaseActivity() {

    private lateinit var binding: ActivityNotificacoesBinding
    private lateinit var historico: TextView
    private lateinit var voltar: ImageView
    private lateinit var historiconot: Intent
    private lateinit var navegarInicio: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        historiconot = Intent(this, HistoricoNotificacoes::class.java)
        navegarInicio = Intent(this, RodapeFM::class.java)
        binding = ActivityNotificacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        historico = binding.txtHistorico
        voltar = binding.btnVoltarNot

        val sharedPref = getSharedPreferences("MeuAppPrefs", Context.MODE_PRIVATE)
        val codigoUsuario = sharedPref.getString("codigoUsuario", null)

        val config = Config.Builder().autoplay(true).speed(1f).loop(true)
            .source(DotLottieSource.Asset("loading_preto.json")).playMode(Mode.FORWARD)
            .useFrameInterpolation(true).build()

        val dotLottieAnimationView = binding.loading

        dotLottieAnimationView.load(config)
        binding.loading.visibility = View.VISIBLE;

        lifecycleScope.launch {
            try {
                val notificacoesList = lerNotificacoes(codigoUsuario.toString(), false)

                if (notificacoesList.isNotEmpty()) {

                    val adapter = NotificationListAdapter(
                        context = this@Notificacoes,
                        notificacoes = notificacoesList,
                        lifecycleScope,
                        onListEmpty = {
                            binding.tvNenhumEcontrado.visibility = View.VISIBLE
                        }
                    )

                    val swipeHandler = SwipeToDeleteCallback(adapter)
                    val itemTouchHelper = ItemTouchHelper(swipeHandler)
                    itemTouchHelper.attachToRecyclerView(binding.recyclerview)

                    binding.recyclerview.adapter = adapter
                    binding.recyclerview.layoutManager = LinearLayoutManager(this@Notificacoes)
                } else {
                    binding.tvNenhumEcontrado.visibility = View.VISIBLE
                }
            } finally {
                binding.loading.visibility = View.GONE;
            }
        }
    }

    override fun onStart() {
        super.onStart()
        historico.setOnClickListener {
            startActivity(historiconot)
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

        voltar.setOnClickListener {
            finish()
//            startActivity(navegarInicio)
//            this.overridePendingTransition(
//                R.anim.animate_fade_enter,
//                R.anim.animate_fade_exit
//            )
        }
    }
}