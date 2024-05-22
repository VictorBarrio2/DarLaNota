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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragmento_ranking_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNick = view.findViewById(R.id.et_nombreAlumnoRa)
        etNota = view.findViewById(R.id.et_notaAlumnoRa)
        btoSumar = view.findViewById(R.id.bto_sumar)
        btoRestar = view.findViewById(R.id.bto_restar)

        configurarListenersDeBotones()
    }

    private fun configurarListenersDeBotones() {
        btoSumar.setOnClickListener {
            gestionarCambioDePuntuacion(esIncremento = true)
        }

        btoRestar.setOnClickListener {
            gestionarCambioDePuntuacion(esIncremento = false)
        }
    }

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
                        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                        dismiss() // Cerrar el diálogo
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        etNota.error = "Usuario no encontrado."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error en la operación de puntuación: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.fragmento_ranking_layout)
        return dialog
    }
}
