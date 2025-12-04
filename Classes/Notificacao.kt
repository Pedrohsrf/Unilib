package com.example.unilib.Classes

import android.content.Intent

data class Notificacao(
        val codigo: String,
        val titulo: String,
        val descricao: String,
        val data: String,
        val icone: String,
        val redirecionamento: String = "",
        val usuario: String
)