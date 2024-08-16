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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.withContext
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

private const val ALGORITMO = "AES"
private const val CLAVE = "claveSegura12345"

class PaginaLogin : AppCompatActivity() {

    private lateinit var etNick: EditText
    private lateinit var etContra: EditText
    private lateinit var btoInicioSesion: Button
    private lateinit var btoRegistro: Button
    private lateinit var cbInicioAutomatico: CheckBox
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("login_preferences", Context.MODE_PRIVATE)

        aplicarTemaGuardado()

        val guardarCredenciales = sharedPreferences.getBoolean("guardar_credenciales", false)
        if (guardarCredenciales) {
            val nick = sharedPreferences.getString("nick", "")
            val contra = sharedPreferences.getString("contraseña", "")
            if (!nick.isNullOrEmpty() && !contra.isNullOrEmpty()) {
                iniciarSesion(nick, contra)
            }
        }
        setContentView(R.layout.login_layout)
        FirebaseApp.initializeApp(this)

        db = FirebaseFirestore.getInstance()

        inicializarVistas()
        cargarCredenciales()
    }

    private fun inicializarVistas() {
        etNick = findViewById(R.id.et_nick)
        etContra = findViewById(R.id.et_contra)
        btoInicioSesion = findViewById(R.id.bto_iniciarSesion)
        btoRegistro = findViewById(R.id.bto_registro)
        cbInicioAutomatico = findViewById(R.id.cb_guardarSesion)
    }

    private fun cargarCredenciales() {
        btoInicioSesion.setOnClickListener {
            val nick = etNick.text.toString().trim()
            val contra = etContra.text.toString().trim()
            iniciarSesion(nick, contra)
        }

        btoRegistro.setOnClickListener {
            startActivity(Intent(this, PaginaRegistro::class.java))
        }
    }

    private fun iniciarSesion(nick: String, contra: String) {
        if (nick.isEmpty() || contra.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val contraCifrada = cifrar(contra)
        Log.d("Debug", "Contraseña ingresada: $contra")
        Log.d("Debug", "Contraseña cifrada: $contraCifrada")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val query = db.collection("usuarios")
                    .whereEqualTo("nombre", nick)
                    .whereEqualTo("contrasena", contraCifrada)
                    .get()
                    .await()

                Log.d("Debug", "Número de documentos encontrados: ${query.size()}")

                if (query.isEmpty) {
                    withContext(Dispatchers.Main) {
                        mostrarAlerta("Error de inicio de sesión", "Nombre de usuario o contraseña incorrectos.")
                    }
                } else {
                    val usuario = query.documents.first()
                    val id = usuario.id
                    withContext(Dispatchers.Main) {
                        guardarCredenciales(nick, contraCifrada)
                    }
                    determinarTipoUsuarioYRedirigir(id, nick)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarAlerta("Error de inicio de sesión", "No se pudo iniciar sesión: ${e.localizedMessage}")
                }
            }
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


    private fun guardarCredenciales(nick: String, contra: String) {
        if (cbInicioAutomatico.isChecked) {
            sharedPreferences.edit().apply {
                putBoolean("guardar_credenciales", true)
                putString("nick", nick)
                putString("contrasena", contra)
                apply()
            }
        } else {
            sharedPreferences.edit().clear().apply()
        }
    }

    private fun determinarTipoUsuarioYRedirigir(id: String, nombre: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
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
                intent.putExtra("NICK", nombre)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                mostrarAlerta("Error", "No se pudo determinar el tipo de usuario: ${e.localizedMessage}")
            }
        }
    }

    private fun aplicarTemaGuardado() {
        val prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE)
        val esTemaOscuro = prefs.getBoolean("tema_oscuro", false)
        AppCompatDelegate.setDefaultNightMode(if (esTemaOscuro) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun cifrar(dato: String): String {
        val clave = generarClave()
        val cifrador = Cipher.getInstance(ALGORITMO)
        cifrador.init(Cipher.ENCRYPT_MODE, clave)
        val valorCifrado = cifrador.doFinal(dato.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(valorCifrado, Base64.NO_WRAP)
    }


    private fun generarClave(): Key {
        return SecretKeySpec(CLAVE.toByteArray(Charsets.UTF_8), ALGORITMO)
    }

}
