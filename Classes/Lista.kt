package com.example.unilib.Classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Lista(
    val nomeLista: String,
    val livros: List<Livro>,
    val cor: Int,
) : Parcelable {
}