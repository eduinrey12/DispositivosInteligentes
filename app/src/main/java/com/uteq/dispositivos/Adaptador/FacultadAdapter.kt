package com.uteq.dispositivos.Adaptador

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.uteq.dispositivos.Modelo.Facultad
import com.uteq.dispositivos.R

class FacultadAdapter(
    private val facultades: List<Facultad>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<FacultadAdapter.DispositivosViewHolder>() {

    class DispositivosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtFacultad)
        val foto: ImageView = itemView.findViewById(R.id.imgFacultad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DispositivosViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_facultades, parent, false)
        return DispositivosViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DispositivosViewHolder, position: Int) {
        val facultad = facultades[position]
        holder.nombre.text = facultad.nombre

        holder.itemView.setOnClickListener { v ->
            onItemClickListener.onItemClick(facultad.id_facultad, false, v)
        }

        holder.itemView.setOnLongClickListener { v ->
            onItemClickListener.onItemClick(facultad.id_facultad, true, v)
            true
        }
    }

    override fun getItemCount(): Int {
        return facultades.size
    }

    fun interface OnItemClickListener {
        fun onItemClick(id: Int, isLongClick: Boolean, view: View)
    }
}
