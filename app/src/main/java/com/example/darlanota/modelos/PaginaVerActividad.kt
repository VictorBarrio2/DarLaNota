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
        private const val CODIGO_SELECCION_VIDEO = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ver_actividades_layout)

        inicializarVistas()
        configurarNavegacion()
        verificarEntregaYCalificacion()
    }

    private fun inicializarVistas() {
        textoTitulo = findViewById(R.id.tv_tituloActVer)
        textoDescripcion = findViewById(R.id.tv_descripcionVer)
        textoCalificacion = findViewById(R.id.tv_calificacion)
        textoEntregado = findViewById(R.id.tv_entregado)
        iconoActividades = findViewById(R.id.iv_actividadVer)
        fechaCierre = findViewById(R.id.tv_fechaCierre)
        iconoPerfil = findViewById(R.id.iv_perfilVer)
        iconoRanking = findViewById(R.id.iv_rankingVer)
        botonSubirVideo = findViewById(R.id.bto_subirVideo)

        id = intent.getStringExtra("ID").orEmpty()
        id_actividad = intent.getStringExtra("ACTIVIDAD_ID").orEmpty()
        textoTitulo.text = intent.getStringExtra("TITULO")
        textoDescripcion.text = intent.getStringExtra("DESCRIPCION")
        fechaCierre.text = "Fecha cierre: " + intent.getStringExtra("FECHA")


        botonSubirVideo.setOnClickListener {
            seleccionarVideo()
        }
    }

    private fun configurarNavegacion() {
        iconoActividades.setOnClickListener { navegarA(PaginaActividadAlumno::class.java) }
        iconoRanking.setOnClickListener { navegarA(PaginaRankingAlumno::class.java) }
        iconoPerfil.setOnClickListener { navegarA(PaginaPerfilAlumno::class.java) }
    }

    private fun navegarA(destino: Class<*>) {
        val intent = Intent(this, destino)
        intent.putExtra("ID", id)
        startActivity(intent)
    }

    private fun seleccionarVideo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
        }
        startActivityForResult(intent, CODIGO_SELECCION_VIDEO)
    }

    private fun verificarEntregaYCalificacion() {
        val firestoreService = FireStore()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val actividadRef = firestoreService.db.collection("actividades").document(id_actividad)
                val actividadSnapshot = actividadRef.get().await()
                val entregas = actividadSnapshot.get("entregas") as? ArrayList<HashMap<String, Any>> ?: ArrayList()
                val entregaMap = entregas.find { it["idAlumno"] == id }

                if (entregaMap != null) {
                    val entrega = Entrega(
                        idAlumno = entregaMap["idAlumno"] as String,
                        video = entregaMap["video"] as String,
                        calificacion = (entregaMap["calificacion"] as Long).toInt()
                    )
                    entregado = true
                    textoEntregado.text = "Entregado: Sí"
                    if(entrega.calificacion >= 0){
                        textoCalificacion.text = "Calificación: ${entrega.calificacion}"
                    }else{
                        textoCalificacion.text = "Calificación: No calificado"
                    }

                } else {
                    textoEntregado.text = "Entregado: No"
                }
            } catch (e: Exception) {
                val firestore = FireStore()
                firestore.registrarIncidencia("Error al verificar la entrega: ${e.localizedMessage}")
                Log.e("FireStore", "Error al verificar la entrega: ${e.localizedMessage}", e)
            }
        }
    }

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

    private fun actualizarVideoEnFirestore(uriVideo: Uri) {
        val nuevoVideoPath = "videos/${uriVideo.lastPathSegment}"
        val firestoreService = FireStore()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val actividadRef = firestoreService.db.collection("actividades").document(id_actividad)
                val actividadSnapshot = actividadRef.get().await()
                val entregas = actividadSnapshot.get("entregas") as? ArrayList<HashMap<String, Any>> ?: ArrayList()
                val entrega = entregas.find { it["idAlumno"] == id }
                val videoAntiguoPath = entrega?.get("video") as? String

                if (!videoAntiguoPath.isNullOrEmpty()) {
                    val storageRef = FirebaseStorage.getInstance().getReference(videoAntiguoPath)
                    storageRef.delete().await()
                }

                val storageRefNuevo = FirebaseStorage.getInstance().reference.child(nuevoVideoPath)
                storageRefNuevo.putFile(uriVideo).await()

                firestoreService.actualizarVideoEntrega(id_actividad, id, nuevoVideoPath)

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

    private fun subirVideoAFirebase(uriVideo: Uri) {
        val videoPath = "videos/${uriVideo.lastPathSegment}"
        val storageRef = FirebaseStorage.getInstance().reference.child(videoPath)

        storageRef.putFile(uriVideo).addOnSuccessListener {
            val nuevaEntrega = Entrega(idAlumno = id, video = videoPath, calificacion = 0)
            nuevaEntrega.subirEntregaFirestore(id_actividad)
            entregado = true
            textoEntregado.text = "Entregado: Sí"
            Toast.makeText(this, "Video subido exitosamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al subir video", Toast.LENGTH_SHORT).show()
        }
    }
}
