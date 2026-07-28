package com.uteq.dispositivos

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.api.IThingDevice
import com.uteq.dispositivos.Adaptador.DispositivoAdapter
import com.uteq.dispositivos.Modelo.Dispositivo

class Activity_FincaDetalle : AppCompatActivity() {

    private lateinit var rcvDispositivos: RecyclerView
    private var adapterDispositivo: DispositivoAdapter? = null
    private var currentHomeId: Long = 0
    private var allDevicesList = ArrayList<Dispositivo>()
    private var filteredDevicesList = ArrayList<Dispositivo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finca_detalle)

        currentHomeId = intent.getLongExtra("homeId", 0L)
        if (currentHomeId == 0L) {
            Toast.makeText(this, "Error: Espacio no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rcvDispositivos = findViewById(R.id.rcvDispositivosFinca)
        rcvDispositivos.layoutManager = GridLayoutManager(this, 2)

        val btnSalirEspacio: Button = findViewById(R.id.btnSalirEspacio)
        btnSalirEspacio.setOnClickListener {
            // Remove the preference
            val prefs = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE)
            prefs.edit().remove("last_finca_id").apply()

            val intent = Intent(this, Activity_Fincas::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val fabAgregarDispositivo: FloatingActionButton = findViewById(R.id.fabAgregarDispositivo)
        fabAgregarDispositivo.setOnClickListener {
            val intent = Intent(this, Activity_DispositivoAgregar::class.java)
            intent.putExtra("homeId", currentHomeId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDetallesFinca()
    }

    private fun cargarDetallesFinca() {
        ThingHomeSdk.newHomeInstance(currentHomeId).getHomeDetail(object : IThingHomeResultCallback {
            override fun onSuccess(homeBean: HomeBean?) {
                if (homeBean != null) {
                    val txtTitulo: TextView = findViewById(R.id.txtTituloDetalle)
                    txtTitulo.text = homeBean.name

                    prepararDispositivos(homeBean)
                    prepararFiltrosLotes(homeBean)
                }
            }

            override fun onError(errorCode: String?, errorMsg: String?) {
                Toast.makeText(this@Activity_FincaDetalle, "Error: \$errorMsg", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun prepararDispositivos(homeBean: HomeBean) {
        allDevicesList.clear()
        val deviceBeans = homeBean.deviceList
        if (deviceBeans != null) {
            for (i in deviceBeans.indices) {
                val d = deviceBeans[i]
                var estado = false
                if (d.dps != null && d.dps.containsKey("1")) {
                    val state = d.dps["1"]
                    if (state is Boolean) {
                        estado = state
                    }
                }
                // Determine model for correct UI
                var modelo = "Generic"
                if (d.iconUrl?.contains("wf_cz") == true) modelo = "wf_cz"
                if (d.iconUrl?.contains("wf_ble_cz") == true) modelo = "wf_ble_cz"
                if (d.iconUrl?.contains("wf_ble_kg") == true) modelo = "wf_ble_kg"

                allDevicesList.add(
                    Dispositivo(
                        i + 1,
                        d.name,
                        d.devId,
                        "Tuya",
                        modelo,
                        estado,
                        false
                    )
                )
            }
        }
        aplicarFiltro(null, homeBean) // Show all by default
    }

    private fun prepararFiltrosLotes(homeBean: HomeBean) {
        val llLotesFiltro: LinearLayout = findViewById(R.id.llLotesFiltro)
        llLotesFiltro.removeAllViews()

        // Button "Todos"
        val btnTodos = Button(this)
        btnTodos.text = "Todos"
        btnTodos.backgroundTintList = resources.getColorStateList(android.R.color.darker_gray, null)
        btnTodos.setTextColor(Color.WHITE)
        val paramsTodos = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        paramsTodos.setMargins(0, 0, 16, 0)
        btnTodos.layoutParams = paramsTodos
        btnTodos.setOnClickListener { aplicarFiltro(null, homeBean) }
        llLotesFiltro.addView(btnTodos)

        // Buttons for each Room (Lote)
        val rooms = homeBean.rooms
        if (rooms != null) {
            for (room in rooms) {
                val btnRoom = Button(this)
                btnRoom.text = room.name
                btnRoom.backgroundTintList = resources.getColorStateList(android.R.color.darker_gray, null)
                btnRoom.setTextColor(Color.WHITE)
                val paramsRoom = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                paramsRoom.setMargins(0, 0, 16, 0)
                btnRoom.layoutParams = paramsRoom
                
                btnRoom.setOnClickListener { aplicarFiltro(room.roomId, homeBean) }
                llLotesFiltro.addView(btnRoom)
            }
        }
    }

    private fun aplicarFiltro(roomId: Long?, homeBean: HomeBean) {
        filteredDevicesList.clear()
        
        if (roomId == null) {
            // Todos
            filteredDevicesList.addAll(allDevicesList)
        } else {
            // Encontrar dispositivos en este cuarto
            val room = homeBean.rooms?.firstOrNull { it.roomId == roomId }
            val devicesInRoom = room?.deviceList ?: emptyList()
            
            for (d in allDevicesList) {
                if (devicesInRoom.any { it.devId == d.devId }) {
                    filteredDevicesList.add(d)
                }
            }
        }

        if (adapterDispositivo == null) {
            adapterDispositivo = DispositivoAdapter(filteredDevicesList) { id, posicion, op, it ->
                manejarClicDispositivo(id, posicion, op, it)
            }
            rcvDispositivos.adapter = adapterDispositivo
        } else {
            adapterDispositivo?.notifyDataSetChanged()
        }
    }

    private fun manejarClicDispositivo(id: Int, posicion: Int, op: Int, view: View) {
        val dispositivo = filteredDevicesList.firstOrNull { it.id_dispositivo == id } ?: return
        
        when (op) {
            0 -> {
                // Long click
                Toast.makeText(this, "Dispositivo: \${dispositivo.nombre}", Toast.LENGTH_SHORT).show()
            }
            1, 2 -> {
                val mDevice: IThingDevice = ThingHomeSdk.newDeviceInstance(dispositivo.devId)
                try {
                    val newState = !dispositivo.estado
                    dispositivo.estado = newState
                    mDevice.publishDps("{\"1\": \$newState}", object : IResultCallback {
                        override fun onError(code: String, error: String) {
                            Log.e("DP_ERROR", "\$code \$error")
                        }
                        override fun onSuccess() {}
                    })
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                }
                adapterDispositivo?.notifyItemChanged(posicion)
            }
            3 -> {
                val mDevice3: IThingDevice = ThingHomeSdk.newDeviceInstance(dispositivo.devId)
                try {
                    val newState = !dispositivo.estado2
                    dispositivo.estado2 = newState
                    mDevice3.publishDps("{\"2\": \$newState}", object : IResultCallback {
                        override fun onError(code: String, error: String) {}
                        override fun onSuccess() {}
                    })
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                }
                adapterDispositivo?.notifyItemChanged(posicion)
            }
        }
    }
}
