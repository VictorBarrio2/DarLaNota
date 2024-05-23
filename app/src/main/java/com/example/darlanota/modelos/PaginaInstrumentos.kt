package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.example.darlanota.clases.Alumno

class PaginaInstrumentos : AppCompatActivity() {

    // Declaración de variables UI
    private lateinit var btoSiguiente: Button
    private lateinit var cbPiano: CheckBox
    private lateinit var cbBateria: CheckBox
    private lateinit var cbGuitarra: CheckBox
    private lateinit var cbCanto: CheckBox

    // Método principal que se ejecuta al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instrumentos_layout)

        // Obtener los datos pasados por intent
        val id = intent.getStringExtra("ID")
        val nick = intent.getStringExtra("NICK")
        val contra = intent.getStringExtra("CONTRA")

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
                val alumno = Alumno(contra.toString(), nick.toString(), "alumno", instrumentos, 0)

                alumno.subirAlumnoFirestore(id.toString())

                // Iniciar la actividad PaginaActividadAlumno
                iniciarActividadAlumno(id)
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

    // Método para iniciar la actividad PaginaActividadAlumno
    private fun iniciarActividadAlumno(id: String?) {
        finishAffinity()
        val intent = Intent(this, PaginaActividadAlumno::class.java)
        intent.putExtra("ID", id)
        startActivity(intent)
        finish()
    }

    // Método no utilizado actualmente
    private fun setup(email: String, provider: String) {
        title = "Inicio"
    }
}
