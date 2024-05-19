package com.example.darlanota.clases

import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log  // Importante para el logging en Android

class Incidencia(
    var fechafin: Timestamp? = null,
    val descripcion: String = ""
)
