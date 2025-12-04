package com.example.unilib.Perfil

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

import com.example.unilib.Classes.BaseActivity;

import com.example.unilib.R;
import com.example.unilib.RedefinirSenha.OpcoesRedefinicao
import com.example.unilib.RedefinirSenha.RedefinirViaEmail.InserirCodigoEmail
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unilib.Classes.Rotinas
import com.example.unilib.Classes.RotinasBD
import com.example.unilib.Classes.RotinasAuth // Importar RotinasAuth
import DialogCallback
import com.example.unilib.Classes.RotinasAuth.atualizarEmailAuth

import com.example.unilib.RedefinirSenha.RedefinirViaEmail.InserirEmail
import com.example.unilib.RodapeFM
import com.example.unilib.databinding.ActivityEditarPerfilBinding
import kotlinx.coroutines.launch
import java.io.InputStream

class EditarPerfil : BaseActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private lateinit var txtNome: EditText
    private lateinit var txtEmail: EditText
    private lateinit var imgFotoPerfil: ImageView

    private var fotoBitmap: Bitmap? = null
    private var fotoBase64: String? = null
    private var fotoAlterada = false

    private val REQUEST_IMAGE_PICK = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navegarRedefinicaoSenha = Intent(this, InserirEmail::class.java)
        val navegarPerfil = Intent(this, RodapeFM::class.java)
        val sharedPref = getSharedPreferences("MeuAppPrefs", Context.MODE_PRIVATE)

        txtNome = binding.edEditarNome
        txtEmail = binding.edEditarEmail
        imgFotoPerfil = binding.imgEditarFotoPerfil

        navegarPerfil.putExtra("ID_DESTINO", R.id.navigation_perfil)

        val emailTelefone = sharedPref.getString("emailUsuario", null)
        val UID = sharedPref.getString("UID", null)
        val codigoUsuario = sharedPref.getString("codigoUsuario", null)
        val nomeUsuario = sharedPref.getString("nomeUsuario", null)

        if (emailTelefone != null && UID != null) {
            carregarInformacoesUsuario(emailTelefone, UID)
        } else {
            binding.edEditarNome.setText("Usuário não encontrado")
            binding.edEditarEmail.setText("")
        }

        txtNome.setText(nomeUsuario)
        txtEmail.setText(emailTelefone)

        // Botão Voltar
        findViewById<ImageView>(R.id.btnVoltar3).setOnClickListener {
            finish()
            this.overridePendingTransition(
                R.anim.animate_fade_enter,
                R.anim.animate_fade_exit
            )
        }

        // Botão Editar Foto
        binding.btnEditarFotoPerfil.setOnClickListener {
            abrirGaleria()
        }

        // Botão Alterar Senha
        binding.btnAlterarSenha.setOnClickListener {
            try {
                redefinirSenha()
            } catch (e: Exception) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = e.message.toString(),
                    dialogoAtivo = null
                )
            }
        }

        // Botão Salvar
        binding.btnSalvar.setOnClickListener {

            if (codigoUsuario == null) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Código do usuário não encontrado.",
                    dialogoAtivo = null
                )
                return@setOnClickListener
            }


            val nome = binding.edEditarNome.text.toString()
            val email = binding.edEditarEmail.text.toString()

            if (nome.isEmpty() || email.isEmpty()) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Preencha todos os campos."
                )
                return@setOnClickListener
            }

            Rotinas.mostrarDialogo(
                contexto = this,
                pLayoutDialogo = R.layout.dialogo_medio,
                pMensagemPrincipal = "Salvar Alterações?",
                pMensagemSecundaria = "Essa ação não pode ser revertida. Todos os dados desse usuário serão atualizados.",
                pBtnSim = "SALVAR",
                pBtnNao = "CANCELAR",
                pDottieAnimation = "",
                object : DialogCallback {
                    override fun onSimClicked() {
                        val loadingProcessando = Rotinas.mostrarLoading(
                            contexto = this@EditarPerfil,
                            pLayoutDialogo = R.layout.activity_loading_dialog,
                            pViewMensagem = R.id.loading_mensagem,
                            pMensagem = "Processando...",
                            pDottieAnimation = "loading_icon.json"
                        )

                        lifecycleScope.launch {
                            try {
                                val fotoBase64ParaSalvar = if (fotoAlterada) {
                                    fotoBitmap?.let { Rotinas.ImageToBase64(it) }
                                } else {
                                    fotoBase64
                                }

                                var sucessoAuth: Boolean
                                if (emailTelefone != email) {
                                    sucessoAuth = atualizarEmailAuth(email)

                                    if (sucessoAuth) {
                                        val sucessoFirestore = RotinasBD.atualizarUsuario(
                                            codigoUsuario,
                                            nome,
                                            email,
                                            fotoBase64ParaSalvar ?: ""
                                        )

                                        if (sucessoFirestore) {
                                            with(sharedPref.edit()) {
                                                putString("nomeUsuario", nome)
                                                putString("emailUsuario", email)
                                                apply()
                                            }

                                            Rotinas.mostrarSnackbar(
                                                parentView = binding.root,
                                                layoutAlerta = R.layout.toast_informacao,
                                                mensagem = "Email de atualização enviado para ${email}!",
                                                dialogoAtivo = null
                                            )
                                        }
                                    }
                                }
                                Rotinas.fecharDialogo(loadingProcessando)
                            } catch (e: Exception) {
                                Rotinas.fecharDialogo(loadingProcessando)

                                Rotinas.mostrarSnackbar(
                                    parentView = binding.root,
                                    layoutAlerta = R.layout.toast_alerta,
                                    mensagem = "Erro: ${e.message}",
                                    dialogoAtivo = null
                                )
                            }
                        }
                    }

                    override fun onNaoClicked() {
                    }
                }
            )
        }
    }

    private fun carregarInformacoesUsuario(emailTelefone: String, UID: String) {
        lifecycleScope.launch {
            val sharedPref = getSharedPreferences("MeuAppPrefs", Context.MODE_PRIVATE)

            try {
                val usuarioDoc = RotinasBD.lerUsuario(UID)
                val nomeUsuario = usuarioDoc.getString("nomeCompleto")
                val emailUsuario = sharedPref.getString("emailUsuario", null)
                val fotoPerfilBase64 = usuarioDoc.getString("fotoPerfil")


                runOnUiThread {
                    txtNome.setText(nomeUsuario)
                    txtEmail.setText(emailUsuario)

                    fotoPerfilBase64?.let { base64 ->
                        fotoBase64 = base64
                        Rotinas.Base64ToImage(base64)?.let { bitmap ->
                            imgFotoPerfil.setImageBitmap(bitmap)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("EditarPerfil", "Erro ao carregar informações do usuário: ", e)
                runOnUiThread {
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_alerta,
                        mensagem = "Erro ao carregar informações do perfil.",
                        dialogoAtivo = null
                    )
                }
            }
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(uri)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    imgFotoPerfil.setImageBitmap(bitmap)

                    fotoBitmap = bitmap
                    fotoAlterada = true
                } catch (e: Exception) {
                    Rotinas.mostrarSnackbar(
                        parentView = binding.root,
                        layoutAlerta = R.layout.toast_alerta,
                        mensagem = "Erro ao carregar a imagem.",
                        dialogoAtivo = null
                    )
                    e.printStackTrace()
                }
            }
        }
    }


    private fun redefinirSenha() {
        lifecycleScope.launch {
            val sucesso = RotinasAuth.redefinirSenha()
            if (sucesso) {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_informacao,
                    mensagem = "Instruções de redefinição enviadas para o email ${binding.edEditarEmail.text.toString()}.",
                    dialogoAtivo = null
                )
            } else {
                Rotinas.mostrarSnackbar(
                    parentView = binding.root,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Erro ao enviar instruções de redefinição.",
                    dialogoAtivo = null
                )
            }
        }
    }
}