package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers

class PaginaCorregirActividad : AppCompatActivity() {

    private lateinit var btnDescargar: Button
    private lateinit var btnCorregir: Button
    private lateinit var txtTitulo: TextView
    private lateinit var txtCorregido: TextView
    private lateinit var imgPerfil: ImageView
    private lateinit var imgActividades: ImageView
    private lateinit var imgRanking: ImageView
    private lateinit var spinnerAlumnos: Spinner
    private val firestore = FireStore()
    private var mapaIdAlumno: MutableMap<String, String> = mutableMapOf()
    private lateinit var idActividad: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.corregir_actividad_layout)

        idActividad = intent.getStringExtra("ACTIVIDAD_ID") ?: ""  // Corrección aquí
        configurarUI()
        txtTitulo.text = intent.getStringExtra("TITULO") ?: ""

        CoroutineScope(Dispatchers.Main).launch {
            actualizarListaEstudiantes(idActividad)
        }
    }

    private fun configurarUI() {
        imgActividades = findViewById(R.id.iv_actividadesAlta)
        imgPerfil = findViewById(R.id.iv_perfilAlta)
        imgRanking = findViewById(R.id.iv_rankingAlta)
        txtTitulo = findViewById(R.id.tv_tituloCorregir)
        txtCorregido = findViewById(R.id.tv_corregido)
        btnDescargar = findViewById(R.id.bto_descargarVideo)
        btnCorregir = findViewById(R.id.bto_corregirActividad)
        spinnerAlumnos = findViewById(R.id.sp_alumnos)
        configurarListeners()
    }

    private fun configurarListeners() {
        imgRanking.setOnClickListener { startActivity(Intent(this, PaginaRankingProfe::class.java)) }
        imgPerfil.setOnClickListener { startActivity(Intent(this, PaginaPerfilProfe::class.java)) }
        imgActividades.setOnClickListener { startActivity(Intent(this, PaginaActividadProfe::class.java)) }
        btnCorregir.setOnClickListener {
            val nombreAlumnoSeleccionado = spinnerAlumnos.selectedItem?.toString()
            if (nombreAlumnoSeleccionado != null) {
                val idAlumnoActual = mapaIdAlumno[nombreAlumnoSeleccionado]

                if (idAlumnoActual != null) {
                    val fragmentoCalificar = FragmentoCalificar.newInstance(idAlumnoActual, idActividad)
                    fragmentoCalificar.show(supportFragmentManager, "fragmento_corregir")
                } else {
                    Toast.makeText(this, "Error: No se pudo obtener el ID del alumno.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error: No se ha seleccionado ningún alumno.", Toast.LENGTH_SHORT).show()
            }
        }

        btnDescargar.setOnClickListener {
            val nombreAlumnoSeleccionado = spinnerAlumnos.selectedItem.toString()
            val idAlumnoActual = mapaIdAlumno[nombreAlumnoSeleccionado]
            if (idAlumnoActual != null && idActividad.isNotEmpty()) {
                lifecycleScope.launch {
                    val videoPath = firestore.obtenerRutaVideo(idActividad, idAlumnoActual)
                    if (videoPath != null) {
                        try {
                            firestore.descargarVideo(this@PaginaCorregirActividad, videoPath)
                            Toast.makeText(this@PaginaCorregirActividad, "Video descargado en la galería", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@PaginaCorregirActividad, "Error al descargar el video: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.d("VideoDownload", "No se encontró la ruta del video.")
                        Toast.makeText(this@PaginaCorregirActividad, "Ruta del video no encontrada", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@PaginaCorregirActividad, "Error: No se pudo obtener el ID del alumno o de la actividad.", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private suspend fun actualizarListaEstudiantes(idActividad: String) {
        val idsAlumnos = obtenerIdsDeAlumnosPorActividad(idActividad)
        idsAlumnos.forEach { id ->
            val nombre = firestore.obtenerNombreAlumno(id)
            if (nombre != null) {
                mapaIdAlumno[nombre] = id
            }
        }

        if (mapaIdAlumno.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mapaIdAlumno.keys.toList())
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerAlumnos.adapter = adapter
        } else {
            Log.d("SpinnerData", "La lista de alumnos está vacía.")
        }
    }

    private suspend fun obtenerIdsDeAlumnosPorActividad(idActividad: String): List<String> = withContext(Dispatchers.IO) {
        firestore.obtenerIdsDeAlumnosDeEntregasPorActividad(idActividad)
    }
}
