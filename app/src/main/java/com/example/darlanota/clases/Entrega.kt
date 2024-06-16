package com.example.darlanota.clases

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import java.util.Date

data class Entrega(
    var idActividad: String = "",
    var video: String = "",
    var calificacion: Int = -1,
    var idAlumno : String = ""
) {

}
