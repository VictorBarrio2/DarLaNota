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
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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


    suspend fun existeEntrega(idActividad: String, idAlumno: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val actividadRef = db.collection("actividades").document(idActividad)
            val resultado = actividadRef.get().await()
            if (resultado.exists()) {
                val entregas = resultado.get("entregas") as? ArrayList<Map<String, Any>> ?: ArrayList()
                // Verifica si alguna de las entregas en la lista tiene el idAlumno
                entregas.any { entrega -> entrega["idAlumno"] == idAlumno }
            } else {
                false // No existe el documento de actividad, por lo tanto no puede haber entregas
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun actualizarVideoEntrega(idActividad: String, idAlumno: String, nuevoVideo: String) {
        try {
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
        } catch (e: Exception) {
            Log.e("Firestore", "Error al actualizar el video: ${e.localizedMessage}", e)
            throw e // Puedes manejar el error como creas conveniente
        }
    }
    // Método para buscar un usuario por su nombre en Firestore.
    suspend fun buscarAlumnoPorId(id: String): Alumno? = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        try {
            val documento = db.collection("usuarios").document(id).get().await()
            if (documento.exists() && documento.getString("tipo") == "alumno") {
                documento.toObject<Alumno>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun buscarProfesorPorId(id: String): Profesor? = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        try {
            val documento = db.collection("usuarios").document(id).get().await()
            if (documento.exists() && documento.getString("tipo") == "profesor") {
                documento.toObject<Profesor>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}
