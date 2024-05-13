package com.example.darlanota.clases

class Profesor(
    id: String,
    contrasena: String,
    nombre: String
) : Usuario(id, contrasena, nombre, "profesor") {
    // Métodos específicos de Profesor, si necesarios
}