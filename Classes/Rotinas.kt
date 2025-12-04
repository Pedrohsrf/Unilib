package com.example.unilib.Classes

import DialogCallback
import DialogoSalaCallback
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.dotlottie.dlplayer.Mode
import com.example.unilib.R
import com.example.unilib.databinding.DialogoSelecionarListaBinding
import com.google.android.material.snackbar.Snackbar
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.lottiefiles.dotlottie.core.widget.DotLottieAnimation
import java.text.SimpleDateFormat
import android.util.Base64
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.Timestamp
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.text.Normalizer
import java.util.TimeZone

object Rotinas {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var onImagemSelecionada: ((android.graphics.Bitmap) -> Unit)? = null


    fun preencherEstrelas(
        estrela1: ImageView,
        estrela2: ImageView,
        estrela3: ImageView,
        estrela4: ImageView,
        estrela5: ImageView,
        media: Double
    ) {
        val estrelas: List<ImageView> = listOf(
            estrela1, estrela2, estrela3, estrela4, estrela5
        )

        for (i in estrelas.indices) {
            val estrela = estrelas[i]

            val notaDaEstrela = i + 1

            if (notaDaEstrela <= (media).toInt()) {
                estrela.alpha = 1.0f
            } else if ((notaDaEstrela == (media).toInt() + 1) && (((media).toInt() - media) > 0.0)) {
                estrela.alpha = (media).toInt() - media.toFloat();
            } else {
                estrela.alpha = 0.5f
            }
        }
    }

    data class DadosAvaliacao(
        val mediaGeral: Double, val qtdAvaliacoes: Int, val percentuaisPorNota: Map<Int, Double>
    )

    fun calcularEstatisticasAvaliacao(reviews: List<Review>): DadosAvaliacao {
        if (reviews.isEmpty()) {
            return DadosAvaliacao(
                mediaGeral = 0.0, qtdAvaliacoes = 0, percentuaisPorNota = emptyMap()
            )
        }

        val contagemTotal = reviews.size
        var somaTotalPontos = 0.0

        val contagemPorNota = mutableMapOf<Int, Int>()

        for (review in reviews) {
            val nota = review.nota

            if (nota in 1..5) {
                somaTotalPontos += review.nota

                contagemPorNota[nota] = contagemPorNota.getOrDefault(nota, 0) + 1
            }
        }

        val mediaGeral = somaTotalPontos / contagemTotal

        val formatoDecimal = DecimalFormat("#.#")

        val percentuaisPorNota = mutableMapOf<Int, Double>()

        for (nota in 5 downTo 1) {
            val contagem = contagemPorNota.getOrDefault(nota, 0)

            val percentual = (contagem.toDouble() / contagemTotal) * 100

            val percentualFormatado = formatoDecimal.format(percentual).replace(',', '.').toDouble()

            percentuaisPorNota[nota] = percentualFormatado
        }

        return DadosAvaliacao(
            mediaGeral = mediaGeral,
            qtdAvaliacoes = contagemTotal,
            percentuaisPorNota = percentuaisPorNota
        )
    }

    fun ImageViewtoBase64(imageView: ImageView): String? {
        val bitmap = imageView.drawable?.toBitmap()

        return ImageToBase64(bitmap);
    }

