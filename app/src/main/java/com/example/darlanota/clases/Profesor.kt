package com.example.darlanota.clases

class Profesor(
    contrasena: String = "",
    nombre: String = "",
    tipo: String = "profesor"
) : Usuario(contrasena, nombre, tipo)
