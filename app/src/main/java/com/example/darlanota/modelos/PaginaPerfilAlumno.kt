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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.darlanota.R
import com.example.darlanota.clases.SocketViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

private const val ALGORITMO = "AES" // Algoritmo de cifrado
private const val CLAVE = "claveSegura12345" // Clave de cifrado

class PaginaPerfilAlumno : AppCompatActivity() {

    // Declaración de variables para los elementos de la interfaz
    private lateinit var btnInstrumento: Button
    private lateinit var btnContra: Button
    private lateinit var ivRanking: ImageView
    private lateinit var ivActividades: ImageView
    private lateinit var ivCerrarSesion: ImageView
    private lateinit var imagenTema: ImageView
    private lateinit var etContra: EditText
    private lateinit var tvNombre: TextView
    private lateinit var nickAlumno: String
    private lateinit var socketViewModel: SocketViewModel

    // Método que se ejecuta al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_alumno_layout)

        // Obtención del ID del intent
        nickAlumno = intent.getStringExtra("NICK") ?: "DefaultID"

        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)

        lifecycleScope.launch {
            try {
                socketViewModel.conectarServidor("148.3.110.121", 42069)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PaginaPerfilAlumno, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
            }
        }

        // Inicialización de las vistas de la actividad
        inicializarVistas()

        // Aplicación de la imagen del tema
        aplicarImagenTema()

        // Configuración de los manejadores de eventos
        configurarManejadoresEventos()
    }

    // Método para inicializar las vistas de la actividad
    private fun inicializarVistas() {
        ivRanking = findViewById(R.id.iv_rankingPerfil)
        ivActividades = findViewById(R.id.iv_actividadesPerfil)
        btnContra = findViewById(R.id.bto_cambiarContra)
        etContra = findViewById(R.id.et_contraPerfilAlumno)
        ivCerrarSesion = findViewById(R.id.iv_salir)
        tvNombre = findViewById(R.id.tv_nickPerfil)
        imagenTema = findViewById(R.id.iv_temaAlumno)

        tvNombre.text = nickAlumno
    }

    // Método para configurar los manejadores de eventos de los botones e imágenes
    private fun configurarManejadoresEventos() {

        // Manejador de evento para el botón de cambiar contraseña
        btnContra.setOnClickListener {
            cambiarContrasena()
        }

        // Manejador de evento para la imagen de actividades
        ivActividades.setOnClickListener {
            startActivity(Intent(this, PaginaActividadAlumno::class.java).also { it.putExtra("NICK", nickAlumno) })
        }

        // Manejador de evento para la imagen de ranking
        ivRanking.setOnClickListener {
            startActivity(Intent(this, PaginaRankingAlumno::class.java).also { it.putExtra("NICK", nickAlumno) })
        }

        // Manejador de evento para la imagen de cerrar sesión
        ivCerrarSesion.setOnClickListener {
            limpiarPreferenciasLogin()
            cerrarSesion()
        }

        // Manejador de evento para la imagen de tema
        imagenTema.setOnClickListener {
            cambiarTema()
        }
    }

    // Método para cambiar la contraseña del usuario
    private fun cambiarContrasena() {
        CoroutineScope(Dispatchers.Main).launch {
            // Obtención de la nueva contraseña desde el EditText
            val nuevaContrasena = etContra.text.toString()


            // Verificación de que la contraseña no esté vacía
            if (nuevaContrasena.isBlank() || nuevaContrasena.length < 5) {
                Toast.makeText(this@PaginaPerfilAlumno, "La contraseña no puede estar vacía o ser menor de 6 caracteres", Toast.LENGTH_SHORT).show()
                return@launch
            }
            lifecycleScope.launch {
                if(socketViewModel.cambiarContrasena(nickAlumno, nuevaContrasena)){
                    Toast.makeText(
                        this@PaginaPerfilAlumno,
                        "Contraseña cambiada",
                        Toast.LENGTH_LONG
                    ).show()
                }else{
                    Toast.makeText(
                        this@PaginaPerfilAlumno,
                        "Error al cambiar la contraseña",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Método para cambiar el tema de la aplicación
    private fun cambiarTema() {
        val prefs = getSharedPreferences("preferencias_tema", MODE_PRIVATE)
        val esTemaOscuro = prefs.getBoolean("tema_oscuro", false)
        if (esTemaOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            imagenTema.setImageResource(R.drawable.sol) // Imagen de sol cuando el tema es claro
            prefs.edit().putBoolean("tema_oscuro", false).apply()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            imagenTema.setImageResource(R.drawable.luna) // Imagen de luna cuando el tema es oscuro
            prefs.edit().putBoolean("tema_oscuro", true).apply()
        }
    }

    // Método para limpiar las preferencias de inicio de sesión
    private fun limpiarPreferenciasLogin() {
        val sharedPreferences = getSharedPreferences("login_preferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("nick")
            remove("contraseña")
            putBoolean("guardar_credenciales", false)
            apply()
        }
    }

    // Método para aplicar la imagen del tema basado en las preferencias
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
        finishAffinity() // Cierra todas las actividades
        startActivity(Intent(this, PaginaLogin::class.java)) // Redirige a la página de inicio de sesión
    }

    // Método para cifrar datos
    private fun cifrar(dato: String): String {
        val clave = generarClave()
        val cifrador = Cipher.getInstance(ALGORITMO)
        cifrador.init(Cipher.ENCRYPT_MODE, clave)
        val valorCifrado = cifrador.doFinal(dato.toByteArray())
        return Base64.encodeToString(valorCifrado, Base64.DEFAULT)
    }

    // Método para generar la clave de cifrado
    private fun generarClave(): Key {
        return SecretKeySpec(CLAVE.toByteArray(), ALGORITMO)
    }
}
