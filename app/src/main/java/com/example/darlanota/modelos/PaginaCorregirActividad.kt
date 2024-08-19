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

    private lateinit var btnDescargar: Button
    private lateinit var btnCorregir: Button
    private lateinit var txtTitulo: TextView
    private lateinit var txtCorregido: TextView
    private lateinit var imgPerfil: ImageView
    private lateinit var imgActividades: ImageView
    private lateinit var imgRanking: ImageView
    private lateinit var imgLogro: ImageView
    private lateinit var spinnerAlumnos: Spinner
    private val firestore = FireStore()
    private lateinit var idActividad: String
    private lateinit var nick: String
    private lateinit var nombreAlumnoSeleccionado: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.corregir_actividad_layout)

        nick = intent.getStringExtra("NICK") ?: ""
        idActividad = intent.getStringExtra("ACTIVIDAD_ID") ?: ""

        configurarUI()

        txtTitulo.text = intent.getStringExtra("TITULO") ?: ""

        lifecycleScope.launch {
            actualizarListaEstudiantes(idActividad)
        }
    }

    private fun configurarUI() {
        imgActividades = findViewById(R.id.iv_actividadesCorregir)
        imgPerfil = findViewById(R.id.iv_perfilAlta)
        imgRanking = findViewById(R.id.iv_rankingCorregir)
        txtTitulo = findViewById(R.id.tv_tituloCorregir)
        txtCorregido = findViewById(R.id.tv_corregido)
        btnDescargar = findViewById(R.id.bto_descargarVideo)
        btnCorregir = findViewById(R.id.bto_corregirActividad)
        spinnerAlumnos = findViewById(R.id.sp_alumnos)
        imgLogro = findViewById(R.id.iv_logroCorregir)

        configurarListeners()
    }

    private fun configurarListeners() {
        spinnerAlumnos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                nombreAlumnoSeleccionado = parent.getItemAtPosition(position) as String
                if (nombreAlumnoSeleccionado.isNotEmpty()) {
                    lifecycleScope.launch {
                        val calificacion = firestore.obtenerCalificacion(idActividad, nombreAlumnoSeleccionado)
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

        btnDescargar.setOnClickListener {
            if (nombreAlumnoSeleccionado.isNotEmpty() && idActividad.isNotEmpty()) {
                lifecycleScope.launch {
                    descargarVideo(idActividad, nombreAlumnoSeleccionado)
                }
            }
        }

        btnCorregir.setOnClickListener {
            if (nombreAlumnoSeleccionado.isNotEmpty()) {
                val fragmentoCalificar = FragmentoCalificar.newInstance(nombreAlumnoSeleccionado, idActividad)
                fragmentoCalificar.show(supportFragmentManager, "fragmento_corregir")
                txtCorregido.text = "Calificado: Sí"
            } else {
                Toast.makeText(this, "Error: No se pudo obtener el nombre del alumno.", Toast.LENGTH_SHORT).show()
            }
        }

        imgActividades.setOnClickListener {
            startActivity(Intent(this, PaginaActividadProfe::class.java).also { it.putExtra("NICK", nick) })
        }

        imgPerfil.setOnClickListener {
            startActivity(Intent(this, PaginaPerfilProfe::class.java).also { it.putExtra("NICK", nick) })
        }

        imgRanking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingProfe::class.java).also { it.putExtra("NICK", nick) })
        }

        imgLogro.setOnClickListener {

        }
    }

    private suspend fun descargarVideo(idActividad: String, nombreAlumno: String) {
        val videoPath = withContext(Dispatchers.IO) {
            firestore.obtenerRutaVideo(idActividad, nombreAlumno)
        }

        if (videoPath != null) {
            Log.d("VideoDownload", "Ruta del video: $videoPath")
            try {
                firestore.descargarVideo(this, videoPath)
                Toast.makeText(this, "Video descargado en la galería", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al descargar el video: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                Log.e("VideoDownload", "Error al descargar el video", e)
            }
        } else {
            Toast.makeText(this, "Ruta del video no encontrada", Toast.LENGTH_SHORT).show()
            Log.d("VideoDownload", "Ruta del video es null")
        }
    }


    private suspend fun actualizarListaEstudiantes(idActividad: String) {
        // Aquí cambiamos para utilizar el nuevo método `obtenerNombresDeEntregas`
        val nombresAlumnos = withContext(Dispatchers.IO) {
            firestore.obtenerNombresDeEntregas(idActividad)
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
