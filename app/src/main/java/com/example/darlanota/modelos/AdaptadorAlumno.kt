package com.example.darlanota.modelos

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad
import java.text.SimpleDateFormat
import java.util.Date

class AdaptadorAlumno(private val id: String, private val dataList: List<Actividad>) :
    RecyclerView.Adapter<AdaptadorAlumno.DatosHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatosHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.item_layout, parent, false)
        return DatosHolder(view)
    }

    override fun onBindViewHolder(holder: DatosHolder, position: Int) {
        val actividad = dataList[position]
        holder.titulo.text = actividad.titulo
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, PaginaVerActividad::class.java)
            intent.putExtra("ACTIVIDAD_ID", actividad.id)
            intent.putExtra("TITULO", actividad.titulo)
            intent.putExtra("DESCRIPCION", actividad.descripcion)
            intent.putExtra("ID", id)

            // Convertir el Timestamp a una fecha legible si no es nulo
            actividad.fechafin?.let { timestamp ->
                val formato = SimpleDateFormat("dd/MM/yyyy")
                val fecha: Date = timestamp.toDate()
                val fechaString = formato.format(fecha)
                intent.putExtra("FECHA", fechaString)
            }

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = dataList.size

    class DatosHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titulo: TextView = itemView.findViewById(R.id.tv_tituloActividad)
    }
}
