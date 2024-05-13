package com.example.darlanota.clases

import android.util.Log
import com.example.darlanota.clases.Alumno
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
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
    suspend fun añadirUsuario(usuario: Usuario) = withContext(Dispatchers.IO) {
        try {
            db.collection("usuarios").add(usuario).await()
            "Usuario añadido con éxito"
        } catch (e: Exception) {
            Log.e("FireStore", "Error al añadir usuario: ${e.localizedMessage}", e)
            "Error al añadir usuario: ${e.localizedMessage}"
        }
    }

    // Método para obtener todos los alumnos de la base de datos.
    // Filtra por el campo 'tipo' que debe ser igual a "alumno".
    suspend fun obtenerAlumnos(): List<Alumno> = withContext(Dispatchers.IO) {
        try {
            // Realiza la consulta a Firestore, espera por los resultados, y los transforma en objetos Alumno.
            db.collection("usuarios")
                .whereEqualTo("tipo", "alumno")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject<Alumno>() }
        } catch (e: Exception) {
            Log.e("FireStore", "Error al obtener alumnos: ${e.localizedMessage}", e)
            emptyList<Alumno>()
        }
    }

    // Método para obtener todos los profesores de la base de datos.
    // Similar al método de obtener alumnos, pero filtra por "profesor".
    suspend fun obtenerProfesores(): List<Profesor> = withContext(Dispatchers.IO) {
        try {
            // Realiza la consulta a Firestore, espera por los resultados, y los transforma en objetos Profesor.
            db.collection("usuarios")
                .whereEqualTo("tipo", "profesor")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject<Profesor>() }
        } catch (e: Exception) {
            Log.e("FireStore", "Error al obtener profesores: ${e.localizedMessage}", e)
            emptyList<Profesor>()
        }
    }
}
