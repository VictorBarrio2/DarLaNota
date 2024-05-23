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
import com.google.firebase.Timestamp
import kotlinx.coroutines.*
import java.util.Calendar

class PaginaAltaActividad : AppCompatActivity() {

    // Declaración de variables
    private lateinit var btoSubirActividad: Button
    private lateinit var btoFecha: Button
    private lateinit var tvFecha: TextView
    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private var fechaSeleccionada: Calendar? = null  // Cambiado a nullable para asegurar selección explícita
    private lateinit var ivPerfil: ImageView
    private lateinit var ivRanking: ImageView
    private lateinit var ivActividad: ImageView
    private lateinit var id: String

    // Método onCreate, inicializa la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alta_actividad_layout)
        FirebaseApp.initializeApp(this)

        id = intent.getStringExtra("ID") ?: ""

        inicializarVistas()
        configurarListeners()
    }

    // Inicializa las vistas
    private fun inicializarVistas() {
        btoSubirActividad = findViewById(R.id.bto_subirActividad)
        tvFecha = findViewById(R.id.tv_fecha)
        btoFecha = findViewById(R.id.bto_fecha)
        etTitulo = findViewById(R.id.et_tituloAlta)
        etDescripcion = findViewById(R.id.et_descripcionAlta)
        ivPerfil = findViewById(R.id.iv_perfilAltaAc)
        ivRanking = findViewById(R.id.iv_rankingAltaAc)
        ivActividad = findViewById(R.id.iv_actividadesAltaAc)
    }

    // Configura los listeners para los botones y las imágenes
    private fun configurarListeners() {
        btoSubirActividad.setOnClickListener {
            subirActividad()
        }

        btoFecha.setOnClickListener {
            mostrarDatePicker()
        }

        setImageViewListeners()
    }

    // Método para subir la actividad a Firestore
    private fun subirActividad() {
        val titulo = etTitulo.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
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

    // Muestra el DatePicker para seleccionar la fecha
    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            fechaSeleccionada = Calendar.getInstance().apply {
                set(year, monthOfYear, dayOfMonth)
            }
            tvFecha.text = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // Configura los listeners para las imágenes
    private fun setImageViewListeners() {
        ivPerfil.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)  // Retardo de 300 milisegundos para prevenir clicks fantasma
                val intent = Intent(this@PaginaAltaActividad, PaginaPerfilAlumno::class.java)
                intent.putExtra("ID", id)  // Asegura que el ID se pase correctamente
                startActivity(intent)
            }
        }
        ivRanking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingProfe::class.java).apply {
                putExtra("ID", id)
            })
        }
        ivActividad.setOnClickListener {
            startActivity(Intent(this, PaginaActividadProfe::class.java).apply {
                putExtra("ID", id)
            })
        }
    }
}
