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

class AdaptadorAlumno(private val id: String, dataList: List<Actividad>) :
    RecyclerView.Adapter<AdaptadorAlumno.DatosHolder>() {

    private var dataList: List<Actividad> = dataList
        set(value) {
            field = value
            notifyDataSetChanged()  // Nota: Considera usar DiffUtil aquí para mejorar el rendimiento
        }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy")

    init {
        // Preprocesa la lista para eliminar actividades pasadas
        this.dataList = dataList.filterNot { it.fechafin?.toDate()?.before(Date()) ?: false }
    }

    // Método para crear nuevas vistas (invocado por el layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatosHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return DatosHolder(view)
    }

    // Método para reemplazar el contenido de una vista (invocado por el layout manager)
    override fun onBindViewHolder(holder: DatosHolder, position: Int) {
        val actividad = dataList[position]
        holder.titulo.text = actividad.titulo

        // Establecer el listener para el clic en la vista del elemento
        val fechaFin = actividad.fechafin?.toDate()
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFormateada = fechaFin?.let { formatoFecha.format(it) } ?: "Sin fecha"

        holder.fecha.text = fechaFormateada

        // Establecer el listener para el clic en la vista del elemento
        holder.itemView.setOnClickListener {
            iniciarPaginaVerActividad(holder, actividad, fechaFormateada)
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
            putExtra("ID", id)
        }
        holder.itemView.context.startActivity(intent)
    }

    // Clase interna que describe la vista del elemento y los metadatos sobre su lugar en el RecyclerView
    class DatosHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titulo: TextView = itemView.findViewById(R.id.tv_tituloActividades)
        var fecha : TextView = itemView.findViewById(R.id.tv_fechaAct)
    }
}
