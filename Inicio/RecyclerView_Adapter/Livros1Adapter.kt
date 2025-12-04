package com.example.unilib.Inicio.RecyclerView_Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.R
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.LivrosItem1Binding
import com.example.unilib.Classes.Livro

class Livros1Adapter(
    private val context: Context,
    private var livros: MutableList<Livro>,
    private val onItemClickListener: (Livro) -> Unit,
    private val onFavoritoClickListener: ((Livro, Int) -> Unit)? = null
) :
    RecyclerView.Adapter<Livros1Adapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: LivrosItem1Binding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        var contexto = itemBinding.root.context

        fun bind(livro: Livro) {

            itemBinding.capaLivro1.setOnClickListener {
                onItemClickListener(livro)
            }

            if (onFavoritoClickListener !== null) {
                itemBinding.bgFavoritado1.setOnClickListener {
                    onFavoritoClickListener?.let { it1 -> it1(livro, adapterPosition) }
                }
            }

            val estatisticasLivro = Rotinas.calcularEstatisticasAvaliacao(livro.reviews);
            itemBinding.avaliacaoNota.text = String.format("%.1f", estatisticasLivro.mediaGeral);

            itemBinding.capaLivro1.setImageBitmap(Rotinas.Base64ToImage(livro.capa))
            if (!livro.emprestado!!) {
                itemBinding.emprestado.visibility = GONE;
            } else {
                itemBinding.emprestado.visibility = VISIBLE;
            }

            if (!livro.favoritado!!) {
                itemBinding.bgFavoritado1.setCardBackgroundColor(
                    Rotinas.getColor(
                        contexto,
                        R.color.bg_favoritado_deselecionado
                    )
                );
                itemBinding.favoritadoIcon1.setImageResource(R.drawable.estrela_deselecionada)
            } else {
                itemBinding.bgFavoritado1.setCardBackgroundColor(
                    Rotinas.getColor(
                        contexto,
                        R.color.bg_favoritado_selecionado
                    )
                );
                itemBinding.favoritadoIcon1.setImageResource(R.drawable.estrela_selecionada)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LivrosItem1Binding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return livros.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var livro = livros[position]
        holder.bind(livro)
    }

    fun atualizarListaLivros(novaLista: MutableList<Livro>) {
        this.livros.clear()
        this.livros.addAll(novaLista)

        notifyDataSetChanged()
    }

}

