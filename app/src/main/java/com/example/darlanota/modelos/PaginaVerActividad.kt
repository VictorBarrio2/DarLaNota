package com.example.darlanota.modelos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.example.darlanota.clases.Entrega
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PaginaVerActividad : AppCompatActivity() {

    // Declaración de elementos de la interfaz
    private lateinit var botonSubirVideo: Button
    private lateinit var textoDescripcion: TextView
    private lateinit var textoTitulo: TextView
    private lateinit var fechaCierre: TextView
    private lateinit var textoCalificacion: TextView
    private lateinit var textoEntregado: TextView
    private lateinit var iconoPerfil: ImageView
    private lateinit var iconoActividades: ImageView
    private lateinit var iconoRanking: ImageView
    private lateinit var id: String
    private lateinit var id_actividad: String
    private var entregado: Boolean = false

    companion object {
        private const val CODIGO_SELECCION_VIDEO = 1000 // Código para la selección de video
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ver_actividades_layout) // Configurar el layout de la actividad

        inicializarVistas() // Inicializar los elementos de la interfaz
        configurarNavegacion() // Configurar la navegación entre actividades
        verificarEntregaYCalificacion() // Verificar si la actividad ya fue entregada y calificada
    }

    // Inicializa las vistas del layout
    private fun inicializarVistas() {
        // Vincular los elementos del layout con las variables
        textoTitulo = findViewById(R.id.tv_tituloActVer)
        textoDescripcion = findViewById(R.id.tv_descripcionVer)
        textoCalificacion = findViewById(R.id.tv_calificacion)
        textoEntregado = findViewById(R.id.tv_entregado)
        iconoActividades = findViewById(R.id.iv_actividadVer)
        fechaCierre = findViewById(R.id.tv_fechaCierre)
        iconoPerfil = findViewById(R.id.iv_perfilVer)
        iconoRanking = findViewById(R.id.iv_rankingVer)
        botonSubirVideo = findViewById(R.id.bto_subirVideo)

        // Obtener los datos pasados desde la actividad anterior
        id = intent.getStringExtra("ID").orEmpty()
        id_actividad = intent.getStringExtra("ACTIVIDAD_ID").orEmpty()
        textoTitulo.text = intent.getStringExtra("TITULO")
        textoDescripcion.text = intent.getStringExtra("DESCRIPCION")
        fechaCierre.text = "Fecha cierre: " + intent.getStringExtra("FECHA")

        // Configurar el botón para subir video
        botonSubirVideo.setOnClickListener {
            seleccionarVideo()
        }
    }

    // Configura los iconos para navegar a otras actividades
    private fun configurarNavegacion() {
        // Configurar listeners para los iconos de navegación
        iconoActividades.setOnClickListener { navegarA(PaginaActividadAlumno::class.java) }
        iconoRanking.setOnClickListener { navegarA(PaginaRankingAlumno::class.java) }
        iconoPerfil.setOnClickListener { navegarA(PaginaPerfilAlumno::class.java) }
    }

    // Método para navegar a otra actividad pasando el ID del usuario
    private fun navegarA(destino: Class<*>) {
        val intent = Intent(this, destino)
        intent.putExtra("ID", id)
        startActivity(intent)
    }

    // Método para seleccionar un video de la galería
    private fun seleccionarVideo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
        }
        startActivityForResult(intent, CODIGO_SELECCION_VIDEO)
    }

    // Verifica si la actividad ya ha sido entregada y calificada
    private fun verificarEntregaYCalificacion() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Obtener la referencia a la actividad en Firestore

                // Verificar si ya existe una entrega

            } catch (e: Exception) {


            }
        }
    }

    // Maneja el resultado de la selección de video
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODIGO_SELECCION_VIDEO && resultCode == RESULT_OK && data != null) {
            data.data?.let { uri ->
                if (entregado) {

                } else {

                }
            }
        }
    }

    // Actualiza un video ya entregado en Firestore


    // Sube un nuevo video a Firebase

}
