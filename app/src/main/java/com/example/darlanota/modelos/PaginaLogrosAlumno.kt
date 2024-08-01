package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaginaLogrosAlumno: AppCompatActivity() {

    private lateinit var imagenClasificacion: ImageView
    private lateinit var imagenActividades: ImageView
    private lateinit var imagenLogro: ImageView
    private lateinit var imagenPerfil: ImageView

    private lateinit var id: String
    private lateinit var db: FireStore

    private lateinit var progressBar: ProgressBar
    private lateinit var puntuacion: String

    // Método que se ejecuta al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.logros_alumno_layout)

        // Inicialización de vistas y obtención del ID del intent
        inicializarVistas()
        id = intent.getStringExtra("ID") ?: "ID_default"
        db = FireStore()


        CoroutineScope(Dispatchers.Main).launch {
            puntuacion = db.devolverPuntuacion(id) ?: "Puntuacion no disponible"
            Toast.makeText(this@PaginaLogrosAlumno, "La puntuacion es " + puntuacion, Toast.LENGTH_SHORT).show()
        }

        configurarListeners()
    }

    // Método para inicializar las vistas de la actividad
    private fun inicializarVistas() {
        imagenPerfil = findViewById(R.id.iv_perfilLogro)
        imagenLogro = findViewById(R.id.iv_logroLogro)
        imagenActividades = findViewById(R.id.iv_actividadesLogro)
        imagenClasificacion = findViewById(R.id.iv_rankingLogro)
    }

    // Método para configurar los listeners de los botones e imágenes
    private fun configurarListeners() {
        imagenPerfil.setOnClickListener {
            val intent = Intent(this, PaginaPerfilAlumno::class.java)
            intent.putExtra("ID", id)
            startActivity(intent)
        }

        imagenLogro.setOnClickListener {
        }

        imagenActividades.setOnClickListener {
            val intent = Intent(this, PaginaActividadAlumno::class.java)
            intent.putExtra("ID", id)
            startActivity(intent)
        }

        imagenClasificacion.setOnClickListener {
            val intent = Intent(this, PaginaRankingAlumno::class.java)
            intent.putExtra("ID", id)
            startActivity(intent)
        }
    }
}