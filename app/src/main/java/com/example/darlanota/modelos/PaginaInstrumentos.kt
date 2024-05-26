package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.example.darlanota.clases.Alumno
import com.google.firebase.firestore.FirebaseFirestore

class PaginaInstrumentos : AppCompatActivity() {

    // Declaración de variables UI
    private lateinit var btoSiguiente: Button
    private lateinit var cbPiano: CheckBox
    private lateinit var cbBateria: CheckBox
    private lateinit var cbGuitarra: CheckBox
    private lateinit var cbCanto: CheckBox

    private lateinit var db: FirebaseFirestore

    private lateinit var id: String
    private lateinit var nick: String
    private lateinit var contra: String

    // Método principal que se ejecuta al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instrumentos_layout)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener los datos pasados por intent
        id = intent.getStringExtra("ID").toString()
        nick = intent.getStringExtra("NICK").toString()
        contra = intent.getStringExtra("CONTRA").toString()

        // Inicializar las vistas
        inicializarVistas()

        // Configurar el listener para el botón
        configurarListenerBoton(id, nick, contra)
    }

    // Método para inicializar las vistas
    private fun inicializarVistas() {
        cbPiano = findViewById(R.id.cb_piano)
        cbBateria = findViewById(R.id.cb_bateria)
        cbGuitarra = findViewById(R.id.cb_guitarra)
        cbCanto = findViewById(R.id.cb_canto)
        btoSiguiente = findViewById(R.id.bto_elegirInstrumento)
    }

    // Método para configurar el listener del botón
    private fun configurarListenerBoton(id: String?, nick: String?, contra: String?) {
        btoSiguiente.setOnClickListener {
            if (!cbPiano.isChecked && !cbBateria.isChecked && !cbGuitarra.isChecked && !cbCanto.isChecked) {
                Toast.makeText(this, "Debes seleccionar al menos un instrumento", Toast.LENGTH_SHORT).show()
            } else {
                val instrumentos = obtenerInstrumentosSeleccionados()

                if (id != null && id != "null") {
                    actualizarInstrumentosAlumno(id, instrumentos)
                } else {
                    crearNuevoAlumno(instrumentos)
                }
            }
        }
    }

    // Método para obtener los instrumentos seleccionados
    private fun obtenerInstrumentosSeleccionados(): ArrayList<String> {
        val instrumentos = ArrayList<String>()

        if (cbPiano.isChecked) instrumentos.add("Piano")
        if (cbBateria.isChecked) instrumentos.add("Batería")
        if (cbGuitarra.isChecked) instrumentos.add("Guitarra")
        if (cbCanto.isChecked) instrumentos.add("Canto")

        return instrumentos
    }

    // Método para actualizar los instrumentos del alumno
    private fun actualizarInstrumentosAlumno(id: String, instrumentos: ArrayList<String>) {
        val alumnoRef = db.collection("usuarios").document(id)
        alumnoRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Actualizar los instrumentos del alumno existente
                alumnoRef.update("instrumentos", instrumentos)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Instrumentos actualizados correctamente", Toast.LENGTH_SHORT).show()
                        iniciarActividadAlumno(id)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al actualizar los instrumentos: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Crear un nuevo registro de alumno
                crearNuevoAlumno(instrumentos)
            }
        }
    }

    // Método para crear un nuevo alumno
    private fun crearNuevoAlumno(instrumentos: ArrayList<String>) {
        val alumno = Alumno(contra, nick, "alumno", instrumentos, 0)
        db.collection("usuarios")
            .add(alumno)
            .addOnSuccessListener { documentReference ->
                val newId = documentReference.id
                Toast.makeText(this, "Alumno creado con ID: $newId", Toast.LENGTH_SHORT).show()
                iniciarActividadAlumno(newId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear alumno: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // Método para iniciar la actividad PaginaActividadAlumno
    private fun iniciarActividadAlumno(id: String) {
        finishAffinity()
        val intent = Intent(this, PaginaActividadAlumno::class.java)
        intent.putExtra("ID", id)
        startActivity(intent)
        finish()
    }
}
