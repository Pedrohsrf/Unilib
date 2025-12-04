package com.example.unilib.RedefinirSenha

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.unilib.Classes.BaseActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.unilib.Classes.Rotinas
import com.example.unilib.R
import com.example.unilib.Login.Login
import com.example.unilib.RedefinirSenha.RedefinirViaEmail.InserirEmail
import com.example.unilib.RedefinirSenha.RedefinirViaSMS.InserirTelefone
import com.example.unilib.databinding.ActivityOpcoesRedefinicaoBinding

class OpcoesRedefinicao : BaseActivity() {
    private lateinit var binding : ActivityOpcoesRedefinicaoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcoesRedefinicaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val navegarLogin = Intent(this, Login::class.java);
        val navegarInserirEmail = Intent(this, InserirEmail::class.java);
        val navegarInserirTel = Intent(this, InserirTelefone::class.java);

        var opcaoEscolhida = "Email";

       binding.imgSelectedEmail.visibility = View.VISIBLE;
       binding.imgSelectedSMS.visibility = View.GONE;

       binding.btnVoltar.setOnClickListener {
            startActivity(navegarLogin);
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

       binding.btnContinuarBoasVindas.setOnClickListener {
            if (opcaoEscolhida == "Email") {
                startActivity(navegarInserirEmail);
                this.overridePendingTransition(
                    R.anim.animate_fade_enter,
                    R.anim.animate_fade_exit
                )
            } else {
                startActivity(navegarInserirTel);
                this.overridePendingTransition(
                    R.anim.animate_fade_enter,
                    R.anim.animate_fade_exit
                )
            }
        }


       binding.btnViaEmail.setOnClickListener {
            opcaoEscolhida = "Email";
           binding.btnViaEmail.background = ContextCompat.getDrawable(this, R.drawable.botao_com_check_ativo);
           binding.btnViaSMS.background = ContextCompat.getDrawable(this, R.drawable.botao_com_check_inativo);
           binding.imgSelectedEmail.visibility = View.VISIBLE;
           binding.imgSelectedSMS.visibility = View.GONE;
           binding.labViaEmail.typeface = ResourcesCompat.getFont(this, R.font.nunito_bold);
           binding.labViaSMS.typeface = ResourcesCompat.getFont(this, R.font.nunito_semibold);
           binding.labViaEmail.setTextColor(ContextCompat.getColor(this, R.color.cinza_ativo));
           binding.labViaSMS.setTextColor(ContextCompat.getColor(this, R.color.cinza_inativo));
           binding.imgViaEmail.background = ContextCompat.getDrawable(this, R.drawable.quadrado_icone_ativo);
           binding.imgViaSMS.background = ContextCompat.getDrawable(this, R.drawable.quadrado_icone_inativo);
           binding.imgViaEmail.setColorFilter(Rotinas.getColor(this, R.color.white))
           binding.imgViaSMS.setColorFilter(Rotinas.getColor(this, R.color.opcaoDeselecionada))
        }

       binding.btnViaSMS.setOnClickListener {
            opcaoEscolhida = "SMS";
           binding.btnViaSMS.background = ContextCompat.getDrawable(this, R.drawable.botao_com_check_ativo);
           binding.btnViaEmail.background = ContextCompat.getDrawable(this, R.drawable.botao_com_check_inativo);
           binding.imgSelectedSMS.visibility = View.VISIBLE;
           binding.imgSelectedEmail.visibility = View.GONE;
           binding.labViaSMS.typeface = ResourcesCompat.getFont(this, R.font.nunito_bold);
           binding.labViaEmail.typeface = ResourcesCompat.getFont(this, R.font.nunito_semibold);
           binding.labViaSMS.setTextColor(ContextCompat.getColor(this, R.color.cinza_ativo));
           binding.labViaEmail.setTextColor(ContextCompat.getColor(this, R.color.cinza_inativo));
           binding.imgViaSMS.background = ContextCompat.getDrawable(this, R.drawable.quadrado_icone_ativo);
           binding.imgViaEmail.background = ContextCompat.getDrawable(this, R.drawable.quadrado_icone_inativo);
           binding.imgViaSMS.setColorFilter(Rotinas.getColor(this, R.color.white))
           binding.imgViaEmail.setColorFilter(Rotinas.getColor(this, R.color.opcaoDeselecionada))
        }

    }
}