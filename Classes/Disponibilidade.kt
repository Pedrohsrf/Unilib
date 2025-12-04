package com.example.unilib.Classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Disponibilidade(
    val horarios: List<Horario>,
    val dia: String
) : Parcelable {
}