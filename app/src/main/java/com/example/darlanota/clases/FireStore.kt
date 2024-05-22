package com.example.darlanota.clases

import com.example.darlanota.modelos.PaginaCorregirActividad
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import java.io.File
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
class FireStore {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    suspend fun altaUsuario(id: String, usuario: Usuario) = withContext(Dispatchers.IO) {
        try {
            // Usa 'set' en lugar de 'add' para poder especificar el ID del documento.
            db.collection("usuarios").document(id).set(usuario).await()
            "Usuario añadido con éxito con ID: $id"
        } catch (e: Exception) {
            registrarIncidencia("Error al añadir usuario: ${e.localizedMessage}")
            Log.e("FireStore", "Error al añadir usuario: ${e.localizedMessage}", e)
            "Error al añadir usuario: ${e.localizedMessage}"
        }
    }

    suspend fun altaActividad(actividad: Actividad) = withContext(Dispatchers.IO) {
        try {
            db.collection("actividades").add(actividad).await()
            Log.d("Firestore", "Actividad añadida correctamente")
        } catch (e: Exception) {
            val firestore = FireStore()
            firestore.registrarIncidencia("Error al añadir actividad: ${e.localizedMessage}")
            Log.e("Firestore", "Error al añadir actividad: ${e.localizedMessage}", e)
        }
    }

    suspend fun cargarActividades(): List<Actividad> = withContext(Dispatchers.IO) {
        val actividadesList = mutableListOf<Actividad>()
        try {
            val documentos = db.collection("actividades").get().await()
            for (documento in documentos) {
                val actividad = documento.toObject<Actividad>().apply {
                    id = documento.id  // Asegúrate de que la clase Actividad tiene un campo 'id'
                    Log.e("FireStore", "La ID es: " + id)
                }
                actividadesList.add(actividad)
            }
        } catch (e: Exception) {
            Log.e("FireStore", "Error al cargar actividades: ${e.localizedMessage}", e)
            registrarIncidencia("Error al cargar actividades: ${e.localizedMessage}")
        }
        actividadesList  // Retornar la lista de actividades
    }

    suspend fun altaEntrega(idActividad: String, nuevaEntrega: Entrega): Result<String> = withContext(Dispatchers.IO) {
        try {
            val actividadRef = db.collection("actividades").document(idActividad)
            db.runTransaction { transaction ->
                val actividadSnapshot = transaction.get(actividadRef)
                val entregas = actividadSnapshot.get("entregas") as? ArrayList<Entrega> ?: ArrayList()
                entregas.add(nuevaEntrega)
                transaction.update(actividadRef, "entregas", entregas)
            }.await()
            Result.success("Entrega agregada exitosamente a la actividad")
        } catch (e: Exception) {
            registrarIncidencia(e.localizedMessage)
            Result.failure(e)
        }
    }

    suspend fun actualizarVideoEntrega(idActividad: String, idAlumno: String, nuevoVideo: String) {
        val actividadRef = db.collection("actividades").document(idActividad)
        db.runTransaction { transaction ->
            val actividadSnapshot = transaction.get(actividadRef)
            val entregas = actividadSnapshot.get("entregas") as? ArrayList<HashMap<String, Any>> ?: ArrayList()
            val entregaIndex = entregas.indexOfFirst { it["idAlumno"] == idAlumno }
            if (entregaIndex != -1) {
                entregas[entregaIndex]["video"] = nuevoVideo
                transaction.update(actividadRef, "entregas", entregas)
            }
        }.await()
    }
    suspend fun registrarIncidencia(descripcion: String) {
        val nuevaIncidencia = Incidencia(
            fechafin = Timestamp(Date()),
            descripcion = descripcion
        )
        db.collection("incidencias").add(nuevaIncidencia).await()
    }


