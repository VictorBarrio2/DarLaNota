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
import com.example.darlanota.R
import com.example.darlanota.clases.Alumno
import com.example.darlanota.clases.FireStore
import com.example.darlanota.clases.Profesor
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PaginaLogin : AppCompatActivity() {

    private lateinit var et_nick: EditText
    private lateinit var et_contra: EditText
    private lateinit var bto_inicioSesion: Button
    private lateinit var bto_registro: Button
    private lateinit var cb_inicioAutomatico: CheckBox
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fireStore : FireStore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fireStore = FireStore()
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
        if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val id = task.result?.user?.uid.toString()

                        CoroutineScope(Dispatchers.Main).launch {
                            val db = FirebaseFirestore.getInstance()
                            val documento = db.collection("usuarios").document(id).get().await()
                            if (documento.exists()) {
                                when (documento.getString("tipo")) {
                                    "alumno" -> {
                                        val alumno = documento.toObject<Alumno>()
                                        if (alumno != null) {
                                            val intent = Intent(this@PaginaLogin, PaginaActividadAlumno::class.java)
                                            intent.putExtra("ID", id)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            mostrarAlerta("Error de inicio de sesión", "Usuario no válido.")
                                            val firestore = FireStore()
                                            firestore.registrarIncidencia("Error de inicio de sesión")
                                        }
                                    }
                                    "profesor" -> {
                                        val profesor = documento.toObject<Profesor>()
                                        if (profesor != null) {
                                            val intent = Intent(this@PaginaLogin, PaginaActividadProfe::class.java)
                                            intent.putExtra("ID", id)
                                            startActivity(intent)
                                        } else {
                                            mostrarAlerta("Error de inicio de sesión", "Usuario no válido.")
                                            val firestore = FireStore()
                                            firestore.registrarIncidencia("Error de inicio de sesión")
                                        }
                                    }
                                    else -> mostrarAlerta("Error de inicio de sesión", "Tipo de usuario desconocido.")
                                }
                            } else {
                                mostrarAlerta("Error de inicio de sesión", "Usuario no encontrado en la base de datos.")
                            }
                        }

                    } else {
                        val firestore = FireStore()
                        mostrarAlerta("Error de inicio de sesión", "No se pudo iniciar sesión: ${task.exception?.localizedMessage}")
                    }
                }
        } else {
            mostrarAlerta("Campos incompletos", "Por favor, complete todos los campos.")
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
