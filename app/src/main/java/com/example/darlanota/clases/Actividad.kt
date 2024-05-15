package com.example.darlanota.clases

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Actividad(
    var descripcion: String,
    var fechafin: String,
    var titulo: String,
    var id_profesor: String,
    var entregas: MutableList<Entrega> = mutableListOf()
){

    fun subirActividadFirestore() {
        // Crea una instancia de FireStore
        val firestore = FireStore()

        // Ejecutar en una corutina para manejar la operación asíncrona
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Añadir este Alumno usando el método de la clase FireStore que acepta un ID y un usuario
                firestore.altaActividad(this@Actividad)
                println("Actividad añadida con éxito a través de FireStore")
            } catch (e: Exception) {
                println("Error al añadir actividad: ${e.localizedMessage}")
            }
        }
    }
}
