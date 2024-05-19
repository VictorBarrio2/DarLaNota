package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.*

class PaginaRankingAlumno : AppCompatActivity() {

    private lateinit var et_1: TextView
    private lateinit var et_2: TextView
    private lateinit var et_3: TextView
    private lateinit var et_4: TextView
    private lateinit var et_5: TextView
    private lateinit var et_6: TextView
    private lateinit var et_7: TextView
    private lateinit var et_8: TextView
    private lateinit var et_9: TextView
    private lateinit var et_10: TextView
    private lateinit var et_puntuacion: TextView
    private lateinit var et_posicion: TextView

    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_perfil: ImageView

    private lateinit var id: String
    private val fireStore = FireStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_alumno_layout)

        id = intent.getStringExtra("ID").toString()
        et_1 = findViewById(R.id.tv_primeraPosAl)
        et_2 = findViewById(R.id.tv_segundaPosAl)
        et_3 = findViewById(R.id.tv_terceraPosAl)
        et_4 = findViewById(R.id.tv_cuartaPosicionAl)
        et_5 = findViewById(R.id.tv_quintaPosAl)
        et_6 = findViewById(R.id.tv_sextaPosAl)
        et_7 = findViewById(R.id.tv_septimaPosAl)
        et_8 = findViewById(R.id.tv_octavaPosAl)
        et_9 = findViewById(R.id.tv_novenaPosAl)
        et_10 = findViewById(R.id.tv_decimaPosAl)
        et_puntuacion = findViewById(R.id.tv_puntuacion)
        et_posicion = findViewById(R.id.tv_posicion)

        iv_ranking = findViewById(R.id.iv_rankingRaAl)
        iv_actividades = findViewById(R.id.iv_actividadesRaAl)
        iv_perfil = findViewById(R.id.iv_perfilRaAl)

        iv_actividades.setOnClickListener {
            val intent = Intent(this, PaginaActividadAlumno::class.java)
            intent.putExtra("ID", id)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            val intent = Intent(this, PaginaPerfilAlumno::class.java)
            intent.putExtra("ID", id)
            startActivity(intent)
        }

        cargarRanking()
    }

    private fun cargarRanking() {
        CoroutineScope(Dispatchers.Main).launch {
            val ranking = withContext(Dispatchers.IO) { fireStore.obtenerRankingUsuarios() }
            val nombreUsuario = withContext(Dispatchers.IO) { fireStore.obtenerNombreUsuario(id) }
            val posicion = ranking.indexOfFirst { it.first == nombreUsuario }
            val puntuacion = ranking.find { it.first == nombreUsuario }?.second

            actualizarRanking(ranking)
            actualizarPosicionYPuntuacion(posicion, puntuacion)
        }
    }

    private fun actualizarRanking(ranking: List<Pair<String, Long>>) {
        val rankingViews = listOf(et_1, et_2, et_3, et_4, et_5, et_6, et_7, et_8, et_9, et_10)

        for (i in rankingViews.indices) {
            if (i < ranking.size) {
                rankingViews[i].text = ranking[i].first
            } else {
                rankingViews[i].text = ""
            }
        }
    }

    private fun actualizarPosicionYPuntuacion(posicion: Int, puntuacion: Long?) {
        if (posicion != -1 && puntuacion != null) {
            et_posicion.text = "Posición: ${posicion + 1}"
            et_puntuacion.text = "Puntuación: $puntuacion"
        } else {
            et_posicion.text = "Posición: N/A"
            et_puntuacion.text = "Puntuación: N/A"
        }
    }
}
