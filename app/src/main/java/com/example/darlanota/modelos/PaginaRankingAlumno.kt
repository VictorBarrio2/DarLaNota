package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.*

class PaginaRankingAlumno : AppCompatActivity() {

    // Variables para las vistas de la interfaz
    private lateinit var et_1: TextView
    private lateinit var et_2: TextView
    private lateinit var et_3: TextView
    private lateinit var et_4: TextView
    private lateinit var et_5: TextView
    private lateinit var et_6: TextView
    private lateinit var et_7: TextView
    private lateinit var et_8: TextView
    private lateinit var et_puntuacion: TextView
    private lateinit var et_posicion: TextView

    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_perfil: ImageView
    private lateinit var iv_logro: ImageView

    private lateinit var nick: String
    private val fireStore = FireStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_alumno_layout)

        // Inicializar vistas
        inicializarVistas()

        // Obtener el ID del intent
        nick = intent.getStringExtra("NICK").toString()

        // Configurar listeners de los botones
        configurarListeners()

        // Cargar el ranking de usuarios
        cargarRanking()
    }

    // Método para inicializar las vistas
    private fun inicializarVistas() {
        et_1 = findViewById(R.id.tv_primeraPosAl)
        et_2 = findViewById(R.id.tv_segundaPosAl)
        et_3 = findViewById(R.id.tv_terceraPosAl)
        et_4 = findViewById(R.id.tv_cuartaPosAl)
        et_5 = findViewById(R.id.tv_quintaPosAl)
        et_6 = findViewById(R.id.tv_sextaPosAl)
        et_7 = findViewById(R.id.tv_septimaPosAl)
        et_8 = findViewById(R.id.tv_octavaPosAl)
        et_puntuacion = findViewById(R.id.tv_puntuacion)
        et_posicion = findViewById(R.id.tv_posicion)

        iv_ranking = findViewById(R.id.iv_rankingRaAl)
        iv_actividades = findViewById(R.id.iv_actividadesRaAl)
        iv_perfil = findViewById(R.id.iv_perfilRaAl)
        iv_logro = findViewById(R.id.iv_logroRankingAl)
    }

    // Método para configurar los listeners de los botones
    private fun configurarListeners() {
        iv_actividades.setOnClickListener {
            val intent = Intent(this, PaginaActividadAlumno::class.java)
            intent.putExtra("NICK", nick)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)  // Retardo de 300 milisegundos para prevenir clicks fantasma
                val intent = Intent(this@PaginaRankingAlumno, PaginaPerfilAlumno::class.java)
                intent.putExtra("NICK", nick)
                startActivity(intent)
            }
        }

        iv_logro.setOnClickListener {
            startActivity(Intent(this, PaginaLogrosAlumno::class.java).apply {
                putExtra("NICK", nick)
            })
        }
    }

    // Método para cargar el ranking de usuarios
    private fun cargarRanking() {
        CoroutineScope(Dispatchers.Main).launch {
            val ranking = withContext(Dispatchers.IO) { fireStore.obtenerRankingUsuarios() }
            val nombreUsuario = nick
            val posicion = ranking.indexOfFirst { it.first == nombreUsuario }
            val puntuacion = ranking.find { it.first == nombreUsuario }?.second

            // Actualizar el ranking y la posición/puntuación del usuario
            actualizarRanking(ranking)
            actualizarPosicionYPuntuacion(posicion, puntuacion)
        }
    }

    // Método para actualizar las vistas con el ranking de usuarios
    private fun actualizarRanking(ranking: List<Pair<String, Long>>) {
        val rankingViews = listOf(et_1, et_2, et_3, et_4, et_5, et_6, et_7, et_8)

        // Itera sobre cada TextView y establece el texto según el ranking o lo deja en blanco si no hay suficientes usuarios
        rankingViews.forEachIndexed { index, textView ->
            textView.text = if (index < ranking.size) {
                "${ranking[index].first}"
            } else {
                "" // Limpia el TextView si no hay suficientes entradas
            }
        }
    }

    // Método para actualizar la posición y puntuación del usuario en las vistas
    private fun actualizarPosicionYPuntuacion(posicion: Int, puntuacion: Long?) {
        if (posicion != -1 && puntuacion != null) {
            et_posicion.text = "Posición: ${posicion + 1}"
            et_puntuacion.text = "Puntuación: $puntuacion"
        } else {
            et_posicion.text = "Posición: N/A"
            et_puntuacion.text = "Puntuación: N/A"
        }
    }
}
