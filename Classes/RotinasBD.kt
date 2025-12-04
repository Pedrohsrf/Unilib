@file:Suppress("UNREACHABLE_CODE")

package com.example.unilib.Classes

import android.content.Intent
import com.example.unilib.R
import android.util.Log
import com.example.unilib.Classes.Rotinas.criarDataCompleta
import com.example.unilib.Classes.Rotinas.formatarData
import com.example.unilib.Classes.Rotinas.normalizeAndLowercase
import com.example.unilib.Classes.Rotinas.timestampToString
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.String

object RotinasBD {
    lateinit var fb: FirebaseFirestore

    suspend fun cadastrarUsuario(
        nome: String, emailTelefone: String, UID: String, administrador: Boolean
    ): Boolean {
        return try {
            fb = Firebase.firestore
            fb.collection("usuario").add(
                mapOf(
                    "codigo" to proximoCodigo("usuario", 5, "0".first()),
                    "nomeCompleto" to nome,
                    "acessibilidadePrefs" to "font100",
                    "emailTelefone" to emailTelefone,
                    "UID" to UID,
                    "administrador" to administrador,
                    "fotoPerfil" to "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"
                )
            ).await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun ConsultaPorPrefixo(
        colecao: String,
        prefixo: String,
        campo_normal: String,
        campo_lower: String,
    ): MutableList<DocumentSnapshot> {
        return try {
            fb = Firebase.firestore

            val query = fb.collection(colecao)
                .whereGreaterThanOrEqualTo(campo_lower, prefixo.lowercase())
                .whereLessThanOrEqualTo(campo_lower, prefixo.lowercase() + '\uf8ff')
                .get()
                .await()

            query.documents;
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    suspend fun verificarUsuario(emailTelefone: String): Boolean {
        return try {
            fb = Firebase.firestore
            val query =
                fb.collection("usuario").whereEqualTo("emailTelefone", emailTelefone).get().await()

            !query.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removerFavorito(usuario: String, livro: String): Boolean {
        return try {
            fb = Firebase.firestore
            val query = fb.collection("Listas")
                .whereEqualTo("usuario", usuario)
                .whereEqualTo("nome", "Favoritos")
                .whereEqualTo("livro", livro)
                .get()
                .await()

            val documentoFavorito =
                query.documents.firstOrNull()?.let { fb.collection("Listas").document(it.id) }

            if (documentoFavorito != null) {
                documentoFavorito.delete().await()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removerDesejo(usuario: String, livro: String): Boolean {
        return try {
            fb = Firebase.firestore
            val query = fb.collection("Listas")
                .whereEqualTo("usuario", usuario)
                .whereEqualTo("nome", "Lista de desejos")
                .whereEqualTo("livro", livro)
                .get()
                .await()

            val documentoDesejo =
                query.documents.firstOrNull()?.let { fb.collection("Listas").document(it.id) }

            if (documentoDesejo != null) {
                documentoDesejo.delete().await()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun adicionarFavorito(usuario: String, livro: String): Boolean {
        return try {
            fb = Firebase.firestore
            fb.collection("Listas").add(
                mapOf(
                    "nome" to "Favoritos",
                    "usuario" to usuario,
                    "livro" to livro,
                    "cor" to 1
                )
            ).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseError", "Erro ao adicionar favorito: ${e.message}")
            false
        }
    }

    suspend fun adicionarDesejo(usuario: String, livro: String): Boolean {
        return try {
            fb = Firebase.firestore
            fb.collection("Listas").add(
                mapOf(
                    "nome" to "Lista de desejos",
                    "usuario" to usuario,
                    "livro" to livro,
                    "cor" to 2
                )
            ).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseError", "Erro ao adicionar desejo: ${e.message}")
            false
        }
    }

    suspend fun adicionarEmprestimo(usuarioCodigo: String, livroCodigo: String): Boolean {
        return try {
            val fb = Firebase.firestore

            val novoCodigo = proximoCodigo("Emprestimo", 5, '0')
            val dataAtual = Timestamp.now()

            val dataAtualMillis = dataAtual.toDate().time
            val prazoEmMillis = 14 * 24 * 60 * 60 * 1000L
            val dataLimiteMillis = dataAtualMillis + prazoEmMillis
            val dataLimiteDate = Date(dataLimiteMillis)
            val dataLimiteTimestamp = Timestamp(dataLimiteDate)

            val emprestimo = hashMapOf(
                "codigo" to novoCodigo,
                "dataEmprestimo" to dataAtual,
                "dataDevolucao" to dataAtual,
                "dataLimite" to dataLimiteTimestamp,
                "livro" to livroCodigo,
                "status" to "Pendente",
                "usuario" to usuarioCodigo
            )

            fb.collection("Emprestimo")
                .add(emprestimo)
                .await()

            Log.d("RotinasBD", "Sucesso: Empréstimo salvo localmente e enviado para a fila.")
            true
        } catch (e: Exception) {
            Log.e("RotinasBD", "Erro ao adicionar empréstimo: ${e.message}")
            false
        }
    }

    suspend fun verificarEmprestimoAtivo(usuarioCodigo: String, livroCodigo: String): Boolean {
        return try {
            val fb = Firebase.firestore

            val statusAtivos = listOf("Pendente", "Ativo", "EM ANDAMENTO")

            val query = fb.collection("Emprestimo")
                .whereEqualTo("usuario", usuarioCodigo)
                .whereEqualTo("livro", livroCodigo)
                .whereIn("status", statusAtivos)
                .limit(1)
                .get()
                .await()

            return !query.isEmpty
        } catch (e: Exception) {
            Log.e("RotinasBD", "Erro ao verificar empréstimo ativo: ${e.message}")
            return false
        }
    }

    suspend fun devolverLivro(codigoLivro: String, codigoUsuario: String): Boolean {
        return try {
            val fb = Firebase.firestore
            val dataAtual = com.google.firebase.Timestamp.now()

            val queryEmprestimo = fb.collection("Emprestimo")
                .whereEqualTo("livro", codigoLivro)
                .whereEqualTo("usuario", codigoUsuario)
                .whereNotEqualTo("status", "Concluído")
                .get()
                .await()

            val documentoEmprestimo = queryEmprestimo.documents.firstOrNull()

            if (documentoEmprestimo != null) {
                documentoEmprestimo.reference.update(
                    mapOf(
                        "status" to "Concluído",
                        "dataDevolucao" to dataAtual
                    )
                ).await()

                val queryLivro = fb.collection("livros")
                    .whereEqualTo("codigo", codigoLivro)
                    .get()
                    .await()

                val documentoLivro = queryLivro.documents.firstOrNull()

                if (documentoLivro != null) {
                    val copiasAtuais =
                        (documentoLivro.get("copiasDisponiveis") as? Long)?.toInt() ?: 0

                    documentoLivro.reference.update(
                        "copiasDisponiveis", copiasAtuais + 1
                    ).await()
                }

                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun lerEmprestimos(usuario: String, status: String = ""): MutableList<Emprestimo> {
        val listaEmprestimos: MutableList<Emprestimo> = mutableListOf();
        fb = Firebase.firestore
        var query = fb.collection("Emprestimo").whereEqualTo("usuario", usuario);

        if (status !== "") {
            query = query.whereEqualTo("status", status);
        }

        var snapshotEmprestimos = query.get().await();

        for (emprestimo in snapshotEmprestimos.documents) {
            var listaReviews: MutableList<Review> = mutableListOf()
            var queryReviews = fb.collection("Reviews")
                .whereEqualTo("livroCodigo", emprestimo.get("livro") as String)
                .get()
                .await()

            for (review in queryReviews.documents) {
                val codigoUsuario = review.getString("usuario") ?: continue

                val queryUsuario = fb.collection("usuario")
                    .whereEqualTo("codigo", codigoUsuario)
                    .get()
                    .await()

                if (queryUsuario.isEmpty) continue
                val snapshotUsuario = queryUsuario.documents[0]

                val objUsuario = Usuario(
                    nome = snapshotUsuario.getString("nomeCompleto") ?: "",
                    email = snapshotUsuario.getString("emailTelefone") ?: "",
                    fotoPerfil = snapshotUsuario.getString("fotoPerfil") ?: ""
                )

                val objReview = Review(
                    codigo = review.get("codigo") as String,
                    usuario = objUsuario,
                    dataPublicacao = review.get("dataPublicacao") as String,
                    nota = (review.get("nota") as Long).toInt(),
                    comentario = review.get("comentario") as String,
                    suspeita = review.get("suspeita") as Boolean,
                    gerenciavel = false,
                    livroCodigo = review.getString("livroCodigo") ?: ""
                );
                listaReviews.add(objReview);
            }

            var snapshotLivro =
                fb.collection("livros").whereEqualTo("codigo", emprestimo.get("livro") as String)
                    .get().await();

            val objLivro = Livro(
                codigo = snapshotLivro.documents[0].get("codigo") as String,
                nome = snapshotLivro.documents[0].get("nome") as String,
                autor = snapshotLivro.documents[0].get("autor") as String,
                capa = snapshotLivro.documents[0].get("capa") as String,
                sinopse = snapshotLivro.documents[0].get("sinopse") as String,
                genero = snapshotLivro.documents[0].get("genero") as String,
                qtdAvaliacoes = (snapshotLivro.documents[0].get("qtdAvaliacoes") as Long).toInt(),
                qtdLeituras = (snapshotLivro.documents[0].get("qtdLeituras") as Long).toInt(),
                editora = snapshotLivro.documents[0].get("editora") as String,
                anoPublicacao = snapshotLivro.documents[0].get("anoPublicacao") as String,
                idioma = snapshotLivro.documents[0].get("idioma") as String,
                reviews = listaReviews,
                iSBN = snapshotLivro.documents[0].get("ISBN") as String,
                localizacao = snapshotLivro.documents[0].get("localizacao") as String,
                qtdPaginas = snapshotLivro.documents[0].get("qtdPaginas") as String,
                copiasDisponiveis = (snapshotLivro.documents[0].get("copiasDisponiveis") as Long).toInt(),
            );

            var snapshotUsuario =
                fb.collection("usuario")
                    .whereEqualTo("codigo", emprestimo.get("usuario") as String)
                    .get().await();

            val objUsuario = Usuario(
                nome = snapshotUsuario.documents[0].get("nomeCompleto") as String,
                email = snapshotUsuario.documents[0].get("emailTelefone") as String,
                fotoPerfil = snapshotUsuario.documents[0].get("fotoPerfil") as String,
            );

            val dataDevolucaoTimestamp =
                emprestimo.get("dataDevolucao") as? com.google.firebase.Timestamp
            val dataDevolucaoDate = dataDevolucaoTimestamp?.toDate()

            val dataLimiteTimestamp = emprestimo.get("dataLimite") as? com.google.firebase.Timestamp
            val dataLimiteDate = dataLimiteTimestamp?.toDate() ?: java.util.Date()

            val objEmprestimo = Emprestimo(
                livro = objLivro,
                dataEmprestimo = (emprestimo.get("dataEmprestimo") as com.google.firebase.Timestamp).toDate(),
                dataDevolucao = dataDevolucaoDate, // Usando o campo lido (pode ser null)
                usuario = objUsuario,
                status = emprestimo.get("status") as String,
                dataLimite = dataLimiteDate
            );
            listaEmprestimos.add(objEmprestimo);
        }
        return listaEmprestimos;
    }

    suspend fun editarAcessibilidadePrefs(UID: String, pref: String): Boolean {
        return try {
            fb = Firebase.firestore
            val query = fb.collection("usuario").whereEqualTo("UID", UID).get().await()

            val documentoUsuario =
                query.documents.firstOrNull()?.let { fb.collection("usuario").document(it.id) }

            if (documentoUsuario != null) {
                documentoUsuario.update(
                    mapOf(
                        "acessibilidadePrefs" to pref
                    )
                ).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun lerAcessibilidadePref(UID: String): String {
        fb = Firebase.firestore
        var query = fb.collection("usuario")
            .whereEqualTo("UID", UID)
            .get()
            .await();

        return query.documents[0].get("acessibilidadePrefs") as String
    }

    suspend fun lerGenero(codigo: String): String {
        fb = Firebase.firestore
        var query = fb.collection("Generos")
            .whereEqualTo("codigo", codigo)
            .get()
            .await();

        val objGenero = Genero(
            codigo = query.documents[0].get("codigo") as String,
            nome = query.documents[0].get("nome") as String,
            icone = query.documents[0].get("icone") as String
        );

        return objGenero.nome
    }

    suspend fun lerGeneros(): MutableList<Genero> {
        val listaGeneros: MutableList<Genero> = mutableListOf();
        fb = Firebase.firestore
        var query = fb.collection("Generos")
            .orderBy("codigo")
            .get()
            .await();

        for (genero in query.documents) {
            val objGenero = Genero(
                codigo = genero.get("codigo") as String,
                nome = genero.get("nome") as String,
                icone = genero.get("icone") as String
            );

            listaGeneros.add(objGenero);
        }
        return listaGeneros;
    }

    suspend fun lerUsuario(UID: String): DocumentSnapshot {
        fb = Firebase.firestore
        val query =
            fb.collection("usuario").whereEqualTo("UID", UID).get().await()

        return query.documents[0];
    }

    suspend fun atualizarUsuario(
        codigoUsuario: String,
        nomeCompleto: String,
        emailTelefone: String,
        fotoPerfil: String = ""
    ): Boolean {
        return try {
            val db = Firebase.firestore
            val usuarioRef =
                db.collection("usuario").whereEqualTo("codigo", codigoUsuario).get().await()

            val documentoUsuario =
                usuarioRef.documents.firstOrNull()?.let { fb.collection("usuario").document(it.id) }

            val dadosAtualizados = hashMapOf<String, Any>(
                "nomeCompleto" to nomeCompleto,
                "emailTelefone" to emailTelefone,
                "fotoPerfil" to fotoPerfil
            )
            if (fotoPerfil.isNotEmpty()) {
                dadosAtualizados["fotoPerfil"] = fotoPerfil
            }
            if (documentoUsuario != null) {
                documentoUsuario.update(dadosAtualizados).await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // fiz pra testar

    suspend fun criarEmprestimo(
        emprestimo: Emprestimo
    ): Boolean {
        return try {
            fb = Firebase.firestore

            fb.collection("Emprestimo").add(
                mapOf(
                    "codigo" to proximoCodigo("Emprestimo", 5, "0".first()),
                    "dataDevolucao" to emprestimo.dataDevolucao?.let { Timestamp(it) },
                    "dataEmprestimo" to Timestamp(emprestimo.dataEmprestimo),
                    "dataLimite" to Timestamp(emprestimo.dataLimite),
                    "livro" to emprestimo.livro.codigo,
                    "status" to emprestimo.status,
                    "usuario" to emprestimo.usuario.codigo
                )
            ).await()

            val query =
                fb.collection("livros").whereEqualTo("codigo", emprestimo.livro.codigo).get()
                    .await()

            val documentoLivro =
                query.documents.firstOrNull()?.let { fb.collection("livros").document(it.id) }

            if (documentoLivro != null) {
                documentoLivro.update(
                    mapOf(
                        "copiasDisponiveis" to emprestimo.livro.copiasDisponiveis - 1,
                    )
                ).await()
            }

            criarNotificacao(
                titulo = "Empréstimo",
                descricao = "O seu empréstimo foi criado e está aguardando aprovação pela biblioteca.",
                usuario = emprestimo.usuario.codigo,
                redirecionamento = ""
            )

            val queryAdms =
                fb.collection("usuario").whereEqualTo("administrador", true).get()
                    .await()

            for (administrador in queryAdms.documents) {
                criarNotificacao(
                    titulo = "Empréstimo",
                    descricao = "${emprestimo.usuario.nome} deseja fazer um empréstimo do livro ${emprestimo.livro.nome}",
                    usuario = administrador.get("codigo") as String,
                    redirecionamento = "Empréstimos;${emprestimo.usuario.codigo}"
                )
            }

            true
        } catch (e: Exception) {
            Log.i("Erro", e.message.toString())
            false
        }
    }

    suspend fun enviarNotificacoesEmprestimo(
        usuario: String
    ): Boolean {
        return try {
            fb = Firebase.firestore
            var icone = lerCategoriaNotificacao("Empréstimo")

            val query =
                fb.collection("Emprestimo").whereEqualTo("usuario", usuario).get()
                    .await()

            for (emprestimo in query.documents) {

                var query2 =
                    fb.collection("livros")
                        .whereEqualTo("codigo", emprestimo.get("livro") as String).get()
                        .await()

                var nomeLivro = query2.documents[0].get("nome") as String

                val dataDevolucaoTimestamp =
                    emprestimo.get("dataDevolucao") as? com.google.firebase.Timestamp
                val dataDevolucaoDate = dataDevolucaoTimestamp?.toDate()

                val dataLimiteTimestamp = emprestimo.get("dataLimite") as? com.google.firebase.Timestamp
                val dataLimiteDate = dataLimiteTimestamp?.toDate() ?: java.util.Date()

                val calHoje = Calendar.getInstance().apply {
                    time = Date() // Pega "hoje"
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val calLimite = Calendar.getInstance().apply {
                    time = dataLimiteDate // Pega a data limite vinda do Firebase
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // 2. Calcular os dias restantes (Limite - Hoje)
                val diffMillisRestantes = calLimite.timeInMillis - calHoje.timeInMillis
                val diasRestantes =
                    TimeUnit.DAYS.convert(diffMillisRestantes, TimeUnit.MILLISECONDS).toInt()

                var descricao =
                    if (diasRestantes > 0 && diasRestantes < 3) "Faltam ${diasRestantes} dias para devolução do livro ${nomeLivro}."
                    else "O prazo para devolver o livro ${nomeLivro} encerrou a ${diasRestantes} dias."

                fb.collection("Notificacoes").add(
                    mapOf(
                        "codigo" to proximoCodigo("Notificacoes", 10, "0".first()),
                        "data" to formatarData(Date(), "dd/MM/yyyy"),
                        "titulo" to "Empréstimo",
                        "descricao" to descricao,
                        "icone" to icone,
                        "redirecionamento" to "",
                        "usuario" to usuario,
                        "visualizada" to false
                    )
                ).await()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun criarNotificacao(
        titulo: String,
        descricao: String,
        usuario: String,
        redirecionamento: String = ""
    ): Boolean {
        return try {
            fb = Firebase.firestore
            var icone = lerCategoriaNotificacao(titulo)

            fb.collection("Notificacoes").add(
                mapOf(
                    "codigo" to proximoCodigo("Notificacoes", 10, "0".first()),
                    "data" to formatarData(Date(), "dd/MM/yyyy"),
                    "titulo" to titulo,
                    "descricao" to descricao,
                    "icone" to icone,
                    "redirecionamento" to redirecionamento,
                    "usuario" to usuario,
                    "visualizada" to false
                )
            ).await()

            true
        } catch (e: Exception) {
            Log.i("Erro", e.message.toString())
            false
        }
    }

    suspend fun lerCategoriaNotificacao(titulo: String): String {
        fb = Firebase.firestore
        var query = fb.collection("CategoriasNotificacao")
            .whereEqualTo("titulo", titulo)
            .get()
            .await();

        return query.documents.firstOrNull()?.get("icone") as String
    }

    suspend fun visualizarNotificacao(codigo: String, usuario: String): Boolean {
        return try {
            fb = Firebase.firestore
            var query = fb.collection("Notificacoes")
                .whereEqualTo("usuario", usuario)
                .whereEqualTo("codigo", codigo)
                .get()
                .await();

            val documentoNotificacao =
                query.documents.firstOrNull()?.let { fb.collection("Notificacoes").document(it.id) }

            if (documentoNotificacao != null) {
                documentoNotificacao.update(
                    mapOf(
                        "visualizada" to true
                    )
                ).await()
            }

            true
        } catch (E: Exception) {
            Log.i("Exceção", E.message.toString())
            false
        }
    }

    suspend fun lerNotificacoes(usuario: String, visualizada: Boolean): MutableList<Notificacao> {
        val listaNotificacoes: MutableList<Notificacao> = mutableListOf();
        fb = Firebase.firestore
        var query = fb.collection("Notificacoes")
            .whereEqualTo("usuario", usuario)
            .whereEqualTo("visualizada", visualizada)
            .orderBy("codigo", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await();

        for (notificacao in query.documents) {
            var objNotificacao = Notificacao(
                codigo = notificacao.getString("codigo") as String,
                titulo = notificacao.getString("titulo") as String,
                descricao = notificacao.getString("descricao") as String,
                data = notificacao.getString("data") as String,
                icone = notificacao.getString("icone") as String,
                redirecionamento = notificacao.getString("redirecionamento") as String,
                usuario = notificacao.getString("usuario") as String
            );
            listaNotificacoes.add(objNotificacao);
        }
        return listaNotificacoes;
    }

    suspend fun lerLivros(genero: String? = ""): MutableList<Livro> {
        val listaLivros: MutableList<Livro> = mutableListOf();
        fb = Firebase.firestore
        var query: Query = fb.collection("livros")

        if (genero !== "") {
            query = query.whereEqualTo("genero", genero as String)
        }

        var snapshotLivros = query.get().await();

        for (livro in snapshotLivros.documents) {
            var listaReviews: MutableList<Review> = mutableListOf()
            var queryReviews = fb.collection("Reviews")
                .whereEqualTo("livroCodigo", livro.get("codigo") as String)
                .get()
                .await()

            for (review in queryReviews.documents) {
                val codigoUsuario = review.getString("usuario") ?: continue

                val queryUsuario = fb.collection("usuario")
                    .whereEqualTo("codigo", codigoUsuario)
                    .get()
                    .await()

                if (queryUsuario.isEmpty) continue
                val snapshotUsuario = queryUsuario.documents[0]

                val objUsuario = Usuario(
                    nome = snapshotUsuario.getString("nomeCompleto") ?: "",
                    email = snapshotUsuario.getString("emailTelefone") ?: "",
                    fotoPerfil = snapshotUsuario.getString("fotoPerfil") ?: ""
                )

                val objReview = Review(
                    codigo = review.get("codigo") as String,
                    usuario = objUsuario,
                    dataPublicacao = review.get("dataPublicacao") as String,
                    nota = (review.get("nota") as Long).toInt(),
                    comentario = review.get("comentario") as String,
                    suspeita = review.get("suspeita") as Boolean,
                    gerenciavel = false,
                    livroCodigo = review.getString("livroCodigo") ?: ""
                );
                listaReviews.add(objReview);
            }

            val objLivro = Livro(
                codigo = livro.get("codigo") as String,
                nome = livro.get("nome") as String,
                autor = livro.get("autor") as String,
                capa = livro.get("capa") as String,
                sinopse = livro.get("sinopse") as String,
                genero = livro.get("genero") as String,
                qtdAvaliacoes = (livro.get("qtdAvaliacoes") as Long).toInt(),
                qtdLeituras = (livro.get("qtdLeituras") as Long).toInt(),
                editora = livro.get("editora") as String,
                anoPublicacao = livro.get("anoPublicacao") as String,
                idioma = livro.get("idioma") as String,
                iSBN = livro.get("ISBN") as String,
                reviews = listaReviews,
                localizacao = livro.get("localizacao") as String,
                qtdPaginas = livro.get("qtdPaginas") as String,
                copiasDisponiveis = (livro.get("copiasDisponiveis") as Long).toInt(),
            );
            listaLivros.add(objLivro);
        }
        return listaLivros;
    }

    suspend fun lerLivro(codigo: String): Livro {
        val livro: Livro;
        fb = Firebase.firestore
        val query = fb.collection("livros").whereEqualTo("codigo", codigo).get().await()

        val dadosRetorno = query.documents[0];

        var listaReviews: MutableList<Review> = mutableListOf()
        var queryReviews = fb.collection("Reviews")
            .whereEqualTo("livroCodigo", dadosRetorno.get("codigo") as String)
            .get()
            .await()

        for (review in queryReviews.documents) {
            val codigoUsuario = review.getString("usuario") ?: continue

            val queryUsuario = fb.collection("usuario")
                .whereEqualTo("codigo", codigoUsuario)
                .get()
                .await()

            if (queryUsuario.isEmpty) continue
            val snapshotUsuario = queryUsuario.documents[0]

            val objUsuario = Usuario(
                nome = snapshotUsuario.getString("nomeCompleto") ?: "",
                email = snapshotUsuario.getString("emailTelefone") ?: "",
                fotoPerfil = snapshotUsuario.getString("fotoPerfil") ?: ""
            )

            val objReview = Review(
                codigo = review.get("codigo") as String,
                usuario = objUsuario,
                dataPublicacao = review.get("dataPublicacao") as String,
                nota = (review.get("nota") as Long).toInt(),
                comentario = review.get("comentario") as String,
                suspeita = review.get("suspeita") as Boolean,
                gerenciavel = false,
                livroCodigo = review.getString("livroCodigo") ?: ""
            );
            listaReviews.add(objReview);
        }

        livro = Livro(
            codigo = dadosRetorno.get("codigo") as String,
            nome = dadosRetorno.get("nome") as String,
            autor = dadosRetorno.get("autor") as String,
            capa = dadosRetorno.get("capa") as String,
            sinopse = dadosRetorno.get("sinopse") as String,
            genero = dadosRetorno.get("genero") as String,
            qtdAvaliacoes = (dadosRetorno.get("qtdAvaliacoes") as Long).toInt(),
            qtdLeituras = (dadosRetorno.get("qtdLeituras") as Long).toInt(),
            editora = dadosRetorno.get("editora") as String,
            anoPublicacao = dadosRetorno.get("anoPublicacao") as String,
            idioma = dadosRetorno.get("idioma") as String,
            reviews = listaReviews,
            iSBN = dadosRetorno.get("ISBN") as String,
            localizacao = dadosRetorno.get("localizacao") as String,
            qtdPaginas = dadosRetorno.get("qtdPaginas") as String,
            copiasDisponiveis = (dadosRetorno.get("copiasDisponiveis") as Long).toInt(),
        );
        return livro;
    }

    suspend fun proximoCodigo(colecao: String, tamanho: Int, char: Char): String {
        fb = Firebase.firestore
        val query = fb.collection(colecao)
            .orderBy("codigo", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(1)
            .get().await()

        if (query.documents.isNotEmpty()) {
            return (((query.documents.firstOrNull()
                ?.get("codigo")) as String).toInt() + 1).toString()
                .padStart(tamanho, char);
        } else {
            return "0".padStart(tamanho, char)
        }
    }

    suspend fun criarLivro(livro: Livro): Boolean {
        return try {
            fb = Firebase.firestore
            fb.collection("livros").add(
                mapOf(
                    "codigo" to livro.codigo,
                    "nome" to livro.nome,
                    "nome_lower" to livro.nome.normalizeAndLowercase(),
                    "autor" to livro.autor,
                    "capa" to livro.capa,
                    "sinopse" to livro.sinopse,
                    "genero" to livro.genero,
                    "qtdAvaliacoes" to livro.qtdAvaliacoes,
                    "qtdLeituras" to livro.qtdLeituras,
                    "editora" to livro.editora,
                    "anoPublicacao" to livro.anoPublicacao,
                    "idioma" to livro.idioma,
                    "ISBN" to livro.iSBN,
                    "localizacao" to livro.localizacao,
                    "qtdPaginas" to livro.qtdPaginas,
                    "copiasDisponiveis" to livro.copiasDisponiveis,
                )
            ).await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun editarLivro(livro: Livro): Boolean {
        return try {
            fb = Firebase.firestore
            val query = fb.collection("livros").whereEqualTo("codigo", livro.codigo).get().await()

            val documentoLivro =
                query.documents.firstOrNull()?.let { fb.collection("livros").document(it.id) }

            if (documentoLivro != null) {
                documentoLivro.update(
                    mapOf(
                        "codigo" to livro.codigo,
                        "nome" to livro.nome,
                        "nome_lower" to livro.nome.normalizeAndLowercase(),
                        "autor" to livro.autor,
                        "capa" to livro.capa,
                        "sinopse" to livro.sinopse,
                        "genero" to livro.genero,
                        "qtdAvaliacoes" to livro.qtdAvaliacoes,
                        "qtdLeituras" to livro.qtdLeituras,
                        "editora" to livro.editora,
                        "anoPublicacao" to livro.anoPublicacao,
                        "idioma" to livro.idioma,
                        "ISBN" to livro.iSBN,
                        "localizacao" to livro.localizacao,
                        "qtdPaginas" to livro.qtdPaginas,
                        "copiasDisponiveis" to livro.copiasDisponiveis,
                    )
                ).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deletarLivro(codigo: String): Boolean {
        return try {
            fb = Firebase.firestore
            val query = fb.collection("livros").whereEqualTo("codigo", codigo).get().await()

            val documentoLivro =
                query.documents.firstOrNull()?.let { fb.collection("livros").document(it.id) }

            if (documentoLivro != null) {
                documentoLivro.delete().await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun autorizarLogin(emailTelefone: String, senha: String): Boolean {
        return try {
            fb = Firebase.firestore
            val query =
                fb.collection("usuario").whereEqualTo("emailTelefone", emailTelefone).get().await()

            query.documents.firstOrNull()?.getString("senha") == senha;
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obterCodigoCustomizado(email: String?): String {
        if (email.isNullOrEmpty()) return "" // Proteção contra email nulo

        return try {
            fb = Firebase.firestore
            val query = fb.collection("usuario")
                .whereEqualTo(
                    "emailTelefone",
                    email
                ) // O campo usado para armazenar o email/telefone
                .get()
                .await()

            // Verifica se encontrou e retorna o "codigo"
            val codigo = query.documents.firstOrNull()?.get("codigo") as? String
            codigo ?: ""
        } catch (e: Exception) {
            // Log.e("RotinasBD", "Erro ao obter código customizado: ${e.message}")
            ""
        }
    }

    suspend fun lerEventos(gerenciavel: Boolean): MutableList<Evento> {
        val listaEventos: MutableList<Evento> = mutableListOf()
        return try {
            fb = Firebase.firestore
            val query = fb.collection("evento")
                .get()
                .await()
            for (evento in query.documents) {
                val objEvento = Evento(
                    codigo = evento.get("codigo") as String,
                    nomeEvento = evento.get("nomeEvento") as String,
                    dataEvento = evento.get("dataEvento") as String,
                    Imagem = evento.get("imagem") as String,
                    gerenciavel = gerenciavel
                )
                listaEventos.add(objEvento)
            }
            listaEventos;
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    suspend fun editarEvento(
        evento: Evento,
        nomeEvento: String,
        dataEvento: String,
        Imagem: String
    ): Boolean {
        fb = Firebase.firestore
        return try {
            val query = fb.collection("evento")
                .whereEqualTo("codigo", evento.codigo)
                .get()
                .await()
            val ref = query.documents.firstOrNull()
            if (ref != null) {
                ref.reference.update(
                    mapOf(
                        "nomeEvento" to nomeEvento,
                        "dataEvento" to dataEvento,
                        "imagem" to Imagem,
                        "nomeEvento_lower" to nomeEvento.lowercase()
                    )
                ).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun criarEvento(evento: Evento): Boolean {
        fb = Firebase.firestore
        return try {
            fb.collection("evento").add(
                mapOf(
                    "codigo" to evento.codigo,
                    "nomeEvento" to evento.nomeEvento,
                    "dataEvento" to evento.dataEvento,
                    "imagem" to evento.Imagem,
                    "gerenciavel" to evento.gerenciavel
                )
            ).await()

            val queryUsuarios =
                fb.collection("usuario").whereEqualTo("administrador", false).get()
                    .await()

            for (usuario in queryUsuarios.documents) {
                criarNotificacao(
                    titulo = "Novidade",
                    descricao = "O dia do evento ${evento.nomeEvento} está chegando, marque sua presença!",
                    usuario = usuario.get("codigo") as String,
                    redirecionamento = ""
                )
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deletarEvento(codigo: String): Boolean {
        return try {
            fb = Firebase.firestore
            val query = fb.collection("evento")
                .whereEqualTo("codigo", codigo)
                .get()
                .await()
            val documentoEvento =
                query.documents.firstOrNull()?.let { fb.collection("evento").document(it.id) }
            if (documentoEvento != null) {
                documentoEvento.delete().await()
            }
            true
        } catch (e: Exception) {
            false
        }
        //Duas novas funções, pedir pro Pedro checar se a lerListas tá boa o suficiente pra substituir a lerFavoritos
        //Não tirei ainda pq fiquei com medo de tirar - Felipe

        suspend fun lerReviews(codigoUsuario: String, suspeitas: Boolean): MutableList<Review> {
            fb = Firebase.firestore
            var Reviews: MutableList<Review> = mutableListOf()
            var query = fb.collection("Reviews")
                .whereEqualTo("usuario", codigoUsuario)
                .whereEqualTo("suspeita", suspeitas)
                .get()
                .await()
            for (review in query.documents) {
                val objReview = Review(
                    codigo = review.get("codigo") as String,
                    usuario = review.get("usuario") as Usuario,
                    dataPublicacao = review.get("dataPublicacao") as String,
                    nota = review.get("nota") as Int,
                    comentario = review.get("comentario") as String,
                    suspeita = review.get("suspeita") as Boolean,
                    gerenciavel = review.get("gerenciavel") as Boolean,
                    livroCodigo = review.getString("livroCodigo") ?: ""
                );
                Reviews.add(objReview);
            }
            return Reviews
        }


//    suspend fun lerSalas(): MutableList<Sala> {
//        val listaSalas: MutableList<Sala> = mutableListOf()
//        return try {
//            val fb = Firebase.firestore
//            val query = fb.collection("salas").get().await()
//
//            for (sala in query.documents) {
//                val listaDisponibilidades: MutableList<Disponibilidade> = mutableListOf()
//                val query2 = fb.collection("disponibilidades")
//                    .whereEqualTo("codigoSala", sala.get("codigo"))
//                    .get().await()
//
//                for (disp in query2.documents) {
//                    val objDisp = Disponibilidade(
//                        dia = disp.get("dia") as String,
//                        horarios = Horario(
//                            horaIni = disp.get("horarioInicio") as String,
//                            horaFim = disp.get("horarioFim") as String
//                        )
//                    )
//                    listaDisponibilidades.add(objDisp)
//                }
//
//                val objSala = Sala(
//                    numero = sala.get("codigo") as String,
//                    status = sala.get("status") as String,
//                    disponibilidades = listaDisponibilidades
//                )
//                listaSalas.add(objSala)
//            }
//            listaSalas
//        } catch (e: Exception) {
//            mutableListOf()
//        }
//    }
    suspend fun criarSala(sala: Sala): Boolean {
        return try {
            val fb = Firebase.firestore

            fb.collection("salas").add(
                mapOf(
                    "codigo" to sala.numero,
                    "status" to sala.status
                )
            ).await()

            sala.disponibilidades.forEach { disp ->
                fb.collection("disponibilidades").add(
                    mapOf(
                        "sala" to sala.numero,
                        "dia" to disp.dia,
                        "horarioIni" to disp.horarios,
                        "horarioFim" to disp.horarios
                    )
                ).await()
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun editarSala(sala: Sala): Boolean {
        return try {
            val fb = Firebase.firestore
            val query = fb.collection("salas")
                .whereEqualTo("codigo", sala.numero)
                .get().await()

            val documentoSala = query.documents.firstOrNull()?.let {
                fb.collection("salas").document(it.id)
            }

            if (documentoSala != null) {
                documentoSala.update(
                    mapOf("status" to sala.status)
                ).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    }

    suspend fun lerListas(usuario: String): Pair<MutableList<Livro>, MutableList<Livro>> {
        val listaFavoritos: MutableList<Livro> = mutableListOf()
        val listaDesejos: MutableList<Livro> = mutableListOf()
        fb = Firebase.firestore

        val query = fb.collection("Listas")
            .whereEqualTo("usuario", usuario)
            .get().await()

        for (documentoLista in query.documents) {
            val nomeLista = documentoLista.get("nome") as? String ?: continue
            val idDoLivro = documentoLista.get("livro") as? String ?: continue

            val snapshotLivro = fb.collection("livros")
                .whereEqualTo("codigo", idDoLivro)
                .get()
                .await()

            if (snapshotLivro.isEmpty) {
                Log.w("FirebaseWarning", "Livro com ID $idDoLivro não encontrado.")
                continue
            }

            val docLivro = snapshotLivro.documents[0]

            try {
                val objLivro = Livro(
                    codigo = docLivro.get("codigo") as String,
                    nome = docLivro.get("nome") as String,
                    autor = docLivro.get("autor") as String,
                    capa = docLivro.get("capa") as String,
                    sinopse = docLivro.get("sinopse") as String,
                    genero = docLivro.get("genero") as String,
                    qtdAvaliacoes = (docLivro.get("qtdAvaliacoes") as Long).toInt(),
                    qtdLeituras = (docLivro.get("qtdLeituras") as Long).toInt(),
                    editora = docLivro.get("editora") as String,
                    anoPublicacao = docLivro.get("anoPublicacao") as String,
                    idioma = docLivro.get("idioma") as String,
                    iSBN = docLivro.get("ISBN") as String,
                    localizacao = docLivro.get("localizacao") as String,
                    qtdPaginas = docLivro.get("qtdPaginas") as String,
                    copiasDisponiveis = (docLivro.get("copiasDisponiveis") as Long).toInt(),
                )

                if (nomeLista == "Favoritos") {
                    listaFavoritos.add(objLivro)
                } else if (nomeLista == "Lista de desejos") {
                    listaDesejos.add(objLivro)
                }

            } catch (e: Exception) {
                Log.e("FirebaseError", "Erro ao converter livro ${docLivro.id}: ${e.message}")
            }
        }
        return Pair(listaFavoritos, listaDesejos)
    }

    suspend fun criarListasVazias(usuario: String): Boolean {
        fb = Firebase.firestore
        return try {
            var codigo = proximoCodigo("Listas", 5, "0".first())
            fb.collection("Listas").add(
                mapOf(
                    "codigo" to codigo,
                    "nome" to "Favoritos",
                    "seq" to "00001",
                    "usuario" to usuario,
                    "livro" to ""
                )
            ).await()

            fb.collection("Listas").add(
                mapOf(
                    "codigo" to (codigo.toInt() + 1).toString().padStart(5, "0".first()),
                    "nome" to "Lista de Desejos",
                    "seq" to "00001",
                    "usuario" to usuario,
                    "livro" to ""
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun lerReviews(codigoUsuario: String, suspeitas: Boolean): MutableList<Review> {
        fb = Firebase.firestore
        var Reviews: MutableList<Review> = mutableListOf()
        var query = fb.collection("Reviews")
            .whereEqualTo("usuario", codigoUsuario)
            .whereEqualTo("suspeita", suspeitas)
            .get()
            .await()
        for (review in query.documents) {
            val codigoUsuario = review.getString("usuario") ?: continue

            val queryUsuario = fb.collection("usuario")
                .whereEqualTo("codigo", codigoUsuario)
                .get()
                .await()

            if (queryUsuario.isEmpty) continue
            val snapshotUsuario = queryUsuario.documents[0]

            val objUsuario = Usuario(
                nome = snapshotUsuario.getString("nomeCompleto") ?: "",
                email = snapshotUsuario.getString("emailTelefone") ?: "",
                fotoPerfil = snapshotUsuario.getString("fotoPerfil") ?: ""
            )
            val objReview = Review(
                codigo = review.get("codigo") as String,
                usuario = objUsuario,
                dataPublicacao = review.get("dataPublicacao") as String,
                nota = (review.get("nota") as Long).toInt(),
                comentario = review.get("comentario") as String,
                suspeita = review.get("suspeita") as Boolean,
                gerenciavel = false,
                livroCodigo = review.getString("livroCodigo") ?: ""
            );
            Reviews.add(objReview);
        }
        return Reviews
    }

    suspend fun lerSala(codigoSala: String): Sala? {
        return try {
            val fb = Firebase.firestore

            val salaSnapshot = fb.collection("salas")
                .whereEqualTo("codigo", codigoSala)
                .get()
                .await()

            val salaDocument = salaSnapshot.documents.firstOrNull()
            if (salaDocument == null) {
                return null
            }

            val listaDisponibilidades: MutableList<Disponibilidade> = mutableListOf()

            val queryDisponibilidades = fb.collection("disponibilidades")
                .whereEqualTo("sala", salaDocument.get("codigo") as String)
                .get()
                .await()

            val datasDisponiveis: Set<String> = queryDisponibilidades.documents
                .map { document -> document.getString("data") ?: "" }
                .toSet()

            for (data in datasDisponiveis) {
                val listaHorarios: MutableList<Horario> = mutableListOf()

                val horariosPorData = queryDisponibilidades.documents.filter { doc ->
                    doc.getString("data") == data
                }

                for (disponibilidadeDoc in horariosPorData) {
                    val objHorario = Horario(
                        codigo = disponibilidadeDoc.get("sala") as String + data + disponibilidadeDoc.get(
                            "horaIni"
                        ) as String + disponibilidadeDoc.get("horaFim") as String,
                        data = data,
                        horaIni = disponibilidadeDoc.get("horaIni") as String,
                        horaFim = disponibilidadeDoc.get("horaFim") as String,
                        usuario = disponibilidadeDoc.get("usuario") as String
                    )
                    listaHorarios.add(objHorario)
                }

                val objDisponibilidade = Disponibilidade(
                    dia = data,
                    horarios = listaHorarios
                )
                listaDisponibilidades.add(objDisponibilidade)
            }

            Sala(
                numero = (salaDocument.get("codigo") as String).toInt().toString(),
                status = salaDocument.get("status") as String,
                disponibilidades = listaDisponibilidades
            )

        } catch (e: Exception) {
            Log.i("Exceção", "Erro ao ler sala: ${e.message}")
            null
        }
    }

    suspend fun lerSalas(
        status: String,
        usuario: String,
        administrador: Boolean
    ): MutableList<Sala> {
        return try {
            val listaSalas: MutableList<Sala> = mutableListOf()

            val fb = Firebase.firestore
            var query: Query = fb.collection("salas").orderBy("codigo")

//            if (status !== "") {
//                query = query.whereEqualTo("status", status)
//            } else
//                if (status == "" && !administrador) {
//                query = query.whereNotEqualTo("status", "MANUTENCAO")
//            }

            var snapshotSalas = query.get().await()

            for (sala in snapshotSalas.documents) {
                val listaDisponibilidades: MutableList<Disponibilidade> = mutableListOf()
                val query2 = fb.collection("disponibilidades")
                    .whereEqualTo("sala", sala.get("codigo") as String)
                    .get()
                    .await()

                var datasDisponiveis: Set<String> = query2.documents
                    .map { document -> document.getString("data") ?: "" }
                    .toSet()

                for (data in datasDisponiveis) {
                    val listaHorarios: MutableList<Horario> = mutableListOf()
                    val query3 = fb.collection("disponibilidades")
                        .whereEqualTo("data", data)
                        .whereEqualTo("sala", sala.get("codigo") as String)
                        .get()
                        .await()

                    for (disponibiliade in query3.documents) {
                        val objHorario = Horario(
                            codigo = disponibiliade.get("sala") as String + data + disponibiliade.get(
                                "horaIni"
                            ) as String + disponibiliade.get("horaFim") as String,
                            data = data,
                            horaIni = disponibiliade.get("horaIni") as String,
                            horaFim = disponibiliade.get("horaFim") as String,
                            usuario = disponibiliade.get("usuario") as String
                        )

                        listaHorarios.add(objHorario)
                    }

                    val objDisponibilidade = Disponibilidade(
                        dia = data,
                        horarios = listaHorarios
                    )
                    listaDisponibilidades.add(objDisponibilidade)
                }

                var statusSala = (sala.get("status") as String)
                var reservada = false

                if (!statusSala.equals("MANUTENCAO")) {
                    var disponivel = false;
                    for (data in listaDisponibilidades) {
                        for (disponibiliade in data.horarios) {
                            val timezone = TimeZone.getTimeZone("America/Sao_Paulo")

                            val formatadorCompleto =
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
                                    timeZone = timezone
                                }

                            val formatadorDia =
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                    timeZone = timezone
                                }

                            val dataHoraAtualString = formatadorCompleto.format(Date())

                            val momentoAtual = formatadorCompleto.parse(dataHoraAtualString)!!

                            val dataComAno =
                                formatadorDia.format(criarDataCompleta(disponibiliade.data)!!)

                            val dataHoraFimItemString = "$dataComAno ${disponibiliade.horaFim}"

                            val dataHoraFimItem = formatadorCompleto.parse(dataHoraFimItemString)

                            val aindaNaoPassou =
                                dataHoraFimItem != null && !dataHoraFimItem.before(momentoAtual)
                            if (!administrador) {
                                if (disponibiliade.usuario.equals(usuario)) {
                                    reservada = true;
                                }

                                if ((disponibiliade.usuario.equals(usuario) || disponibiliade.usuario.equals(
                                        ""
                                    )) && aindaNaoPassou
                                ) {
                                    disponivel = true;
                                    break;
                                }
                            } else {
                                if (disponibiliade.usuario.equals("") && aindaNaoPassou) {
                                    disponivel = true;
                                    break;
                                }
                            }
                        }
                    }

                    statusSala = if (disponivel) "DISPONIVEL" else "OCUPADA"
                } else {
                    statusSala = "MANUTENCAO"
                }

                Log.i("Sala ${(sala.get("codigo") as String)}", statusSala)

                if (((status == "" && !administrador) && (statusSala == "MANUTENCAO")) ||
                    ((status == "DISPONIVEL") && (statusSala !== "DISPONIVEL")) ||
                    ((status == "OCUPADA") && (statusSala !== "OCUPADA"))
                ) {
                    continue;
                }

                val objSala = Sala(
                    numero = (sala.get("codigo") as String).toInt().toString(),
                    status = statusSala,
                    disponibilidades = listaDisponibilidades,
                    reservada = reservada
                )
                listaSalas.add(objSala)
            }
            listaSalas
        } catch (e: Exception) {
            Log.i("Exceção", e.message.toString())
            mutableListOf()
        }
    }

    suspend fun salaExiste(sala: String): Boolean {
        return try {
            val fb = Firebase.firestore
            var query = fb.collection("salas")
                .whereEqualTo("codigo", sala)
                .get()
                .await()

            if (query.isEmpty) {
                false
            } else {
                true
            }
        } catch (e: Exception) {
            Log.i("Exceção", e.message.toString())
            false
        }
    }

    suspend fun criarSala(sala: Sala): Boolean {
        return try {
            val fb = Firebase.firestore

            fb.collection("salas").add(
                mapOf(
                    "codigo" to sala.numero,
                    "status" to sala.status
                )
            ).await()

            sala.disponibilidades.forEach { disp ->
                disp.horarios.forEach { horario ->
                    fb.collection("disponibilidades").add(
                        mapOf(
                            "sala" to sala.numero,
                            "data" to horario.data,
                            "horaIni" to horario.horaIni,
                            "horaFim" to horario.horaFim,
                            "usuario" to ""
                        )
                    ).await()
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }
    // testando metodo para salvar review

    suspend fun salvarAvaliacao(
        livroCodigo: String,
        codigoUsuario: String,
        nota: Int,
        comentario: String
    ): Boolean { // <-- Novo parâmetro
        return try {
            val fb = Firebase.firestore

           var palavrasSuspeitas = listOf(
                "porra",
                "merda",
                "caralho",
                "filho da puta",
                "arrombado",
                "cacete",
                "desgraçado",
                "desgraça",
                "cacetes",
                "arrombados",
                "filhos da puta",
                "fodase",
                "foda-se",
                "fodam-se",
                "fodamse",
                "imundo",
                "nojento",
                "vadia",
                "puta",
                "arrombada",
                "arrombadas",
                "desgraçadas",
                "desgraçada"
            )

            val usuarioSnapshot = fb.collection("usuario")
                .whereEqualTo("codigo", codigoUsuario)
                .limit(1)
                .get()
                .await()

            if (usuarioSnapshot.isEmpty) {
                Log.e(
                    "RotinasBD",
                    "Usuário com código $codigoUsuario não encontrado na coleção 'usuario'."
                )
                return false
            }

            val usuarioDoc = usuarioSnapshot.documents[0]
            val usuarioCodigo = usuarioDoc.getString("codigo")
                ?: "" // esse valor tem q ser o mesmo que codigoUsuario
            if (usuarioCodigo.isEmpty()) {
                Log.e(
                    "RotinasBD",
                    "Campo 'codigo' não encontrado no documento do usuário $codigoUsuario."
                )
                return false
            }


            val livroSnapshot = fb.collection("livros")
                .whereEqualTo("codigo", livroCodigo)
                .limit(1)
                .get()
                .await()

            if (livroSnapshot.isEmpty) {
                Log.e("RotinasBD", "Livro com código $livroCodigo não encontrado.")
                return false
            }

            val timezone = TimeZone.getTimeZone("America/Sao_Paulo")

            val formatadorCompleto =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                    timeZone = timezone
                }

            val formatadorDia =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                    timeZone = timezone
                }

            val dataHoraAtualString = formatadorCompleto.format(Date())

            var suspeita = false

            for (palavra in palavrasSuspeitas) {
                if (comentario.toLowerCase().contains(palavra)) {
                    suspeita = true
                }
            }

            val dadosAvaliacao = hashMapOf<String, Any>(
                "codigo" to proximoCodigo("Reviews", 5, "0".first()),
                "livroCodigo" to livroCodigo,
                "usuario" to usuarioCodigo,
                "nota" to nota,
                "dataPublicacao" to dataHoraAtualString,
                "comentario" to comentario,
                "suspeita" to suspeita
            )

            fb.collection("Reviews").add(dadosAvaliacao).await()

            val livroDoc = livroSnapshot.documents[0]
            val qtdAvaliacoesAntiga = (livroDoc.get("qtdAvaliacoes") as? Long ?: 0L).toInt()
            val novaQtdAvaliacoes = qtdAvaliacoesAntiga + 1

            fb.collection("livros").document(livroDoc.id).update(
                "qtdAvaliacoes", novaQtdAvaliacoes
            ).await()



            Log.d(
                "RotinasBD",
                "Avaliação salva e livro atualizado com sucesso para livro $livroCodigo e usuário $usuarioCodigo."
            )
            true
        } catch (e: Exception) {
            Log.e("RotinasBD", "Erro ao salvar avaliação: ", e)
            false
        }
    }

    suspend fun editarSala(sala: Sala): Boolean {
        val fb = Firebase.firestore
        val salaCodigo = sala.numero // O campo 'sala' na coleção 'disponibilidades'

        return try {
            // --- 1. Edição da Sala Principal ---
            val salaQuery = fb.collection("salas").whereEqualTo("codigo", salaCodigo).get().await()
            val salaDocumentSnapshot = salaQuery.documents.firstOrNull()

            if (salaDocumentSnapshot == null) {
                Log.w("EditarSala", "Sala com código $salaCodigo não encontrada.")
                return false
            }
            val salaDocRef = fb.collection("salas").document(salaDocumentSnapshot.id)
            salaDocRef.update(mapOf("status" to sala.status)).await()


            // --- 2. Preparação das Chaves (Lei de Formação) ---

            // Cria um Set de chaves concatenadas com base nos DADOS DA APLICAÇÃO.
            // Chave: salaCodigo + dia + horaIni + horaFim
            val paramChavesUnicas = sala.disponibilidades.flatMap { disp ->
                disp.horarios.map { horario ->
                    "$salaCodigo${disp.dia}${horario.horaIni}${horario.horaFim}"
                }
            }.toSet()

            // --- 3. Busca e Remoção de Disponibilidades Excluídas ---

            // Pega todos os documentos de disponibilidade existentes no BD para esta sala
            val dbDisponibilidadesQuery = fb.collection("disponibilidades")
                .whereEqualTo("sala", salaCodigo)
                .get().await()

            // Filtra os documentos do BD que NÃO estão presentes nos parâmetros da aplicação
            val documentosParaExcluir = dbDisponibilidadesQuery.documents.filter { dbDoc ->
                val dbData = dbDoc.getString("data") ?: ""
                val dbHoraIni = dbDoc.getString("horaIni") ?: ""
                val dbHoraFim = dbDoc.getString("horaFim") ?: ""

                // Cria a chave de comparação (lei de formação) a partir dos dados DO BANCO
                val dbChaveConcatenada = "$salaCodigo$dbData$dbHoraIni$dbHoraFim"

                // Se a chave do BD (dbChaveConcatenada) não está no Set de chaves da aplicação, deve ser excluída.
                !paramChavesUnicas.contains(dbChaveConcatenada)
            }

            // Executa a exclusão usando o ID real do documento do Firestore
            for (dbDoc in documentosParaExcluir) {
                fb.collection("disponibilidades").document(dbDoc.id).delete().await()
            }

            // --- 4. Edição ou Criação de Disponibilidades ---

            for (disponibilidadeParam in sala.disponibilidades) {
                for (horarioParam in disponibilidadeParam.horarios) {

                    val dataToSave = mapOf(
                        "sala" to salaCodigo,
                        "data" to disponibilidadeParam.dia,
                        "horaIni" to horarioParam.horaIni,
                        "horaFim" to horarioParam.horaFim,
                        "usuario" to ""
                    )

                    // Busca o ID do documento real no BD usando a "lei de formação" (consulta de campos)
                    val buscaExistenteQuery = fb.collection("disponibilidades")
                        .whereEqualTo("sala", salaCodigo)
                        .whereEqualTo("data", disponibilidadeParam.dia)
                        .whereEqualTo("horaIni", horarioParam.horaIni)
                        .whereEqualTo("horaFim", horarioParam.horaFim)
                        .get().await()

                    val documentoExistente = buscaExistenteQuery.documents.firstOrNull()

                    if (documentoExistente != null) {
                        // Se o documento EXISTE, ATUALIZA (Edição) usando o ID do documento do Firestore
                        fb.collection("disponibilidades").document(documentoExistente.id)
                            .update(dataToSave)
                            .await()
                    } else {
                        // Se o documento NÃO EXISTE, CRIA
                        fb.collection("disponibilidades").add(dataToSave).await()
                    }
                }
            }

            return true

        } catch (e: Exception) {
            Log.e("EditarSala", "Erro ao editar sala e disponibilidades: ${e.message}", e)
            e.printStackTrace()
            return false
        }
    }

    suspend fun atualizarHorario(
        sala: String,
        data: String,
        horaIni: String,
        horaFim: String,
        usuario: String
    ): Boolean {
        return try {
            val fb = Firebase.firestore
            val query = fb.collection("disponibilidades")
                .whereEqualTo("sala", sala)
                .whereEqualTo("data", data)
                .whereEqualTo("horaIni", horaIni)
                .whereEqualTo("horaFim", horaFim)
                .get().await()

            if (!query.isEmpty) {

                val documentoDisponibilidade = query.documents.firstOrNull()?.let {
                    fb.collection("disponibilidades").document(it.id)
                }

                if (documentoDisponibilidade != null) {
                    documentoDisponibilidade.update(
                        mapOf("usuario" to usuario)
                    ).await()

                    criarNotificacao(
                        titulo = "Reserva de sala",
                        descricao = "Sala ${(sala.toInt()).toString()} reservada com sucesso das ${horaIni} as ${horaFim}!",
                        usuario = usuario,
                        redirecionamento = ""
                    )

                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deletarReview(codigo: String): Boolean {
        return try {
            fb = Firebase.firestore
            val query = fb.collection("Reviews")
                .whereEqualTo("codigo", codigo)
                .get()
                .await()
            val documentoReview =
                query.documents.firstOrNull()?.let { fb.collection("Reviews").document(it.id) }
            if (documentoReview != null) {
                documentoReview.delete().await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun lerReviewsSuspeitas(suspeitas: Boolean): MutableList<Review> {
        fb = Firebase.firestore
        var Reviews: MutableList<Review> = mutableListOf()
        var query = fb.collection("Reviews")
            .whereEqualTo("suspeita", suspeitas)
            .get()
            .await()
        for (review in query.documents) {

            val codigoUsuario = review.getString("usuario") ?: continue

            val queryUsuario = fb.collection("usuario")
                .whereEqualTo("codigo", codigoUsuario)
                .get()
                .await()

            if (queryUsuario.isEmpty) continue
            val snapshotUsuario = queryUsuario.documents[0]

            val objUsuario = Usuario(
                nome = snapshotUsuario.getString("nomeCompleto") ?: "",
                email = snapshotUsuario.getString("emailTelefone") ?: "",
                fotoPerfil = snapshotUsuario.getString("fotoPerfil") ?: ""
            )

            val codigoLivro = review.getString("livroCodigo") ?: ""

            val queryLivro = fb.collection("livros")
                .whereEqualTo("codigo", codigoLivro)
                .get()
                .await()

            val snapshotLivro = queryLivro.documents.firstOrNull()
            val nomeLivro = snapshotLivro?.getString("nome") ?: "Livro não encontrado"

            val objReview = Review(
                codigo = review.get("codigo") as String,
                usuario = objUsuario,
                dataPublicacao = review.getString("dataPublicacao") ?: "",
                nota = (review.getLong("nota") ?: 0L).toInt(),
                comentario = review.getString("comentario") ?: "",
                suspeita = review.getBoolean("suspeita") ?: false,
                gerenciavel = true,
                livroCodigo = nomeLivro
            )
            Reviews.add(objReview)
        }
        return Reviews
    }

    suspend fun deletarSala(codigo: String): Boolean {
        return try {
            fb = Firebase.firestore
            val query1 = fb.collection("salas")
                .whereEqualTo("codigo", codigo).get().await()

            val documentoSala =
                query1.documents.firstOrNull()?.let {
                    fb.collection("salas").document(it.id)

                }

            if (documentoSala != null) {
                documentoSala.delete().await()
            }

            val query2 = fb.collection("disponibilidades")
                .whereEqualTo("sala", codigo).get().await()

            for (disponibilidade in query2.documents) {
                val documentoDisponibilidade =
                    fb.collection("disponibilidades").document(disponibilidade.id)

                if (documentoDisponibilidade != null) {
                    documentoDisponibilidade.delete().await()
                }
            }

            true
        } catch (e: Exception) {
            Log.i("Exceção", e.message.toString())
            false
        }
    }

    suspend fun lerTodasReviews(): MutableList<Review> {
        fb = Firebase.firestore
        return try {
            val reviews: MutableList<Review> = mutableListOf()

            val query = fb.collection("Reviews")
                .get()
                .await()

            for (review in query.documents) {
                val codigoUsuario = review.getString("usuario") ?: continue

                val queryUsuario = fb.collection("usuario")
                    .whereEqualTo("codigo", codigoUsuario)
                    .get()
                    .await()

                if (queryUsuario.isEmpty) continue
                val snapshotUsuario = queryUsuario.documents[0]

                val objUsuario = Usuario(
                    nome = snapshotUsuario.getString("nomeCompleto") ?: "",
                    email = snapshotUsuario.getString("emailTelefone") ?: "",
                    fotoPerfil = snapshotUsuario.getString("fotoPerfil") ?: ""
                )

                val codigoLivro = review.getString("livroCodigo") ?: ""

                val queryLivro = fb.collection("livros")
                    .whereEqualTo("codigo", codigoLivro)
                    .get()
                    .await()

                val snapshotLivro = queryLivro.documents.firstOrNull()
                val nomeLivro = snapshotLivro?.getString("nome") ?: "Livro não encontrado"

                val objReview = Review(
                    codigo = review.get("codigo") as String,
                    usuario = objUsuario,
                    dataPublicacao = review.getString("dataPublicacao") ?: "",
                    nota = (review.getLong("nota") ?: 0L).toInt(),
                    comentario = review.getString("comentario") ?: "",
                    suspeita = review.getBoolean("suspeita") ?: false,
                    gerenciavel = true,
                    livroCodigo = nomeLivro
                )

                reviews.add(objReview)
            }
            reviews
        } catch (e: Exception) {
            mutableListOf()
        }

    }

    suspend fun deletarUsuario(codigo: String): Boolean {
        return try {
            fb = Firebase.firestore

            val query = fb.collection("usuario")
                .whereEqualTo("codigo", codigo)
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) {
                return false
            }

            val docRef = fb.collection("usuario").document(query.documents[0].id)

            docRef.delete().await()

            true

        } catch (e: Exception) {
            false
        }
    }


    suspend fun lerUsuarioPorCodigo(codigo: String): Usuario? {
        return try {
            fb = Firebase.firestore

            val query = fb.collection("usuario")
                .whereEqualTo("codigo", codigo)
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) return null

            val doc = query.documents[0]

            val usuario = Usuario(
                nome = doc.getString("nomeCompleto") ?: "",
                email = doc.getString("emailTelefone") ?: "",
                fotoPerfil = doc.getString("fotoPerfil") ?: "",
                codigo = doc.getString("codigo") ?: ""
            )

            usuario

        } catch (e: Exception) {
            null
        }
    }

    suspend fun lerCampoDoUsuarioPorCampoCodigo(codigo: String, campo: String): Any? {
        try {
            fb = Firebase.firestore
            val query = fb.collection("usuario")
                .whereEqualTo("codigo", codigo)
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) return null
            val doc = query.documents[0]
            return doc.get(campo)
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun atualizarCampoUsuarioPorCampoCodigo(
        codigo: String,
        campo: String,
        valor: Any
    ): Boolean {
        try {
            fb = Firebase.firestore
            val query = fb.collection("usuario")
                .whereEqualTo("codigo", codigo)
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) return false
            val docRef = fb.collection("usuario").document(query.documents[0].id)
            docRef.update(campo, valor).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }


//    suspend fun deletarSala(codigo: String): Boolean {
//        return try {
//            fb = Firebase.firestore
//            val query1 = fb.collection("salas")
//                .whereEqualTo("codigo", codigo).get().await()
//
//            val objReview = Review(
//                codigo = codigoUsuario,
//                usuario = objUsuario,
//                dataPublicacao = review.get("dataPublicacao") as String,
//                nota = (review.getLong("nota") ?: 0L).toInt(),
//                comentario = review.get("comentario") as String,
//                suspeita = review.get("suspeita") as Boolean,
//                gerenciavel = true
//            );
//            Reviews.add(objReview);
//        }
//        return Reviews
//            val documentoSala =
//                query1.documents.firstOrNull()?.let {
//                    fb.collection("salas").document(it.id)
//
//                }
//
//            if (documentoSala != null) {
//                documentoSala.delete().await()
//            }
//
//            val query2 = fb.collection("disponibilidades")
//                .whereEqualTo("sala", codigo).get().await()
//
//            for (disponibilidade in query2.documents) {
//                val documentoDisponibilidade =
//                    query1.documents.firstOrNull()?.let {
//                        fb.collection("disponibilidades").document(it.id)
//                    }
//
//                if (documentoDisponibilidade != null) {
//                    documentoDisponibilidade.delete().await()
//                }
//            }
//
//            true
//        } catch (e: Exception) {
//            Log.i("Exceção", e.message.toString())
//            false
//        }
//    }
//
//
//}
}