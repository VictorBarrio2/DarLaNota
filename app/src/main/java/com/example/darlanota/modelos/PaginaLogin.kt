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
import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

private const val ALGORITMO = "AES"
private const val CLAVE = "claveSegura12345"
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
        aplicarTemaGuardado()
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
            et_nick.setText(sharedPreferences.getString("nick", ""))
            et_contra.setText(sharedPreferences.getString("contraseña", ""))
            cb_inicioAutomatico.isChecked = true
            iniciarSesion(et_nick.text.toString(), et_contra.text.toString())
        }

        bto_inicioSesion.setOnClickListener {
            val email = et_nick.text.toString().trim()
            val contra = et_contra.text.toString().trim()
            iniciarSesion(email, contra)
        }

        bto_registro.setOnClickListener {
            startActivity(Intent(this, PaginaRegistro::class.java))
        }
    }

    private fun iniciarSesion(email: String?, contra: String?) {
        if (email.isNullOrEmpty() || contra.isNullOrEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, cifrar(contra))
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (cb_inicioAutomatico.isChecked) {
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("guardar_credenciales", true)
                        editor.putString("nick", email)
                        editor.putString("contraseña", contra)
                        editor.apply()
                    } else {
                        val editor = sharedPreferences.edit()
                        editor.clear()
                        editor.apply()
                    }
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

    private fun aplicarTemaGuardado() {
        val prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE)
        val esTemaOscuro = prefs.getBoolean("tema_oscuro", false)
        if (esTemaOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }


    private fun generarClave(): Key {
        return SecretKeySpec(CLAVE.toByteArray(), ALGORITMO)
    }

    private fun cifrar(dato: String): String {
        val clave = generarClave()
        val cifrador = Cipher.getInstance(ALGORITMO)
        cifrador.init(Cipher.ENCRYPT_MODE, clave)
        val valorCifrado = cifrador.doFinal(dato.toByteArray())
        return Base64.encodeToString(valorCifrado, Base64.DEFAULT)
    }
}
