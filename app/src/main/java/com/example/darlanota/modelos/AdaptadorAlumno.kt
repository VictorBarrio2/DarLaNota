package com.example.darlanota.modelos

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad
import com.example.darlanota.modelos.PaginaVerActividad
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdaptadorAlumno(private val nick: String, dataList: List<Actividad>) :
    RecyclerView.Adapter<AdaptadorAlumno.DatosHolder>() {
    private val instrumentos = listOf(
        R.drawable.nota to 1,
        R.drawable.piano to 2,
        R.drawable.guitarra to 3,
        R.drawable.bateria to 4,
        R.drawable.canto to 5
    )

    private val candados = listOf(
        R.drawable.candado_abierto to 1,
        R.drawable.candado_cerrado to 2
    )

    private var dataList: List<Actividad> = dataList
        set(value) {
            field = value
            notifyDataSetChanged()  // Nota: Considera usar DiffUtil aquí para mejorar el rendimiento
        }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy")


    // Método para crear nuevas vistas (invocado por el layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatosHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return DatosHolder(view)
    }

    // Método para reemplazar el contenido de una vista (invocado por el layout manager)
    // Método para reemplazar el contenido de una vista (invocado por el layout manager)
    override fun onBindViewHolder(holder: DatosHolder, position: Int) {
        val actividad = dataList[position]
        holder.titulo.text = actividad.titulo

        // Suponiendo que el objeto Actividad tiene un atributo que indica el número del instrumento
        val numeroInstrumento = actividad.instrumento // Cambia esto por el nombre correcto del atributo

        // Buscar la imagen correspondiente al número del instrumento
        val instrumentoImagen = instrumentos.find { it.second == numeroInstrumento }?.first ?: R.drawable.nota

        // Establecer la imagen del instrumento en el ImageView
        holder.instrument.setImageResource(instrumentoImagen)

        // Obtener la fecha de fin de la actividad
        val fechaFin = actividad.fechafin?.toDate()

        // Obtener la fecha actual
        val fechaActual = Date()

        // Establecer la imagen del candado y el listener según la fecha
        if (fechaFin != null && fechaActual.after(fechaFin)) {
            holder.candado.setImageResource(R.drawable.candado_cerrado)
            holder.itemView.setOnClickListener(null) // Desactivar el listener
        } else {
            holder.candado.setImageResource(R.drawable.candado_abierto)
            holder.itemView.setOnClickListener {
                val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaFormateada = fechaFin?.let { formatoFecha.format(it) } ?: "Sin fecha"
                iniciarPaginaVerActividad(holder, actividad, fechaFormateada)
            }
        }
    }
    // Método para obtener el tamaño de la lista de datos (invocado por el layout manager)
    override fun getItemCount(): Int = dataList.size

    // Método para iniciar la actividad de visualización de detalles de la actividad
    private fun iniciarPaginaVerActividad(holder: DatosHolder, actividad: Actividad, fechaString: String) {
        val intent = Intent(holder.itemView.context, PaginaVerActividad::class.java).apply {
            putExtra("ACTIVIDAD_ID", actividad.id)
            putExtra("TITULO", actividad.titulo)
            putExtra("DESCRIPCION", actividad.descripcion)
            putExtra("FECHA", fechaString)
            putExtra("NICK", nick)
        }
        holder.itemView.context.startActivity(intent)
    }

    // Clase interna que describe la vista del elemento y los metadatos sobre su lugar en el RecyclerView
    class DatosHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titulo: TextView = itemView.findViewById(R.id.tv_tituloActividades)
        var instrument : ImageView = itemView.findViewById(R.id.iv_itemInstrumento)
        var candado : ImageView = itemView.findViewById(R.id.iv_itemCandado)
    }
}
