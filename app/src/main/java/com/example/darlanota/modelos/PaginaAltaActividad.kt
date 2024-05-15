package com.example.darlanota.modelos

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class PaginaAltaActividad : AppCompatActivity() {
    private lateinit var bto_subirActividad: Button
    private lateinit var bto_fecha: Button
    private lateinit var et_titulo: EditText
    private lateinit var et_des: EditText
    private lateinit var fechaSeleccionada: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alta_actividad_layout)
        FirebaseApp.initializeApp(this)

        bto_subirActividad = findViewById(R.id.bto_subirActividad)
        bto_fecha = findViewById(R.id.bto_fecha)
        et_titulo = findViewById(R.id.et_tituloAlta)
        et_des = findViewById(R.id.et_descripcionAlta)
        fechaSeleccionada = findViewById(R.id.tv_fecha)

        val id_profe = intent.getStringExtra("ID") ?: ""  // No default to "0", should handle null properly.

        bto_subirActividad.setOnClickListener {
            val titulo = et_titulo.text.toString().trim()
            val descripcion = et_des.text.toString().trim()
            val fechaFin = fechaSeleccionada.text.toString()

            if (titulo.isEmpty() || descripcion.isEmpty() || fechaFin == "--/--/----") {
                Toast.makeText(this, "Faltan campos por seleccionar", Toast.LENGTH_SHORT).show()
            } else {
                val nuevaActividad = Actividad(descripcion, fechaFin, titulo, id_profe)
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        altaActividad(nuevaActividad)
                        Toast.makeText(this@PaginaAltaActividad, "Actividad creada", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@PaginaAltaActividad, PaginaActividadProfe::class.java))
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error al añadir actividad: ${e.localizedMessage}")
                    }
                }
            }
        }

        bto_fecha.setOnClickListener {
            mostrarDatePicker()
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            fechaSeleccionada.text = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private suspend fun altaActividad(actividad: Actividad) {
        val db = FirebaseFirestore.getInstance()
        try {
            db.collection("actividades").add(actividad).await()
            Log.d("Firestore", "Actividad añadida correctamente")
        } catch (e: Exception) {
            Log.e("Firestore", "Error al añadir actividad: ${e.localizedMessage}", e)
        }
    }

}
