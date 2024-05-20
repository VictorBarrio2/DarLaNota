package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
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
    private lateinit var reciclador: RecyclerView
    private lateinit var adaptadorAlumno: AdaptadorAlumno
    private val fireStore = FireStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividades_alumno_layout)

        iv_ranking = findViewById(R.id.iv_rankingAcAl)
        iv_actividades = findViewById(R.id.iv_actividadesAcAl)
        iv_perfil = findViewById(R.id.iv_perfilAcAl)
        reciclador = findViewById(R.id.rv_reciclador)

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
                intent.putExtra("ID", id)  // Ensure the ID is passed correctly
                startActivity(intent)
            }
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

                adaptadorAlumno = AdaptadorAlumno(id.toString(), actividadesValidas)
                reciclador.layoutManager = LinearLayoutManager(this@PaginaActividadAlumno)
                reciclador.adapter = adaptadorAlumno
            } catch (e: Exception) {
                Toast.makeText(this@PaginaActividadAlumno, "Error al cargar actividades: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                val firestore = FireStore()
                firestore.registrarIncidencia("Error al cargar actividades: ${e.localizedMessage}")
            }
        }
    }
}