    fun ImageToBase64(imagem: Bitmap?): String? {
        if (imagem == null) {
            return null
        }

        return try {
            val outputStream = ByteArrayOutputStream()

            imagem.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            val byteArray = outputStream.toByteArray()

            Base64.encodeToString(byteArray, Base64.DEFAULT)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun Base64ToImage(pBase64: String): Bitmap? {
        try {
            val decodedBytes = Base64.decode(pBase64, Base64.DEFAULT)

            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        }
    }

    fun atualizarPrefs(contexto: Context, chave: String, valor: String) {
        val sharedPref = contexto.getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(chave, valor)
        editor.apply()
    }
    fun lerPreferencia(contexto: Context, chave: String): String {
        val sharedPref = contexto.getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
        return sharedPref.getString(chave, "") ?: ""
    }

    fun lerPreferenciaBoolean(contexto: Context, chave: String): Boolean {
        val sharedPref = contexto.getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
        return sharedPref.getBoolean(chave, false) // Retorna false por padrão
    }
    fun getColor(context: Context, corResId: Int): Int {
        return ContextCompat.getColor(context, corResId)
    }

    fun createDate(year: Int, month: Int, day: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    fun createDateTime(hour: Int, minute: Int, daysOffset: Int = 0): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getDate(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return calendar.time
    }

    fun calcularDiferencaEntreDatas(
        data1: Date,
        data2: Date,
        unit: ChronoUnit
    ): Long {
        val zoneId = ZoneId.systemDefault()

        val localDate1 = data1.toInstant().atZone(zoneId).toLocalDate()
        val localDate2 = data2.toInstant().atZone(zoneId).toLocalDate()

        return ChronoUnit.DAYS.between(localDate1, localDate2)
    }

    fun addDaysToDate(originalDate: Date, daysToAdd: Long): Date {
        val instant = originalDate.toInstant()

        val zonedDateTime = instant.atZone(ZoneId.systemDefault())

        val futureDateTime = zonedDateTime.plusDays(daysToAdd)

        return Date.from(futureDateTime.toInstant())
    }

    fun String.normalizeAndLowercase(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)

        val withoutAccents = normalized.replace("\\p{M}".toRegex(), "")

        return withoutAccents.toLowerCase()
    }

    fun mostrarLoading(
        contexto: Context,
        pLayoutDialogo: Int,
        pViewMensagem: Int,
        pMensagem: String,
        pDottieAnimation: String
    ): Dialog {
        val dialog = Dialog(contexto);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(pLayoutDialogo); // dialog.setContentView(R.layout.activity_loading_dialog);
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        val mensagem: TextView = dialog.findViewById(pViewMensagem)
        mensagem.text = pMensagem;

        if (pDottieAnimation !== "") {
            val config = Config.Builder()
                .autoplay(true)
                .speed(1f)
                .loop(true)
                .source(DotLottieSource.Asset(pDottieAnimation))
                .playMode(Mode.FORWARD)
                .useFrameInterpolation(true)
                .build()

            val dotLottieAnimationView =
                dialog.findViewById<DotLottieAnimation>(R.id.dot_lottie_view)

            dotLottieAnimationView.load(config)
        }

        dialog.show()

        return dialog;
    }

    fun criarDataCompleta(dataDDMM: String): Date? {
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"))

        val formatoBase = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatoBase.timeZone =
            TimeZone.getTimeZone("America/Sao_Paulo")

        val dataHora: Date? = try {
            formatoBase.parse("$dataDDMM/$anoAtual")
        } catch (e: Exception) {
            return null
        }

        if (dataHora != null) {
            calendar.time = dataHora
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.time // Retorna a Date correta
        }
        return null
    }

    fun Context.dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    fun formatarData(data: Date?, formato: String): String {
        val formatador = SimpleDateFormat(formato, Locale.getDefault())

        return formatador.format(data)
    }

    fun stringToDate(dataString: String, formato: String): Date? {
        val formatador = SimpleDateFormat(formato, Locale.getDefault())

        return try {
            formatador.parse(dataString)
        } catch (e: Exception) {
            println("Erro ao converter string para data: ${e.message}")
            null
        }
    }

    fun mostrarDialogoListas(
        contexto: Context,
        parentView: View,
        adicionarClickFavorito: () -> Unit,
        adicionarClickDesejo: () -> Unit
    ) {
        val dialog = Dialog(contexto);
        var favoritos = false;
        var listadesejos = false;

        val binding = DialogoSelecionarListaBinding.inflate(LayoutInflater.from(contexto))

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        dialog.show()

        binding.btnAdicionar.setOnClickListener {
            if (favoritos) {
                adicionarClickFavorito();
                dialog.dismiss();
            } else if (listadesejos) {
                adicionarClickDesejo();
                dialog.dismiss();
            } else {
                mostrarSnackbar(
                    parentView = parentView,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Por favor, selecione uma lista"
                )
            }
        }

        binding.btnFavoritos.setOnClickListener {
            favoritos = true;
            listadesejos = false;

            binding.btnFavoritos.background =
                ContextCompat.getDrawable(contexto, R.drawable.botao_com_check_ativo);
            binding.labFavoritos.typeface =
                ResourcesCompat.getFont(contexto, R.font.nunito_bold);
            binding.labFavoritos.setTextColor(
                ContextCompat.getColor(
                    contexto,
                    R.color.cinza_ativo
                )
            );

            binding.btnListaDesejos.background =
                ContextCompat.getDrawable(contexto, R.drawable.botao_com_check_inativo);
            binding.labListaDesejos.typeface =
                ResourcesCompat.getFont(contexto, R.font.nunito_medium);
            binding.labListaDesejos.setTextColor(
                ContextCompat.getColor(
                    contexto,
                    R.color.genero_deselecionado
                )
            );
        }

        binding.btnListaDesejos.setOnClickListener {
            favoritos = false;
            listadesejos = true;

            binding.btnFavoritos.background =
                ContextCompat.getDrawable(contexto, R.drawable.botao_com_check_inativo);
            binding.labFavoritos.typeface =
                ResourcesCompat.getFont(contexto, R.font.nunito_medium);
            binding.labFavoritos.setTextColor(
                ContextCompat.getColor(
                    contexto,
                    R.color.genero_deselecionado
                )
            );

            binding.btnListaDesejos.background =
                ContextCompat.getDrawable(contexto, R.drawable.botao_com_check_ativo);
            binding.labListaDesejos.typeface =
                ResourcesCompat.getFont(contexto, R.font.nunito_bold);
            binding.labListaDesejos.setTextColor(
                ContextCompat.getColor(
                    contexto,
                    R.color.cinza_ativo
                )
            );
        }
    }

    fun mostrarDialogoSala(
        contexto: Context,
        nomeSala: String,
        status: String,
        callback: DialogoSalaCallback
    ) {
        val dialog = Dialog(contexto);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialogo_sala); // dialog.setContentView(R.layout.activity_loading_dialog);
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        val btnManutencao: Button = dialog.findViewById(R.id.btnManutencao)

        val btnExcluir: Button = dialog.findViewById(R.id.btnExcluir)

        val labSala: TextView = dialog.findViewById(R.id.labSala)

        if (status == "MANUTENCAO") {
            btnManutencao.text = "FINALIZAR MANUTENÇÃO"
            btnManutencao.setTextColor(ContextCompat.getColor(contexto, R.color.white))
            btnManutencao.background = ContextCompat.getDrawable(contexto, R.drawable.botao_colocar_manutencao)
        }

        labSala.text = nomeSala;

        dialog.show()

        btnManutencao.setOnClickListener {
            callback.onManutencaoClicked()
            dialog.dismiss();
        }

        btnExcluir.setOnClickListener {
            callback.onExcluirClicked()
            dialog.dismiss();
        }
    }

    fun mostrarDialogo(
        contexto: Context,
        pLayoutDialogo: Int,
        pMensagemPrincipal: String,
        pMensagemSecundaria: String,
        pBtnSim: String,
        pBtnNao: String,
        pDottieAnimation: String,
        callback: DialogCallback
    ) {
        val dialog = Dialog(contexto);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(pLayoutDialogo); // dialog.setContentView(R.layout.activity_loading_dialog);
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        val mensagemPrincipal: TextView = dialog.findViewById(R.id.mensagem_principal)
        mensagemPrincipal.text = pMensagemPrincipal;

        val mensagemSecundaria: TextView = dialog.findViewById(R.id.mensagem_secundaria)
        mensagemSecundaria.text = pMensagemSecundaria;

        val btnSim: Button = dialog.findViewById(R.id.btnSim)
        btnSim.text = pBtnSim;

        val btnNao: Button = dialog.findViewById(R.id.btnNao)
        btnNao.text = pBtnNao;

        Log.i("btnNao", pBtnNao)
        Log.i("btnSim", pBtnSim)

        if (pBtnNao == "") {
            btnNao.visibility = View.GONE;
        } else if (pBtnSim == "") {
            throw Exception("É necessário pelo menos um botão.")
        }

        if (pDottieAnimation !== "") {
            val config = Config.Builder()
                .autoplay(true)
                .speed(1f)
                .loop(true)
                .source(DotLottieSource.Asset(pDottieAnimation))
                .playMode(Mode.FORWARD)
                .useFrameInterpolation(true)
                .build()

            val dotLottieAnimationView =
                dialog.findViewById<DotLottieAnimation>(R.id.dot_lottie_view)


            dotLottieAnimationView.load(config)
        }

        dialog.show()

        btnSim.setOnClickListener {
            callback.onSimClicked()
            dialog.dismiss();
        }

        btnNao.setOnClickListener {
            callback.onNaoClicked()
            dialog.dismiss();
        }
    }






    fun mostrarSnackbar(
        parentView: View,
        layoutAlerta: Int,
        mensagem: String,
        dialogoAtivo: Dialog? = null
    ) {

        val snackbar = Snackbar.make(
            parentView,
            "",
            Snackbar.LENGTH_LONG
        )

        val snackbarView = snackbar.view
        val snackbarLayout = snackbarView as ViewGroup

        snackbarLayout.setBackgroundColor(Color.TRANSPARENT)

        snackbarLayout.setPadding(0, 0, 0, 0)

        val textView =
            snackbarLayout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.visibility = View.INVISIBLE

        val customSnackView = LayoutInflater.from(parentView.context)
            .inflate(layoutAlerta, snackbarLayout, false)

        val customTextView = customSnackView.findViewById<TextView>(R.id.mensagem_alerta)
        customTextView.text = mensagem

        snackbarLayout.addView(customSnackView, 0)

        dialogoAtivo?.dismiss();
        snackbar.show()
    }

    fun timestampToString(timestamp: Timestamp): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(timestamp.toDate())
    }

