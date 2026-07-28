package com.uteq.dispositivos.Adaptador

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.uteq.dispositivos.Modelo.Aula
import com.uteq.dispositivos.R

class AulaAdapter(
    private val aula: List<Aula>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AulaAdapter.DispositivosViewHolder>() {

    class DispositivosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtAula)
        val foto: ImageView = itemView.findViewById(R.id.imgAula)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DispositivosViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_aulas, parent, false)
        return DispositivosViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DispositivosViewHolder, position: Int) {
        val aulas = aula[position]
        holder.nombre.text = aulas.nombre

        holder.itemView.setOnClickListener { v ->
            onItemClickListener.onItemClick(aulas.id_aula, false, v)
        }

        holder.itemView.setOnLongClickListener { v ->
            onItemClickListener.onItemClick(aulas.id_aula, true, v)
            true
        }
    }

    override fun getItemCount(): Int {
        return aula.size
    }

    fun interface OnItemClickListener {
        fun onItemClick(id: Int, isLongClick: Boolean, view: View)
    }
}
