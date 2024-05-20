package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaginaPerfilAlumno : AppCompatActivity() {

    private lateinit var bto_instrumento: Button
    private lateinit var bto_contra: Button
    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_nota: ImageView
    private lateinit var iv_cerrarSesion: ImageView
    private lateinit var et_contra: EditText
    private lateinit var tv_nombre: TextView
    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_alumno_layout)

        // Get the ID from the Intent
        id = intent.getStringExtra("ID") ?: "DefaultID" // Provide a default ID or handle the case where ID is null

        // Inicialización de vistas
        iv_ranking = findViewById(R.id.iv_rankingPerfil)
        iv_actividades = findViewById(R.id.iv_actividadesPerfil)
        bto_contra = findViewById(R.id.bto_cambiarContra)
        bto_instrumento = findViewById(R.id.bto_cambiarInstrumento)
        iv_nota = findViewById(R.id.iv_notaPerfil)
        et_contra = findViewById(R.id.et_contraPerfilAlumno)
        iv_cerrarSesion = findViewById(R.id.iv_salir)
        tv_nombre = findViewById(R.id.tv_nickPerfil)

        val fireStore = FireStore()

        CoroutineScope(Dispatchers.Main).launch {
            val nombre = fireStore.obtenerNombreUsuario(id)
            tv_nombre.text = nombre ?: "Nombre no disponible" // Handle the case where name might be null
        }

        val sharedPreferences = getSharedPreferences("preferencia_tema", MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("tema_oscuro_activado", false)
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            iv_nota.setImageResource(R.drawable.luna)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            iv_nota.setImageResource(R.drawable.sol)
        }

        // Manejadores de eventos
        bto_instrumento.setOnClickListener {
            startActivity(Intent(this, PaginaInstrumentos::class.java))
        }

        bto_contra.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val nuevaContrasena = et_contra.text.toString()
                if (nuevaContrasena.isBlank()) {
                    Toast.makeText(this@PaginaPerfilAlumno, "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val usuarioActual = FirebaseAuth.getInstance().currentUser
                if (usuarioActual != null) {
                    try {
                        val resultado = fireStore.cambiarContrasenaUsuario(nuevaContrasena)
                        Toast.makeText(this@PaginaPerfilAlumno, resultado, Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@PaginaPerfilAlumno, "Error al cambiar la contraseña: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@PaginaPerfilAlumno, "No hay usuario autenticado. Por favor, inicie sesión.", Toast.LENGTH_LONG).show()
                }
            }
        }

        iv_actividades.setOnClickListener {
            Intent(this, PaginaActividadAlumno::class.java).also {
                it.putExtra("ID", id)
                startActivity(it)
            }
        }

        iv_ranking.setOnClickListener {
            Intent(this, PaginaRankingAlumno::class.java).also {
                it.putExtra("ID", id)
                startActivity(it)
            }
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
            startActivity(Intent(this, PaginaLogin::class.java))
        }
    }

    private fun guardarPreferenciaTema(temaOscuroActivado: Boolean) {
        val sharedPreferences = getSharedPreferences("preferencia_tema", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("tema_oscuro_activado", temaOscuroActivado)
            apply()
        }
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
