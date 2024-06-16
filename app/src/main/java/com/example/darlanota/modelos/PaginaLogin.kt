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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.darlanota.R
import com.example.darlanota.clases.SocketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaginaLogin : AppCompatActivity() {

    private lateinit var etNick: EditText
    private lateinit var etContra: EditText
    private lateinit var btoInicioSesion: Button
    private lateinit var btoRegistro: Button
    private lateinit var cbInicioAutomatico: CheckBox
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var socketViewModel: SocketViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aplicarTemaGuardado()
        setContentView(R.layout.login_layout)

        socketViewModel = ViewModelProvider(this).get(SocketViewModel::class.java)

        // Conectar con el servidor (ajusta la IP y el puerto según tu configuración)
        lifecycleScope.launch(Dispatchers.Main) {
            val conexionExitosa = socketViewModel.conectarServidor("192.168.56.1", 42069)

            if (conexionExitosa) {
                // El servidor se ha conectado correctamente
                cargarCredenciales()
            } else {
                // Mostrar mensaje de error
                mostrarAlerta("Error de conexión", "No se pudo conectar al servidor.")
            }
        }

        // Inicializar las vistas
        inicializarVistas()
        cargarCredenciales()
    }

    private fun inicializarVistas() {
        etNick = findViewById(R.id.et_nick)
        etContra = findViewById(R.id.et_contra)
        btoInicioSesion = findViewById(R.id.bto_iniciarSesion)
        btoRegistro = findViewById(R.id.bto_registro)
        cbInicioAutomatico = findViewById(R.id.cb_guardarSesion)

        sharedPreferences = getSharedPreferences("login_preferences", Context.MODE_PRIVATE)
    }

    private fun cargarCredenciales() {
        val guardarCredenciales = sharedPreferences.getBoolean("guardar_credenciales", false)
        if (guardarCredenciales) {
            etNick.setText(sharedPreferences.getString("nick", ""))
            etContra.setText(sharedPreferences.getString("contraseña", ""))
            cbInicioAutomatico.isChecked = true
        }

        btoInicioSesion.setOnClickListener {
            val nick = etNick.text.toString().trim()
            val contra = etContra.text.toString().trim()

            lifecycleScope.launch {
                val exitoso = socketViewModel.enviarDatosInicioSesion(nick, contra)
                if (exitoso) {
                    Toast.makeText(this@PaginaLogin, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    guardarCredenciales(nick, contra)
                    val intent = Intent(this@PaginaLogin, PaginaActividadAlumno::class.java)
                    intent.putExtra("NICK", nick)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@PaginaLogin, "Alguno de los campos no son correctos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btoRegistro.setOnClickListener {
            startActivity(Intent(this, PaginaRegistro::class.java))
        }
    }

    private fun guardarCredenciales(email: String, contra: String) {
        if (cbInicioAutomatico.isChecked) {
            sharedPreferences.edit().apply {
                putBoolean("guardar_credenciales", true)
                putString("nick", email)
                putString("contraseña", contra)
                apply()
            }
        } else {
            sharedPreferences.edit().clear().apply()
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
}
