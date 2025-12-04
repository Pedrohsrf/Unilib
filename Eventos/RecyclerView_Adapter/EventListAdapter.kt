package com.example.unilib.Notificacoes.RecyclerView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Evento
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.EventoItemBinding

class EventListAdapter(
    private val context: Context,
    private val eventos: MutableList<Evento>,
    private val onEditarClick: (Evento) -> Unit,
    private val onDeletarClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventListAdapter.ViewHolder>() {

    class ViewHolder(
        private val binding: EventoItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            evento: Evento,
            onEditarClick: (Evento) -> Unit,
            onDeletarClick: (Evento) -> Unit
        ) {
            binding.nomeEvento.text = evento.nomeEvento
            binding.dataEvento.text = evento.dataEvento

            val bitmap = Rotinas.Base64ToImage(evento.Imagem)
            bitmap?.let { binding.imageEvento.setImageBitmap(it) }

            binding.btnEditar.visibility = if (evento.gerenciavel == true) View.VISIBLE else View.GONE
            binding.btnDeletar.visibility = if (evento.gerenciavel == true) View.VISIBLE else View.GONE

            binding.btnEditar.setOnClickListener {
                onEditarClick(evento)
            }
            binding.btnDeletar.setOnClickListener {
                onDeletarClick(evento)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            EventoItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = eventos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(eventos[position], onEditarClick, onDeletarClick)
    }
    fun atualizarListaEventos(novaLista: MutableList<Evento>) {
        this.eventos.clear()
        this.eventos.addAll(novaLista)

        notifyDataSetChanged()
    }
}