    fun calcularDiasRestantes(dataEmprestimo: Date, prazoTotalDias: Int): Int {
        val calendarLimite = Calendar.getInstance().apply {
            time = dataEmprestimo
            add(Calendar.DAY_OF_YEAR, prazoTotalDias)
        }
        val dataLimite = calendarLimite.time

        val dataHoje = Date()

        val diffEmMillis = dataLimite.time - dataHoje.time

        val diffEmDias = TimeUnit.DAYS.convert(diffEmMillis, TimeUnit.MILLISECONDS)

        return diffEmDias.toInt()
    }

    fun mostrarDialogoEvento(
        contexto: Context,
        parentView: View,
        dadosEvento: Evento? = null,
        onConfirmar: (String, String, String) -> Unit,
        dialogoLayout: Int,
    ) {
        val dialog = Dialog(contexto)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogoLayout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val edNomeEvento: EditText = dialog.findViewById(R.id.edNomeEvento)
        val edDataEvento: EditText = dialog.findViewById(R.id.edDataEvento)
        val fotoEvento: ImageView = dialog.findViewById(R.id.fotoEvento)
        val btnCriarEditar: Button = dialog.findViewById(R.id.btnCriarEditar)

        val btnEditarFoto: View? = dialog.findViewById(R.id.btnEditarFoto)
        val btnCriarFoto: View? = dialog.findViewById(R.id.btnCriarFoto)

        var imagemBase64Atual: String? = null

        if (dadosEvento != null) {
            edNomeEvento.setText(dadosEvento.nomeEvento)
            edDataEvento.setText(dadosEvento.dataEvento)
            if (!dadosEvento.Imagem.isNullOrEmpty()) {
                val bitmap = Rotinas.Base64ToImage(dadosEvento.Imagem)
                bitmap?.let { fotoEvento.setImageBitmap(it) }
            }
        }

        btnEditarFoto?.setOnClickListener {
            Rotinas.onImagemSelecionada = { bitmap ->
                dialog.findViewById<ImageButton>(R.id.btnCriarFoto).visibility = View.GONE
                fotoEvento.setImageBitmap(bitmap)
                imagemBase64Atual = Rotinas.ImageToBase64(bitmap) ?: ""
            }

            val galeria = Intent(Intent.ACTION_PICK)
            galeria.type = "image/*"
            if (contexto is Activity) {
                contexto.startActivityForResult(galeria, 1001)
            } else {
                mostrarSnackbar(
                    parentView = parentView,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Erro ao abrir galeria"
                )
            }
        }

        btnCriarFoto?.setOnClickListener {
            Rotinas.onImagemSelecionada = { bitmap ->
                dialog.findViewById<ImageButton>(R.id.btnCriarFoto).visibility = View.GONE
                fotoEvento.setImageBitmap(bitmap)
                imagemBase64Atual = Rotinas.ImageToBase64(bitmap) ?: ""
            }

            val galeria = Intent(Intent.ACTION_PICK)
            galeria.type = "image/*"
            if (contexto is Activity) {
                contexto.startActivityForResult(galeria, 1001)
            } else {
                mostrarSnackbar(
                    parentView = parentView,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Erro ao abrir galeria"
                )
            }
        }

        btnCriarEditar.setOnClickListener {
            val nome = edNomeEvento.text.toString().trim()
            val data = edDataEvento.text.toString().trim()

            if (nome.isEmpty() || data.isEmpty()) {
                mostrarSnackbar(
                    parentView = parentView,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Preencha todos os campos!",
                    dialogoAtivo = dialog
                )
            } else {
                val imagemBase64 = Rotinas.ImageViewtoBase64(fotoEvento) ?: ""
                onConfirmar(nome, data, imagemBase64)
                dialog.dismiss()
            }
        }

        dialog.show()
    }


