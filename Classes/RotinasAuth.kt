package com.example.unilib.Classes

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.RotinasBD.fb
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.String

object RotinasAuth {
    lateinit var auth: FirebaseAuth

    suspend fun redefinirSenha(email: String? = ""): Boolean {
        auth = Firebase.auth;
        var emailUsuario = "";

        if (email != null) {
            if (email.isNotBlank()){
                emailUsuario = email;
            }
            else{
                emailUsuario = auth.currentUser?.email.toString();
            }
        }
        Log.d("email", "emailUsuario: ${emailUsuario}")


            if (emailUsuario != null) {
                auth.sendPasswordResetEmail(emailUsuario)
                    .await()
            }

            return true

    }

    suspend fun cadastrarUsuario(
        email: String, senha: String
    ): String {
        auth = Firebase.auth;

        val result = auth.createUserWithEmailAndPassword(email, senha)
            .await()

        val user = result.user

       return user?.uid.toString()
    }

    suspend fun atualizarEmailAuth(novoEmail: String): Boolean {
        val currentUser = auth.currentUser

        return try {
            if (currentUser != null) {
                currentUser.verifyBeforeUpdateEmail(novoEmail).await()
                true
            } else {
                // Nenhum usuário logado
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun logarUsuario(
        email: String, senha: String
    ): String {
        auth = Firebase.auth;

        val result = auth.signInWithEmailAndPassword(email, senha)
                .await()

        val user = result.user

        return user?.uid.toString()
    }
}