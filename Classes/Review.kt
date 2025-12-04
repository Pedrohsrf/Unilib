package com.example.unilib.Classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    val codigo : String,
    val usuario: Usuario,
    val dataPublicacao: String,
    val nota: Int,
    val comentario: String,
    val suspeita: Boolean,
    val gerenciavel: Boolean,
    val livroCodigo: String
) : Parcelable {
}