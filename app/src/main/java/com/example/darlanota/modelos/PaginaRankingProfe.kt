package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R

class PaginaRankingProfe : AppCompatActivity() {

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
    private lateinit var bto_cambiarRanking: Button

    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_perfil: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_profe_layout)

        et_1 = findViewById(R.id.tv_primeraPosPro)
        et_2 = findViewById(R.id.tv_segundaPosPro)
        et_3 = findViewById(R.id.tv_terceraPosPro)
        et_4 = findViewById(R.id.tv_cuartaPosPro)
        et_5 = findViewById(R.id.tv_quintaPosPro)
        et_6 = findViewById(R.id.tv_sextaPosPro)
        et_7 = findViewById(R.id.tv_septimaPosPro)
        et_8 = findViewById(R.id.tv_octavaPosPro)
        et_9 = findViewById(R.id.tv_novenaPosPro)
        et_10 = findViewById(R.id.tv_decimaPosPro)

        bto_cambiarRanking = findViewById(R.id.bto_cambiarRanking)

        iv_ranking = findViewById(R.id.iv_rankingRaPro)
        iv_actividades = findViewById(R.id.iv_actividadesRaPro)
        iv_perfil = findViewById(R.id.iv_perfilRaPro)

        iv_actividades.setOnClickListener {
            val intent = Intent(this, PaginaActividadProfe::class.java)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            val intent = Intent(this, PaginaPerfilProfe::class.java)
            startActivity(intent)
        }

        bto_cambiarRanking.setOnClickListener {
            val fragmentoDialogo = FragmentoNota()
            fragmentoDialogo.show(supportFragmentManager, "fragmento_dialogo")
        }
    }
}