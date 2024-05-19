package com.example.darlanota.modelos

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad
import com.example.darlanota.clases.FireStore
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import com.google.firebase.Timestamp

class PaginaAltaActividad : AppCompatActivity() {
    private lateinit var bto_subirActividad: Button
    private lateinit var bto_fecha: Button
    private lateinit var tv_fecha: TextView
    private lateinit var et_titulo: EditText
    private lateinit var et_des: EditText
    private var fechaSeleccionada: Calendar? = null  // Cambiado a nullable para asegurar selección explícita
    private lateinit var iv_perfil: ImageView
    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividad: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alta_actividad_layout)
        FirebaseApp.initializeApp(this)

        bto_subirActividad = findViewById(R.id.bto_subirActividad)
        tv_fecha = findViewById(R.id.tv_fecha)
        bto_fecha = findViewById(R.id.bto_fecha)
        et_titulo = findViewById(R.id.et_tituloAlta)
        et_des = findViewById(R.id.et_descripcionAlta)

        bto_subirActividad.setOnClickListener {
            subirActividad()
        }

        bto_fecha.setOnClickListener {
            mostrarDatePicker()
        }

        iv_perfil = findViewById(R.id.iv_perfilAltaAc)
        iv_ranking = findViewById(R.id.iv_rankingAltaAc)
        iv_actividad = findViewById(R.id.iv_actividadesAltaAc)

        setImageViewListeners()
    }

    private fun subirActividad() {
        val titulo = et_titulo.text.toString().trim()
        val descripcion = et_des.text.toString().trim()
        val fechaFin = fechaSeleccionada?.let { Timestamp(it.time) }

        if (titulo.isEmpty() || descripcion.isEmpty() || fechaFin == null) {
            Toast.makeText(this, "Por favor, completa todos los campos requeridos.", Toast.LENGTH_SHORT).show()
        } else {
            val nuevaActividad = Actividad(
                descripcion = descripcion,
                fechafin = fechaFin,
                titulo = titulo,
                id_profesor = intent.getStringExtra("ID") ?: ""
            )
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    nuevaActividad.subirActividadFirestore()
                    Toast.makeText(this@PaginaAltaActividad, "Actividad creada exitosamente.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@PaginaAltaActividad, PaginaActividadProfe::class.java))
                } catch (e: Exception) {
                    val firestore = FireStore()
                    firestore.registrarIncidencia("Error al añadir actividad: ${e.localizedMessage}")
                    Log.e("Firestore", "Error al añadir actividad: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            fechaSeleccionada = Calendar.getInstance().apply {
                set(year, monthOfYear, dayOfMonth)
            }
            tv_fecha.text = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setImageViewListeners() {
        iv_perfil.setOnClickListener {
            startActivity(Intent(this, PaginaPerfilProfe::class.java))
        }
        iv_ranking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingProfe::class.java))
        }
        iv_actividad.setOnClickListener {
            startActivity(Intent(this, PaginaActividadProfe::class.java))
        }
    }
}

