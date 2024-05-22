package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividades_profesor_layout)

        val id = intent.getStringExtra("ID") ?: ""

        ivRanking = findViewById(R.id.iv_rankingProfe)
        ivActividades = findViewById(R.id.iv_actividadesProfe)
        ivPerfil = findViewById(R.id.iv_perfilProfe)
        reciclador = findViewById(R.id.rv_reclicadorProfe)
        btnSubirActividad = findViewById(R.id.bto_altaActividad)

        ivRanking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingProfe::class.java).apply {
                putExtra("ID", id)
            })
        }

        ivPerfil.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val intent = Intent(this@PaginaActividadProfe, PaginaPerfilProfe::class.java)
                intent.putExtra("ID", id)  // Ensure the ID is passed correctly
                startActivity(intent)
            }
        }

        btnSubirActividad.setOnClickListener {
            startActivity(Intent(this, PaginaAltaActividad::class.java).apply {
                putExtra("ID", id)
            })
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val actividadesList = withContext(Dispatchers.IO) { fireStore.cargarActividades() }

                // Filtrar actividades pasadas y eliminarlas
                val actividadesValidas = actividadesList.filter { actividad ->
                    val fechaFin = actividad.fechafin?.toDate()
                    if (fechaFin != null && fechaFin.before(Date())) {
                        withContext(Dispatchers.IO) {
                            fireStore.eliminarActividad(actividad.id)
                        }
                        false
                    } else {
                        true
                    }
                }

                adaptadorProfe = AdaptadorProfe(id, actividadesValidas)
                reciclador.layoutManager = LinearLayoutManager(this@PaginaActividadProfe)
                reciclador.adapter = adaptadorProfe
            } catch (e: Exception) {
                val firestore = FireStore()
                firestore.registrarIncidencia("Error al cargar actividades: ${e.localizedMessage}")
                Toast.makeText(this@PaginaActividadProfe, "Error al cargar actividades: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
