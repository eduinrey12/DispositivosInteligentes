package com.uteq.dispositivos.Adaptador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.uteq.dispositivos.R

class IrCategoriaAdapter(
    private val categorias: JsonArray,
    private val devId: String,
    private val onCategoriaClick: (Int, String) -> Unit
) : RecyclerView.Adapter<IrCategoriaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtNombreCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria_ir, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoria = categorias.get(position).asJsonObject
        val nombre = categoria.get("category_name").asString
        val categoryId = categoria.get("category_id").asInt

        holder.txtNombre.text = nombre

        holder.itemView.setOnClickListener {
            onCategoriaClick(categoryId, nombre)
        }
    }

    override fun getItemCount() = categorias.size()
}
