package com.uteq.dispositivos.Adaptador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.uteq.dispositivos.R

class IrMarcaAdapter(
    private var marcas: List<JsonObject>,
    private val onMarcaClick: (Int, String) -> Unit
) : RecyclerView.Adapter<IrMarcaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtNombreCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria_ir, parent, false) // Reutilizamos el mismo layout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val marca = marcas[position]
        val nombre = marca.get("brand_name").asString
        val brandId = marca.get("brand_id").asInt

        holder.txtNombre.text = nombre

        holder.itemView.setOnClickListener {
            onMarcaClick(brandId, nombre)
        }
    }

    override fun getItemCount() = marcas.size

    fun actualizar(nuevasMarcas: List<JsonObject>) {
        this.marcas = nuevasMarcas
        notifyDataSetChanged()
    }
}
