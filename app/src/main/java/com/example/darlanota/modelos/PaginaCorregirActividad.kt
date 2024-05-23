package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaginaCorregirActividad : AppCompatActivity() {

    // Declaración de variables UI y Firestore
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

    // Método principal que se ejecuta al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.corregir_actividad_layout)

        // Obtener los datos pasados por intent
        id = intent.getStringExtra("ID") ?: ""
        idActividad = intent.getStringExtra("ACTIVIDAD_ID") ?: ""

        // Configurar la interfaz de usuario
        configurarUI()

        // Establecer el título de la actividad
        txtTitulo.text = intent.getStringExtra("TITULO") ?: ""

        // Cargar la lista de estudiantes que han entregado la actividad
        lifecycleScope.launch {
            actualizarListaEstudiantes(idActividad)
        }
    }

    // Método para configurar la interfaz de usuario
    private fun configurarUI() {
        // Inicializar las vistas
        imgActividades = findViewById(R.id.iv_actividadesAlta)
        imgPerfil = findViewById(R.id.iv_perfilAlta)
        imgRanking = findViewById(R.id.iv_rankingAlta)
        txtTitulo = findViewById(R.id.tv_tituloCorregir)
        txtCorregido = findViewById(R.id.tv_corregido)
        btnDescargar = findViewById(R.id.bto_descargarVideo)
        btnCorregir = findViewById(R.id.bto_corregirActividad)
        spinnerAlumnos = findViewById(R.id.sp_alumnos)

        // Configurar los listeners de los elementos de la interfaz de usuario
        configurarListeners()
    }

    // Método para configurar los listeners de los elementos de la interfaz de usuario
    private fun configurarListeners() {
        // Listener para el spinner de alumnos
        spinnerAlumnos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Obtener el nombre y ID del alumno seleccionado
                val nombreAlumno = parent.getItemAtPosition(position) as String
                id_alumno = mapaIdAlumno[nombreAlumno].toString()
                if (id_alumno != null) {
                    // Cargar la calificación del alumno seleccionado
                    lifecycleScope.launch {
                        val calificacion = firestore.obtenerCalificacion(idActividad, id_alumno)
                        // Actualizar el estado de corrección basado en la calificación obtenida
                        txtCorregido.text = if (calificacion == null || calificacion < 0) {
                            "Calificado: No"
                        } else {
                            "Calificado: Sí"
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hay nada que hacer aquí
            }
        }

        // Listener para el botón de descargar video
        btnDescargar.setOnClickListener {
            val nombreAlumnoSeleccionado = spinnerAlumnos.selectedItem.toString()
            id_alumno = mapaIdAlumno[nombreAlumnoSeleccionado].toString()
            if (id_alumno != null && idActividad.isNotEmpty()) {
                // Descargar el video del alumno seleccionado
                lifecycleScope.launch {
                    descargarVideo(idActividad, id_alumno)
                }
            }
        }

        // Listener para el botón de corregir actividad
        btnCorregir.setOnClickListener {
            val nombreAlumnoSeleccionado = spinnerAlumnos.selectedItem.toString()
            val idAlumnoActual = mapaIdAlumno[nombreAlumnoSeleccionado]
            if (idAlumnoActual != null) {
                // Mostrar el fragmento de calificación
                val fragmentoCalificar = FragmentoCalificar.newInstance(idAlumnoActual, idActividad)
                fragmentoCalificar.show(supportFragmentManager, "fragmento_corregir")
                txtCorregido.text = "Calificado: Sí"
            } else {
                Toast.makeText(this, "Error: No se pudo obtener el ID del alumno.", Toast.LENGTH_SHORT).show()
            }
        }

        // Listeners para las imágenes de navegación
        imgActividades.setOnClickListener {
            startActivity(Intent(this, PaginaActividadProfe::class.java).also { it.putExtra("ID", id) })
        }

        imgPerfil.setOnClickListener {
            startActivity(Intent(this, PaginaPerfilProfe::class.java).also { it.putExtra("ID", id) })
        }

        imgRanking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingProfe::class.java).also { it.putExtra("ID", id) })
        }
    }

    // Método para descargar el video de un alumno
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

    // Método para actualizar la lista de estudiantes que han entregado la actividad
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
