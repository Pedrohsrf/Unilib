package com.example.unilib.Estante.RecyclerView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Lista
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.ListaItemBinding

class ListasAdapter(
    private val context: Context, private val listas: List<Lista>,
    private val onItemClickListener: (Lista) -> Unit,
) :
    RecyclerView.Adapter<ListasAdapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: ListaItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(lista: Lista) {
            val contexto = itemBinding.root.context
            itemBinding.cardBtnAbrir.setOnClickListener {
                onItemClickListener(lista)
            }
            itemBinding.nomeLista.text = lista.nomeLista
            val qtdLivros = lista.livros.size
            itemBinding.qtdLivros.text = if (qtdLivros == 1) "1 livro" else "$qtdLivros livros"

            // --- VERIFICAÇÕES DE SEGURANÇA (A CORREÇÃO ESTÁ AQUI) ---

            // 1. Limpa as imagens primeiro
            itemBinding.capaLivro1.setImageBitmap(null)
            itemBinding.imageEvento.setImageBitmap(null)
            itemBinding.capaLivro3.setImageBitmap(null)

            // 2. Verifica se a lista TEM pelo menos 1 livro
            if (qtdLivros > 0 && lista.livros[0].capa.isNotEmpty()) {
                itemBinding.capaLivro1.setImageBitmap(Rotinas.Base64ToImage(lista.livros[0].capa))
                itemBinding.capaLivro1.visibility = View.VISIBLE
            } else {
                itemBinding.capaLivro1.visibility = View.INVISIBLE
            }

            // 3. Verifica se a lista TEM pelo menos 2 livros
            if (qtdLivros > 1 && lista.livros[1].capa.isNotEmpty()) {
                itemBinding.imageEvento.setImageBitmap(Rotinas.Base64ToImage(lista.livros[1].capa))
                itemBinding.imageEvento.visibility = View.VISIBLE
            } else {
                itemBinding.imageEvento.visibility = View.INVISIBLE
            }

            // 4. Verifica se a lista TEM pelo menos 3 livros
            if (qtdLivros > 2 && lista.livros[2].capa.isNotEmpty()) {
                itemBinding.capaLivro3.setImageBitmap(Rotinas.Base64ToImage(lista.livros[2].capa))
                itemBinding.capaLivro3.visibility = View.VISIBLE
            } else {
                itemBinding.capaLivro3.visibility = View.INVISIBLE
            }
            // --- FIM DA CORREÇÃO ---

            itemBinding.cartaoGeral.setCardBackgroundColor(Rotinas.getColor(context, lista.cor))
            itemBinding.cardBtnAbrir.setCardBackgroundColor(Rotinas.getColor(context, lista.cor))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListaItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }
    override fun getItemCount(): Int {
        return listas.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listas[position])
    }
}