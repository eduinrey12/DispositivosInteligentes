package com.uteq.dispositivos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thingclips.smart.home.sdk.bean.HomeBean

class FincaAdapter(
    private val fincas: List<HomeBean>,
    private val onItemClick: (Long) -> Unit
) : RecyclerView.Adapter<FincaAdapter.FincaViewHolder>() {

    class FincaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombreFinca)
        val txtEstado: TextView = itemView.findViewById(R.id.txtEstadoFinca)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FincaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_finca, parent, false)
        return FincaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FincaViewHolder, position: Int) {
        val finca = fincas[position]
        holder.txtNombre.text = finca.name
        
        // Intentar obtener la cantidad de dispositivos del cache local
        val cachedHome = com.thingclips.smart.home.sdk.ThingHomeSdk.newHomeInstance(finca.homeId).homeBean
        val deviceCount = cachedHome?.deviceList?.size ?: finca.deviceList?.size ?: 0
        
        holder.txtEstado.text = "Dispositivos: ${deviceCount}"
        
        holder.itemView.setOnClickListener {
            onItemClick(finca.homeId)
        }
    }

    override fun getItemCount(): Int = fincas.size
}
