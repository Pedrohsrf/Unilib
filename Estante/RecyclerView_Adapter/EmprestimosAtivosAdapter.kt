package com.example.unilib.Estante.RecyclerView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.unilib.Classes.Emprestimo
import com.example.unilib.R
import com.example.unilib.Classes.Rotinas
import com.example.unilib.databinding.LivrosItem3Binding
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class EmprestimosAtivosAdapter(
    private val context: Context, private val emprestimos: List<Emprestimo>
) :
    RecyclerView.Adapter<EmprestimosAtivosAdapter.ViewHolder>() {

    inner class ViewHolder(private val itemBinding: LivrosItem3Binding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(emprestimo: Emprestimo) {
            val contexto = itemBinding.root.context

            // --- INÍCIO DA CORREÇÃO DA LÓGICA DE DATA ---

            // 1. Obter as datas corretas (zerando o horário para comparações justas)
            val calHoje = Calendar.getInstance().apply {
                time = Date() // Pega "hoje"
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calLimite = Calendar.getInstance().apply {
                time = emprestimo.dataLimite // Pega a data limite vinda do Firebase
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calEmprestimo = Calendar.getInstance().apply {
                time = emprestimo.dataEmprestimo // Pega a data de início do empréstimo
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // 2. Calcular os dias restantes (Limite - Hoje)
            val diffMillisRestantes = calLimite.timeInMillis - calHoje.timeInMillis
            val diasRestantes = TimeUnit.DAYS.convert(diffMillisRestantes, TimeUnit.MILLISECONDS).toInt()

            // 3. Calcular o total de dias do empréstimo (Limite - Início)
            val diffMillisTotal = calLimite.timeInMillis - calEmprestimo.timeInMillis
            val totalDias = TimeUnit.DAYS.convert(diffMillisTotal, TimeUnit.MILLISECONDS).toInt()

            // --- FIM DA CORREÇÃO DA LÓGICA DE DATA ---


            // 4. Atualizar a UI com os dados corretos
            itemBinding.nomeLivroo.text = emprestimo.livro.nome
            itemBinding.autorLivroo.text = emprestimo.livro.autor
            itemBinding.imageEventoo.setImageBitmap(Rotinas.Base64ToImage(emprestimo.livro.capa))

            // Lógica da Barra de Progresso
            if (totalDias > 0) {
                val diasDecorridos = totalDias - diasRestantes

                val percentualDecorrido = (100 * diasDecorridos) / totalDias

                val progresso = 100 - percentualDecorrido

                itemBinding.progressoEmprestimo.visibility = View.VISIBLE
                itemBinding.progressoEmprestimo.progress = progresso.coerceIn(0, 100)
            } else {
                itemBinding.progressoEmprestimo.progress = 0
                itemBinding.progressoEmprestimo.visibility = View.GONE
            }

            // Lógica de Alertas e Mensagens
            itemBinding.diasRestantes.text = "$diasRestantes dias restantes"

            if (diasRestantes >= 4) {
                // Tudo OK
                itemBinding.diasRestantes.setTypeface(ResourcesCompat.getFont(context, R.font.nunito_regular))
                itemBinding.diasRestantes.setTextColor(Rotinas.getColor(context, R.color.cinza_status_texto))
                itemBinding.iconeAlerta.visibility = View.GONE
                itemBinding.mensagem.visibility = View.GONE
                itemBinding.progressoEmprestimo.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.barra_progresso)

            } else if (diasRestantes in 1..3) {
                // Alerta Médio (Faltam 1-3 dias)
                itemBinding.diasRestantes.setTypeface(ResourcesCompat.getFont(context, R.font.nunito_regular))
                itemBinding.diasRestantes.setTextColor(Rotinas.getColor(context, R.color.cinza_status_texto))
                itemBinding.iconeAlerta.visibility = View.VISIBLE
                itemBinding.bgIconeAlerta.setCardBackgroundColor(
                    Rotinas.getColor(context, R.color.alerta_media)
                )
                itemBinding.progressoEmprestimo.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.barra_progresso_medio)
                itemBinding.mensagem.visibility = View.GONE

            } else if (diasRestantes == 0) {
                // Vence Hoje
                itemBinding.diasRestantes.text = "Devolva hoje!"
                itemBinding.diasRestantes.setTypeface(ResourcesCompat.getFont(context, R.font.nunito_extrabold))
                itemBinding.diasRestantes.setTextColor(Rotinas.getColor(context, R.color.alerta_media))
                itemBinding.iconeAlerta.visibility = View.VISIBLE
                itemBinding.bgIconeAlerta.setCardBackgroundColor(
                    Rotinas.getColor(context, R.color.alerta_media)
                )
                itemBinding.mensagem.visibility = View.VISIBLE
                itemBinding.mensagem.text = "Hoje é o último dia para devolução do livro."
                itemBinding.mensagem.setTextColor(Rotinas.getColor(context, R.color.alerta_media))
                itemBinding.progressoEmprestimo.visibility = View.GONE

            } else {
                // Atrasado (diasRestantes < 0)
                itemBinding.diasRestantes.text = "Devolução atrasada!"
                itemBinding.diasRestantes.setTypeface(ResourcesCompat.getFont(context, R.font.nunito_extrabold))
                itemBinding.diasRestantes.setTextColor(Rotinas.getColor(context, R.color.alerta_critica))
                itemBinding.iconeAlerta.visibility = View.VISIBLE
                itemBinding.bgIconeAlerta.setCardBackgroundColor(
                    Rotinas.getColor(context, R.color.alerta_critica)
                )
                itemBinding.mensagem.visibility = View.VISIBLE
                itemBinding.mensagem.text = "Você está passível de receber multa por dia de atraso."
                itemBinding.mensagem.setTextColor(Rotinas.getColor(context, R.color.alerta_critica))
                itemBinding.progressoEmprestimo.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LivrosItem3Binding.inflate(
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