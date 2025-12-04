package com.example.unilib.ChatIA.RecyclerView_Adapter

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.R
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.GenerosItemBinding
import com.example.unilib.Classes.Genero
import com.example.unilib.databinding.MensagemItemBinding

class ChatAdapter(
    private val context: Context,
    private val mensagensUsuario: List<String>,
    private val mensagensBot: List<String>
) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(private val itemBinding: MensagemItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(mensagensUsuario: String, mensagensBot: String) {
            var contexto = itemBinding.root.context;

            itemBinding.valMsgBot.text = mensagensBot
            itemBinding.valMsgUsuario.text = mensagensUsuario
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MensagemItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mensagensUsuario.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mensagensUsuario[position], mensagensBot[position])
    }

    fun refresh() {
        notifyDataSetChanged()
    }
}