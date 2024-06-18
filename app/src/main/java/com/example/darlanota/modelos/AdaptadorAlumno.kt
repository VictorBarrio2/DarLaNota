package com.example.darlanota.modelos

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad
import com.example.darlanota.modelos.PaginaVerActividad

class AdaptadorAlumno(private var dataList: List<Actividad>, private var nickAlumno: String) :
    RecyclerView.Adapter<AdaptadorAlumno.DatosHolder>() {

    // Método para actualizar la lista de actividades mostradas
    fun actualizarDatos(nuevaLista: List<Actividad>) {
        dataList = nuevaLista
        notifyDataSetChanged()  // Notifica al RecyclerView que los datos han cambiado
    }

    // Método para crear nuevas vistas (invocado por el layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatosHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return DatosHolder(view)
    }

    // Método para reemplazar el contenido de una vista (invocado por el layout manager)
    override fun onBindViewHolder(holder: DatosHolder, position: Int) {
        val actividad = dataList[position]
        holder.bind(actividad)
    }

    // Método para obtener el tamaño de la lista de datos (invocado por el layout manager)
    override fun getItemCount(): Int = dataList.size

    // Clase interna que describe la vista del elemento y los metadatos sobre su lugar en el RecyclerView
    inner class DatosHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.tv_tituloActividades)

        // Método para vincular datos de una actividad específica a la vista
        fun bind(actividad: Actividad) {
            titulo.text = actividad.titulo

            // Establecer el listener para el clic en la vista del elemento
            itemView.setOnClickListener {
                iniciarPaginaVerActividad(itemView.context, actividad, nickAlumno)
            }
        }
    }

    // Método para iniciar la actividad de visualización de detalles de la actividad
    private fun iniciarPaginaVerActividad(context: Context, actividad: Actividad, nickAlumno: String) {
        val intent = Intent(context, PaginaVerActividad::class.java).apply {
            putExtra("ACTIVIDAD_ID", actividad.id)
            putExtra("TITULO", actividad.titulo)
            putExtra("DESCRIPCION", actividad.descripcion)
            putExtra("FECHAFIN", actividad.fechafin)
            putExtra("NICK", nickAlumno)
        }
        context.startActivity(intent)
    }
}
