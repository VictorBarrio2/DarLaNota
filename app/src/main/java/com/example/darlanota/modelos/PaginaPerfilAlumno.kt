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
    private lateinit var iv_cerrarSesion: ImageView
    private lateinit var et_contra: EditText
    private lateinit var tv_nombre: TextView
    private lateinit var id: String
    private lateinit var fireStore : FireStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_alumno_layout)

        id = intent.getStringExtra("ID") ?: "DefaultID"
        initializeViews()
        configureTheme()
        loadUserData()

        setupEventHandlers()
    }

    private fun initializeViews() {
        iv_ranking = findViewById(R.id.iv_rankingPerfil)
        iv_actividades = findViewById(R.id.iv_actividadesPerfil)
        bto_contra = findViewById(R.id.bto_cambiarContra)
        bto_instrumento = findViewById(R.id.bto_cambiarInstrumento)
        et_contra = findViewById(R.id.et_contraPerfilAlumno)
        iv_cerrarSesion = findViewById(R.id.iv_salir)
        tv_nombre = findViewById(R.id.tv_nickPerfil)
        fireStore = FireStore()
    }

    private fun configureTheme() {
        // Set the light theme always
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun loadUserData() {
        val fireStore = FireStore()
        CoroutineScope(Dispatchers.Main).launch {
            val nombre = fireStore.obtenerNombreUsuario(id)
            tv_nombre.text = nombre ?: "Nombre no disponible"
        }
    }

    private fun setupEventHandlers() {
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



        iv_cerrarSesion.setOnClickListener {
            clearLoginPreferences()
            logout()
        }
    }

    private fun clearLoginPreferences() {
        val sharedPreferences = getSharedPreferences("login_preferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("nick")
            remove("contraseña")
            putBoolean("guardar_credenciales", false)
            apply()
        }
    }

    private fun logout() {
        finishAffinity()
        startActivity(Intent(this, PaginaLogin::class.java))
    }
}
