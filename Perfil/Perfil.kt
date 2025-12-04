package com.example.unilib.Perfil

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.unilib.Classes.BaseFragment
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.R
import com.example.unilib.databinding.ActivityPerfilBinding
import kotlinx.coroutines.launch

class Perfil : BaseFragment() {
    lateinit var txtNome: TextView
    lateinit var txtEmail: TextView
    lateinit var btnEditarPerfil: ImageView
    lateinit var imgFotoPerfil: ImageView
    lateinit var binding: ActivityPerfilBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        binding.adminSwitch.visibility = View.GONE
        binding.btnVoltarAcessibilidade2.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtNome = binding.txtNomeCompleto
        txtEmail = binding.txtemailUsuario
        btnEditarPerfil = binding.imgEditar
        imgFotoPerfil = binding.imgFotoPerfil

        val sharedPref = requireContext().getSharedPreferences("MeuAppPrefs", Context.MODE_PRIVATE)
        val UID = sharedPref.getString("UID", null)
        val emailTelefone = sharedPref.getString("emailUsuario", null)
        val codigoUsuario = sharedPref.getString("codigoUsuario", null)
        val nomeUsuario = sharedPref.getString("nomeUsuario", null)

        if (emailTelefone != null && codigoUsuario != null && UID != null) {
            carregarInformacoesUsuario(emailTelefone, codigoUsuario, UID)
        } else {
            binding.txtNomeCompleto.text = "Usuário não encontrado"
            binding.txtemailUsuario.text = ""
            // imgFotoPerfil.setImageResource(R.drawable.ic_perfil_padrao) placeholder
        }

        txtNome.text = nomeUsuario
        txtEmail.text = emailTelefone
    }

    override fun onStart() {
        super.onStart()
        btnEditarPerfil.setOnClickListener {
            val editarPerfilIntent = Intent(requireContext(), EditarPerfil::class.java)
            startActivity(editarPerfilIntent)
            this.requireActivity().overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }
    }

    private fun carregarInformacoesUsuario(emailTelefone: String, codigoUsuario: String, UID: String) {
        lifecycleScope.launch {
            val usuarioDoc = RotinasBD.lerUsuario(UID)
            val sharedPref = requireContext().getSharedPreferences("MeuAppPrefs", Context.MODE_PRIVATE)

            val nomeUsuario = usuarioDoc.getString("nomeCompleto")
            val emailUsuario = sharedPref.getString("emailUsuario", null)
            val fotoPerfilBase64 = usuarioDoc.getString("fotoPerfil") // Pegando a foto do Firestore

            val emprestimosAtivos = RotinasBD.lerEmprestimos(codigoUsuario, "Ativo")
            val emprestimosFinalizados = RotinasBD.lerEmprestimos(codigoUsuario, "Finalizado")
            val reviewsPublicadas = RotinasBD.lerReviews(codigoUsuario, false)
            val reviewsRejeitadas = RotinasBD.lerReviews(codigoUsuario, true)
            val (listaFavoritos, listaDesejos) = RotinasBD.lerListas(codigoUsuario)

            binding.txtNomeCompleto.text = nomeUsuario
            binding.txtemailUsuario.text = emailUsuario

            fotoPerfilBase64?.let { base64 ->
                base64ToBitmap(base64)?.let { bitmap ->
                    imgFotoPerfil.setImageBitmap(bitmap)
                }
            } ?: run {
                // imgFotoPerfil.setImageResource(R.drawable.) placeholder se não houver foto
            }

            binding.txtAtivos.text = emprestimosAtivos.size.toString()
            binding.txtFinalizados.text = emprestimosFinalizados.size.toString()
            binding.txtPublicadas.text = reviewsPublicadas.size.toString()
            binding.txtRejeitadas.text = reviewsRejeitadas.size.toString()
            binding.txtFavoritos.text = listaFavoritos.size.toString()
            binding.txtListadeDesejos.text = listaDesejos.size.toString()
        }
    }

    // Função para converter Base64 em Bitmap
    private fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}