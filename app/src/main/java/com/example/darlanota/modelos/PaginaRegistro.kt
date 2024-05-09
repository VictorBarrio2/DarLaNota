package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R

class PaginaRegistro : AppCompatActivity() {

    private lateinit var et_nick : EditText
    private lateinit var et_contra : EditText
    private lateinit var et_confirmacionContra : EditText

    private lateinit var bto_crearCuenta : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_layout)

        et_nick = findViewById(R.id.et_crearNick)
        et_contra = findViewById(R.id.et_crearContrasena)
        et_confirmacionContra = findViewById(R.id.et_confirmarContra)

        bto_crearCuenta = findViewById(R.id.bto_crearCuenta)

        bto_crearCuenta.setOnClickListener {
            val contra = et_contra.text.toString()
            val contraRepe = et_confirmacionContra.text.toString()
            if(et_nick.text.toString().equals("") || et_contra.text.toString().equals("") || et_confirmacionContra.text.toString().equals("")){
                Toast.makeText(this, "Campos vacios", Toast.LENGTH_SHORT).show()
            }else{
                if(contra.equals(contraRepe)){
                    val intent = Intent(this, PaginaInstrumentos::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}