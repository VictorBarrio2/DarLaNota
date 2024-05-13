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

class PaginaPerfilProfe : AppCompatActivity() {

    private lateinit var bto_contra: Button
    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_nota: ImageView
    private lateinit var iv_cerrarSesion: ImageView
    private lateinit var et_contra: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_profe_layout)

        iv_ranking = findViewById(R.id.iv_rankingPerfilProfe)
        iv_actividades = findViewById(R.id.iv_actividadesPerfilProfe)
        bto_contra = findViewById(R.id.bto_cambiarContraProfe)
        iv_nota = findViewById(R.id.iv_notaPerfilProfe)
        et_contra = findViewById(R.id.et_contraPerfilProfe)
        iv_cerrarSesion = findViewById(R.id.iv_salirProfe)

        val sharedPreferences = getSharedPreferences("preferencia_tema", MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("tema_oscuro_activado", false)
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            iv_nota.setImageResource(R.drawable.luna)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            iv_nota.setImageResource(R.drawable.sol)
        }

        bto_contra.setOnClickListener {
            if(et_contra.text.toString().equals("")){
                Toast.makeText(this, "El campo esta vacio", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Contraseña cambiada", Toast.LENGTH_SHORT).show()
            }
        }


        iv_actividades.setOnClickListener {
            val intent = Intent(this, PaginaActividadProfe::class.java)
            startActivity(intent)
        }

        iv_ranking.setOnClickListener {
            val intent = Intent(this, PaginaRankingProfe::class.java)
            startActivity(intent)
        }

        iv_nota.setOnClickListener {
            guardarPreferenciaTema(!isDarkModeEnabled)
            alternarTema()
        }

        iv_cerrarSesion.setOnClickListener {
            val sharedPreferences = getSharedPreferences("login_preferences", MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                remove("nick")
                remove("contraseña")
                putBoolean("guardar_credenciales", false)
                apply()
            }
            finishAffinity()
            val intent = Intent(this, PaginaLogin::class.java)
            startActivity(intent)
        }
    }

    private fun guardarPreferenciaTema(temaOscuroActivado: Boolean) {
        val sharedPreferences = getSharedPreferences("preferencia_tema", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("tema_oscuro_activado", temaOscuroActivado)
        editor.apply()
    }

    private fun alternarTema() {
        val temaOscuroActivado = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        if (temaOscuroActivado) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        recreate()
    }
}
