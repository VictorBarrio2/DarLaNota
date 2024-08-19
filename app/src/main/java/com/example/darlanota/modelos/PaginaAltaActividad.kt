package com.example.darlanota.modelos

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
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
import java.util.Locale
import kotlin.properties.Delegates

class PaginaAltaActividad : AppCompatActivity() {

    // Declaración de variables
    private var posSpinner: Int = 0
    private lateinit var btoSubirActividad: Button
    private lateinit var btoFecha: ImageView
    private lateinit var tvFecha: TextView
    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private var fechaSeleccionada: Calendar? = null  // Cambiado a nullable para asegurar selección explícita
    private lateinit var ivPerfil: ImageView
    private lateinit var ivRanking: ImageView
    private lateinit var ivActividad: ImageView
    private lateinit var nick: String
    private lateinit var spinner: Spinner
    private val options = listOf(
        R.drawable.nota to 1,
        R.drawable.piano to 2,
        R.drawable.guitarra to 3,
        R.drawable.bateria to 4,
        R.drawable.canto to 5
    )


    // Método onCreate, inicializa la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alta_actividad_layout)
        FirebaseApp.initializeApp(this)

        nick = intent.getStringExtra("NICK") ?: ""
        posSpinner = 1
        inicializarVistas()
        configurarListeners()
        configurarSpinner()
    }

    // Inicializa las vistas
    private fun inicializarVistas() {
        btoSubirActividad = findViewById(R.id.bto_subirActividad)
        tvFecha = findViewById(R.id.tv_fecha)
        btoFecha = findViewById(R.id.iv_fecha)
        etTitulo = findViewById(R.id.et_tituloAlta)
        etDescripcion = findViewById(R.id.et_descripcionAlta)
        ivPerfil = findViewById(R.id.iv_perfilAltaAc)
        ivRanking = findViewById(R.id.iv_rankingAltaAc)
        ivActividad = findViewById(R.id.iv_actividadesAltaAc)
        spinner = findViewById(R.id.sp_altaActividad)
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
        val instrumento = posSpinner

        if (titulo.isEmpty() || descripcion.isEmpty() || fechaFin == null) {
            Toast.makeText(this, "Por favor, completa todos los campos requeridos.", Toast.LENGTH_SHORT).show()
        } else {
            val nuevaActividad = Actividad(
                descripcion = descripcion,
                fechafin = fechaFin,
                titulo = titulo,
                id_profesor = intent.getStringExtra("NICK") ?: "",
                instrumento = instrumento
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

        // Establecer la configuración regional en español
        val locale = Locale("es", "ES")
        Locale.setDefault(locale)

        val datePickerDialog = DatePickerDialog(
            this,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth, // Tema claro por defecto
            { _, year, monthOfYear, dayOfMonth ->
                fechaSeleccionada = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }
                tvFecha.text = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Establecer fondo transparente para el diálogo
        datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Mostrar el diálogo
        datePickerDialog.show()
    }




    // Configura los listeners para las imágenes
    private fun setImageViewListeners() {
        ivPerfil.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)  // Retardo de 300 milisegundos para prevenir clicks fantasma
                val intent = Intent(this@PaginaAltaActividad, PaginaPerfilProfe::class.java)
                intent.putExtra("NICK", nick)  // Asegura que el ID se pase correctamente
                startActivity(intent)
            }
        }
        ivRanking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingProfe::class.java).apply {
                putExtra("NICK", nick)
            })
        }
        ivActividad.setOnClickListener {
            startActivity(Intent(this, PaginaActividadProfe::class.java).apply {
                putExtra("NICK", nick)
            })
        }
    }
    private fun configurarSpinner() {

        val adapter = object : ArrayAdapter<Pair<Int, Int>>(this, R.layout.spinner_item, options) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = convertView ?: layoutInflater.inflate(R.layout.spinner_item, parent, false)
                val imageView = view.findViewById<ImageView>(R.id.spinner_image)
                imageView.setImageResource(options[position].first)
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = convertView ?: layoutInflater.inflate(R.layout.spinner_dropdown_item, parent, false)
                val imageView = view.findViewById<ImageView>(R.id.spinner_dropdown_image)
                imageView.setImageResource(options[position].first)
                return view
            }
        }

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                // Obtén el valor asociado a la opción seleccionada
                val selectedValue = options[position].second
                // Haz algo con el valor seleccionado, por ejemplo, mostrarlo en un Toast
                // Toast.makeText(this@MainActivity, "Valor seleccionado: $selectedValue", Toast.LENGTH_SHORT).show()
                posSpinner = selectedValue
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Opcional: manejar el caso en el que no se seleccione ninguna opción
            }
        }
    }
}
