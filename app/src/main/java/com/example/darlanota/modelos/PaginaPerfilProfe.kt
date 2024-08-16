package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

// Constantes para el cifrado
private const val ALGORITMO = "AES"
private const val CLAVE = "claveSegura12345"

class PaginaPerfilProfe : AppCompatActivity() {

    // Declaración de variables para los elementos de la interfaz
    private lateinit var botonCambiarContrasena: Button
    private lateinit var imagenClasificacion: ImageView
    private lateinit var imagenActividades: ImageView
    private lateinit var imagenCerrarSesion: ImageView
    private lateinit var imagenTema: ImageView
    private lateinit var campoContrasena: EditText
    private lateinit var textoNombre: TextView
    private lateinit var nick: String
    private lateinit var fireStore: FireStore

    // Método que se ejecuta al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContentView(R.layout.perfil_profe_layout)

        // Inicialización de vistas y obtención del ID del intent
        inicializarVistas()
        nick = intent.getStringExtra("NICK") ?: "ID_default"

        // Carga de datos del usuario y configuración de los listeners
        cargarDatosUsuario()
        configurarListeners()
        aplicarImagenTema()
    }

    // Método para inicializar las vistas de la actividad
    private fun inicializarVistas() {
        imagenClasificacion = findViewById(R.id.iv_rankingPerfilProfe)
        imagenActividades = findViewById(R.id.iv_actividadesPerfilProfe)
        botonCambiarContrasena = findViewById(R.id.bto_cambiarContraProfe)
        campoContrasena = findViewById(R.id.et_contraPerfilProfe)
        imagenCerrarSesion = findViewById(R.id.iv_salirProfe)
        textoNombre = findViewById(R.id.tv_nickPerfilProfe)
        imagenTema = findViewById(R.id.iv_tema)
    }

    // Método para cargar los datos del usuario desde Firestore
    private fun cargarDatosUsuario() {
        CoroutineScope(Dispatchers.Main).launch {
            textoNombre.text = nick
        }
    }

    // Método para configurar los listeners de los botones e imágenes
    private fun configurarListeners() {
        // Listener para el botón de cambiar contraseña
        botonCambiarContrasena.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                // Obtención de la nueva contraseña desde el EditText
                val nuevaContrasena = campoContrasena.text.toString()
                val contraCifrada = cifrar(nuevaContrasena) // Cifrado de la contraseña

                // Verificación de que la contraseña no esté vacía
                if (contraCifrada.isBlank() || nuevaContrasena.length < 6) {
                    Toast.makeText(this@PaginaPerfilProfe, "La contraseña no puede estar vacía o ser menor de 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                // Cambio de la contraseña del usuario en Firestore
                val resultado = fireStore.cambiarContrasenaUsuario(contraCifrada, nick)
                Toast.makeText(this@PaginaPerfilProfe, resultado, Toast.LENGTH_LONG).show()


            }
        }

        // Listener para la imagen de actividades
        imagenActividades.setOnClickListener {
            startActivity(Intent(this, PaginaActividadProfe::class.java).apply {
                putExtra("NICK", nick)
            })
        }

        // Listener para la imagen de clasificación
        imagenClasificacion.setOnClickListener {
            startActivity(Intent(this, PaginaRankingProfe::class.java).apply {
                putExtra("NICK", nick)
            })
        }

        // Listener para la imagen de cerrar sesión
        imagenCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        // Listener para la imagen de cambiar tema
        imagenTema.setOnClickListener {
            cambiarTema()
        }
    }

    // Método para cambiar el tema de la aplicación
    private fun cambiarTema() {
        val prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE)
        val esTemaOscuro = prefs.getBoolean("tema_oscuro", false)
        if (esTemaOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            imagenTema.setImageResource(R.drawable.sol)
            prefs.edit().putBoolean("tema_oscuro", false).apply()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            imagenTema.setImageResource(R.drawable.luna)
            prefs.edit().putBoolean("tema_oscuro", true).apply()
        }
    }

    // Método para aplicar la imagen correspondiente al tema
    private fun aplicarImagenTema() {
        val prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE)
        val esTemaOscuro = prefs.getBoolean("tema_oscuro", false)
        if (esTemaOscuro) {
            imagenTema.setImageResource(R.drawable.luna)
        } else {
            imagenTema.setImageResource(R.drawable.sol)
        }
    }

    // Método para cerrar la sesión del usuario
    private fun cerrarSesion() {
        val sharedPreferences = getSharedPreferences("login_preferences", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        finishAffinity()
        startActivity(Intent(this, PaginaLogin::class.java))
    }

    // Método para cifrar un dato
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
