package com.example.darlanota.clases

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

data class Entrega(
    var idAlumno: String = "",
    var video: String = "",
    var calificacion: Int = 0
) : Serializable {
    fun subirEntregaFirestore(idActividad: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val firestore = FireStore()
            val resultado = firestore.altaEntrega(idActividad, this@Entrega)
            if (resultado.isSuccess) {
                Log.d("Entrega", resultado.getOrNull() ?: "Subida exitosa")
            } else {
                Log.e("Entrega", "Error al subir entrega: ${resultado.exceptionOrNull()?.localizedMessage}", resultado.exceptionOrNull())
            }
        }
    }
}
