package com.example.darlanota.modelos
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.darlanota.R

class FragmentoNota : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragmento_ranking_layout, container, false)
    }

    private lateinit var et_nick: EditText
    private lateinit var et_nota: EditText

    private lateinit var bto_sumar: Button
    private lateinit var bto_restar: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener referencias a los elementos del layout
        val et_nick = view.findViewById<EditText>(R.id.et_nick)
        val et_nota = view.findViewById<EditText>(R.id.et_notaAlumnoRa)
        val bto_sumar = view.findViewById<Button>(R.id.bto_sumar)
        val bto_restar = view.findViewById<Button>(R.id.bto_restar)

        // Configurar acciones de los botones
        bto_sumar.setOnClickListener {

            dismiss() // Cerrar el diálogo
        }

        bto_restar.setOnClickListener {

            dismiss() // Cerrar el diálogo
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Crear un diálogo con un estilo que no tenga marco
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.fragmento_ranking_layout)
        return dialog
    }
}
