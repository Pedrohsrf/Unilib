package com.example.unilib.Livros.RecyclerView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.R
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.LivrosItem2Binding
import com.example.unilib.Classes.Livro
import com.example.unilib.databinding.LivrosItem5Binding

class GerenciarLivrosAdapter(
    private val context: Context,
    private val livros: MutableList<Livro>,
    private val onEditarClickListener: ((Livro) -> Unit)? = null,
    private val onDeletarClickListener: ((Livro) -> Unit)? = null
) :
    RecyclerView.Adapter<GerenciarLivrosAdapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: LivrosItem5Binding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        var contexto = itemBinding.root.context;


        fun bind(livro: Livro) {
            itemBinding.btnEditar.setOnClickListener{
                onEditarClickListener?.let { it1 -> it1(livro) }
            }

            itemBinding.btnDeletar.setOnClickListener{
                onDeletarClickListener?.let { it1 -> it1(livro) }
            }

            itemBinding.nomeLivro.text = livro.nome;
            itemBinding.autorLivro.text = livro.autor;
            itemBinding.capaLivro.setImageBitmap(Rotinas.Base64ToImage(livro.capa));
            if (livro.gerenciavel == 1) {
                itemBinding.espacoBotoes.visibility = View.GONE;
                itemBinding.btnEditar.visibility = View.GONE;
                itemBinding.btnDeletar.visibility = View.VISIBLE;
            } else if(livro.gerenciavel == 2) {
                itemBinding.espacoBotoes.visibility = View.VISIBLE;
                itemBinding.btnEditar.visibility = View.VISIBLE;
                itemBinding.btnDeletar.visibility = View.VISIBLE;
            } else {
                itemBinding.btnEditar.visibility = View.GONE;
                itemBinding.btnDeletar.visibility = View.GONE;
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LivrosItem5Binding.inflate(
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
        val livro = livros[position]
        holder.bind(livro)
    }

    fun atualizarLista(novaLista: MutableList<Livro>) {
        this.livros.clear()
        this.livros.addAll(novaLista)

        notifyDataSetChanged()
    }
}