package com.example.darlanota.clases

import android.util.Log
import android.widget.Toast
import com.example.darlanota.modelos.PaginaLogin
import com.google.api.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.withContext

class FireStore {
    // Instancia de Firestore para acceder a la base de datos de Firebase.
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
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

    suspend fun incrementarPuntuacionAlumno(idAlumno: String, valorIncremento: Int) = withContext(Dispatchers.IO) {
        try {
            val alumnoRef = db.collection("alumnos").document(idAlumno)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(alumnoRef)
                val puntuacionActual = snapshot.getLong("puntuacion") ?: 0L  // Obtiene la puntuación actual o 0 si no existe
                val nuevaPuntuacion = puntuacionActual + valorIncremento
                transaction.update(alumnoRef, "puntuacion", nuevaPuntuacion)
                nuevaPuntuacion  // Retorna la nueva puntuación
            }.await()
        } catch (e: Exception) {
            Log.e("Firestore", "Error al incrementar la puntuación: ${e.localizedMessage}", e)
        }
    }

    suspend fun obtenerNombreAlumno(idAlumno: String): String? = withContext(Dispatchers.IO) {
        try {
            val docAlumno = db.collection("usuarios").document(idAlumno).get().await()
            if (docAlumno.exists()) {
                docAlumno.getString("nombre")
            } else {
                Log.e("Firestore", "No se encontró el alumno con ID: $idAlumno")
                null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener el nombre del alumno: ${e.localizedMessage}", e)
            null
        }
    }

}
