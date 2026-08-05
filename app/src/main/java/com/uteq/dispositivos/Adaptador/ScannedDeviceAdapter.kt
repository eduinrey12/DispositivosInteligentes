package com.uteq.dispositivos.Adaptador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thingclips.smart.android.ble.api.ScanDeviceBean
import com.uteq.dispositivos.R

class ScannedDeviceAdapter(
    private val onDeviceSelected: (ScanDeviceBean) -> Unit
) : RecyclerView.Adapter<ScannedDeviceAdapter.ViewHolder>() {

    private val deviceList = mutableListOf<ScanDeviceBean>()
    private val customDeviceNames = mutableMapOf<String, String>()

    fun addDevice(device: ScanDeviceBean) {
        val newKey = device.uuid?.ifEmpty { null } 
            ?: device.address?.ifEmpty { null } 
            ?: device.mac?.ifEmpty { null } 
            ?: device.productId

        val exists = deviceList.any { existing ->
            val existingKey = existing.uuid?.ifEmpty { null } 
                ?: existing.address?.ifEmpty { null } 
                ?: existing.mac?.ifEmpty { null } 
                ?: existing.productId
            existingKey != null && existingKey == newKey
        }

        if (!exists) {
            deviceList.add(device)
            notifyItemInserted(deviceList.size - 1)
        }
    }

    fun updateDeviceName(uuid: String, friendlyName: String) {
        if (uuid.isEmpty() || friendlyName.isEmpty()) return
        customDeviceNames[uuid] = friendlyName
        val index = deviceList.indexOfFirst { it.uuid == uuid }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }
    
    fun clearDevices() {
        deviceList.clear()
        customDeviceNames.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scanned_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceList[position]
        val uuid = device.uuid ?: ""
        val friendlyNameFromCloud = customDeviceNames[uuid]
        
        val devName = when {
            !friendlyNameFromCloud.isNullOrEmpty() -> friendlyNameFromCloud
            !device.name.isNullOrEmpty() && !device.name!!.startsWith("key") && device.name!!.length <= 15 -> device.name
            device.productId == "key54vrth5askhsj" -> "Control Infrarrojo Steren"
            device.productId == "g30xstbrfn01skee" -> "Tomacorriente Smart"
            !device.productId.isNullOrEmpty() -> "Dispositivo Smart (${device.productId})"
            else -> "Dispositivo Tuya"
        }
        
        holder.txtDeviceName.text = devName
        holder.txtDeviceMac.text = "UUID: ${device.uuid ?: device.mac ?: "Desconocido"}"
        
        holder.itemView.setOnClickListener {
            onDeviceSelected(device)
        }
    }

    override fun getItemCount(): Int = deviceList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDeviceName: TextView = itemView.findViewById(R.id.txtDeviceName)
        val txtDeviceMac: TextView = itemView.findViewById(R.id.txtDeviceMac)
        val imgDeviceIcon: ImageView = itemView.findViewById(R.id.imgDeviceIcon)
    }
}
