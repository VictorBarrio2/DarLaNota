package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.google.firebase.auth.FirebaseAuth

class PaginaRegistro : AppCompatActivity() {

    private lateinit var et_nick : EditText
    private lateinit var et_contra : EditText
    private lateinit var et_confirmacionContra : EditText
    private lateinit var bto_crearCuenta : Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_layout)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        et_nick = findViewById(R.id.et_crearNick)
        et_contra = findViewById(R.id.et_crearContrasena)
        et_confirmacionContra = findViewById(R.id.et_confirmarContra)
        bto_crearCuenta = findViewById(R.id.bto_crearCuenta)

        bto_crearCuenta.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val nick = et_nick.text.toString().trim()
        val contra = et_contra.text.toString().trim()
        val contraFuerte = et_confirmacionContra.text.toString().trim()

        if (nick.isEmpty() || contra.isEmpty() || contraFuerte.isEmpty()) {
            Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            return
        }

        if (contra != contraFuerte) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        // Creating user account with email and password on Firebase
        auth.createUserWithEmailAndPassword(nick, contra).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Toast.makeText(this, "Registro exitoso.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, PaginaInstrumentos::class.java)
                startActivity(intent)

            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "Error de registro: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
