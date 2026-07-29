package com.uteq.dispositivos

import android.content.ClipboardManager
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
import androidx.appcompat.app.AlertDialog
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
                
                // Inspeccionar dps para estado inicial ("1", "20", "101")
                var estado = false
                if (d.dps != null) {
                    val stateVal = d.dps["1"] ?: d.dps["20"] ?: d.dps["101"]
                    if (stateVal is Boolean) {
                        estado = stateVal
                    }
                }

                // Clasificar modelo (Infrarrojo Steren vs Tomacorriente / Enchufe)
                val category = d.category ?: ""
                val productId = d.productId ?: ""
                val name = d.name ?: ""

                val esInfrarrojo = category.contains("wnykq", ignoreCase = true) ||
                        category.contains("ir", ignoreCase = true) ||
                        productId.contains("key54vrth5askhsj", ignoreCase = true) ||
                        name.contains("steren", ignoreCase = true) ||
                        name.contains("infrarrojo", ignoreCase = true) ||
                        name.contains("ir", ignoreCase = true) ||
                        name.contains("control", ignoreCase = true)

                val modelo = if (esInfrarrojo) {
                    "infrarrojo"
                } else if (category.contains("cz", ignoreCase = true) || category.contains("socket", ignoreCase = true) || category.contains("plug", ignoreCase = true)) {
                    "wf_cz"
                } else {
                    "wf_ble_cz"
                }

                allDevicesList.add(
                    Dispositivo(
                        i + 1,
                        d.name ?: "Dispositivo",
                        d.devId,
                        "Tuya",
                        modelo,
                        estado,
                        false
                    )
                )
            }
        }
        aplicarFiltro(null, homeBean)
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
            filteredDevicesList.addAll(allDevicesList)
        } else {
            val room = homeBean.rooms?.firstOrNull { it.roomId == roomId }
            val devicesInRoom = room?.deviceList ?: emptyList()
            
            for (d in allDevicesList) {
                if (devicesInRoom.any { it.devId == d.devId }) {
                    filteredDevicesList.add(d)
                }
            }
        }

        if (adapterDispositivo == null) {
            adapterDispositivo = DispositivoAdapter(filteredDevicesList) { id, posicion, op, view ->
                manejarClicDispositivo(id, posicion, op, view)
            }
            rcvDispositivos.adapter = adapterDispositivo
        } else {
            adapterDispositivo?.notifyDataSetChanged()
        }
    }

    private fun manejarClicDispositivo(id: Int, posicion: Int, op: Int, view: View) {
        val dispositivo = filteredDevicesList.firstOrNull { it.id_dispositivo == id } ?: return
        val devId = dispositivo.devId ?: return

        // Si es dispositivo Infrarrojo o se solicitó la opción 99
        if (dispositivo.modelo == "infrarrojo" || op == 99) {
            Log.d("MiHogar-Control", "Abriendo mando Infrarrojo para devId: $devId")
            val intent = Intent(this, Activity_IrCategorias::class.java)
            intent.putExtra("devId", devId)
            startActivity(intent)
            return
        }

        if (op == 0) {
            // Long click: Diálogo con detalles y opción de Infrarrojo / Copiar ID
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle(dispositivo.nombre)
            dialogBuilder.setMessage("ID Dispositivo:\n$devId\n\nEstado actual: ${if (dispositivo.estado) "ENCENDIDO" else "APAGADO"}")

            dialogBuilder.setPositiveButton("Copiar ID") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("Device ID", devId)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "ID Copiado", Toast.LENGTH_SHORT).show()
            }

            dialogBuilder.setNeutralButton("Infrarrojo") { _, _ ->
                val intent = Intent(this, Activity_IrCategorias::class.java)
                intent.putExtra("devId", devId)
                startActivity(intent)
            }

            dialogBuilder.setNegativeButton("Cerrar", null)
            dialogBuilder.show()
        } else {
            // Short click / Botón ON-OFF: Conmutar Encendido / Apagado (Tomacorriente)
            val mDevice: IThingDevice = ThingHomeSdk.newDeviceInstance(devId)
            val devBean = ThingHomeSdk.getDataInstance().getDeviceBean(devId)

            // Determinar la clave DP correcta (normalmente 1, 20 o 101)
            var dpKey = "1"
            if (devBean?.dps != null) {
                if (devBean.dps.containsKey("1")) dpKey = "1"
                else if (devBean.dps.containsKey("20")) dpKey = "20"
                else if (devBean.dps.containsKey("101")) dpKey = "101"
            }

            val newState = !dispositivo.estado
            dispositivo.estado = newState
            adapterDispositivo?.notifyItemChanged(posicion)

            val jsonCommand = "{\"$dpKey\": $newState}"
            Log.d("MiHogar-Control", "Enviando comando ON/OFF a $devId ($dpKey): $jsonCommand")

            mDevice.publishDps(jsonCommand, object : IResultCallback {
                override fun onError(code: String, error: String) {
                    Log.e("MiHogar-Control", "Error enviando comando ON/OFF: $code - $error")
                    runOnUiThread {
                        dispositivo.estado = !newState // Revertir si hubo error
                        adapterDispositivo?.notifyItemChanged(posicion)
                        Toast.makeText(applicationContext, "Error al cambiar estado: $error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onSuccess() {
                    Log.i("MiHogar-Control", "¡Estado cambiado exitosamente a $newState!")
                    runOnUiThread {
                        val estadoStr = if (newState) "ENCENDIDO" else "APAGADO"
                        Toast.makeText(applicationContext, "${dispositivo.nombre}: $estadoStr", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
}
