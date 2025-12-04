package com.example.unilib.Classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Horario(
    var codigo: String,
    val data: String,
    val horaIni: String,
    val horaFim: String,
    var usuario: String? = ""
) : Parcelable {
}