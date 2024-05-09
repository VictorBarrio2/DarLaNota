package com.example.darlanota.modelos
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R

class PaginaActividadAlumno : AppCompatActivity() {

    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_perfil: ImageView
    private lateinit var reciclador: RecyclerView
    private lateinit var adaptadorAlumno: AdaptadorAlumno

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividades_alumno_layout)

        iv_ranking = findViewById(R.id.iv_rankingAcAl)
        iv_actividades = findViewById(R.id.iv_actividadesAcAl)
        iv_perfil = findViewById(R.id.iv_perfilAcAl)
        reciclador = findViewById(R.id.rv_reciclador) // Inicializa reciclador

        // Crea una lista de datos de ejemplo
        val dataList = listOf("Actividad 1", "Actividad 2", "Actividad 3" , "Actividad 4", "Actividad 5" , "Actividad 6", "Actividad 7")

        // Crea el adaptador y pasa la lista de datos
        adaptadorAlumno = AdaptadorAlumno(dataList)

        // Configura el LinearLayoutManager y el adaptador para el RecyclerView
        reciclador.layoutManager = LinearLayoutManager(this)
        reciclador.adapter = adaptadorAlumno

        iv_ranking.setOnClickListener {
            val intent = Intent(this, PaginaRankingAlumno::class.java)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            val intent = Intent(this, PaginaPerfil::class.java)
            startActivity(intent)
        }
    }
}
