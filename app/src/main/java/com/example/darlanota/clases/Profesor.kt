package com.example.darlanota.clases

import android.app.Activity

class Profesor(
    id_alumno: Int,
    contrasena: String,
    nombre: String
) : Usuario(id_alumno, contrasena, nombre) {

    open fun cambiarContra(c: String) {
        contrasena = c
    }

}
