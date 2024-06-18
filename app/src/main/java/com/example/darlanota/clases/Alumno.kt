package com.example.darlanota.clases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Alumno(
    contrasena: String = "",
    nombre: String = "",
    tipo: String = "alumno",
    var puntuacion: Int = 0
) : Usuario(contrasena, nombre, tipo)
 {

    // Método para añadir este alumno a Firestore usando la clase FireStore
}
