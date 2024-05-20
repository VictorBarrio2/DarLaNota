package com.example.darlanota.modelos

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.darlanota.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PaginaLogin : AppCompatActivity() {

    private lateinit var et_nick: EditText
    private lateinit var et_contra: EditText
    private lateinit var bto_inicioSesion: Button
    private lateinit var bto_registro: Button
    private lateinit var cb_inicioAutomatico: CheckBox
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        et_nick = findViewById(R.id.et_nick)
        et_contra = findViewById(R.id.et_contra)
        bto_inicioSesion = findViewById(R.id.bto_iniciarSesion)
        bto_registro = findViewById(R.id.bto_registro)
        cb_inicioAutomatico = findViewById(R.id.cb_guardarSesion)

        sharedPreferences = getSharedPreferences("login_preferences", Context.MODE_PRIVATE)

        // Configura el tema claro de manera obligatoria
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        cargarCredenciales()
    }

    private fun cargarCredenciales() {
        val guardarCredenciales = sharedPreferences.getBoolean("guardar_credenciales", false)
        et_nick.setText(sharedPreferences.getString("nick", ""))
        et_contra.setText(sharedPreferences.getString("contraseña", ""))
        cb_inicioAutomatico.isChecked = guardarCredenciales

        if (guardarCredenciales && cb_inicioAutomatico.isChecked) {
            iniciarSesion(et_nick.text.toString(), et_contra.text.toString())
        }

        bto_inicioSesion.setOnClickListener {
            iniciarSesion(et_nick.text.toString().trim(), et_contra.text.toString().trim())
        }

        bto_registro.setOnClickListener {
            startActivity(Intent(this, PaginaRegistro::class.java))
        }
    }

    private fun iniciarSesion(email: String?, password: String?) {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    determinarTipoUsuarioYRedirigir(task.result?.user?.uid ?: "")
                } else {
                    mostrarAlerta("Error de inicio de sesión", "No se pudo iniciar sesión: ${task.exception?.localizedMessage}")
                }
            }
    }

    private fun determinarTipoUsuarioYRedirigir(id: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val documento = db.collection("usuarios").document(id).get().await()
            if (!documento.exists()) {
                mostrarAlerta("Error de inicio de sesión", "Usuario no encontrado en la base de datos.")
                return@launch
            }

            val tipoUsuario = documento.getString("tipo")
            val intent = when (tipoUsuario) {
                "alumno" -> Intent(this@PaginaLogin, PaginaActividadAlumno::class.java)
                "profesor" -> Intent(this@PaginaLogin, PaginaActividadProfe::class.java)
                else -> {
                    mostrarAlerta("Error de inicio de sesión", "Tipo de usuario desconocido.")
                    return@launch
                }
            }
            intent.putExtra("ID", id)
            startActivity(intent)
            finish()
        }
    }

    private fun mostrarAlerta(titulo: String, mensaje: String) {
        AlertDialog.Builder(this).apply {
            setTitle(titulo)
            setMessage(mensaje)
            setPositiveButton("Aceptar", null)
            create()
            show()
        }
    }
}
