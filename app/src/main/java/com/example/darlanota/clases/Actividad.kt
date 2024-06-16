package com.example.darlanota.clases

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Actividad(
    var id: String = "",
    var descripcion: String = "",
    var fechafin: Timestamp? = null,  // Puede ser nulo para cumplir con la necesidad de un constructor sin argumentos.
    var titulo: String = "",
    var id_profesor: String = "",
    var instrumento: String = "",
    var entregas: MutableList<Entrega> = mutableListOf()  // Se asegura de que `Entrega` también tenga un constructor sin argumentos si es necesario.
) {
    fun subirActividadFirestore() {

        // Ejecutar en una corutina para manejar la operación asíncrona
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("Actividad añadido con éxito a través de FireStore con ID: $id")
            } catch (e: Exception) {
                println("Error al añadir actividad: ${e.localizedMessage}")
            }
        }
    }
}
