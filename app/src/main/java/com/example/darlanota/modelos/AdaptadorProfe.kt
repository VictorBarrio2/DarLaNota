package com.example.darlanota.modelos

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R
import com.example.darlanota.clases.Actividad

class   AdaptadorProfe(private val profesorId: String, private val dataList: List<Actividad>) :
    RecyclerView.Adapter<AdaptadorProfe.DatosHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatosHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.item_layout, parent, false)
        return DatosHolder(view)
    }

    override fun onBindViewHolder(holder: DatosHolder, position: Int) {
        val actividad = dataList[position]
        holder.textView.text = actividad.titulo

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, PaginaCorregirActividad::class.java)
            intent.putExtra("ACTIVIDAD_ID", actividad.id)
            intent.putExtra("TITULO", actividad.titulo)
            intent.putExtra("ID", profesorId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = dataList.size

    class DatosHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.tv_tituloActividades)
    }

}
