package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R

class PaginaCorregirActividad : AppCompatActivity() {

    private lateinit var bto_descargar : Button
    private lateinit var bto_corregir : Button

    private lateinit var iv_perfil : ImageView
    private lateinit var iv_actividades : ImageView
    private lateinit var iv_ranking : ImageView

    private lateinit var spinner : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.corregir_actividad_layout)

        iv_actividades = findViewById(R.id.iv_actividadesAlta)
        iv_perfil = findViewById(R.id.iv_perfilAlta)
        iv_ranking = findViewById(R.id.iv_rankingAlta)

        bto_descargar = findViewById(R.id.bto_descargarVideo)
        bto_corregir = findViewById(R.id.bto_corregirActividad)

        spinner = findViewById(R.id.sp_alumnos)

        val alumnos = arrayOf("Alumno 1", "Alumno 2", "Alumno 3", "Alumno 4", "Alumno 5")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, alumnos)

        // Especificar el diseño del dropdown
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Asignar el adaptador al Spinner
        spinner.adapter = adapter

        iv_ranking.setOnClickListener {
            val intent = Intent(this, PaginaRankingProfe::class.java)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            val intent = Intent(this, PaginaPerfilProfe::class.java)
            startActivity(intent)
        }

        iv_actividades.setOnClickListener {
            val intent = Intent(this, PaginaRankingProfe::class.java)
            startActivity(intent)
        }

        bto_corregir.setOnClickListener {
            val fragmentoCalificar = FragmentoCalificar()
            fragmentoCalificar.show(supportFragmentManager, "fragmento_corregir")
        }

        bto_descargar.setOnClickListener {

        }
    }
}