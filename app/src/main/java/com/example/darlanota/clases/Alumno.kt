package com.example.darlanota.clases

import android.app.Activity
import com.example.darlanota.modelos.PaginaInstrumentos
import java.io.File

open class Alumno(
    id_alumno: Int,
    contrasena: String,
    nombre: String,
    private var instrumentos: ArrayList<String>,
    private var puntuacion: Float
) : Usuario(id_alumno, contrasena, nombre) {

    open fun cambiarInstrumento(i: ArrayList<String>) {
        instrumentos = i
    }

    open fun restarPuntuacion(p: Float) {
        puntuacion -= p // Restar el valor proporcionado a la puntuación actual
    }

    open fun sumarPuntuacion(p: Float) {
        puntuacion += p // Sumar el valor proporcionado a la puntuación actual
    }

    open fun cambiarContra(c: String) {
        contrasena = c // Sumar el valor proporcionado a la puntuación actual
    }
}
