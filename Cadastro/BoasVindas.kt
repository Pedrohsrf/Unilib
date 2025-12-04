package com.example.unilib.Cadastro

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.example.unilib.Classes.BaseActivity
import com.example.unilib.RodapeFM
import com.example.unilib.R

class BoasVindas : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boas_vindas)

        val intentRecebida = intent;
        val usuarioCadastrado = intentRecebida.getStringExtra("USUARIO_CADASTRADO")

        val navegarTelaInicial = Intent(this, RodapeFM::class.java);

        findViewById<TextView>(R.id.labNome).text = usuarioCadastrado;

        findViewById<TextView?>(R.id.btnContinuarBoasVindas).setOnClickListener {
            startActivity(navegarTelaInicial);
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

    }
}