    fun mostrarDialogoEventoEditar(
        contexto: Context,
        parentView: View,
        dadosEvento: Evento,
        onConfirmar: (String, String, String) -> Unit
    ) {
        val dialog = Dialog(contexto)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialogo_editar_evento)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val edNomeEvento: EditText = dialog.findViewById(R.id.edNomeEvento)
        val edDataEvento: EditText = dialog.findViewById(R.id.edDataEvento)
        val fotoEvento: ImageView = dialog.findViewById(R.id.fotoEvento)
        val btnCriarEditar: Button = dialog.findViewById(R.id.btnCriarEditar)



        edNomeEvento.setText(dadosEvento.nomeEvento)
        edDataEvento.setText(dadosEvento.dataEvento)
        if (!dadosEvento.Imagem.isNullOrEmpty()) {
            val bitmap = Rotinas.Base64ToImage(dadosEvento.Imagem)
            bitmap?.let { fotoEvento.setImageBitmap(it) }
        }

        btnCriarEditar.setOnClickListener {
            val nome = edNomeEvento.text.toString().trim()
            val data = edDataEvento.text.toString().trim()

            if (nome.isEmpty() || data.isEmpty()) {
                mostrarSnackbar(
                    parentView = parentView,
                    layoutAlerta = R.layout.toast_alerta,
                    mensagem = "Preencha todos os campos!",
                    dialogoAtivo = dialog
                )
            } else {
                val imagemBase64 = Rotinas.ImageViewtoBase64(fotoEvento) ?: ""
                onConfirmar(nome, data, imagemBase64)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // criei pra testar em editar perfil
    fun fecharDialogo(dialogo: Dialog?) {
        dialogo?.dismiss()
    }
}