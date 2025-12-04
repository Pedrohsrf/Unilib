package com.example.unilib.Notificacoes;

import android.content.Context
import android.content.Intent
import android.os.Bundle;
import android.view.View
import android.widget.ImageView
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
import com.example.unilib.databinding.ActivityHistoricoNotificacoesBinding
import com.example.unilib.databinding.ActivityNotificacoesBinding
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoricoNotificacoes : BaseActivity() {

    private lateinit var binding: ActivityHistoricoNotificacoesBinding
    private lateinit var voltar2: ImageView
    private lateinit var notificacoes: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        notificacoes = Intent(this, Notificacoes::class.java)
        binding = ActivityHistoricoNotificacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        voltar2 = binding.btnVoltarNot2

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
                val notificacoesList = lerNotificacoes(codigoUsuario.toString(), true)

                if (notificacoesList.isNotEmpty()) {
                    val adapter = NotificationListAdapter(
                        context = this@HistoricoNotificacoes,
                        notificacoes = notificacoesList,
                        lifecycleScope,
                        onListEmpty = {
                            binding.tvNenhumEcontrado.visibility = View.VISIBLE
                        }
                    )

                    binding.recyclerview.adapter = adapter
                    binding.recyclerview.layoutManager =
                        LinearLayoutManager(this@HistoricoNotificacoes)
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
        voltar2.setOnClickListener {
            finish()
//            startActivity(notificacoes)
//            this.overridePendingTransition(
//                R.anim.animate_fade_enter,
//                R.anim.animate_fade_exit
//            )
        }


    }
}