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
        val id = intent.getStringExtra("ID")

        iv_ranking.setOnClickListener {
            val intent = Intent(this, PaginaRankingAlumno::class.java)
            intent.putExtra("ID", id)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)  // Retardo de 300 milisegundos para prevenir clicks fantasma
                val intent = Intent(this@PaginaActividadAlumno, PaginaPerfilAlumno::class.java)
                intent.putExtra("ID", id)  // Asegura que el ID se pase correctamente
                startActivity(intent)
            }
        }

        iv_logro.setOnClickListener {
            startActivity(Intent(this, PaginaLogrosAlumno::class.java).apply {
                putExtra("ID", id)
            })
        }
    }


    // Método para cargar las actividades del alumno
    private fun cargarActividades(num: Int) {
        val id = intent.getStringExtra("ID")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val actividadesList = withContext(Dispatchers.IO) { fireStore.cargarActividades() }

                // Filtra actividades pasadas y las elimina
                val actividadesValidas = filtrarYEliminarActividadesPasadas(actividadesList, num)

                // Ordena las actividades válidas por fecha de finalización, de más pronto a más tardío
                val actividadesOrdenadas = actividadesValidas.sortedBy { it.fechafin?.toDate() }

                // Configura el adaptador con las actividades válidas y ordenadas
                configurarAdaptador(actividadesOrdenadas, id.toString())
            } catch (e: Exception) {
                Toast.makeText(this@PaginaActividadAlumno, "Error al cargar actividades: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                fireStore.registrarIncidencia("Error al cargar actividades: ${e.localizedMessage}")
            }
        }
    }


    // Método para filtrar y eliminar actividades pasadas
    private suspend fun filtrarYEliminarActividadesPasadas(actividadesList: List<Actividad>, num: Int): List<Actividad> {
        return actividadesList.filter { actividad ->
            val fechaFin = actividad.fechafin?.toDate()

            if (fechaFin != null && fechaFin.before(Date())) {
                // Eliminar actividades pasadas
                withContext(Dispatchers.IO) {
                    fireStore.eliminarActividad(actividad.id)
                }
                false
            } else {
                if (num == 1) {
                    // Si el número es 0, incluir todas las actividades no pasadas
                    true
                } else {
                    // Incluir solo las actividades que coincidan con el número de instrumento
                    actividad.instrumento == num
                }
            }
        }
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
