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

    // Declaración de variables UI y Firebase
    private lateinit var etNick: EditText
    private lateinit var etContra: EditText
    private lateinit var btoInicioSesion: Button
    private lateinit var btoRegistro: Button
    private lateinit var cbInicioAutomatico: CheckBox
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    // Método principal que se ejecuta al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar sharedPreferences
        sharedPreferences = getSharedPreferences("login_preferences", Context.MODE_PRIVATE)

        aplicarTemaGuardado()

        val guardarCredenciales = sharedPreferences.getBoolean("guardar_credenciales", false)
        if (guardarCredenciales) {
            val nick = sharedPreferences.getString("nick", "")
            val contra = sharedPreferences.getString("contraseña", "")
            val check = true
            iniciarSesion(nick, contra)
        }
        setContentView(R.layout.login_layout)
        FirebaseApp.initializeApp(this)

        // Inicialización de Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar las vistas
        inicializarVistas()

        // Cargar credenciales si están guardadas
        cargarCredenciales()
    }

    // Método para inicializar las vistas
    private fun inicializarVistas() {
        etNick = findViewById(R.id.et_nick)
        etContra = findViewById(R.id.et_contra)
        btoInicioSesion = findViewById(R.id.bto_iniciarSesion)
        btoRegistro = findViewById(R.id.bto_registro)
        cbInicioAutomatico = findViewById(R.id.cb_guardarSesion)
    }

    // Método para cargar credenciales guardadas
    private fun cargarCredenciales() {
        // Configurar el listener para el botón de inicio de sesión
        btoInicioSesion.setOnClickListener {
            val email = etNick.text.toString().trim()
            val contra = etContra.text.toString().trim()
            iniciarSesion(email, contra)
        }

        // Configurar el listener para el botón de registro
        btoRegistro.setOnClickListener {
            startActivity(Intent(this, PaginaRegistro::class.java))
        }
    }

    // Método para iniciar sesión
    private fun iniciarSesion(email: String?, contra: String?) {

        if (email.isNullOrEmpty() || contra.isNullOrEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }
        val contraCifrada = cifrar(contra)
        auth.signInWithEmailAndPassword(email, contraCifrada)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    guardarCredenciales(email, contra)
                    determinarTipoUsuarioYRedirigir(task.result?.user?.uid ?: "")
                } else {
                    mostrarAlerta("Error de inicio de sesión", "No se pudo iniciar sesión: ${task.exception?.localizedMessage}")
                }
            }
    }

    // Método para guardar credenciales
    private fun guardarCredenciales(email: String, contra: String) {
        if (cbInicioAutomatico.isChecked) {
            sharedPreferences.edit().apply {
                putBoolean("guardar_credenciales", true)
                putString("nick", email)
                putString("contraseña", contra) // Consider encrypting the password before storing it
                apply()
            }
        } else {
            sharedPreferences.edit().clear().apply()
        }
    }

    // Método para determinar el tipo de usuario y redirigir a la página correspondiente
    private fun determinarTipoUsuarioYRedirigir(id: String) {
        CoroutineScope(Dispatchers.Main).launch {
            mostrarAlerta("ID", id)
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

    // Método para mostrar una alerta
    private fun mostrarAlerta(titulo: String, mensaje: String) {
        AlertDialog.Builder(this).apply {
            setTitle(titulo)
            setMessage(mensaje)
            setPositiveButton("Aceptar", null)
            create()
            show()
        }
    }

    // Método para aplicar el tema guardado en las preferencias
    private fun aplicarTemaGuardado() {
        val prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE)
        val esTemaOscuro = prefs.getBoolean("tema_oscuro", false)
        if (esTemaOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun cifrar(dato: String): String {
        val clave = generarClave()
        val cifrador = Cipher.getInstance(ALGORITMO)
        cifrador.init(Cipher.ENCRYPT_MODE, clave)
        val valorCifrado = cifrador.doFinal(dato.toByteArray())
        return Base64.encodeToString(valorCifrado, Base64.DEFAULT)
    }

    // Método para generar una clave de cifrado
    private fun generarClave(): Key {
        return SecretKeySpec(CLAVE.toByteArray(), ALGORITMO)
    }
}
