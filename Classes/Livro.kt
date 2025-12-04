package com.example.unilib.Classes

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Livro(
    var codigo: String = "",
    var nome: String,
    var autor: String,
    var capa: String,
    var sinopse: String,
    var genero: String,
    var qtdAvaliacoes: Int,
    var qtdLeituras: Int,
    var editora: String,
    var anoPublicacao: String,
    var idioma: String,
    var iSBN: String,
    var localizacao: String,
    var qtdPaginas: String,
    var copiasDisponiveis: Int,
    var reviews: MutableList<Review> = mutableListOf(),
    var favoritado: Boolean? = false,
    var gerenciavel: Int = 0,
    var emprestado: Boolean? = false,
) : Parcelable {
}