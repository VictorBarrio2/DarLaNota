package com.example.darlanota.modelos
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad
import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.*

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

        iv_ranking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingAlumno::class.java))
        }

        iv_perfil.setOnClickListener {
            startActivity(Intent(this, PaginaPerfilAlumno::class.java))
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val actividadesList = withContext(Dispatchers.IO) { fireStore.cargarActividades() }
                adaptadorAlumno = AdaptadorAlumno(actividadesList)
                reciclador.layoutManager = LinearLayoutManager(this@PaginaActividadAlumno)
                reciclador.adapter = adaptadorAlumno
            } catch (e: Exception) {
                Toast.makeText(this@PaginaActividadAlumno, "Error al cargar actividades: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
