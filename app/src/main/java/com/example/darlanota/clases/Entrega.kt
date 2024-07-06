package com.example.darlanota.clases

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import java.util.Date

data class Entrega(
    var idAlumno: String = "",
    var video: String = "",
    var calificacion: Int = -1
) {

    fun subirEntregaFirestore(idActividad: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val firestore = FireStore()
            val resultado = firestore.altaEntrega(idActividad, this@Entrega)
            if (resultado.isSuccess) {
                Log.d("Entrega", "Subida exitosa: ${resultado.getOrNull()}")
            } else {
                Log.e("Entrega", "Error al subir entrega: ${resultado.exceptionOrNull()?.localizedMessage}")

            }
        }
    }
}
