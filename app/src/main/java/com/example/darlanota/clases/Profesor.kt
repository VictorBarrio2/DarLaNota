package com.example.darlanota.clases

class Profesor(
    contrasena: String = "",
    nombre: String = "",
    tipo: String = "profesor",
    var departamento: String = ""
) : Usuario(contrasena, nombre, tipo)
