package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R

class PaginaActividadProfe: AppCompatActivity() {

    private lateinit var rv_reciclador : RecyclerView

    private lateinit var bto_altaActividad : Button

    private lateinit var iv_activiades : ImageView
    private lateinit var iv_ranking : ImageView
    private lateinit var iv_perfil: ImageView

    private lateinit var reciclador: RecyclerView
    private lateinit var adaptadorProfe: AdaptadorProfe




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividades_profesor_layout)

        iv_activiades = findViewById(R.id.iv_actividadesProfe)
        iv_ranking = findViewById(R.id.iv_rankingProfe)
        iv_perfil = findViewById(R.id.iv_perfilProfe)

        reciclador = findViewById(R.id.rv_reclicadorProfe)

        bto_altaActividad = findViewById(R.id.bto_altaActividad)


        val dataList = listOf("Actividad 1", "Actividad 2", "Actividad 3" , "Actividad 4", "Actividad 5" , "Actividad 6", "Actividad 7")

        // Crea el adaptador y pasa la lista de datos
        adaptadorProfe = AdaptadorProfe(dataList)

        // Configura el LinearLayoutManager y el adaptador para el RecyclerView
        reciclador.layoutManager = LinearLayoutManager(this)
        reciclador.adapter = adaptadorProfe

        iv_activiades.setOnClickListener {

        }

        iv_perfil.setOnClickListener {
            val intent = Intent(this, PaginaPerfil::class.java)
            startActivity(intent)
        }

        iv_ranking.setOnClickListener {
            val intent = Intent(this, PaginaRankingProfe::class.java)
            startActivity(intent)
        }

        bto_altaActividad.setOnClickListener {
            val intent = Intent(this, PaginaAltaActividad::class.java)
            startActivity(intent)
        }

    }
}