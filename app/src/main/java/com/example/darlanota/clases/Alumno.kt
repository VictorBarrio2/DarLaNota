package com.example.darlanota.clases

import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Alumno(
    id: String,
    contrasena: String,
    nombre: String,
    val instrumentos: ArrayList<String>,
    var puntuacion: Int = 0  // Valor predeterminado para puntuación
) : Usuario(id, contrasena, nombre, "alumno") {

    // Método para añadir este alumno a Firestore usando la clase FireStore
    fun subirAFirestore() {
        // Crea una instancia de FireStore
        val firestore = FireStore()

        // Ejecutar en una corutina para manejar la operación asíncrona
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Añadir este Alumno usando el método de la clase FireStore
                firestore.añadirUsuario(this@Alumno)
                println("Alumno añadido con éxito a través de FireStore")
            } catch (e: Exception) {
                println("Error al añadir alumno: ${e.localizedMessage}")
            }
        }
    }
}
