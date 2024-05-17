package com.example.darlanota.modelos

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.darlanota.R
import com.google.firebase.storage.FirebaseStorage

// Define la clase PaginaVerActividad que hereda de AppCompatActivity
class PaginaVerActividad : AppCompatActivity() {

    // Variables para las vistas en la interfaz de usuario
    private lateinit var botonSubirVideo: Button
    private lateinit var textoDescripcion: TextView
    private lateinit var textoTitulo: TextView
    private lateinit var textoCalificacion: TextView
    private lateinit var textoEntregado: TextView
    private lateinit var iconoPerfil: ImageView
    private lateinit var iconoActividades: ImageView
    private lateinit var iconoRanking: ImageView
    private lateinit var id: String
    private lateinit var video: String

    companion object {
        // Códigos de solicitud para la actividad de selección de video y permisos
        private const val CODIGO_SELECCION_VIDEO = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ver_actividades_layout)

        inicializarVistas()
        configurarNavegacion()
    }

    // Inicializa las vistas y configura los listeners de eventos
    private fun inicializarVistas() {
        textoTitulo = findViewById(R.id.tv_tituloActVer)
        textoDescripcion = findViewById(R.id.tv_descripcionVer)
        textoCalificacion = findViewById(R.id.tv_calificacion)
        textoEntregado = findViewById(R.id.tv_entregado)
        iconoActividades = findViewById(R.id.iv_actividadVer)
        iconoPerfil = findViewById(R.id.iv_perfilVer)
        iconoRanking = findViewById(R.id.iv_rankingVer)
        botonSubirVideo = findViewById(R.id.bto_subirVideo)

        id = intent.getStringExtra("ID").orEmpty()
        textoTitulo.text = intent.getStringExtra("TITULO")
        textoDescripcion.text = intent.getStringExtra("DESCRIPCION")

        botonSubirVideo.setOnClickListener {
            seleccionarVideo()
        }
    }

    // Configura la navegación entre actividades
    private fun configurarNavegacion() {
        iconoActividades.setOnClickListener { navegarA(PaginaActividadAlumno::class.java) }
        iconoRanking.setOnClickListener { navegarA(PaginaRankingAlumno::class.java) }
        iconoPerfil.setOnClickListener { navegarA(PaginaPerfilAlumno::class.java) }
    }

    // Navega a la actividad especificada, pasando el ID actual
    private fun navegarA(destino: Class<*>) {
        val intent = Intent(this, destino)
        intent.putExtra("ID", id)
        startActivity(intent)
    }

    // Inicia un Intent para seleccionar un video desde la galería usando el Storage Access Framework
    private fun seleccionarVideo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
        }
        startActivityForResult(intent, CODIGO_SELECCION_VIDEO)
    }

    // Maneja el resultado de la actividad de selección de video
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODIGO_SELECCION_VIDEO && resultCode == RESULT_OK && data != null) {
            val videoUri: Uri? = data.data

            videoUri?.let {
                // Sube el video seleccionado a Firebase Storage
                subirVideoAFirebase(it)
            }
        }
    }

    // Sube el video a Firebase Storage
    private fun subirVideoAFirebase(uriVideo: Uri) {
        video = "videos/${uriVideo.lastPathSegment}"
        val storageRef = FirebaseStorage.getInstance().reference.child(video)
        val uploadTask = storageRef.putFile(uriVideo)
        uploadTask.addOnSuccessListener {
            Toast.makeText(this, "Video subido exitosamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al subir video", Toast.LENGTH_SHORT).show()
        }
    }
}
