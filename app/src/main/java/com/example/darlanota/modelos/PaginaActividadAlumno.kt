package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad
import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.*
import java.util.Calendar
import java.util.Date

class PaginaActividadAlumno : AppCompatActivity() {

    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_perfil: ImageView
    private lateinit var iv_logro: ImageView
    private lateinit var reciclador: RecyclerView
    private lateinit var adaptadorAlumno: AdaptadorAlumno
    private lateinit var spinner: Spinner
    private val fireStore = FireStore()
    private val options = listOf(
        R.drawable.nota to 1,
        R.drawable.piano to 2,
        R.drawable.guitarra to 3,
        R.drawable.bateria to 4,
        R.drawable.canto to 5
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividades_alumno_layout)

        // Inicializa las vistas
        inicializarVistas()
        // Configura los listeners para las vistas
        configurarListeners()

        configurarSpinner()
    }

    // Método para inicializar las vistas
    private fun inicializarVistas() {
        iv_ranking = findViewById(R.id.iv_rankingAcAl)
        iv_actividades = findViewById(R.id.iv_actividadesAcAl)
        iv_logro = findViewById(R.id.iv_logoActividadAl)
        iv_perfil = findViewById(R.id.iv_perfilAcAl)
        reciclador = findViewById(R.id.rv_reciclador)
        spinner = findViewById(R.id.sp_actividadesAlumnos)
    }

    // Método para configurar los listeners
    private fun configurarListeners() {
        val nick = intent.getStringExtra("NICK")

        iv_ranking.setOnClickListener {
            val intent = Intent(this, PaginaRankingAlumno::class.java)
            intent.putExtra("NICK", nick)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)  // Retardo de 300 milisegundos para prevenir clicks fantasma
                val intent = Intent(this@PaginaActividadAlumno, PaginaPerfilAlumno::class.java)
                intent.putExtra("NICK", nick)  // Asegura que el ID se pase correctamente
                startActivity(intent)
            }
        }

        iv_logro.setOnClickListener {
            startActivity(Intent(this, PaginaLogrosAlumno::class.java).apply {
                putExtra("NICK", nick)
            })
        }
    }


    // Método para cargar las actividades del alumno
    private fun cargarActividades(num: Int) {
        val id = intent.getStringExtra("NICK")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val actividadesList = withContext(Dispatchers.IO) { fireStore.cargarActividades() }

                // Filtra actividades pasadas y las organiza
                val actividadesOrdenadas = filtrarYEliminarActividadesPasadas(actividadesList, num)

                // Configura el adaptador con las actividades válidas y ordenadas
                configurarAdaptador(actividadesOrdenadas, id.toString())
            } catch (e: Exception) {
                Toast.makeText(this@PaginaActividadAlumno, "Error al cargar actividades: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                fireStore.registrarIncidencia("Error al cargar actividades: ${e.localizedMessage}")
            }
        }
    }



    private suspend fun filtrarYEliminarActividadesPasadas(actividadesList: List<Actividad>, num: Int): List<Actividad> {
        val fechaActual = Date()

        val actividadesValidas = mutableListOf<Actividad>()
        val actividadesConMargen = mutableListOf<Actividad>()

        actividadesList.forEach { actividad ->
            val fechaFin = actividad.fechafin?.toDate()

            if (fechaFin != null) {
                val cincoDiasDespues = Calendar.getInstance().apply {
                    time = fechaFin
                    add(Calendar.DAY_OF_YEAR, 5)
                }.time

                if (cincoDiasDespues.before(fechaActual)) {
                    // Eliminar actividades que hayan pasado más de 5 días después de la fecha de fin
                    withContext(Dispatchers.IO) {
                        fireStore.eliminarActividad(actividad.id)
                    }
                } else if (fechaFin.before(fechaActual)) {
                    // Si ha pasado la fecha actual pero no 5 días, mover al final de la lista
                    actividadesConMargen.add(actividad)
                } else {
                    // Actividades válidas que no han pasado la fecha actual ni el margen de 5 días
                    actividadesValidas.add(actividad)
                }
            } else {
                // Actividades sin fecha de fin se consideran válidas
                actividadesValidas.add(actividad)
            }
        }

        // Combina las listas, con actividades dentro del margen de 5 días al final
        return actividadesValidas + actividadesConMargen
    }

    // Método para configurar el adaptador del RecyclerView
    private fun configurarAdaptador(actividadesValidas: List<Actividad>, id: String) {
        adaptadorAlumno = AdaptadorAlumno(id, actividadesValidas)
        reciclador.layoutManager = LinearLayoutManager(this@PaginaActividadAlumno)
        reciclador.adapter = adaptadorAlumno
    }

    // Método para configurar el Spinner con imágenes
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
                cargarActividades(selectedValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Opcional: manejar el caso en el que no se seleccione ninguna opción
            }
        }
    }

}
