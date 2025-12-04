package com.example.unilib.Emprestimos.RecyclerView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Emprestimo
import com.example.unilib.Classes.Evento
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.Rotinas.Base64ToImage
import com.example.unilib.R
import com.example.unilib.databinding.EmprestimoItemBinding

class GerenciarEmprestimosAdapter(
    private val context: Context,
    private val emprestimos: MutableList<Emprestimo>,
    private val onItemClickListener: (Emprestimo, Int) -> Unit
) :
    RecyclerView.Adapter<GerenciarEmprestimosAdapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: EmprestimoItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        // 💡 NOTA: Removido 'var contexto', pois 'context' da classe já está disponível.

        fun bind(emprestimo: Emprestimo) {
            // Configura o clique (já estava certo)
            itemBinding.imgStatusEmprestimo.setOnClickListener {
                onItemClickListener.let { it(emprestimo, adapterPosition) }
            }

            // Configura os dados (já estava certo)
            itemBinding.imgFotoUsuario.setImageBitmap(Base64ToImage(emprestimo.usuario.fotoPerfil));
            itemBinding.nomeUsuario.text = emprestimo.usuario.nome;
            itemBinding.nomeLivro.text = emprestimo.livro.nome;

            // --- Lógica de Ícones (Verde -> Preto) ---

            if (emprestimo.status == "Ativo") {
                // 1. ESTADO FINALIZADO (PRETO)
                itemBinding.imgStatusEmprestimo.setImageResource(R.drawable.ic_emprestimo_finalizado) // ⬅️ Ícone Preto
                itemBinding.cardStatusEmprestimo.setCardBackgroundColor(
                    Rotinas.getColor(
                        context,
                        R.color.cinza_ativo // Cor Cinza
                    )
                )
            } else {
                // 2. ESTADO PENDENTE (VERDE)

                // ⬇️ MUDE O NOME DO DRAWABLE AQUI
                itemBinding.imgStatusEmprestimo.setImageResource(R.drawable.check_icon)

                itemBinding.cardStatusEmprestimo.setCardBackgroundColor(
                    Rotinas.getColor(
                        context,R.color.concluir_emprestimo
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            EmprestimoItemBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return emprestimos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val emprestimo = emprestimos[position]
        holder.bind(emprestimo)
    }
}