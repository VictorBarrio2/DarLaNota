package com.example.darlanota.modelos

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import java.util.Calendar

class PaginaAltaActividad : AppCompatActivity() {
    private lateinit var bto_subirActividad: Button
    private lateinit var bto_fecha: Button
    private lateinit var et_titulo: EditText
    private lateinit var et_des: EditText
    private lateinit var fechaSeleccionada: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alta_actividad_layout)

        bto_subirActividad = findViewById(R.id.bto_subirActividad)
        bto_fecha = findViewById(R.id.bto_fecha)
        et_titulo = findViewById(R.id.et_tituloAlta)
        et_des = findViewById(R.id.et_descripcionAlta)
        fechaSeleccionada = findViewById(R.id.tv_fecha)


        bto_subirActividad.setOnClickListener {
            if(et_titulo.text.toString().equals("") || et_des.text.toString().equals("") || fechaSeleccionada.toString().equals("--/--/----")){
                Toast.makeText(this, "Faltan campos por seleccionar", Toast.LENGTH_SHORT).show()
            }else{

            }

        }

        bto_fecha.setOnClickListener {
            // Mostrar el DatePickerDialog
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Cambio en el formato de la fecha: día/mes/año
                fechaSeleccionada.setText("$dayOfMonth/${monthOfYear + 1}/$year")
                // Mostrar la fecha seleccionada con un Toast
                Toast.makeText(this, "Fecha seleccionada: ${fechaSeleccionada.text.toString()}", Toast.LENGTH_LONG).show()
            }, year, month, day)
            datePickerDialog.show()
        }
    }
}