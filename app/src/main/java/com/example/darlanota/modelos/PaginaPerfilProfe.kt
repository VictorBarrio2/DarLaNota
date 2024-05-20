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

class PaginaPerfilProfe : AppCompatActivity() {

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
        setContentView(R.layout.perfil_profe_layout)

        // Initialize ID from intent
        id = intent.getStringExtra("ID") ?: "DefaultID"  // Use a default or handle the case where ID is null

        iv_ranking = findViewById(R.id.iv_rankingPerfilProfe)
        iv_actividades = findViewById(R.id.iv_actividadesPerfilProfe)
        bto_contra = findViewById(R.id.bto_cambiarContraProfe)
        iv_nota = findViewById(R.id.iv_notaPerfilProfe)
        et_contra = findViewById(R.id.et_contraPerfilProfe)
        iv_cerrarSesion = findViewById(R.id.iv_salirProfe)
        tv_nombre = findViewById(R.id.tv_nickPerfilProfe)

        val fireStore = FireStore()

        CoroutineScope(Dispatchers.Main).launch {
            val nombre = fireStore.obtenerNombreUsuario(id)
            tv_nombre.text = nombre ?: "Usuario Desconocido" // Handle null case
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

        bto_contra.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val nuevaContrasena = et_contra.text.toString()
                if (nuevaContrasena.isBlank()) {
                    Toast.makeText(this@PaginaPerfilProfe, "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val usuarioActual = FirebaseAuth.getInstance().currentUser
                if (usuarioActual != null) {
                    try {
                        val resultado = fireStore.cambiarContrasenaUsuario(nuevaContrasena)
                        Toast.makeText(this@PaginaPerfilProfe, resultado, Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@PaginaPerfilProfe, "Error al cambiar la contraseña: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@PaginaPerfilProfe, "No hay usuario autenticado. Por favor, inicie sesión.", Toast.LENGTH_LONG).show()
                }
            }
        }

        iv_actividades.setOnClickListener {
            startActivity(Intent(this, PaginaActividadProfe::class.java))
        }

        iv_ranking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingProfe::class.java))
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
