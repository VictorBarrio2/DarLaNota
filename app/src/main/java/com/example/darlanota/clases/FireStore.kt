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
    suspend fun añadirUsuario(id: String, usuario: Usuario) = withContext(Dispatchers.IO) {
        try {
            // Usa 'set' en lugar de 'add' para poder especificar el ID del documento.
            db.collection("usuarios").document(id).set(usuario).await()
            "Usuario añadido con éxito con ID: $id"
        } catch (e: Exception) {
            Log.e("FireStore", "Error al añadir usuario: ${e.localizedMessage}", e)
            "Error al añadir usuario: ${e.localizedMessage}"
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
