package com.example.unilib.Classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Usuario(
    val nome: String,
    val email: String,
    val fotoPerfil: String,
    var codigo: String = ""
) : Parcelable {
}