package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R

class PaginaVerActividad : AppCompatActivity() {

    private lateinit var bto_subirVideo : Button

    private lateinit var tv_descripcion : TextView
    private lateinit var tv_titulo : TextView
    private lateinit var tv_calificacion : TextView

    private lateinit var iv_perfil : ImageView
    private lateinit var iv_actividades : ImageView
    private lateinit var iv_ranking : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ver_actividades_layout)

        val id_actividad = intent.getStringExtra("ACTIVIDAD_ID")
        Toast.makeText(this, id_actividad, Toast.LENGTH_LONG).show()

        iv_actividades = findViewById(R.id.iv_actividadVer)
        iv_perfil = findViewById(R.id.iv_perfilVer)
        iv_ranking = findViewById(R.id.iv_rankingVer)

        iv_actividades.setOnClickListener {
            val intent = Intent(this, PaginaActividadAlumno::class.java)
            startActivity(intent)
        }

        iv_ranking.setOnClickListener {
            val intent = Intent(this, PaginaRankingAlumno::class.java)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            val intent = Intent(this, PaginaPerfilAlumno::class.java)
            startActivity(intent)
        }

    }
}