package com.example.unilib.Notificacoes.RecyclerView_Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Notificacao
import com.example.unilib.Classes.Rotinas.Base64ToImage
import com.example.unilib.Classes.RotinasBD.visualizarNotificacao
import com.example.unilib.Emprestimos.GerenciarEmprestimos
import com.example.unilib.Notificacoes.Notificacoes
import com.example.unilib.Reviews.GerenciarReviews
import com.example.unilib.databinding.NotificationsItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NotificationListAdapter(
    private val context: Context,
    private val notificacoes: MutableList<Notificacao>,
    private val scope: CoroutineScope,
    private val onListEmpty: () -> Unit
) : RecyclerView.Adapter<NotificationListAdapter.ViewHolder>() {

    class ViewHolder(
        private var binding: NotificationsItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        var contexto = binding.root.context;
        fun bind(notificacao: Notificacao) {

            binding.categoria.text = notificacao.titulo
            binding.descricao.text = notificacao.descricao
            binding.data.text = notificacao.data
            binding.imgIcone.setImageBitmap(Base64ToImage(notificacao.icone))
            if (notificacao.redirecionamento !== "") {
                binding.botao.visibility = View.VISIBLE;
                var redirecionamento = notificacao.redirecionamento.split(";")
                var telaDestino = redirecionamento[0]
                var codigoDestino = redirecionamento[1]
                var tituloBotao = "Ir até ${telaDestino}"

                binding.botao.text = tituloBotao;

                if (telaDestino.equals("Empréstimos")) {
                    binding.botao.setOnClickListener {
                        contexto.startActivity(
                            Intent(
                                contexto,
                                GerenciarEmprestimos::class.java
                            ).putExtra("CODIGO_EMPRESTIMO", codigoDestino)
                        )
                    }
                } else if (telaDestino.equals("Reviews")) {
                    binding.botao.setOnClickListener {
                        contexto.startActivity(
                            Intent(
                                contexto,
                                GerenciarReviews::class.java
                            ).putExtra("CODIGO_REVIEWS", codigoDestino)
                        )
                    }
                }
            } else {
                binding.botao.visibility = View.GONE;
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder =
        ViewHolder(
            NotificationsItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = notificacoes.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificacao = notificacoes[position]
        holder.bind(notificacao)
    }

    fun deleteItem(position: Int) {
        var codigoNotificacao = notificacoes[position].codigo
        var codigoUsuario = notificacoes[position].usuario
        scope.launch {
            notificacoes.removeAt(position)

            if (notificacoes.isEmpty()) {
                onListEmpty()
            }

            visualizarNotificacao(codigoNotificacao, codigoUsuario)
            notifyItemRemoved(position)
        }
    }
}