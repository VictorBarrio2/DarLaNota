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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaginaPerfilAlumno : AppCompatActivity() {

    private lateinit var btnInstrumento: Button
    private lateinit var btnContra: Button
    private lateinit var ivRanking: ImageView
    private lateinit var ivActividades: ImageView
    private lateinit var ivCerrarSesion: ImageView
    private lateinit var imagenTema: ImageView
    private lateinit var etContra: EditText
    private lateinit var tvNombre: TextView
    private lateinit var id: String
    private lateinit var fireStore: FireStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_alumno_layout)

        FirebaseApp.initializeApp(this)

        id = intent.getStringExtra("ID") ?: "DefaultID"
        inicializarVistas()
        cargarDatosUsuario()
        aplicarImagenTema()
        configurarManejadoresEventos()
    }

    private fun inicializarVistas() {
        ivRanking = findViewById(R.id.iv_rankingPerfil)
        ivActividades = findViewById(R.id.iv_actividadesPerfil)
        btnContra = findViewById(R.id.bto_cambiarContra)
        btnInstrumento = findViewById(R.id.bto_cambiarInstrumento)
        etContra = findViewById(R.id.et_contraPerfilAlumno)
        ivCerrarSesion = findViewById(R.id.iv_salir)
        tvNombre = findViewById(R.id.tv_nickPerfil)
        imagenTema = findViewById(R.id.iv_temaAlumno)
        fireStore = FireStore()
    }

    private fun cargarDatosUsuario() {
        val fireStore = FireStore()
        CoroutineScope(Dispatchers.Main).launch {
            val nombre = fireStore.obtenerNombreUsuario(id)
            tvNombre.text = nombre ?: "Nombre no disponible"
        }
    }

    private fun configurarManejadoresEventos() {
        btnInstrumento.setOnClickListener {
            startActivity(Intent(this, PaginaInstrumentos::class.java))
        }

        btnContra.setOnClickListener {
            cambiarContrasena()
        }

        ivActividades.setOnClickListener {
            startActivity(Intent(this, PaginaActividadAlumno::class.java).also { it.putExtra("ID", id) })
        }

        ivRanking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingAlumno::class.java).also { it.putExtra("ID", id) })
        }

        ivCerrarSesion.setOnClickListener {
            limpiarPreferenciasLogin()
            cerrarSesion()
        }

        imagenTema.setOnClickListener {
            cambiarTema()
        }
    }

    private fun cambiarContrasena() {
        CoroutineScope(Dispatchers.Main).launch {
            val nuevaContrasena = etContra.text.toString()
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

    private fun cambiarTema() {
        val prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE)
        val esTemaOscuro = prefs.getBoolean("tema_oscuro", false)
        if (esTemaOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            imagenTema.setImageResource(R.drawable.sol) // Imagen de sol cuando el tema es claro
            prefs.edit().putBoolean("tema_oscuro", false).apply()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            imagenTema.setImageResource(R.drawable.luna) // Imagen de luna cuando el tema es oscuro
            prefs.edit().putBoolean("tema_oscuro", true).apply()
        }
    }


    private fun limpiarPreferenciasLogin() {
        val sharedPreferences = getSharedPreferences("login_preferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("nick")
            remove("contraseña")
            putBoolean("guardar_credenciales", false)
            apply()
        }
    }

    private fun aplicarImagenTema() {
        val prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE)
        val esTemaOscuro = prefs.getBoolean("tema_oscuro", false)
        if (esTemaOscuro) {
            imagenTema.setImageResource(R.drawable.luna)
        } else {
            imagenTema.setImageResource(R.drawable.sol)
        }
    }


    private fun cerrarSesion() {
        finishAffinity()
        startActivity(Intent(this, PaginaLogin::class.java))
    }
}
