package com.example.unilib.Reviews.RecyclerView_Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Review
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Rotinas.Base64ToImage
import com.example.unilib.R
import com.example.unilib.databinding.ReviewItemBinding

class GerenciarReviewsAdapter(
    private val context: Context,
    private val reviews: List<Review>,
    private val onItemClickListener: ((Review) -> Unit)? = null
) :
    RecyclerView.Adapter<GerenciarReviewsAdapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: ReviewItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        var contexto = itemBinding.root.context;

        fun bind(review: Review) {
            Log.i("Suspeita", review.suspeita.toString())
            itemBinding.btnDeletar.setOnClickListener {
                onItemClickListener?.let { it1 -> it1(review) }
            }
            if (review.suspeita) {
                itemBinding.cardPrincipal.setStrokeColor(Rotinas.getColor(context, R.color.stroke_suspeito))
                itemBinding.cardPrincipal.setCardBackgroundColor(Rotinas.getColor(context, R.color.bg_suspeito))
                itemBinding.cardPrincipal.setBackgroundColor(Rotinas.getColor(context, R.color.bg_suspeito))
                itemBinding.constraintPrincipal.setBackgroundColor(Rotinas.getColor(context, R.color.bg_suspeito))
            }


            itemBinding.imgFotoUsuario.setImageBitmap(Base64ToImage(review.usuario.fotoPerfil));
            if (review.gerenciavel) {
                itemBinding.btnDeletar.visibility = View.VISIBLE;
            } else {
                itemBinding.btnDeletar.visibility = View.GONE;
            }
            itemBinding.nomelivroreview.text = review.livroCodigo
            itemBinding.imgFotoUsuario.setImageBitmap(Base64ToImage(review.usuario.fotoPerfil));
            itemBinding.usuario.text = review.usuario.nome;
            itemBinding.comentario.text = review.comentario;
            itemBinding.data.text = review.dataPublicacao;
            itemBinding.qtdEstrelas.text = review.nota.toString();
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
        val review = reviews[position]
        holder.bind(review)
    }
}