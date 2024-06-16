package com.example.darlanota.clases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Alumno(
    contrasena: String = "",
    nombre: String = "",
    tipo: String = "alumno",
    var instrumentos: ArrayList<String> = ArrayList(),
    var puntuacion: Int = 0
) : Usuario(contrasena, nombre, tipo)
 {

    // Método para añadir este alumno a Firestore usando la clase FireStore
    fun subirAlumnoFirestore(id: String) {

        // Ejecutar en una corutina para manejar la operación asíncrona
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("Alumno añadido con éxito a través de FireStore con ID: $id")
            } catch (e: Exception) {
                println("Error al añadir alumno: ${e.localizedMessage}")
            }
        }
    }
}
