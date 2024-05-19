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

class FireStore {
    // Instancia de Firestore para acceder a la base de datos de Firebase.
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Método para añadir un usuario a la base de datos.
    // Usuario puede ser un Alumno o un Profesor.
    // El tipo de usuario se maneja internamente según la clase del objeto.
    suspend fun altaUsuario(id: String, usuario: Usuario) = withContext(Dispatchers.IO) {
        try {
            // Usa 'set' en lugar de 'add' para poder especificar el ID del documento.
            db.collection("usuarios").document(id).set(usuario).await()
            "Usuario añadido con éxito con ID: $id"
        } catch (e: Exception) {
            Log.e("FireStore", "Error al añadir usuario: ${e.localizedMessage}", e)
            "Error al añadir usuario: ${e.localizedMessage}"
        }
    }

    suspend fun altaActividad(actividad: Actividad) = withContext(Dispatchers.IO) {
        try {
            val documento = db.collection("actividades").add(actividad).await()
            Log.d("FireStore", "Actividad añadida con éxito, ID: ${documento.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error al añadir actividad: ${e.localizedMessage}", e)
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

    suspend fun obtenerCalificacionEntrega(idActividad: String, idAlumno: String): Int? = withContext(Dispatchers.IO) {
        try {
            val actividadRef = db.collection("actividades").document(idActividad)
            val resultado = actividadRef.get().await()
            if (resultado.exists()) {
                val entregas = resultado.get("entregas") as? ArrayList<Map<String, Any>> ?: ArrayList()
                val entrega = entregas.find { it["idAlumno"] == idAlumno }
                if (entrega != null) {
                    Log.d("FireStore", "Entrega encontrada: $entrega")
                    val calificacion = entrega["calificacion"]
                    Log.d("FireStore", "Calificación obtenida: $calificacion, tipo: ${calificacion?.javaClass?.name}")
                    (calificacion as? Long)?.toInt() ?: calificacion as? Int
                } else {
                    Log.d("FireStore", "No se encontró entrega para el alumno con id: $idAlumno")
                    null
                }
            } else {
                Log.d("FireStore", "El documento de actividad con id $idActividad no existe")
                null
            }
        } catch (e: Exception) {
            Log.e("FireStore", "Error al obtener la calificación de la entrega: ${e.localizedMessage}", e)
            null
        }
    }


}
