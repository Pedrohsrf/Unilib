package com.example.unilib.Usuarios

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.BaseActivity
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.databinding.ActivityPerfilBinding
import kotlinx.coroutines.launch

class VisualizarUsuario : BaseActivity() {

    private lateinit var binding: ActivityPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgEditar.visibility = View.GONE

        binding.btnVoltarAcessibilidade2.visibility = View.VISIBLE
        binding.btnVoltarAcessibilidade2.setOnClickListener {
            val intent = Intent(this, GerenciarUsuarios::class.java)
            startActivity(intent)
            finish()
        }

        val codigoUsuario = intent.getStringExtra("codigo")

        if (codigoUsuario != null) {
            carregarInformacoesUsuario(codigoUsuario)
        } else {
            binding.txtNomeCompleto.text = "Usuário não encontrado"
            binding.txtemailUsuario.text = ""
        }
    }

    private fun carregarInformacoesUsuario(codigo: String) {
        lifecycleScope.launch {

            val usuario = RotinasBD.lerUsuarioPorCodigo(codigo)

            if (usuario == null) {
                binding.txtNomeCompleto.text = "Usuário não encontrado"
                return@launch
            }

            val email = usuario.email
            val codigoUsuario = usuario.codigo

            val emprestimosAtivos = RotinasBD.lerEmprestimos(codigoUsuario, "Ativo")
            val emprestimosFinalizados = RotinasBD.lerEmprestimos(codigoUsuario, "Finalizado")

            val reviewsPublicadas = RotinasBD.lerReviews(codigoUsuario, false)
            val reviewsRejeitadas = RotinasBD.lerReviews(codigoUsuario, true)

            val (listaFavoritos, listaDesejos) = RotinasBD.lerListas(codigoUsuario)


            binding.txtNomeCompleto.text = usuario.nome
            binding.txtemailUsuario.text = usuario.email
            binding.txtAtivos.text = emprestimosAtivos.size.toString()
            binding.txtFinalizados.text = emprestimosFinalizados.size.toString()
            binding.txtPublicadas.text = reviewsPublicadas.size.toString()
            binding.txtRejeitadas.text = reviewsRejeitadas.size.toString()
            binding.txtFavoritos.text = listaFavoritos.size.toString()
            binding.txtListadeDesejos.text = listaDesejos.size.toString()


            val fotoPerfilBase64 = usuario.fotoPerfil
            if (!fotoPerfilBase64.isNullOrEmpty()) {
                base64ToBitmap(fotoPerfilBase64)?.let { bitmap ->
                    binding.imgFotoPerfil.setImageBitmap(bitmap)
                }
            }

            val isAdmin = RotinasBD.lerCampoDoUsuarioPorCampoCodigo(codigoUsuario, "administrador") as? Boolean ?: false
            binding.adminSwitch.isChecked = isAdmin

            binding.adminSwitch.setOnCheckedChangeListener { _, novoValor ->
                lifecycleScope.launch {
                    RotinasBD.atualizarCampoUsuarioPorCampoCodigo(codigoUsuario, "administrador", novoValor)
                }
            }
        }
    }

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


