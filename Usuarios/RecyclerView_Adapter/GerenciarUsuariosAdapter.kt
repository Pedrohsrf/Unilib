package com.example.unilib.Usuarios.RecyclerView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Evento
import com.example.unilib.Classes.Rotinas.Base64ToImage
import com.example.unilib.Classes.Usuario
import com.example.unilib.databinding.UsuarioItemBinding

class GerenciarUsuariosAdapter(
    private val context: Context,
    private val usuarios: MutableList<Usuario>,
    private val listener: OnUsuarioClickListener
) : RecyclerView.Adapter<GerenciarUsuariosAdapter.ViewHolder>() {

    interface OnUsuarioClickListener {
        fun onItemClick(usuario: Usuario)
        fun onDeleteClick(usuario: Usuario, position: Int)
    }

    inner class ViewHolder(val bind: UsuarioItemBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun bind(usuario: Usuario) {


            bind.nomeUsuario.text = usuario.nome
            bind.emailUsuario.text = usuario.email

            val bitmap = Base64ToImage(usuario.fotoPerfil)
            if (bitmap != null)
                bind.imgFotoUsuario.setImageBitmap(bitmap)


            bind.root.setOnClickListener {
                listener.onItemClick(usuario)
            }


            bind.btnExcluir.setOnClickListener {
                listener.onDeleteClick(usuario, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            UsuarioItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = usuarios.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(usuarios[position])
    }

    fun removerItem(pos: Int) {
        usuarios.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun atualizarListaUsuario(novaLista: MutableList<Usuario>) {
        this.usuarios.clear()
        this.usuarios.addAll(novaLista)

        notifyDataSetChanged()
    }
}
