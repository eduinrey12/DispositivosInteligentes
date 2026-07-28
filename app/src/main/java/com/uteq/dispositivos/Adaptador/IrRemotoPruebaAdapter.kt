package com.uteq.dispositivos.Adaptador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.uteq.dispositivos.R

class IrRemotoPruebaAdapter(
    private val remotos: List<JsonObject>,
    private val onProbarClick: (Int) -> Unit,
    private val onVincularClick: (Int) -> Unit
) : RecyclerView.Adapter<IrRemotoPruebaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtNombreRemoto)
        val btnProbar: Button = view.findViewById(R.id.btnProbar)
        val btnVincular: Button = view.findViewById(R.id.btnVincular)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_remoto_prueba, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val remoto = remotos[position]
        val remoteIndex = remoto.get("remote_index").asInt

        holder.txtNombre.text = "Opción de Control ${position + 1}"

        holder.btnProbar.setOnClickListener {
            onProbarClick(remoteIndex)
        }

        holder.btnVincular.setOnClickListener {
            onVincularClick(remoteIndex)
        }
    }

    override fun getItemCount() = remotos.size
}
