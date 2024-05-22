package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad
import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private lateinit var id: String
    private lateinit var id_alumno: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.corregir_actividad_layout)
        id = intent.getStringExtra("ID") ?: ""
        idActividad = intent.getStringExtra("ACTIVIDAD_ID") ?: ""
        configurarUI()
        txtTitulo.text = intent.getStringExtra("TITULO") ?: ""

        lifecycleScope.launch {
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
        spinnerAlumnos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val nombreAlumno = parent.getItemAtPosition(position) as String
                id_alumno = mapaIdAlumno[nombreAlumno].toString()
                if (id_alumno != null) {
                    lifecycleScope.launch {
                        val calificacion = firestore.obtenerCalificacion(idActividad, id_alumno)
                        txtCorregido.text = if (calificacion == null || calificacion < 0) {
                            "Calificado: No"
                        } else {
                            "Calificado: Si"
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nothing to do here
            }
        }

        btnDescargar.setOnClickListener {
            val nombreAlumnoSeleccionado = spinnerAlumnos.selectedItem.toString()
            id_alumno = mapaIdAlumno[nombreAlumnoSeleccionado].toString()
            if (id_alumno != null && idActividad.isNotEmpty()) {
                lifecycleScope.launch {
                    descargarVideo(idActividad, id_alumno)
                }
            }
        }

        btnCorregir.setOnClickListener {
            val nombreAlumnoSeleccionado = spinnerAlumnos.selectedItem.toString()
            val idAlumnoActual = mapaIdAlumno[nombreAlumnoSeleccionado]
            if (idAlumnoActual != null) {
                val fragmentoCalificar = FragmentoCalificar.newInstance(idAlumnoActual, idActividad)
                fragmentoCalificar.show(supportFragmentManager, "fragmento_corregir")
                txtCorregido.text = "Calificado: Si"
            } else {
                Toast.makeText(this, "Error: No se pudo obtener el ID del alumno.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun descargarVideo(idActividad: String, idAlumno: String) {
        val videoPath = withContext(Dispatchers.IO) {
            firestore.obtenerRutaVideo(idActividad, idAlumno)
        }
        if (videoPath != null) {
            try {
                firestore.descargarVideo(this, videoPath)
                Toast.makeText(this, "Video descargado en la galería", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al descargar el video: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Ruta del video no encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun actualizarListaEstudiantes(idActividad: String) {
        val idsAlumnos = withContext(Dispatchers.IO) {
            firestore.obtenerIdsDeAlumnosDeEntregasPorActividad(idActividad)
        }
        val nombresAlumnos = idsAlumnos.mapNotNull { id ->
            firestore.obtenerNombreUsuario(id)?.let { nombre ->
                mapaIdAlumno[nombre] = id
                nombre
            }
        }
        withContext(Dispatchers.Main) {
            if (nombresAlumnos.isNotEmpty()) {
                val adapter = ArrayAdapter(this@PaginaCorregirActividad, android.R.layout.simple_spinner_item, nombresAlumnos)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerAlumnos.adapter = adapter
            } else {
                Log.d("SpinnerData", "La lista de alumnos está vacía.")
            }
        }
    }
}
