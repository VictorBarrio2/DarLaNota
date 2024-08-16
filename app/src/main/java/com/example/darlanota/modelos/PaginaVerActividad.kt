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
import com.example.darlanota.clases.FireStore
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
    private lateinit var iv_logro: ImageView
    private lateinit var iconoRanking: ImageView
    private lateinit var nick: String
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
        iv_logro = findViewById(R.id.iv_logoVerActividades)
        botonSubirVideo = findViewById(R.id.bto_subirVideo)

        // Obtener los datos pasados desde la actividad anterior
        nick = intent.getStringExtra("NICK").orEmpty()
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
        iv_logro.setOnClickListener {
            startActivity(Intent(this, PaginaLogrosAlumno::class.java).apply {
                putExtra("NICK", nick)
            })
        }
    }

    // Método para navegar a otra actividad pasando el ID del usuario
    private fun navegarA(destino: Class<*>) {
        val intent = Intent(this, destino)
        intent.putExtra("NICK", nick)
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
        val firestoreService = FireStore()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Obtener la referencia a la actividad en Firestore
                val actividadRef = firestoreService.db.collection("actividades").document(id_actividad)
                val actividadSnapshot = actividadRef.get().await()
                val entregas = actividadSnapshot.get("entregas") as? ArrayList<HashMap<String, Any>> ?: ArrayList()
                val entregaMap = entregas.find { it["idAlumno"] == nick }

                // Verificar si ya existe una entrega
                if (entregaMap != null) {
                    val entrega = Entrega(
                        nickAlumno = entregaMap["idAlumno"] as String,
                        video = entregaMap["video"] as String,
                        calificacion = (entregaMap["calificacion"] as Long).toInt()
                    )
                    entregado = true
                    textoEntregado.text = "Entregado: Sí"
                    if (entrega.calificacion >= 0) {
                        textoCalificacion.text = "Calificación: ${entrega.calificacion}"
                    } else {
                        textoCalificacion.text = "Calificación: No calificado"
                    }

                } else {
                    textoEntregado.text = "Entregado: No"
                }
            } catch (e: Exception) {
                // Registrar la incidencia en Firestore en caso de error
                val firestore = FireStore()
                firestore.registrarIncidencia("Error al verificar la entrega: ${e.localizedMessage}")
                Log.e("FireStore", "Error al verificar la entrega: ${e.localizedMessage}", e)
            }
        }
    }

    // Maneja el resultado de la selección de video
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODIGO_SELECCION_VIDEO && resultCode == RESULT_OK && data != null) {
            data.data?.let { uri ->
                if (entregado) {
                    actualizarVideoEnFirestore(uri)
                } else {
                    subirVideoAFirebase(uri)
                }
            }
        }
    }

    // Actualiza un video ya entregado en Firestore
    private fun actualizarVideoEnFirestore(uriVideo: Uri) {
        val nuevoVideoPath = "videos/${uriVideo.lastPathSegment}"
        val firestoreService = FireStore()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Obtener referencia y datos de la actividad en Firestore
                val actividadRef = firestoreService.db.collection("actividades").document(id_actividad)
                val actividadSnapshot = actividadRef.get().await()
                val entregas = actividadSnapshot.get("entregas") as? ArrayList<HashMap<String, Any>> ?: ArrayList()
                val entrega = entregas.find { it["nickAlumno"] == nick }
                val videoAntiguoPath = entrega?.get("video") as? String

                // Eliminar video antiguo si existe
                if (!videoAntiguoPath.isNullOrEmpty()) {
                    try {
                        val storageRefAntiguo = FirebaseStorage.getInstance().reference.child(videoAntiguoPath)
                        storageRefAntiguo.delete().await()
                        Log.d("Vido", "$videoAntiguoPath")
                        Log.d("UpdateVideo", "Video antiguo eliminado exitosamente.")
                    } catch (deleteException: Exception) {
                        Log.e("UpdateVideo", "Error al eliminar video antiguo: ${deleteException.localizedMessage}")
                    }
                }

                // Subir el nuevo video a Firebase Storage
                try {
                    val storageRefNuevo = FirebaseStorage.getInstance().reference.child(nuevoVideoPath)
                    storageRefNuevo.putFile(uriVideo).await()
                    Log.d("UpdateVideo", "Nuevo video subido exitosamente.")
                } catch (uploadException: Exception) {
                    throw RuntimeException("Error al subir nuevo video: ${uploadException.localizedMessage}")
                }

                // Actualizar la entrega en Firestore
                try {
                    firestoreService.actualizarVideoEntrega(id_actividad, nick, nuevoVideoPath)
                    Log.d("UpdateVideo", "Referencia del video actualizada en Firestore.")
                } catch (updateException: Exception) {
                    throw RuntimeException("Error al actualizar referencia del video en Firestore: ${updateException.localizedMessage}")
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Entrega actualizada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("UpdateVideo", "Error actualizando video: ${e.localizedMessage}")
                    Toast.makeText(applicationContext, "Error al actualizar video", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Sube un nuevo video a Firebase
    private fun subirVideoAFirebase(uriVideo: Uri) {
        val videoPath = "videos/${uriVideo.lastPathSegment}"
        val storageRef = FirebaseStorage.getInstance().reference.child(videoPath)

        // Subir video a Firebase Storage y actualizar Firestore
        storageRef.putFile(uriVideo).addOnSuccessListener {
            val nuevaEntrega = Entrega(nickAlumno = nick, video = videoPath, calificacion = -1)
            nuevaEntrega.subirEntregaFirestore(id_actividad)
            entregado = true
            textoEntregado.text = "Entregado: Sí"
            Toast.makeText(this, "Video subido exitosamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al subir video", Toast.LENGTH_SHORT).show()
        }
    }
}
