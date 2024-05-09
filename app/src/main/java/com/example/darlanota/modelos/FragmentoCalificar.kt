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

class FragmentoCalificar : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragmento_calificacion_layout, container, false)
    }

    private lateinit var et_nota: EditText

    private lateinit var bto_enviar: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener referencias a los elementos del layout
        et_nota = view.findViewById<EditText>(R.id.et_notaCalificar)
        bto_enviar = view.findViewById<Button>(R.id.bto_enviarCalificar)


        // Configurar acciones de los botones
        bto_enviar.setOnClickListener {
            val number = Integer.parseInt(et_nota.getText().toString())
            Toast.makeText(requireContext(), "Se ha puesto un " + number.toString(), Toast.LENGTH_SHORT).show()
            dismiss() // Cerrar el diálogo
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create a dialog without a frame
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
