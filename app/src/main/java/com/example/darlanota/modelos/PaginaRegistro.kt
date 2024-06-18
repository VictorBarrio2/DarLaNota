package com.example.darlanota.modelos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.darlanota.R
import android.util.Base64
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.darlanota.clases.SocketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private lateinit var socketViewModel: SocketViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_layout)

        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)

        inicializarVistas()

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

        if (validarCampos(nick, contra, contraConfirmada)){
            lifecycleScope.launch(Dispatchers.Main) {
                socketViewModel.conectarServidor("148.3.110.121", 42069)
                if(socketViewModel.enviarDatosRegistro(nick, contra) == 1){
                    Toast.makeText(this@PaginaRegistro, "Usuario registrado con exito", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@PaginaRegistro, PaginaActividadAlumno::class.java)
                    intent.putExtra("NICK", nick)
                    startActivity(intent)
                }else{
                    Toast.makeText(this@PaginaRegistro, "Error en el registro o usuario ya registrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
}
