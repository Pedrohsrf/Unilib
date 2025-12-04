package com.example.unilib.Reviews;

import android.content.Intent
import android.os.Bundle;

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Administracao
import com.example.unilib.Classes.Review
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.Usuario
import com.example.unilib.Emprestimos.RecyclerView_Adapter.GerenciarEmprestimosAdapter
import com.example.unilib.Inicio.RecyclerView_Adapter.ReviewsAdapter
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.BaseActivity

import com.example.unilib.R;
import com.example.unilib.Reviews.Fragments.FragmentReviews
import com.example.unilib.Reviews.Fragments.FragmentReviewsSuspeitas
import com.example.unilib.RodapeFM
import com.example.unilib.databinding.ActivityGerenciarReviewsBinding
import kotlinx.coroutines.launch
import kotlin.random.Random

class GerenciarReviews : BaseActivity() {
    private lateinit var binding : ActivityGerenciarReviewsBinding
    private lateinit var listUsuarios : MutableList<Usuario>
    private lateinit var listReviews : MutableList<Review>



    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        binding = ActivityGerenciarReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root);
    }

    override fun onStart() {
        super.onStart()

        val navegarAdministracao = Intent(this, RodapeFM::class.java)
        navegarAdministracao.putExtra("ID_DESTINO", R.id.nav_administracao)

        replaceFragment(FragmentReviews())

        binding.btnTodas.setOnClickListener {
            binding.btnTodas.setBackgroundDrawable(this.getDrawable(R.drawable.botao_salas_selecionado))
            binding.btnSuspeitas.setBackgroundDrawable(this.getDrawable(R.drawable.botao_salas_deselecionado))
            binding.btnTodas.setTextColor(Rotinas.getColor(this, R.color.white))
            binding.btnSuspeitas.setTextColor(
                Rotinas.getColor(
                    this,
                    R.color.cinza_ativo
                )
            )
            binding.btnTodas.alpha = 1F
            binding.btnSuspeitas.alpha = 0.4F
            replaceFragment(FragmentReviews())
        }

        binding.btnSuspeitas.setOnClickListener {
            binding.btnSuspeitas.setBackgroundDrawable(this.getDrawable(R.drawable.botao_suspeitas_selecionado))
            binding.btnTodas.setBackgroundDrawable(this.getDrawable(R.drawable.botao_salas_deselecionado))
            binding.btnSuspeitas.setTextColor(Rotinas.getColor(this, R.color.white))
            binding.btnTodas.setTextColor(Rotinas.getColor(this, R.color.cinza_ativo))

            binding.btnSuspeitas.alpha = 1F
            binding.btnTodas.alpha = 0.4F

            replaceFragment(FragmentReviewsSuspeitas())
        }
        
        binding.btnVoltarAdministracao.setOnClickListener {
            startActivity(navegarAdministracao)
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )

        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.layoutFragment, fragment)
        transaction.commit()
    }

}