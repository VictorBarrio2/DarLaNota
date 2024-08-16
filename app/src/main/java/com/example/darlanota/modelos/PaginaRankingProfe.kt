package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
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

    private lateinit var iv_reiniciar: ImageView
    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_perfil: ImageView

    private lateinit var bto_cambiarRanking: Button

    private lateinit var nick: String

    private val fireStore = FireStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_profe_layout)

        // Inicializar las vistas
        inicializarVistas()

        // Obtener el ID del intent
        nick = intent.getStringExtra("NICK") ?: ""

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

        iv_ranking = findViewById(R.id.iv_rankingRaPro)
        iv_actividades = findViewById(R.id.iv_actividadesRaPro)
        iv_perfil = findViewById(R.id.iv_perfilRaPro)
        iv_reiniciar = findViewById(R.id.iv_reinicio)

        bto_cambiarRanking = findViewById(R.id.bto_cambiarRanking)
    }

    // Método para configurar los listeners de los botones e imágenes
    private fun configurarListeners() {
        iv_actividades.setOnClickListener {
            val intent = Intent(this, PaginaActividadProfe::class.java)
            intent.putExtra("NICK", nick)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)  // Retardo de 300 milisegundos para prevenir clicks fantasma
                val intent = Intent(this@PaginaRankingProfe, PaginaPerfilProfe::class.java)
                intent.putExtra("NICK", nick)
                startActivity(intent)
            }
        }

        bto_cambiarRanking.setOnClickListener {
            val fragmentoNota = FragmentoNota()
            intent.putExtra("NICK", nick)
            fragmentoNota.show(supportFragmentManager, "fragmento_nota")
        }


        iv_reiniciar.setOnClickListener {
            // Crear un AlertDialog Builder
            val builder = AlertDialog.Builder(this)

            // Establecer el título y el mensaje
            builder.setTitle("Confirmación")
            builder.setMessage("¿Seguro que quieres reiniciar el ranking?")

            // Crear el diálogo y personalizar los botones
            val alertDialog = builder.create()

            // Botón "Sí"
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Sí") { _, _ ->
                lifecycleScope.launch {
                    CoroutineScope(Dispatchers.Main).launch {
                        fireStore.reiniciarRanking()
                    }
                }
            }

            // Botón "No"
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, _ ->
                dialog.dismiss()
            }

            // Mostrar el diálogo
            alertDialog.show()

            // Personalizar el color de los botones
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.textColor))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.textColor))
        }


    }

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

    private fun cargarRanking() {
        CoroutineScope(Dispatchers.Main).launch {
            val ranking = withContext(Dispatchers.IO) { fireStore.obtenerRankingUsuarios() }

            // Actualizar el ranking y la posición/puntuación del usuario
            actualizarRanking(ranking)
        }
    }
}
