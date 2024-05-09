package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_alumno_layout)

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
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            val intent = Intent(this, PaginaPerfil::class.java)
            startActivity(intent)
        }



    }
}