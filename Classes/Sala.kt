package com.example.unilib.Classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Sala(
    val numero: String,
    val disponibilidades: List<Disponibilidade>,
    var status: String,
    val reservada: Boolean? = false
) : Parcelable {
}