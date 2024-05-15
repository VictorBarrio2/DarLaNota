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

    private lateinit var bto_siguiente : Button

    private lateinit var cb_piano : CheckBox
    private lateinit var cb_bateria : CheckBox
    private lateinit var cb_guitarra : CheckBox
    private lateinit var cb_canto : CheckBox


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instrumentos_layout)

        val id = intent.getStringExtra("ID")
        val nick = intent.getStringExtra("NICK")
        val contra = intent.getStringExtra("CONTRA")

        cb_piano = findViewById(R.id.cb_piano)
        cb_bateria = findViewById(R.id.cb_bateria)
        cb_guitarra = findViewById(R.id.cb_guitarra)
        cb_canto = findViewById(R.id.cb_canto)

        bto_siguiente = findViewById(R.id.bto_elegirInstrumento)

        bto_siguiente.setOnClickListener {
            if (!cb_piano.isChecked && !cb_bateria.isChecked && !cb_guitarra.isChecked && !cb_canto.isChecked) {
                Toast.makeText(this, "Debes seleccionar al menos un instrumento", Toast.LENGTH_SHORT).show()
            } else {
                val instrumentos = ArrayList<String>()

                if (cb_piano.isChecked) instrumentos.add("Piano")
                if (cb_bateria.isChecked) instrumentos.add("Batería")
                if (cb_guitarra.isChecked) instrumentos.add("Guitarra")
                if (cb_canto.isChecked) instrumentos.add("Canto")

// Assuming 'contra' and 'nick' are correctly defined as String types before this snippet
                val alumno = Alumno(contra.toString(), nick.toString(), "alumno", instrumentos, 0)

                alumno.subirAlumnoFirestore(id.toString())

                finishAffinity()
                val intent = Intent(this, PaginaActividadAlumno::class.java)
                intent.putExtra("ID", id)
                startActivity(intent)
            }
        }


    }

    private fun setup(email:String, provider:String){
        title = "Inicio"
    }
}