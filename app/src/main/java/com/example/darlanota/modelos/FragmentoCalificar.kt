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
import androidx.lifecycle.lifecycleScope
import com.example.darlanota.R
import com.example.darlanota.clases.FireStore
import kotlinx.coroutines.launch

class FragmentoCalificar : DialogFragment() {

    private lateinit var etNota: EditText
    private lateinit var btoEnviar: Button
    private var firestore = FireStore()

    companion object {
        const val ARG_ID_ALUMNO = "idAlumno"
        const val ARG_ID_ACTIVIDAD = "idActividad"

        // Método para crear una nueva instancia del fragmento con argumentos
        fun newInstance(idAlumno: String, idActividad: String): FragmentoCalificar {
            val fragment = FragmentoCalificar()
            val args = Bundle()
            args.putString(ARG_ID_ALUMNO, idAlumno)
            args.putString(ARG_ID_ACTIVIDAD, idActividad)
            fragment.arguments = args
            return fragment
        }
    }

    // Método para inflar el layout del fragmento
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragmento_calificacion_layout, container, false)
    }

    // Método para configurar las vistas y los listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inicializarVistas(view)
        configurarListeners(view)
    }

    // Método para inicializar las vistas
    private fun inicializarVistas(view: View) {
        etNota = view.findViewById(R.id.et_notaCalificar)
        btoEnviar = view.findViewById(R.id.bto_enviarCalificar)
    }

    // Método para configurar los listeners de los botones
    private fun configurarListeners(view: View) {
        val idAlumno = obtenerArgumento(ARG_ID_ALUMNO)
        val idActividad = obtenerArgumento(ARG_ID_ACTIVIDAD)

        btoEnviar.setOnClickListener {
            val notaText = etNota.text.toString()
            if (notaText.isNotEmpty()) {
                procesarNota(notaText, idAlumno, idActividad)
            } else {
                mostrarMensaje("El campo de nota no puede estar vacío.")
            }
        }
    }

    // Método para obtener los argumentos del fragmento
    private fun obtenerArgumento(clave: String): String {
        return arguments?.getString(clave) ?: ""
    }

    // Método para procesar la nota ingresada
    private fun procesarNota(notaText: String, idAlumno: String, idActividad: String) {
        try {
            val number = notaText.toInt()
            mostrarMensaje("Se ha puesto un $number")

            // Lanzar una coroutine para llamar a las funciones suspendidas
            lifecycleScope.launch {
                if (idAlumno.isNotEmpty() && idActividad.isNotEmpty()) {
                    actualizarPuntuacionYCalificacion(idAlumno, idActividad, number)
                    dismiss() // Cerrar el diálogo después de actualizar la puntuación
                } else {
                    mostrarMensaje("Error: ID de alumno o actividad no disponible.")
                }
            }
        } catch (e: NumberFormatException) {
            mostrarMensaje("Por favor, introduce un número válido.")
        }
    }

    // Método para actualizar la puntuación y calificación
    private suspend fun actualizarPuntuacionYCalificacion(idAlumno: String, idActividad: String, number: Int) {
        firestore.incrementarPuntuacionUsuario(idAlumno, number)
        firestore.actualizarCalificacionEntrega(idActividad, idAlumno, number)
    }

    // Método para mostrar un mensaje Toast
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    // Método para crear un diálogo sin marco
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
