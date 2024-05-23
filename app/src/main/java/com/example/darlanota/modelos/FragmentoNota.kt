package com.example.darlanota.modelos

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.*

class FragmentoNota : DialogFragment() {

    private lateinit var etNick: EditText
    private lateinit var etNota: EditText
    private lateinit var btoSumar: Button
    private lateinit var btoRestar: Button
    private val firestore = FireStore()

    // Método para inflar el layout del fragmento
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragmento_ranking_layout, container, false)
    }

    // Método para configurar las vistas y los listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializarVistas(view)
        configurarListenersDeBotones()
    }

    // Método para inicializar las vistas
    private fun inicializarVistas(view: View) {
        etNick = view.findViewById(R.id.et_nombreAlumnoRa)
        etNota = view.findViewById(R.id.et_notaAlumnoRa)
        btoSumar = view.findViewById(R.id.bto_sumar)
        btoRestar = view.findViewById(R.id.bto_restar)
    }

    // Método para configurar los listeners de los botones
    private fun configurarListenersDeBotones() {
        btoSumar.setOnClickListener {
            gestionarCambioDePuntuacion(esIncremento = true)
        }

        btoRestar.setOnClickListener {
            gestionarCambioDePuntuacion(esIncremento = false)
        }
    }

    // Método para gestionar el cambio de puntuación (incrementar o decrementar)
    private fun gestionarCambioDePuntuacion(esIncremento: Boolean) {
        val nombre = etNick.text.toString()
        val nota = etNota.text.toString().toIntOrNull()
        if (nota == null) {
            etNota.error = "Por favor, ingrese un valor válido"
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val idUsuario = firestore.obtenerIdPorNombre(nombre)
                if (idUsuario != null) {
                    val mensaje: String
                    if (esIncremento) {
                        firestore.incrementarPuntuacionUsuario(idUsuario, nota)
                        mensaje = "Sumado $nota puntos a $nombre."
                    } else {
                        firestore.decrementarPuntuacionAlumno(idUsuario, nota)
                        mensaje = "Restado $nota puntos a $nombre."
                    }
                    withContext(Dispatchers.Main) {
                        mostrarMensaje(mensaje)
                        dismiss() // Cerrar el diálogo
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        etNick.error = "Usuario no encontrado."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mostrarMensaje("Error en la operación de puntuación: ${e.message}")
                }
            }
        }
    }

    // Método para mostrar un mensaje Toast
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
    }

    // Método para crear un diálogo sin marco
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.fragmento_ranking_layout)
        return dialog
    }
}
