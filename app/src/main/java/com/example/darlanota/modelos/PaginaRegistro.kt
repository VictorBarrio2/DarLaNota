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
import com.example.darlanota.clases.Alumno
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

// Constantes para el cifrado
private const val ALGORITMO = "AES"
private const val CLAVE = "claveSegura12345"

class PaginaRegistro : AppCompatActivity() {

    private lateinit var etNick: EditText
    private lateinit var etContra: EditText
    private lateinit var etConfirmacionContra: EditText
    private lateinit var btnCrearCuenta: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_layout)

        db = FirebaseFirestore.getInstance()
        inicializarVistas()

        btnCrearCuenta.setOnClickListener {
            // Llamar a la función de registro dentro de una corutina
            CoroutineScope(Dispatchers.Main).launch {
                registrarUsuario()
            }
        }
    }

    private fun inicializarVistas() {
        etNick = findViewById(R.id.et_crearNick)
        etContra = findViewById(R.id.et_crearContrasena)
        etConfirmacionContra = findViewById(R.id.et_confirmarContra)
        btnCrearCuenta = findViewById(R.id.bto_crearCuenta)
    }

    private suspend fun registrarUsuario() {
        val nick = etNick.text.toString().trim()
        val contra = etContra.text.toString().trim()
        val contraConfirmada = etConfirmacionContra.text.toString().trim()

        if (validarCampos(nick, contra, contraConfirmada)) {
            val contraCifrada = cifrar(contra)

            if (!usuarioExiste(nick)) {
                crearNuevoAlumno(nick, contraCifrada)
            } else {
                Toast.makeText(this, "Ya hay un usuario con ese nombre", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun usuarioExiste(nick: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = db.collection("usuarios")
                    .whereEqualTo("nombre", nick)
                    .get()
                    .await()

                !querySnapshot.isEmpty
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun crearNuevoAlumno(nick: String, contra: String) {
        val alumno = Alumno(contra, nick, "alumno", 0)

        db.collection("usuarios")
            .add(alumno)
            .addOnSuccessListener {
                iniciarActividadAlumno(nick)
                Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear alumno: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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

        if(contra.length < 6){
            Toast.makeText(this, "La contraseña tiene que tener una longitud minima de 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun iniciarActividadAlumno(nick: String) {
        finishAffinity()
        val intent = Intent(this, PaginaActividadAlumno::class.java)
        intent.putExtra("NICK", nick)
        startActivity(intent)
        finish()
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
