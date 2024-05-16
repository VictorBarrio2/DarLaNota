package com.example.darlanota.clases

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Actividad(
    var id: String = "",
    var descripcion: String = "",
    var fechafin: String = "",
    var titulo: String = "",
    var id_profesor: String = "",
    var entregas: MutableList<Entrega> = mutableListOf()  // Asegúrate de que `Entrega` también tenga un constructor sin argumentos si es necesario.
) {
    fun subirActividadFirestore() {
        // Crea una instancia de FireStore
        val firestore = FirebaseFirestore.getInstance()

        // Ejecutar en una corutina para manejar la operación asíncrona
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("actividades").add(this@Actividad).await()
                println("Actividad añadida con éxito a través de FireStore")
            } catch (e: Exception) {
                println("Error al añadir actividad: ${e.localizedMessage}")
            }
        }
    }
}
