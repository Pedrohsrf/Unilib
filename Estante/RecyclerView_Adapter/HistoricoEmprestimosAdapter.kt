package com.example.unilib.Estante.RecyclerView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Emprestimo
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.LivrosItem4Binding
import com.example.unilib.R
import java.util.Calendar

class HistoricoEmprestimosAdapter(
    private val context: Context, private val emprestimos: List<Emprestimo>
) :
    RecyclerView.Adapter<HistoricoEmprestimosAdapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: LivrosItem4Binding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(emprestimo: Emprestimo) {
            // 1. Título do Livro
            itemBinding.nomeEvento.text = emprestimo.livro.nome

            // 2. Autor do Livro (Corrigido, antes estava mostrando a data)
            itemBinding.dataEvento.text = emprestimo.livro.autor

            // 3. Capa do Livro (Corrigido, estava faltando)
            itemBinding.imageEvento.setImageBitmap(Rotinas.Base64ToImage(emprestimo.livro.capa))

            // 4. Data de Devolução (Corrigido, agora usa a rotina simples)
            val dataDevolucaoFormatada = Rotinas.formatarData(emprestimo.dataDevolucao, "dd/MM/yyyy")

            itemBinding.valDataDevolucao.text = dataDevolucaoFormatada
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LivrosItem4Binding.inflate(
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
        holder.bind(emprestimos[position])
    }
}