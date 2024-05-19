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

        fun newInstance(idAlumno: String, idActividad: String): FragmentoCalificar {
            val fragment = FragmentoCalificar()
            val args = Bundle()
            args.putString(ARG_ID_ALUMNO, idAlumno)
            args.putString(ARG_ID_ACTIVIDAD, idActividad)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragmento_calificacion_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener referencias a los elementos del layout
        etNota = view.findViewById(R.id.et_notaCalificar)
        btoEnviar = view.findViewById(R.id.bto_enviarCalificar)

        // Recuperar el ID del alumno y de la actividad de los argumentos
        val idAlumno = arguments?.getString(ARG_ID_ALUMNO) ?: ""
        val idActividad = arguments?.getString(ARG_ID_ACTIVIDAD) ?: ""

        // Configurar acciones de los botones
        btoEnviar.setOnClickListener {
            val notaText = etNota.text.toString()
            if (notaText.isNotEmpty()) {
                try {
                    val number = notaText.toInt()
                    Toast.makeText(requireContext(), "Se ha puesto un $number", Toast.LENGTH_SHORT).show()

                    // Lanzar una coroutine para llamar a las funciones suspendidas
                    lifecycleScope.launch {
                        if (idAlumno.isNotEmpty() && idActividad.isNotEmpty()) {
                            firestore.incrementarPuntuacionAlumno(idAlumno, number)
                            firestore.actualizarCalificacionEntrega(idActividad, idAlumno, number)
                            dismiss() // Cerrar el diálogo después de actualizar la puntuación
                        } else {
                            Toast.makeText(requireContext(), "Error: ID de alumno o actividad no disponible.", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Por favor, introduce un número válido.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "El campo de nota no puede estar vacío.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create a dialog without a frame
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
