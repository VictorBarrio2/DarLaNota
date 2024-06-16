package com.example.darlanota.clases


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.sql.Timestamp

data class Actividad(
    var id: String = "",
    var descripcion: String = "",
    var fechafin: String = "",  // Puede ser nulo para cumplir con la necesidad de un constructor sin argumentos.
    var titulo: String = "",
    var id_profesor: String = "",
    var instrumento: String = "",
    var entregas: MutableList<Entrega> = mutableListOf()  // Se asegura de que `Entrega` también tenga un constructor sin argumentos si es necesario.
) {

}
