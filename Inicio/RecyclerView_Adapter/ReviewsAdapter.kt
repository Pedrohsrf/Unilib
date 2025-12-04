package com.example.unilib.Inicio.RecyclerView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Review
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Rotinas.Base64ToImage
import com.example.unilib.Classes.RotinasBD.lerLivro
import com.example.unilib.R
import com.example.unilib.databinding.ReviewItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ReviewsAdapter(
    private val context: Context, private val reviews: List<Review>,
    private val onItemClickListener: ((Review) -> Unit)? = null,
    private val scope: CoroutineScope,
) :
    RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: ReviewItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(review: Review) {
            itemBinding.btnDeletar.setOnClickListener {
                onItemClickListener?.let { it1 -> it1(review) }
            }

            if (review.suspeita) {
                itemBinding.cardPrincipal.setStrokeColor(
                    Rotinas.getColor(
                        context,
                        R.color.stroke_suspeito
                    )
                )
//                itemBinding.comentario.setTextColor(Rotinas.getColor(context, R.color.comentario_suspeito))
//                itemBinding.comentario.alpha = 0.8F
            }

            if (review.gerenciavel) {
                itemBinding.btnDeletar.visibility = View.VISIBLE;
            } else {
                itemBinding.btnDeletar.visibility = View.GONE;
            }

            scope.launch {
                val dadoslivro = lerLivro(review.livroCodigo)

                itemBinding.nomelivroreview.text = dadoslivro.nome;
                itemBinding.imgFotoUsuario.setImageBitmap(Base64ToImage(review.usuario.fotoPerfil));
                itemBinding.usuario.text = review.usuario.nome;
                itemBinding.comentario.text = review.comentario;
                itemBinding.data.text = review.dataPublicacao;
                itemBinding.qtdEstrelas.text = review.nota.toString();
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ReviewItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reviews[position])
    }
}