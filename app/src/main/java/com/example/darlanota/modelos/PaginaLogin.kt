package com.example.darlanota.modelos
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        cargarCredenciales()
    }

    private fun cargarCredenciales() {
        val guardarCredenciales = sharedPreferences.getBoolean("guardar_credenciales", false)
        if (guardarCredenciales) {
            val email = sharedPreferences.getString("nick", "")
            val password = sharedPreferences.getString("contraseña", "")
            et_nick.setText(email)
            et_contra.setText(password)
            cb_inicioAutomatico.isChecked = true

            // Intentar iniciar sesión automáticamente si hay credenciales guardadas y el checkbox está marcado
            if (cb_inicioAutomatico.isChecked) {
                iniciarSesion(email, password)
            }
        }

        bto_inicioSesion.setOnClickListener {
            val email = et_nick.text.toString().trim()
            val password = et_contra.text.toString().trim()
            if (cb_inicioAutomatico.isChecked) {
                val editor = sharedPreferences.edit()
                editor.putBoolean("guardar_credenciales", true)
                editor.putString("nick", email)
                editor.putString("contraseña", password)
                editor.apply()
            } else {
                sharedPreferences.edit().clear().apply()
            }
            iniciarSesion(email, password)
        }

        bto_registro.setOnClickListener {
            startActivity(Intent(this, PaginaRegistro::class.java))
        }
    }

    private fun iniciarSesion(email: String?, password: String?) {

        if(email == "profe"){
            startActivity(Intent(this, PaginaActividadProfe::class.java))
        }else{
            if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, PaginaActividadAlumno::class.java))
                        } else {
                            mostrarAlerta("Error de inicio de sesión", "No se pudo iniciar sesión: ${task.exception?.localizedMessage}")
                        }
                    }
            } else {
                mostrarAlerta("Campos incompletos", "Por favor, complete todos los campos.")
            }
        }
    }

    private fun mostrarAlerta(titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}
