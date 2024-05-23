package com.example.darlanota.modelos

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad

class AdaptadorProfe(private val profesorId: String, private val dataList: List<Actividad>) :
    RecyclerView.Adapter<AdaptadorProfe.DatosHolder>() {

    // Método para crear nuevas vistas (invocado por el layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatosHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.item_layout, parent, false)
        return DatosHolder(view)
    }

    // Método para reemplazar el contenido de una vista (invocado por el layout manager)
    override fun onBindViewHolder(holder: DatosHolder, position: Int) {
        val actividad = dataList[position]
        holder.textView.text = actividad.titulo

        // Establecer el listener para el clic en la vista del elemento
        holder.itemView.setOnClickListener {
            iniciarPaginaCorregirActividad(holder, actividad)
        }
    }

    // Método para obtener el tamaño de la lista de datos (invocado por el layout manager)
    override fun getItemCount(): Int = dataList.size

    // Método para iniciar la actividad de corrección de actividad
    private fun iniciarPaginaCorregirActividad(holder: DatosHolder, actividad: Actividad) {
        val intent = Intent(holder.itemView.context, PaginaCorregirActividad::class.java).apply {
            putExtra("ACTIVIDAD_ID", actividad.id)
            putExtra("TITULO", actividad.titulo)
            putExtra("ID", profesorId)
        }
        holder.itemView.context.startActivity(intent)
    }

    // Clase interna que describe la vista del elemento y los metadatos sobre su lugar en el RecyclerView
    class DatosHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.tv_tituloActividades)
    }
}
