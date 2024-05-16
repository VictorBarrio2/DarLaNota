package com.example.darlanota.modelos
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad
import com.google.firebase.firestore.FirebaseFirestore

class PaginaActividadAlumno : AppCompatActivity() {

    private lateinit var iv_ranking: ImageView
    private lateinit var iv_actividades: ImageView
    private lateinit var iv_perfil: ImageView
    private lateinit var reciclador: RecyclerView
    private lateinit var adaptadorAlumno: AdaptadorAlumno

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividades_alumno_layout)

        val id = intent.getStringExtra("ID")

        iv_ranking = findViewById(R.id.iv_rankingAcAl)
        iv_actividades = findViewById(R.id.iv_actividadesAcAl)
        iv_perfil = findViewById(R.id.iv_perfilAcAl)
        reciclador = findViewById(R.id.rv_reciclador) // Inicializa reciclador


        iv_ranking.setOnClickListener {
            val intent = Intent(this, PaginaRankingAlumno::class.java)
            startActivity(intent)
        }

        iv_perfil.setOnClickListener {
            val intent = Intent(this, PaginaPerfilAlumno::class.java)
            startActivity(intent)
        }

        cargarActividades()
    }

    private fun cargarActividades() {
        val db = FirebaseFirestore.getInstance()
        val actividadesList = mutableListOf<Actividad>()

        db.collection("actividades")
            .get()
            .addOnSuccessListener { documentos ->
                for (documento in documentos) {
                    val actividad = documento.toObject(Actividad::class.java)
                    actividad.id = documento.id
                    actividadesList.add(actividad)
                }
                adaptadorAlumno = AdaptadorAlumno(actividadesList)
                reciclador.layoutManager = LinearLayoutManager(this)
                reciclador.adapter = adaptadorAlumno
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar actividades: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

}
