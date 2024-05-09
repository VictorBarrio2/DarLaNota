package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.darlanota.R

class PaginaPerfil : AppCompatActivity() {

    private lateinit var bto_instrumento: Button
    private lateinit var bto_contra: Button
    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_nota: ImageView
    private lateinit var et_contra: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_layout) // Mueve setContentView aquí

        iv_ranking = findViewById(R.id.iv_rankingPerfil)
        iv_actividades = findViewById(R.id.iv_actividadesPerfil)
        bto_contra = findViewById(R.id.bto_cambiarContra)
        bto_instrumento = findViewById(R.id.bto_cambiarInstrumento)
        iv_nota = findViewById(R.id.iv_notaPerfil) // Inicializa iv_nota aquí
        et_contra = findViewById(R.id.et_contraPerfil)

        val sharedPreferences = getSharedPreferences("preferencia_tema", MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("tema_oscuro_activado", false)
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            iv_nota.setImageResource(R.drawable.luna)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            iv_nota.setImageResource(R.drawable.sol)
        }

        bto_instrumento.setOnClickListener {
            val intent = Intent(this, PaginaInstrumentos::class.java)
            startActivity(intent)
        }

        bto_contra.setOnClickListener {
            if(et_contra.text.toString().equals("")){
                Toast.makeText(this, "El campo esta vacio", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Contraseña cambiada", Toast.LENGTH_SHORT).show()
            }
        }


        iv_actividades.setOnClickListener {
            val intent = Intent(this, PaginaActividadAlumno::class.java)
            startActivity(intent)
        }

        iv_ranking.setOnClickListener {
            val intent = Intent(this, PaginaRankingAlumno::class.java)
            startActivity(intent)
        }

        iv_nota.setOnClickListener {
            // Guarda la preferencia del tema cuando se hace clic en iv_nota
            guardarPreferenciaTema(!isDarkModeEnabled)
            // Alterna el tema
            alternarTema()
        }
    }

    // Función para guardar la preferencia del tema en SharedPreferences
    private fun guardarPreferenciaTema(temaOscuroActivado: Boolean) {
        val sharedPreferences = getSharedPreferences("preferencia_tema", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("tema_oscuro_activado", temaOscuroActivado)
        editor.apply()
    }

    // Función para alternar entre el tema claro y oscuro
    private fun alternarTema() {
        val temaOscuroActivado = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        if (temaOscuroActivado) {
            // Cambiar al modo claro
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            // Cambiar al modo oscuro
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        // Reiniciar la actividad para aplicar los cambios de tema
        recreate()
    }
}
