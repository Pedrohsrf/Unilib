package com.example.unilib.Classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Emprestimo(
    var livro: Livro,
    var dataEmprestimo: Date,
    var dataDevolucao: Date?,
    var usuario: Usuario,
    var status: String,
    val dataLimite: Date // 💡 ADICIONE ESTA LINHA
) : Parcelable {
}