package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import com.google.firebase.auth.FirebaseAuth
import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

// Constantes para el cifrado
private const val ALGORITMO = "AES"
private const val CLAVE = "claveSegura12345"

class PaginaRegistro : AppCompatActivity() {

    // Variables para los elementos de la interfaz
    private lateinit var etNick: EditText
    private lateinit var etContra: EditText
    private lateinit var etConfirmacionContra: EditText
    private lateinit var btnCrearCuenta: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_layout)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inicializar vistas
        inicializarVistas()

        // Configurar listener para el botón de crear cuenta
        btnCrearCuenta.setOnClickListener {
            registrarUsuario()
        }
    }

    // Método para inicializar las vistas
    private fun inicializarVistas() {
        etNick = findViewById(R.id.et_crearNick)
        etContra = findViewById(R.id.et_crearContrasena)
        etConfirmacionContra = findViewById(R.id.et_confirmarContra)
        btnCrearCuenta = findViewById(R.id.bto_crearCuenta)
    }

    // Método para registrar un nuevo usuario
    private fun registrarUsuario() {
        val nick = etNick.text.toString().trim()
        val contra = etContra.text.toString().trim()
        val contraConfirmada = etConfirmacionContra.text.toString().trim()

        // Validar los campos de entrada
        if (validarCampos(nick, contra, contraConfirmada)) {
            val contraCifrada = cifrar(contra)

            // Crear cuenta de usuario con email y contraseña en Firebase
            auth.createUserWithEmailAndPassword(nick, contraCifrada).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val id = task.result?.user?.uid // Obtiene el ID del usuario de Firebase
                    Toast.makeText(this, "Registro exitoso.", Toast.LENGTH_SHORT).show()

                    // Redirigir a la página de selección de instrumentos
                    val intent = Intent(this, PaginaInstrumentos::class.java)
                    intent.putExtra("ID", id)
                    intent.putExtra("NICK", nick)
                    intent.putExtra("CONTRA", contraCifrada)
                    startActivity(intent)
                } else {
                    // Si falla el registro, mostrar un mensaje al usuario.
                    Toast.makeText(this, "Error de registro: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Método para validar los campos de entrada
    private fun validarCampos(nick: String, contra: String, contraConfirmada: String): Boolean {
        if (nick.isEmpty() || contra.isEmpty() || contraConfirmada.isEmpty()) {
            Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (contra != contraConfirmada) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // Método para cifrar una cadena de texto
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
