package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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

class PaginaActividadProfe : AppCompatActivity() {

    private lateinit var btnSubirActividad: Button
    private lateinit var ivRanking: ImageView
    private lateinit var ivActividades: ImageView
    private lateinit var ivPerfil: ImageView
    private lateinit var reciclador: RecyclerView
    private lateinit var adaptadorProfe: AdaptadorProfe
    private val fireStore = FireStore()
    private lateinit var spinner: Spinner
    private val options = listOf(
        R.drawable.nota to 1,
        R.drawable.piano to 2,
        R.drawable.guitarra to 3,
        R.drawable.bateria to 4,
        R.drawable.canto to 5
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividades_profesor_layout)

        // Inicializa las vistas
        inicializarVistas()

        // Obtiene el ID del intent
        val id = intent.getStringExtra("ID") ?: ""

        // Configura los listeners para las vistas
        configurarListeners(id)

        configurarSpinner(id)

    }

    // Método para inicializar las vistas
    private fun inicializarVistas() {
        ivRanking = findViewById(R.id.iv_rankingProfe)
        ivActividades = findViewById(R.id.iv_actividadesProfe)
        ivPerfil = findViewById(R.id.iv_perfilProfe)
        reciclador = findViewById(R.id.rv_reclicadorProfe)
        btnSubirActividad = findViewById(R.id.bto_altaActividad)
        spinner = findViewById(R.id.sp_actividadesProfesor)
    }

    // Método para configurar los listeners
    private fun configurarListeners(id: String) {
        ivRanking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingProfe::class.java).apply {
                putExtra("ID", id)
            })
        }

        ivPerfil.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val intent = Intent(this@PaginaActividadProfe, PaginaPerfilProfe::class.java)
                intent.putExtra("ID", id)
                startActivity(intent)
            }
        }

        btnSubirActividad.setOnClickListener {
            startActivity(Intent(this, PaginaAltaActividad::class.java).apply {
                putExtra("ID", id)
            })
        }
    }

    // Método para cargar las actividades del profesor
    private fun cargarActividades(id: String, num : Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val actividadesList = withContext(Dispatchers.IO) { fireStore.cargarActividades() }

                // Filtrar actividades pasadas y eliminarlas
                val actividadesValidas = filtrarYEliminarActividadesPasadas(actividadesList, num)

                // Configura el adaptador con las actividades válidas
                configurarAdaptador(actividadesValidas, id)
            } catch (e: Exception) {
                val firestore = FireStore()
                firestore.registrarIncidencia("Error al cargar actividades: ${e.localizedMessage}")
                Toast.makeText(this@PaginaActividadProfe, "Error al cargar actividades: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Método para filtrar y eliminar actividades pasadas
    private suspend fun filtrarYEliminarActividadesPasadas(actividadesList: List<Actividad>, num : Int): List<Actividad> {
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
        adaptadorProfe = AdaptadorProfe(id, actividadesValidas)
        reciclador.layoutManager = LinearLayoutManager(this@PaginaActividadProfe)
        reciclador.adapter = adaptadorProfe
    }

    private fun configurarSpinner(idProfe : String) {

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
                cargarActividades(idProfe, selectedValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Opcional: manejar el caso en el que no se seleccione ninguna opción
            }
        }
    }
}
