package com.example.darlanota.modelos

import android.content.Intent
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.darlanota.R

class AdaptadorProfe internal constructor(private val dataList: List<String>) :
    RecyclerView.Adapter<AdaptadorProfe.DatosHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatosHolder {
        // Inflar la vista del elemento de la lista
        val inflador = LayoutInflater.from(parent.context)
        val vista: View = inflador.inflate(R.layout.item_layout, parent, false)
        return DatosHolder(vista)
    }

    override fun onBindViewHolder(holder: DatosHolder, position: Int) {
        // Vincular datos a las vistas
        holder.textView.text = dataList[position] // Reemplazar con datos reales
    }

    override fun getItemCount(): Int {
        // Devolver la cantidad de elementos en la lista de datos
        return dataList.size
    }

    inner class DatosHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        OnCreateContextMenuListener {
        var textView: TextView

        init {
            // Inicializar TextView
            textView = itemView.findViewById(R.id.tv_tituloActividad)

            textView.setOnClickListener {
                val intent = Intent(itemView.context, PaginaCorregirActividad::class.java)
                itemView.context.startActivity(intent)
            }

            // Establecer el listener para el menú contextual
            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
            TODO("Not yet implemented")
        }

    }
}
