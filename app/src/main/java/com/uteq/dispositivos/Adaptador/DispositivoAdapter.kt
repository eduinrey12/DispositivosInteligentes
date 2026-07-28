package com.uteq.dispositivos.Adaptador

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.uteq.dispositivos.Modelo.Dispositivo
import com.uteq.dispositivos.R

class DispositivoAdapter(
    private val dispositivos: List<Dispositivo>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<DispositivoAdapter.DispositivosViewHolder>() {

    class DispositivosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtNombreDispositivo)
        val foto: ImageView = itemView.findViewById(R.id.imgFotoDispositivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DispositivosViewHolder {
        val tipoDiseno = if (viewType == 1) R.layout.item_dispositivos else R.layout.item_dispositivos_2
        val itemView = LayoutInflater.from(parent.context).inflate(tipoDiseno, parent, false)
        return DispositivosViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DispositivosViewHolder, position: Int) {
        val dispositivo = dispositivos[position]
        holder.nombre.text = dispositivo.nombre

        holder.itemView.setOnLongClickListener { v ->
            onItemClickListener.onItemClick(dispositivo.id_dispositivo, position, 0, v)
            true
        }

        when (dispositivo.modelo) {
            "wf_cz" -> holder.foto.setImageResource(R.drawable.dispositivo_tomacorriente)
            "wf_ble_cz" -> holder.foto.setImageResource(R.drawable.dispositivo_switch)
            "IOT-BASED" -> holder.foto.setImageResource(R.drawable.dispositivo_breaker)
            "wf_ble_kg" -> holder.foto.setImageResource(R.drawable.dispositivo_touch)
        }

        if (dispositivo.modelo != "wf_cz") {
            val estado: ImageButton = holder.itemView.findViewById(R.id.btnBloquearDispositivo)
            val bloqueo: ImageView = holder.itemView.findViewById(R.id.imgBloqueo)
            bloqueo.visibility = View.GONE
            if (dispositivo.estado) {
                estado.setImageResource(R.drawable.dispositivo_bloquear)
                bloqueo.visibility = View.GONE
            } else {
                estado.setImageResource(R.drawable.dispositivo_desbloquear)
                bloqueo.visibility = View.VISIBLE
            }

            estado.setOnClickListener { v ->
                onItemClickListener.onItemClick(dispositivo.id_dispositivo, position, 1, v)
            }
        } else {
            val estado1: ImageButton = holder.itemView.findViewById(R.id.btnBloquearDispositivo_1)
            val estado2: ImageButton = holder.itemView.findViewById(R.id.btnBloquearDispositivo_2)
            
            if (dispositivo.estado) {
                estado1.setImageResource(R.drawable.dispositivo_bloquear)
            } else {
                estado1.setImageResource(R.drawable.dispositivo_desbloquear)
            }

            if (dispositivo.estado2) {
                estado2.setImageResource(R.drawable.dispositivo_bloquear)
            } else {
                estado2.setImageResource(R.drawable.dispositivo_desbloquear)
            }

            estado1.setOnClickListener { v ->
                onItemClickListener.onItemClick(dispositivo.id_dispositivo, position, 2, v)
            }

            estado2.setOnClickListener { v ->
                onItemClickListener.onItemClick(dispositivo.id_dispositivo, position, 3, v)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val dispositivo = dispositivos[position]
        return if (dispositivo.modelo != "wf_cz") 1 else 2
    }

    override fun getItemCount(): Int {
        return dispositivos.size
    }

    fun interface OnItemClickListener {
        fun onItemClick(dispositivoId: Int, position: Int, type: Int, view: View)
    }
}
