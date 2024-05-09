package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.darlanota.R
import com.example.darlanota.clases.Usuario

class PaginaLogin : AppCompatActivity() {
    // Declare EditText outside onCreate()
    private lateinit var et_nick: EditText
    private lateinit var et_contra: EditText
    private lateinit var bto_inicioSesion: Button
    private lateinit var bto_registro: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        // Configuración de tema oscuro basado en preferencias
        val sharedPreferences = getSharedPreferences("preferencia_tema", MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("tema_oscuro_activado", false)
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        et_nick = findViewById(R.id.et_nick)
        et_contra = findViewById(R.id.et_contra)
        bto_inicioSesion = findViewById(R.id.bto_iniciarSesion)
        bto_registro = findViewById(R.id.bto_registro)

        bto_inicioSesion.setOnClickListener {
            if(et_nick.text.toString().equals("") || et_contra.text.toString().equals("")){
                Toast.makeText(this, "Faltan campos por rellenar", Toast.LENGTH_SHORT).show()
            }else{
                if(et_nick.text.toString().equals("profe")){
                    val intent = Intent(this, PaginaActividadProfe::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(this, PaginaActividadAlumno::class.java)
                    startActivity(intent)
                }
            }
        }

        bto_registro.setOnClickListener {
            val intent = Intent(this, PaginaRegistro::class.java)
            startActivity(intent)
        }

    }

}
