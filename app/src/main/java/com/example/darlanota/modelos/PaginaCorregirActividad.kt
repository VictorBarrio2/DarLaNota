package com.example.darlanota.modelos

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PaginaCorregirActividad : AppCompatActivity() {

    // Definiciones de las variables de la clase
    private lateinit var btnDescargar: ImageView
    private lateinit var btnBorrar: ImageView
    private lateinit var btnCambiarFecha: ImageView
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
    private var fechaSeleccionada: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.corregir_actividad_layout)
        FirebaseApp.initializeApp(this)

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
        btnBorrar = findViewById(R.id.bto_borrarActividad)
        btnCambiarFecha = findViewById(R.id.bto_cambiarFecha)
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
                        obtenerNotaAlumnoPorActividad(idActividad, nombreAlumnoSeleccionado)
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
            } else {
                Toast.makeText(this, "Error: Seleccione un alumno y una actividad válida.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCorregir.setOnClickListener {
            if (nombreAlumnoSeleccionado.isNotEmpty()) {
                val fragmentoCalificar = FragmentoCalificar.newInstance(nombreAlumnoSeleccionado, idActividad)
                fragmentoCalificar.show(supportFragmentManager, "fragmento_corregir")
            } else {
                Toast.makeText(this, "Error: Seleccione un alumno.", Toast.LENGTH_SHORT).show()
            }
        }

        btnBorrar.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            // Crear el diálogo de confirmación
            builder.setTitle("Confirmación de eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar esta actividad? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí") { dialog, _ ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            firestore.eliminarActividad(idActividad)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@PaginaCorregirActividad, "Actividad eliminada.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    dialog.dismiss() // Cerrar el diálogo
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss() // Cerrar el diálogo
                }

            // Crear y mostrar el diálogo
            val alertDialog = builder.create()

            // Mostrar el diálogo antes de modificar el color de los botones
            alertDialog.setOnShowListener {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.textColor, theme))
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.textColor, theme))
            }

            alertDialog.show()
        }



        btnCambiarFecha.setOnClickListener {
            mostrarDatePicker()
        }

        imgActividades.setOnClickListener {
            startActivity(Intent(this, PaginaActividadProfe::class.java).apply {
                putExtra("NICK", nick)
            })
        }

        imgPerfil.setOnClickListener {
            startActivity(Intent(this, PaginaPerfilProfe::class.java).apply {
                putExtra("NICK", nick)
            })
        }

        imgRanking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingProfe::class.java).apply {
                putExtra("NICK", nick)
            })
        }

        imgLogro.setOnClickListener {
            // Acciones futuras
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()

        val locale = Locale("es", "ES")
        Locale.setDefault(locale)

        val datePickerDialog = DatePickerDialog(
            this,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            { _, year, monthOfYear, dayOfMonth ->
                fechaSeleccionada = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }
                cambiarFechaEntregaActividad(idActividad, fechaSeleccionada!!.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.show()
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

    // Método actualizado: obtenerNotaAlumnoPorActividad
    private suspend fun obtenerNotaAlumnoPorActividad(idActividad: String, nombreAlumno: String) {
        val calificacion = withContext(Dispatchers.IO) {
            firestore.obtenerCalificacion(idActividad, nombreAlumno)
        }

        withContext(Dispatchers.Main) {
            txtCorregido.text = if (calificacion == null || calificacion < 0) {
                "Nota: No calificado"
            } else {
                "Nota: $calificacion"
            }
        }
    }

    // Método agregado: cambiarFechaEntregaActividad
    private fun cambiarFechaEntregaActividad(idActividad: String, nuevaFecha: Date) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                firestore.cambiarFechaActividad(idActividad, nuevaFecha)
            }
            Toast.makeText(this@PaginaCorregirActividad, "Fecha de entrega actualizada.", Toast.LENGTH_SHORT).show()
        }
    }


}
