package com.example.darlanota.clases

import com.example.darlanota.clases.FireStore
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
        // Crea una instancia de FireStore
        val firestore = FireStore()

        // Ejecutar en una corutina para manejar la operación asíncrona
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Añadir este Alumno usando el método de la clase FireStore que acepta un ID y un usuario
                firestore.altaUsuario(id, this@Alumno)
                println("Alumno añadido con éxito a través de FireStore con ID: $id")
            } catch (e: Exception) {
                firestore.registrarIncidencia("Error al añadir alumno: ${e.localizedMessage}")
                println("Error al añadir alumno: ${e.localizedMessage}")
            }
        }
    }
}
