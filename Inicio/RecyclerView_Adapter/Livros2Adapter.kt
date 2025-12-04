package com.example.unilib.Inicio.RecyclerView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Emprestimo
import com.example.unilib.R
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.LivrosItem2Binding
import com.example.unilib.Classes.Livro
import com.example.unilib.Classes.Rotinas.addDaysToDate
import com.example.unilib.Classes.Rotinas.calcularDiferencaEntreDatas
import com.example.unilib.Classes.RotinasBD.lerLivro
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit
import java.util.Date

class Livros2Adapter(
    private val context: Context,
    private val emprestimos: MutableList<Emprestimo>,
    private val livros: MutableList<Livro>,
    private val onItemClickListener: (Livro) -> Unit,
    private val onFavoritoClickListener: ((Livro, Int) -> Unit)? = null
) :
    RecyclerView.Adapter<Livros2Adapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: LivrosItem2Binding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        var contexto = itemBinding.root.context;

        fun bind(emprestimo: Emprestimo, livro: Livro) {
            itemBinding.imageEvento.setOnClickListener {
                onItemClickListener(livro)
            }

            if (onFavoritoClickListener !== null) {
                itemBinding.bgFavoritado2.setOnClickListener {
                    onFavoritoClickListener?.let { it1 -> it1(livro, adapterPosition) }
                }
            }

            itemBinding.nomeEvento.text = livro.nome

            val estatisticasLivro = Rotinas.calcularEstatisticasAvaliacao(livro.reviews);
            val diasRestantes = calcularDiferencaEntreDatas(
                Date(),
                emprestimo.dataLimite,
                ChronoUnit.DAYS
            ).toInt();

            itemBinding.diasRestantes.setText(diasRestantes.toString() + " dias restantes")
            itemBinding.avaliacaoNota2.text =
                String.format("%.1f", estatisticasLivro.mediaGeral);
            itemBinding.imageEvento.setImageBitmap(Rotinas.Base64ToImage(livro.capa))

            val totalDias = 7

            if (diasRestantes == 0) {
                itemBinding.progressoEmprestimo.progress = 0
            } else {
                var progresso = (100 * diasRestantes) / totalDias
                itemBinding.progressoEmprestimo.progress = progresso
            }

            if (!livro.favoritado!!) {
                itemBinding.bgFavoritado2.setCardBackgroundColor(
                    Rotinas.getColor(
                        contexto,
                        R.color.bg_favoritado_deselecionado
                    )
                );
                itemBinding.favoritadoIcon2.setImageResource(R.drawable.estrela_deselecionada)
            } else {
                itemBinding.bgFavoritado2.setCardBackgroundColor(
                    Rotinas.getColor(
                        contexto,
                        R.color.bg_favoritado_selecionado
                    )
                );
                itemBinding.favoritadoIcon2.setImageResource(R.drawable.estrela_selecionada)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LivrosItem2Binding.inflate(
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
        val livro = livros[position]
        holder.bind(emprestimo, livro)
    }

    fun atualizarListaLivros(novaLista: MutableList<Livro>) {
        this.livros.clear()
        this.livros.addAll(novaLista)

        notifyDataSetChanged()
    }
}