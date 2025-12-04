package com.example.unilib.Reviews.Fragments

import DialogCallback
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.unilib.Classes.BaseFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Review
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.R
import com.example.unilib.Reviews.RecyclerView_Adapter.GerenciarReviewsAdapter
import com.example.unilib.databinding.FragmentReviewsBinding
import kotlinx.coroutines.launch


class FragmentReviews : BaseFragment() {
    private lateinit var binding: FragmentReviewsBinding;
    private lateinit var listReviews: MutableList<Review>
    private lateinit var reviewsAdapter: GerenciarReviewsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReviewsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listReviews = mutableListOf()

        lifecycleScope.launch {

            listReviews = RotinasBD.lerTodasReviews()
            if (listReviews.isNotEmpty()) {
                reviewsAdapter = GerenciarReviewsAdapter(
                    requireContext(), listReviews,
                    onItemClickListener = { review ->
                        val deletarReview = Rotinas.mostrarDialogo(
                            contexto = requireContext(),
                            pLayoutDialogo = R.layout.dialogo_critico,
                            pMensagemPrincipal = "Confirmar exclusão?",
                            pMensagemSecundaria = "Deseja realmente deletar essa avaliação?",
                            pBtnSim = "DELETAR",
                            pBtnNao = "CANCELAR",
                            pDottieAnimation = "",
                            object : DialogCallback {
                                override fun onSimClicked() {
                                    val loadingReservando = Rotinas.mostrarLoading(
                                        contexto = requireContext(),
                                        pLayoutDialogo = R.layout.activity_loading_dialog,
                                        pViewMensagem = R.id.loading_mensagem,
                                        pMensagem = "Deletando...",
                                        pDottieAnimation = "loading_icon.json"
                                    )
                                    lifecycleScope.launch {
                                        RotinasBD.deletarReview(review.codigo)

                                        listReviews.removeIf { it.codigo == review.codigo }
                                        reviewsAdapter.notifyDataSetChanged()

                                        Rotinas.mostrarSnackbar(
                                            parentView = binding.root,
                                            layoutAlerta = R.layout.toast_informacao,
                                            mensagem = "Review deletada com sucesso!",
                                            dialogoAtivo = loadingReservando
                                        )
                                    }
                                }

                                override fun onNaoClicked() {

                                }

                            })
                    })

                binding.recyclerview.adapter = reviewsAdapter;
                binding.recyclerview.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            } else {
                binding.recyclerview.visibility = View.GONE
                binding.labNenhumEncontrado.visibility = View.VISIBLE
            }
        }
    }
}