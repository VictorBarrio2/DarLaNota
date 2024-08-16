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
import com.google.firebase.storage.ktx.storage

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

    suspend fun actualizarVideoEntrega(idActividad: String, nickAlumno: String, nuevoVideo: String) {
        val actividadRef = db.collection("actividades").document(idActividad)
        try {
            db.runTransaction { transaction ->
                val actividadSnapshot = transaction.get(actividadRef)
                if (actividadSnapshot.exists()) {
                    val entregas = actividadSnapshot.get("entregas") as? MutableList<Map<String, Any>> ?: mutableListOf()
                    Log.d("Firestore", "Entregas encontradas: $entregas")

                    val entregaIndex = entregas.indexOfFirst { it["nickAlumno"] == nickAlumno }
                    if (entregaIndex != -1) {
                        val entregaActualizada = entregas[entregaIndex].toMutableMap()
                        entregaActualizada["video"] = nuevoVideo
                        entregas[entregaIndex] = entregaActualizada
                        transaction.update(actividadRef, "entregas", entregas)
                        Log.d("Firestore", "Entrega actualizada para el alumno: $nickAlumno")
                    } else {
                        throw RuntimeException("Entrega no encontrada para el alumno: $nickAlumno")
                    }
                } else {
                    throw RuntimeException("Actividad no encontrada: $idActividad")
                }
            }.await()
        } catch (e: Exception) {
            Log.e("Firestore", "Error al actualizar video en Firestore: ${e.localizedMessage}", e)
            throw RuntimeException("Error al actualizar video en Firestore: ${e.localizedMessage}")
        }
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

    suspend fun incrementarPuntuacionUsuario(nombreUsuario: String, valorAIncrementar: Int?) {
        try {
            // Asegúrate de que valorAIncrementar no sea nulo
            val incremento = valorAIncrementar ?: 0

            // Primero, obtenemos el documento del usuario por su nombre
            val querySnapshot = db.collection("usuarios")
                .whereEqualTo("nombre", nombreUsuario)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                registrarIncidencia("No se encontró un usuario con el nombre: $nombreUsuario")
                throw RuntimeException("No se encontró un usuario con el nombre: $nombreUsuario")
            }

            val documentoUsuario = querySnapshot.documents[0] // Asumimos que el nombre es único y solo hay un documento
            val referenciaUsuario = documentoUsuario.reference

            // Luego, ejecutamos la transacción para incrementar la puntuación
            db.runTransaction { transaccion ->
                val instantanea = transaccion.get(referenciaUsuario)
                val puntuacionActual = instantanea.getLong("puntuacion") ?: 0
                transaccion.update(referenciaUsuario, "puntuacion", puntuacionActual + incremento)
            }.await()
        } catch (e: Exception) {
            registrarIncidencia("Error al incrementar la puntuación: ${e.message}")
            throw RuntimeException("Error al incrementar la puntuación: ${e.message}")
        }
    }



    suspend fun decrementarPuntuacionAlumno(nombreUsuario: String, valorDecremento: Int) {
        try {
            // Primero, obtenemos el documento del usuario por su nombre
            val querySnapshot = db.collection("usuarios")
                .whereEqualTo("nombre", nombreUsuario)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val documentoUsuario = querySnapshot.documents[0] // Asumimos que el nombre es único y solo hay un documento
                val referenciaUsuario = documentoUsuario.reference

                // Luego, ejecutamos la transacción para decrementar la puntuación
                db.runTransaction { transaccion ->
                    val instantanea = transaccion.get(referenciaUsuario)
                    val puntuacionActual = instantanea.getLong("puntuacion") ?: 0
                    // Calculamos la nueva puntuación, asegurándonos de que no sea negativa
                    val nuevaPuntuacion = (puntuacionActual - valorDecremento).coerceAtLeast(0)
                    transaccion.update(referenciaUsuario, "puntuacion", nuevaPuntuacion)
                }.await()
            } else {
                registrarIncidencia("No se encontró un usuario con el nombre: $nombreUsuario")
                throw RuntimeException("No se encontró un usuario con el nombre: $nombreUsuario")
            }
        } catch (e: Exception) {
            registrarIncidencia("Error al decrementar la puntuación: ${e.message}")
            throw RuntimeException("Error al decrementar la puntuación: ${e.message}")
        }
    }
    suspend fun actualizarCalificacionEntrega(idActividad: String, nombreAlumno: String, nuevaCalificacion: Int) = withContext(Dispatchers.IO) {
        try {
            // Obtener el documento del alumno por su nombre
            val alumnoQuerySnapshot = db.collection("usuarios")
                .whereEqualTo("nombre", nombreAlumno)
                .get()
                .await()

            if (alumnoQuerySnapshot.isEmpty) {
                Log.e("Firestore", "No se encontró un usuario con el nombre: $nombreAlumno")
                return@withContext
            }

            val idAlumno = alumnoQuerySnapshot.documents[0].id

            // Referencia al documento de la actividad en Firestore
            val actividadRef = db.collection("actividades").document(idActividad)
            db.runTransaction { transaction ->
                // Obtener el documento de la actividad
                val actividadSnapshot = transaction.get(actividadRef)
                if (!actividadSnapshot.exists()) {
                    Log.e("Firestore", "No se encontró la actividad: $idActividad")
                    return@runTransaction
                }

                // Obtener y actualizar las entregas
                val entregas = actividadSnapshot.get("entregas") as? MutableList<Map<String, Any>> ?: mutableListOf()
                val entregaIndex = entregas.indexOfFirst { it["nickAlumno"] == nombreAlumno }
                if (entregaIndex == -1) {
                    Log.e("Firestore", "No se encontró la entrega del alumno: $nombreAlumno")
                    return@runTransaction
                }

                // Actualizar la calificación de la entrega
                val entregaActualizada = entregas[entregaIndex].toMutableMap()
                entregaActualizada["calificacion"] = nuevaCalificacion
                entregas[entregaIndex] = entregaActualizada
                transaction.update(actividadRef, "entregas", entregas)
            }.await()
        } catch (e: Exception) {
            Log.e("Firestore", "Error al actualizar la calificación: ${e.localizedMessage}", e)
            registrarIncidencia("Error al actualizar la calificación: ${e.localizedMessage}")
        }
    }

    suspend fun obtenerRutaVideo(idActividad: String, idAlumno: String): String? = withContext(Dispatchers.IO) {
        try {
            // Obtén el documento de la actividad desde Firestore
            val actividadDoc = db.collection("actividades").document(idActividad).get().await()

            // Obtén la lista de entregas desde el documento de la actividad
            val entregas = actividadDoc.get("entregas") as? List<Map<String, Any>>

            // Encuentra la entrega correspondiente al alumno especificado
            val entrega = entregas?.find { it["nickAlumno"] == idAlumno }

            // Obtén la ruta del video de la entrega, si existe
            val videoPath = entrega?.get("video") as? String

            // Registra la ruta del video en los logs para depuración
            Log.d("Firestore", "Ruta del video obtenida: $videoPath")

            videoPath
        } catch (e: Exception) {
            // En caso de excepción, registra un error en los logs
            Log.e("Firestore", "Error al obtener la ruta del video: ${e.localizedMessage}", e)

            // Registra la incidencia en el sistema
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

    suspend fun eliminarActividad(idActividad: String) = withContext(Dispatchers.IO) {
        try {
            // Obtener el documento de la actividad
            val actividadDoc = db.collection("actividades").document(idActividad).get().await()

            // Obtener la lista de entregas asociadas a la actividad
            val entregas = actividadDoc.get("entregas") as? List<Map<String, Any>>

            // Iterar sobre cada entrega para eliminar el video correspondiente
            entregas?.forEach { entrega ->
                val videoPath = entrega["video"] as? String
                if (videoPath != null) {
                    try {
                        // Eliminar el video de la ruta en el almacenamiento
                        eliminarVideoDeStorage(videoPath)
                        Log.d("Firestore", "Video eliminado: $videoPath")
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error al eliminar el video: ${e.localizedMessage}", e)
                        registrarIncidencia("Error al eliminar el video: ${e.localizedMessage}")
                    }
                }
            }

            // Eliminar el documento de la actividad en Firestore
            db.collection("actividades").document(idActividad).delete().await()
            Log.d("Firestore", "Actividad eliminada correctamente")
        } catch (e: Exception) {
            // Manejar cualquier error que ocurra durante el proceso
            registrarIncidencia("Error al eliminar la actividad: ${e.localizedMessage}")
            Log.e("Firestore", "Error al eliminar la actividad: ${e.localizedMessage}", e)
        }
    }

    private suspend fun eliminarVideoDeStorage(videoPath: String) {
        try {
            val storageRef = if (videoPath.startsWith("https://")) {
                Firebase.storage.getReferenceFromUrl(videoPath)
            } else {
                Firebase.storage.reference.child(videoPath)
            }
            storageRef.delete().await()
            Log.d("Firestore", "Video eliminado: $videoPath")
        } catch (e: Exception) {
            Log.e("Firestore", "Error al eliminar el video: ${e.localizedMessage}", e)
            registrarIncidencia("Error al eliminar el video: ${e.localizedMessage}")
        }
    }



    suspend fun obtenerRankingUsuarios(): List<Pair<String, Long>> = withContext(Dispatchers.IO) {
        try {
            val usuariosSnapshot = db.collection("usuarios")
                .orderBy("puntuacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            val rankingUsuarios = mutableListOf<Pair<String, Long>>()

            for (documento in usuariosSnapshot.documents) {
                val tipo = documento.getString("tipo") ?: "alumno"
                if(tipo.equals("alumno")){
                    val nombre = documento.getString("nombre") ?: "Sin nombre"
                    val puntuacion = documento.getLong("puntuacion") ?: 0L
                    rankingUsuarios.add(Pair(nombre, puntuacion))
                }
            }

            rankingUsuarios
        } catch (e: Exception) {
            registrarIncidencia("Error al obtener el ranking de usuarios: ${e.localizedMessage}")
            Log.e("Firestore", "Error al obtener el ranking de usuarios: ${e.localizedMessage}", e)
            emptyList()
        }
    }


    suspend fun obtenerNombreUsuario(idAlumno: String): String? = withContext(Dispatchers.IO) {
        try {
            val alumnoDoc = db.collection("usuarios").document(idAlumno).get().await()
            alumnoDoc.getString("nombre")
        } catch (e: Exception) {
            registrarIncidencia("Error al obtener el nombre del alumno: ${e.localizedMessage}")
            Log.e("Firestore", "Error al obtener el nombre del alumno: ${e.localizedMessage}", e)
            null
        }
    }


    suspend fun cambiarContrasenaUsuario(nuevaContrasena: String, nick: String) = withContext(Dispatchers.IO) {
        try {
            // Obtener referencia a la colección de usuarios
            val usuariosRef = db.collection("usuarios")

            // Buscar el documento del usuario con el nick dado
            val querySnapshot = usuariosRef.whereEqualTo("nick", nick).get().await()
            if (querySnapshot.isEmpty) {
                throw RuntimeException("Usuario con nick $nick no encontrado.")
            }

            // Suponiendo que hay un único usuario con ese nick
            val usuarioDoc = querySnapshot.documents.first()

            // Actualizar la contraseña
            usuarioDoc.reference.update("contraseña", nuevaContrasena).await()

            Log.d("Firestore", "Contraseña actualizada exitosamente para el usuario: $nick")

        } catch (e: Exception) {
            registrarIncidencia("Error al cambiar la contraseña: ${e.localizedMessage}")
            Log.e("Firestore", "Error al cambiar la contraseña: ${e.localizedMessage}", e)
            throw RuntimeException("Error al cambiar la contraseña: ${e.localizedMessage}")
        }
    }

    suspend fun reiniciarRanking() = withContext(Dispatchers.IO) {
        try {
            // Obtener la colección de usuarios
            val usuariosCollection = db.collection("usuarios").get().await()

            // Recorrer todos los documentos (alumnos)
            for (document in usuariosCollection.documents) {
                val idAlumno = document.id

                // Reiniciar la puntuación del alumno a 0 o el valor que desees
                db.collection("usuarios").document(idAlumno)
                    .update("puntuacion", 0) // Cambia "0" por el valor predeterminado si es necesario
                    .await()
            }

            // Puedes agregar aquí un log o un mensaje para confirmar que la operación fue exitosa
            Log.d("Ranking", "El ranking de los alumnos ha sido reiniciado.")
        } catch (e: Exception) {
            // Maneja cualquier excepción que ocurra durante la operación
            e.printStackTrace()
            Log.e("Ranking", "Error al reiniciar el ranking: ${e.message}")
        }
    }
    suspend fun devolverPuntuacion(idAlumno: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = db.collection("usuarios").document(idAlumno).get().await()
            if (doc.exists()) {
                val puntuacionStr = doc.getString("puntuacion")
                return@withContext puntuacionStr
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            // Maneja la excepción (puedes hacer logging o lo que sea necesario)
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun obtenerCalificacion(idActividad: String, idAlumno: String): Int? {
        val actividad = cargarActividadCompleta(idActividad)
        return actividad.entregas.find { it.nickAlumno == idAlumno }?.calificacion
    }

    suspend fun cargarActividadCompleta(idActividad: String): Actividad {
        val doc = db.collection("actividades").document(idActividad).get().await()
        return doc.toObject(Actividad::class.java) ?: Actividad()
    }

    suspend fun obtenerNombresDeEntregas(idActividad: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // Accedemos al documento de la actividad
            val actividadDoc = db.collection("actividades").document(idActividad).get().await()

            // Verificamos si el documento existe
            if (actividadDoc.exists()) {
                // Obtenemos la lista de entregas
                val entregas = actividadDoc.get("entregas") as? List<Map<String, Any>> ?: emptyList()

                // Mapeamos cada entrega al campo "nickAlumno" (nombre del alumno)
                entregas.mapNotNull { it["nickAlumno"] as? String }
            } else {
                Log.e("Firestore", "Actividad no encontrada: $idActividad")
                emptyList() // Retorna una lista vacía si la actividad no existe
            }
        } catch (e: Exception) {
            // En caso de error, registra la incidencia y retorna una lista vacía
            registrarIncidencia("Error al obtener nombres de entregas: ${e.localizedMessage}")
            Log.e("Firestore", "Error al obtener nombres de entregas: ${e.localizedMessage}", e)
            emptyList()
        }
    }

}
