package com.example.unilib.Salas.RecyclerView_Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Sala
import com.example.unilib.R
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.SalaItemBinding

class SalasAdapter(
    private val context: Context,
    private val salas: List<Sala>,
    private var modoEdicao: Boolean,
    private val onItemClickListener: (Sala) -> Unit
) :
    RecyclerView.Adapter<SalasAdapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: SalaItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(sala: Sala) {
            itemBinding.layoutSala.setOnClickListener {
                onItemClickListener(sala)
            }

            if (sala.reservada == true) {
                itemBinding.iconeReservada.visibility = View.VISIBLE
            }

            if (modoEdicao) {
                itemBinding.btnFlutuante.visibility = View.VISIBLE;
            } else {
                itemBinding.btnFlutuante.visibility = View.GONE;
            }

            var contexto = itemBinding.root.context;

            itemBinding.numSala.text = sala.numero.padStart(2, "0".first())

            if (sala.status == "DISPONIVEL") {
                itemBinding.iconeSala.setImageResource(R.drawable.sala_disponivel_icon)
            } else if (sala.status == "OCUPADA") {
                itemBinding.iconeSala.setImageResource(R.drawable.sala_ocupada_icon)
            } else {
                itemBinding.iconeSala.setImageResource(R.drawable.sala_manutencao_icon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SalaItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return salas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(salas[position])
    }

    fun modoEdicao(edicao: Boolean) {
        this.modoEdicao = edicao

        notifyDataSetChanged()
    }
}