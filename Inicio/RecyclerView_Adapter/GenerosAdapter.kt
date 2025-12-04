package com.example.unilib.Inicio.RecyclerView_Adapter

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.R
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.GenerosItemBinding
import com.example.unilib.Classes.Genero
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Rotinas.Base64ToImage

class GenerosAdapter(
    private val context: Context, private val generos: List<Genero>,
    private val onItemClickListener: (Genero) -> Unit
) :
    RecyclerView.Adapter<GenerosAdapter.ViewHolder>() {
    private var selectedPosition = 0

    inner class ViewHolder(private val itemBinding: GenerosItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(genero: Genero, position: Int) {
            var contexto = itemBinding.root.context;

            itemBinding.generoIcone.setOnClickListener {
                val clickedPosition = bindingAdapterPosition

                if (clickedPosition != RecyclerView.NO_POSITION) {
                    setSelected(clickedPosition)

                    onItemClickListener(generos[clickedPosition])
                }
            }

            itemBinding.generoText.text = genero.nome
            if (position == selectedPosition) {
                itemBinding.circuloGenero.setCardBackgroundColor(
                    Rotinas.getColor(
                        contexto,
                        R.color.generoSelecionado
                    )
                );
                itemBinding.generoText.setTypeface(ResourcesCompat.getFont(contexto, R.font.nunito_bold))
                itemBinding.generoText.setTextColor(
                    Rotinas.getColor(
                        contexto,
                        R.color.generoSelecionado
                    )
                )
                itemBinding.generoIcone.setColorFilter(Rotinas.getColor(contexto, R.color.white))
                itemBinding.generoIcone.setImageBitmap(Base64ToImage(genero.icone))
            } else {
                itemBinding.generoText.setTypeface(ResourcesCompat.getFont(contexto, R.font.nunito_regular))
                itemBinding.generoText.setTypeface(null, Typeface.NORMAL)
                itemBinding.generoText.setTextColor(
                    Rotinas.getColor(
                        contexto,
                        R.color.generoSelecionado
                    )
                )
                itemBinding.generoText.alpha = 0.5F;
                itemBinding.circuloGenero.setCardBackgroundColor(
                    Rotinas.getColor(
                        contexto,
                        R.color.generoDeselecionado
                    )
                );
                itemBinding.generoIcone.setColorFilter(Rotinas.getColor(contexto, R.color.genero_deselecionado))
                itemBinding.generoIcone.setImageBitmap(Base64ToImage(genero.icone))

                 }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            GenerosItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return generos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(generos[position], position)
    }

    fun setSelected(position: Int) {
        if (position == selectedPosition) return

        val oldSelectedPosition = selectedPosition

        selectedPosition = position

        notifyItemChanged(oldSelectedPosition)
        notifyItemChanged(selectedPosition)
    }
}