package com.example.unilib.Classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Evento(
    var codigo: String,
    var nomeEvento: String,
    var dataEvento: String,
    var Imagem: String,
    var gerenciavel : Boolean
) : Parcelable {
}