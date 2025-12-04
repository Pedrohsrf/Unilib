package com.example.unilib.Salas.RecyclerView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Disponibilidade
import com.example.unilib.Classes.Horario
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Sala
import com.example.unilib.R
import com.example.unilib.databinding.HorariosItemBinding
import com.example.unilib.databinding.SalaItemBinding

class HorariosAdapter(
    private val context: Context,
    private var horarios: List<Horario>,
    private val onItemClickListener: (Horario) -> Unit
) : RecyclerView.Adapter<HorariosAdapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: HorariosItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(horario: Horario) {
            itemBinding.btnReservar.setOnClickListener {
                onItemClickListener(horario)
            }

            itemBinding.intervaloHora.text =
                horario.horaIni + "-" + horario.horaFim;

            if (horario.usuario?.isBlank() == true) {
                itemBinding.btnReservar.text = "RESERVAR"

                itemBinding.btnReservar.setTextColor(ContextCompat.getColor(context, R.color.white))

                itemBinding.btnReservar.background =
                    ContextCompat.getDrawable(context, R.drawable.botao_padrao)
            } else {
                itemBinding.btnReservar.text = "RESERVADO"

                itemBinding.btnReservar.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.cinza_ativo
                    )
                )

                itemBinding.btnReservar.background =
                    ContextCompat.getDrawable(context, R.drawable.botao_reservado)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HorariosItemBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return horarios.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(horarios[position])
    }

    fun updateHorarios(novosHorarios: List<Horario>) {
        this.horarios = novosHorarios
        notifyDataSetChanged()
    }
}