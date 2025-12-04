package com.example.unilib.Salas.RecyclerView_Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Disponibilidade
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Rotinas.formatarData
import com.example.unilib.Classes.Rotinas.stringToDate
import com.example.unilib.Classes.Sala
import com.example.unilib.R
import com.example.unilib.databinding.DataItemBinding
import com.example.unilib.databinding.SalaItemBinding
import java.util.Calendar
import java.util.Date

class DatasAdapter(
    private val context: Context,
    private val disponibilidades: List<Disponibilidade>,
    val onItemClickListener: (Disponibilidade, Int) -> Unit,
    private val onItemLongClickListener: (Disponibilidade, Int) -> Unit
) : RecyclerView.Adapter<DatasAdapter.ViewHolder>() {

    private var selectedPosition = 0

    inner class ViewHolder(private val itemBinding: DataItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(disponibilidade: Disponibilidade, position: Int) {
            itemBinding.btnData.setOnClickListener {

                val clickedPosition = bindingAdapterPosition

                if (clickedPosition != RecyclerView.NO_POSITION) {
                    setSelected(clickedPosition)

                    onItemClickListener(disponibilidades[clickedPosition], clickedPosition)
                }
            }

            itemBinding.btnData.setOnLongClickListener {
                onItemLongClickListener(disponibilidade, position)
                true
            }

            val anoAtual = Calendar.getInstance().get(Calendar.YEAR)

            val dataDoItem: Date? = stringToDate(disponibilidade.dia, "dd/MM/yyyy")
            val calendarItem = Calendar.getInstance()
            calendarItem.time = dataDoItem

            val anoDoItem = calendarItem.get(Calendar.YEAR)

            if (anoDoItem > anoAtual) {
                itemBinding.btnData.text =
                    formatarData(stringToDate(disponibilidade.dia, "dd/MM/yyyy"), "dd/MM/yyyy")
            } else {
                itemBinding.btnData.text =
                    formatarData(stringToDate(disponibilidade.dia, "dd/MM/yyyy"), "dd/MM")
            }

            if (position == selectedPosition) {
                itemBinding.btnData.setBackgroundResource(R.drawable.botao_data_selecionado)
                itemBinding.btnData.setTextColor(Rotinas.getColor(context, R.color.white))
            } else {
                itemBinding.btnData.setBackgroundResource(R.drawable.botao_data_deselecionado)
                itemBinding.btnData.setTextColor(
                    Rotinas.getColor(
                        context, R.color.data_deselecionada
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataItemBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return disponibilidades.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(disponibilidades[position], position)
    }

    fun setSelected(position: Int) {
        if (position == selectedPosition) return

        val oldSelectedPosition = selectedPosition

        selectedPosition = position

        notifyItemChanged(oldSelectedPosition)
        notifyItemChanged(selectedPosition)
    }
}