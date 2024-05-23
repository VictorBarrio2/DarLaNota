package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.*

class PaginaRankingProfe : AppCompatActivity() {

    // Variables para las vistas de la interfaz
    private lateinit var et_1: TextView
    private lateinit var et_2: TextView
    private lateinit var et_3: TextView
    private lateinit var et_4: TextView
    private lateinit var et_5: TextView
    private lateinit var et_6: TextView
    private lateinit var et_7: TextView
    private lateinit var et_8: TextView
    private lateinit var et_9: TextView
    private lateinit var et_10: TextView

    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_perfil: ImageView

    private lateinit var bto_cambiarRanking: Button

    private lateinit var id: String

    private val fireStore = FireStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_profe_layout)

        // Inicializar las vistas
        inicializarVistas()

        // Obtener el ID del intent
        id = intent.getStringExtra("ID") ?: ""

        // Configurar listeners de los botones e imágenes
        configurarListeners()

        // Cargar el ranking de usuarios
        cargarRanking()
    }

    // Método para inicializar las vistas
    private fun inicializarVistas() {
        et_1 = findViewById(R.id.tv_primeraPosPro)
        et_2 = findViewById(R.id.tv_segundaPosPro)
        et_3 = findViewById(R.id.tv_terceraPosPro)
        et_4 = findViewById(R.id.tv_cuartaPosPro)
        et_5 = findViewById(R.id.tv_quintaPosPro)
        et_6 = findViewById(R.id.tv_sextaPosPro)
        et_7 = findViewById(R.id.tv_septimaPosPro)
        et_8 = findViewById(R.id.tv_octavaPosPro)
        et_9 = findViewById(R.id.tv_novenaPosPro)
        et_10 = findViewById(R.id.tv_decimaPosPro)

        iv_ranking = findViewById(R.id.iv_rankingRaPro)
        iv_actividades = findViewById(R.id.iv_actividadesRaPro)
        iv_perfil = findViewById(R.id.iv_perfilRaPro)

        bto_cambiarRanking = findViewById(R.id.bto_cambiarRanking)
    }

    // Método para configurar los listeners de los botones e imágenes
    private fun configurarListeners() {
        iv_actividades.setOnClickListener {
            val intent = Intent(this, PaginaActividadProfe::class.java)
            intent.putExtra("ID", id)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)  // Retardo de 300 milisegundos para prevenir clicks fantasma
                val intent = Intent(this@PaginaRankingProfe, PaginaPerfilProfe::class.java)
                intent.putExtra("ID", id)
                startActivity(intent)
            }
        }

        bto_cambiarRanking.setOnClickListener {
            val fragmentoNota = FragmentoNota()
            fragmentoNota.show(supportFragmentManager, "fragmento_nota")
        }
    }

    // Método para cargar el ranking de usuarios desde Firestore
    private fun cargarRanking() {
        CoroutineScope(Dispatchers.Main).launch {
            val ranking = withContext(Dispatchers.IO) { fireStore.obtenerRankingUsuarios() }
            actualizarRanking(ranking)
        }
    }

    // Método para actualizar las vistas con el ranking de usuarios
    private fun actualizarRanking(ranking: List<Pair<String, Long>>) {
        val rankingViews = listOf(et_1, et_2, et_3, et_4, et_5, et_6, et_7, et_8, et_9, et_10)

        // Itera sobre cada TextView y establece el texto según el ranking o lo deja en blanco si no hay suficientes usuarios
        rankingViews.forEachIndexed { index, textView ->
            textView.text = if (index < ranking.size) {
                "${index + 1}. ${ranking[index].first}"
            } else {
                "" // Limpia el TextView si no hay suficientes entradas
            }
        }
    }
}