    suspend fun obtenerIdsDeAlumnosDeEntregasPorActividad(idActividad: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // Accede al documento de la actividad
            val actividadDoc = db.collection("actividades").document(idActividad).get().await()
            if (actividadDoc.exists()) {
                // Obtiene el array de entregas del documento de la actividad
                val entregas = actividadDoc.get("entregas") as? List<Map<String, Any>> ?: emptyList()
                // Mapea cada objeto de entrega a la ID del alumno
                entregas.mapNotNull { entrega ->
                    entrega["idAlumno"] as? String
                }
            } else {
                emptyList() // Devuelve una lista vacía si no se encuentra el documento
            }
        } catch (e: Exception) {
            registrarIncidencia("Error al obtener las IDs de los alumnos de las entregas: ${e.localizedMessage}")
            Log.e("Firestore","Error al obtener las IDs de los alumnos de las entregas: ${e.localizedMessage}", e)
            emptyList()
        }
    }

    suspend fun incrementarPuntuacionUsuario(idUsuario: String, valorAIncrementar: Int?) {
        try {
            val referenciaUsuario = db.collection("usuarios").document(idUsuario)
            db.runTransaction { transaccion ->
                val instantanea = transaccion.get(referenciaUsuario)
                val puntuacionActual = instantanea.getLong("puntuacion") ?: 0
                transaccion.update(referenciaUsuario, "puntuacion", puntuacionActual + valorAIncrementar!!)
            }.await()
        } catch (e: Exception) {
            throw RuntimeException("Error al incrementar la puntuación: ${e.message}")
        }
    }

    suspend fun decrementarPuntuacionAlumno(idUsuario: String, valorDecremento: Int) {
        try {
            val referenciaUsuario = db.collection("usuarios").document(idUsuario)
            db.runTransaction { transaccion ->
                val instantanea = transaccion.get(referenciaUsuario)
                val puntuacionActual = instantanea.getLong("puntuacion") ?: 0
                val nuevaPuntuacion = (puntuacionActual - valorDecremento).coerceAtLeast(0)  // Evita valores negativos
                transaccion.update(referenciaUsuario, "puntuacion", nuevaPuntuacion)
            }.await()
        } catch (e: Exception) {
            throw RuntimeException("Error al decrementar la puntuación: ${e.message}")
        }
    }


    suspend fun obtenerIdPorNombre(nombreAlumno: String): String? {
        try {
            // Realizar una consulta a la colección "usuarios"
            val resultado = db.collection("usuarios")
                .whereEqualTo("nombre", nombreAlumno)
                .get()
                .await()

            // Comprobar si se encontraron documentos
            if (resultado.documents.isNotEmpty()) {
                // Devolver el ID del primer documento que coincida con el nombre
                return resultado.documents.first().id
            } else {
                // Devolver null si no se encuentra ningún documento
                return null
            }
        } catch (e: Exception) {
            throw RuntimeException("Error al obtener la ID por nombre: ${e.message}")
        }
    }

    suspend fun actualizarCalificacionEntrega(idActividad: String, idAlumno: String, nuevaCalificacion: Int) = withContext(Dispatchers.IO) {
        try {
            val actividadRef = db.collection("actividades").document(idActividad)
            db.runTransaction { transaction ->
                val actividadSnapshot = transaction.get(actividadRef)
                if (actividadSnapshot.exists()) {
                    val entregas = actividadSnapshot.get("entregas") as? MutableList<Map<String, Any>> ?: mutableListOf()
                    val entregaIndex = entregas.indexOfFirst { it["idAlumno"] == idAlumno }
                    if (entregaIndex != -1) {
                        val entregaActualizada = entregas[entregaIndex].toMutableMap()
                        entregaActualizada["calificacion"] = nuevaCalificacion
                        entregas[entregaIndex] = entregaActualizada
                        transaction.update(actividadRef, "entregas", entregas)
                    } else {
                        Log.e("Firestore", "No se encontró la entrega del alumno: $idAlumno")
                    }
                } else {
                    Log.e("Firestore", "No se encontró la actividad: $idActividad")
                }
            }.await()
        } catch (e: Exception) {
            Log.e("Firestore", "Error al actualizar la calificación: ${e.localizedMessage}", e)
            registrarIncidencia("Error al actualizar la calificación: ${e.localizedMessage}")
        }
    }

    suspend fun obtenerRutaVideo(idActividad: String, idAlumno: String): String? = withContext(Dispatchers.IO) {
        try {
            val actividadDoc = db.collection("actividades").document(idActividad).get().await()
            val entregas = actividadDoc.get("entregas") as List<Map<String, Any>>?
            val entrega = entregas?.find { it["idAlumno"] == idAlumno }
            val videoPath = entrega?.get("video") as String?
            Log.d("Firestore", "Video path obtenida: $videoPath")
            videoPath
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener la ruta del video: ${e.localizedMessage}", e)
            registrarIncidencia("Error al obtener la ruta del video: ${e.localizedMessage}")
            null
        }
    }

    suspend fun descargarVideo(context: PaginaCorregirActividad, videoPath: String) = withContext(Dispatchers.IO) {
        try {
            val storageRef = storage.reference
            val videoRef = storageRef.child(videoPath)
            val localFile = File.createTempFile("video", ".mp4", context.cacheDir)

            videoRef.getFile(localFile).await()

            saveVideoToGallery(context, localFile)

            Log.d("FirebaseStorage", "Video descargado exitosamente en ${localFile.absolutePath}")
        } catch (e: Exception) {
            registrarIncidencia("Error al descargar el video: ${e.localizedMessage}")
            Log.e("FirebaseStorage", "Error al descargar el video: ${e.localizedMessage}", e)
        }
    }

    private fun saveVideoToGallery(context: PaginaCorregirActividad, file: File) {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/")
            } else {
                val videosDir = File(context.getExternalFilesDir(null), "Movies")
                if (!videosDir.exists()) {
                    videosDir.mkdirs()
                }
                put(MediaStore.Video.Media.DATA, File(videosDir, file.name).absolutePath)
            }
        }

        val resolver = context.contentResolver
        resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)?.also { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }

    // Método para obtener el nombre del alumno

    suspend fun eliminarActividad(idActividad: String) = withContext(Dispatchers.IO) {
        try {
            db.collection("actividades").document(idActividad).delete().await()
            Log.d("Firestore", "Actividad eliminada correctamente")
        } catch (e: Exception) {
            registrarIncidencia("Error al eliminar la actividad: ${e.localizedMessage}")
            Log.e("Firestore", "Error al eliminar la actividad: ${e.localizedMessage}", e)
        }
    }

    suspend fun obtenerRankingUsuarios(): List<Pair<String, Long>> = withContext(Dispatchers.IO) {
        try {
            val usuariosSnapshot = db.collection("usuarios")
                .orderBy("puntuacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            val rankingUsuarios = mutableListOf<Pair<String, Long>>()

            for (documento in usuariosSnapshot.documents) {
                val nombre = documento.getString("nombre") ?: "Sin nombre"
                val puntuacion = documento.getLong("puntuacion") ?: 0L
                rankingUsuarios.add(Pair(nombre, puntuacion))
            }

            rankingUsuarios
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener el ranking de usuarios: ${e.localizedMessage}", e)
            emptyList()
        }
    }


    suspend fun obtenerNombreUsuario(idAlumno: String): String? = withContext(Dispatchers.IO) {
        try {
            val alumnoDoc = db.collection("usuarios").document(idAlumno).get().await()
            alumnoDoc.getString("nombre")
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener el nombre del alumno: ${e.localizedMessage}", e)
            null
        }
    }

    suspend fun cambiarContrasenaUsuario(nuevaContrasena: String) = withContext(Dispatchers.IO) {
        try {
            val usuario = Firebase.auth.currentUser // Obtiene el usuario actualmente autenticado

            usuario?.let {
                // Actualizar la contraseña en Firebase Auth
                it.updatePassword(nuevaContrasena).await()

                // Actualizar la contraseña en Firestore
                val usuarioRef = db.collection("usuarios").document(it.uid)
                db.runTransaction { transaction ->
                    transaction.update(usuarioRef, "contrasena", nuevaContrasena)
                }.await()

                Log.d("Firestore", "Contraseña actualizada correctamente para el usuario: ${it.uid}")
                "Contraseña actualizada exitosamente"
            } ?: throw Exception("No hay usuario autenticado")
        } catch (e: Exception) {
            registrarIncidencia("Error al cambiar la contraseña: ${e.localizedMessage}")
            Log.e("Firestore", "Error al cambiar la contraseña: ${e.localizedMessage}", e)
            "Error al cambiar la contraseña: ${e.localizedMessage}"
        }
    }

    suspend fun obtenerCalificacion(idActividad: String, idAlumno: String): Int? {
        val actividad = cargarActividadCompleta(idActividad)
        return actividad.entregas.find { it.idAlumno == idAlumno }?.calificacion
    }

    suspend fun cargarActividadCompleta(idActividad: String): Actividad {
        val doc = db.collection("actividades").document(idActividad).get().await()
        return doc.toObject(Actividad::class.java) ?: Actividad()
    }

}
