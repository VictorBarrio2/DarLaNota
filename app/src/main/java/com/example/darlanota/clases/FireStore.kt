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
            val documento = db.collection("actividades").add(actividad).await()
            Log.d("FireStore", "Actividad añadida con éxito, ID: ${documento.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error al añadir actividad: ${e.localizedMessage}", e)
            registrarIncidencia("Error al añadir actividad: ${e.localizedMessage}")
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

}
