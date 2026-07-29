package com.uteq.dispositivos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.thingclips.smart.android.ble.api.BleScanResponse
import com.thingclips.smart.android.ble.api.LeScanSetting
import com.thingclips.smart.android.ble.api.ScanDeviceBean
import com.thingclips.smart.android.ble.api.ScanType
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder
import com.thingclips.smart.sdk.api.IMultiModeActivatorListener
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.api.IMultiModeActivator
import com.thingclips.smart.sdk.api.IThingActivator
import com.thingclips.smart.sdk.api.IThingActivatorGetToken
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.sdk.bean.MultiModeActivatorBean
import com.thingclips.smart.sdk.enums.ActivatorModelEnum
import com.uteq.dispositivos.Adaptador.ScannedDeviceAdapter

class Activity_DispositivoAgregar : AppCompatActivity() {

    private var paso = 1
    private var devId = ""
    private var homeId = 0L

    // UI elements needed globally for permission callback
    private lateinit var btnContinuarGlobal: Button
    private lateinit var animationViewGlobal: LottieAnimationView
    private lateinit var txtpasoGlobal: TextView
    private lateinit var txtpasoSubGlobal: TextView
    private lateinit var imagenGlobal: ImageView
    private lateinit var txtssidGlobal: TextView
    private lateinit var txtclaveGlobal: TextView
    private lateinit var txtCategoriaGlobal: TextView
    private lateinit var llEscanearGlobal: LinearLayout
    private lateinit var txtNombre: TextView
    private lateinit var rvScannedDevicesGlobal: RecyclerView
    private lateinit var scannedDeviceAdapter: ScannedDeviceAdapter

    private var mThingActivator: IThingActivator? = null

    private var scannedDeviceBeanGlobal: ScanDeviceBean? = null
    private var currentTokenGlobal = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispositivo_agregar)

        homeId = intent.getLongExtra("homeId", 0L)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        val btnContinuar = findViewById<Button>(R.id.btnContinuar)
        val btnAtras = findViewById<ImageButton>(R.id.btnAtras)
        val txtpaso = findViewById<TextView>(R.id.txtvPasoTitulo)
        val txtpasoSub = findViewById<TextView>(R.id.txtvPasoSubTitulo)
        val txtssid = findViewById<TextView>(R.id.txtssid)
        val txtclave = findViewById<TextView>(R.id.txtClave)
        val txtCategoria = findViewById<TextView>(R.id.txtdevid)
        txtNombre = findViewById(R.id.txtNombre)
        val llWifi = findViewById<LinearLayout>(R.id.llDatosWifi)
        val llEscanear = findViewById<LinearLayout>(R.id.llReiniciarDispo)
        val imagen = findViewById<ImageView>(R.id.imgRegistro)
        val rvScannedDevices = findViewById<RecyclerView>(R.id.rvScannedDevices)

        val animationView = findViewById<LottieAnimationView>(R.id.animationView)
        animationView.setAnimation(R.raw.carga)
        animationView.loop(true)
        animationView.playAnimation()

        llEscanear.visibility = View.GONE
        animationView.visibility = View.GONE
        btnAtras.visibility = View.GONE
        rvScannedDevices.visibility = View.GONE
        llWifi.visibility = View.VISIBLE
        imagen.visibility = View.VISIBLE

        // Auto-fill SSID
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        if (wifiManager != null) {
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo?.ssid != null && wifiInfo.ssid.isNotEmpty()) {
                val ssid = wifiInfo.ssid.replace("\"", "")
                if (ssid != "<unknown ssid>") {
                    txtssid.text = ssid
                }
            }
        }

        btnContinuarGlobal = btnContinuar
        animationViewGlobal = animationView
        txtpasoGlobal = txtpaso
        txtpasoSubGlobal = txtpasoSub
        imagenGlobal = imagen
        txtssidGlobal = txtssid
        txtclaveGlobal = txtclave
        txtCategoriaGlobal = txtCategoria
        llEscanearGlobal = llEscanear
        rvScannedDevicesGlobal = rvScannedDevices

        scannedDeviceAdapter = ScannedDeviceAdapter { device ->
            // Cuando se toca un dispositivo en la lista
            scannedDeviceBeanGlobal = device
            ThingHomeSdk.getBleOperator().stopLeScan()
            
            runOnUiThread {
                rvScannedDevicesGlobal.visibility = View.GONE
                txtpasoGlobal.text = "Dispositivo seleccionado"
                txtpasoSubGlobal.text = "Asigna un nombre al dispositivo y presiona Agregar."
                
                val devName = if (device.name.isNullOrEmpty() || device.name!!.startsWith("key") || device.name!!.length > 15) {
                    if (!device.productId.isNullOrEmpty()) "Producto: ${device.productId}" else "Dispositivo Tuya"
                } else {
                    device.name
                }
                txtCategoriaGlobal.text = devName
                imagenGlobal.visibility = View.GONE
                llEscanearGlobal.visibility = View.VISIBLE
                btnContinuarGlobal.visibility = View.VISIBLE
                btnContinuarGlobal.text = "Agregar"
                paso = 4
            }
        }
        
        rvScannedDevices.layoutManager = LinearLayoutManager(this)
        rvScannedDevices.adapter = scannedDeviceAdapter

        btnContinuar.setOnClickListener {
            if (paso == 1) {
                if (txtssid.text.toString().trim().isEmpty() || txtclave.text.toString().trim().isEmpty()) {
                    Toast.makeText(this, "Por favor, ingrese el SSID y la clave WiFi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                txtssidGlobal.text = txtssid.text.toString()
                txtclaveGlobal.text = txtclave.text.toString()
                scannedDeviceBeanGlobal = null
                scannedDeviceAdapter.clearDevices()

                btnAtras.visibility = View.VISIBLE
                txtpaso.text = "Reinicie el dispositivo"
                txtpasoSub.text = "Mantenga pulsado el botón REINICIAR durante 5 segundos hasta que el indicador parpadee"
                imagen.setImageResource(R.drawable.breakewifi)
                imagen.visibility = View.VISIBLE
                llWifi.visibility = View.GONE
                rvScannedDevices.visibility = View.GONE
                btnContinuar.text = "Continuar"
                paso = 2
            } else if (paso == 2) {
                scannedDeviceBeanGlobal = null
                scannedDeviceAdapter.clearDevices()

                txtpaso.text = "Buscando dispositivos..."
                txtpasoSub.text = "Si tu dispositivo aparece abajo, tócalo. De lo contrario, pulsa Continuar."
                animationView.visibility = View.GONE
                imagen.visibility = View.GONE
                llWifi.visibility = View.GONE
                llEscanear.visibility = View.GONE
                rvScannedDevices.visibility = View.VISIBLE
                btnContinuar.visibility = View.VISIBLE
                btnContinuar.text = "Continuar"
                paso = 3

                checkPermissionsAndScan()
                btnAtras.visibility = View.VISIBLE
            } else if (paso == 3) {
                // Usuario no seleccionó un dispositivo de la lista y dio clic a Continuar (Modo EZ directo)
                scannedDeviceBeanGlobal = null
                ThingHomeSdk.getBleOperator().stopLeScan()

                rvScannedDevices.visibility = View.GONE
                txtpaso.text = "Asignar Nombre"
                txtpasoSub.text = "Asigne un nombre a su nuevo dispositivo y presione Agregar."
                txtCategoria.text = "Dispositivo WiFi"
                imagen.visibility = View.GONE
                llEscanear.visibility = View.VISIBLE
                btnContinuar.visibility = View.VISIBLE
                btnContinuar.text = "Agregar"
                paso = 4
            } else if (paso == 4) {
                // Agregar dispositivo
                val nombreIngresado = txtNombre.text.toString().trim()
                if (nombreIngresado.isEmpty()) {
                    txtNombre.error = "Por favor ingrese un nombre"
                    return@setOnClickListener
                }
                
                txtpaso.text = "Vinculando..."
                txtpasoSub.text = "Conectando dispositivo a la nube de Tuya..."
                txtNombre.isEnabled = false
                animationView.visibility = View.GONE
                imagenGlobal.visibility = View.GONE
                llEscanear.visibility = View.GONE
                btnContinuar.visibility = View.GONE
                btnContinuarGlobal.visibility = View.GONE

                iniciarVinculacionConTokenFresco(nombreIngresado)
            }
        }

        btnAtras.setOnClickListener {
            if (paso == 2) {
                btnAtras.visibility = View.GONE
                txtpaso.text = "Selecciona una red WiFi"
                txtpasoSub.text = "Necesitamos detalles de la red WiFi. Completa la información para continuar"
                imagen.setImageResource(R.drawable.no_connection_pana)
                imagen.visibility = View.VISIBLE
                llWifi.visibility = View.VISIBLE
                rvScannedDevices.visibility = View.GONE
                btnContinuar.text = "Continuar"
                paso = 1
            } else if (paso == 3) {
                ThingHomeSdk.getBleOperator().stopLeScan()
                btnAtras.visibility = View.VISIBLE
                txtpaso.text = "Reinicie el dispositivo"
                txtpasoSub.text = "Mantenga pulsado el botón REINICIAR durante 5 segundos hasta que el indicador parpadee"
                imagen.setImageResource(R.drawable.breakewifi)
                imagen.visibility = View.VISIBLE
                llWifi.visibility = View.GONE
                rvScannedDevices.visibility = View.GONE
                llEscanear.visibility = View.GONE
                btnContinuar.visibility = View.VISIBLE
                btnContinuar.text = "Continuar"
                paso = 2
            } else if (paso == 4) {
                btnAtras.visibility = View.VISIBLE
                scannedDeviceBeanGlobal = null
                txtpaso.text = "Buscando dispositivos..."
                txtpasoSub.text = "Si tu dispositivo aparece abajo, tócalo. De lo contrario, pulsa Continuar."
                imagen.visibility = View.GONE
                llWifi.visibility = View.GONE
                llEscanear.visibility = View.GONE
                rvScannedDevices.visibility = View.VISIBLE
                btnContinuar.visibility = View.VISIBLE
                btnContinuar.text = "Continuar"
                paso = 3
                checkPermissionsAndScan()
            }
        }
    }

    private fun checkPermissionsAndScan() {
        val hasBluetoothScan = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        val hasBluetoothConnect = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        val hasLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasBluetoothScan || !hasBluetoothConnect || !hasLocation) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 100
                )
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ), 100
                )
            }
        } else {
            startDeviceScanning()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            startDeviceScanning()
        }
    }

    private fun startDeviceScanning() {
        scannedDeviceBeanGlobal = null
        scannedDeviceAdapter.clearDevices()

        val scanSetting = LeScanSetting.Builder()
            .setTimeout(120000)
            .addScanType(ScanType.SINGLE)
            .addScanType(ScanType.MESH)
            .addScanType(ScanType.SIG_MESH)
            .build()

        val discoveredMacs = mutableSetOf<String>()

        Log.d("MiHogar-Escaneo", "Iniciando escaneo BLE en tiempo real...")

        ThingHomeSdk.getBleOperator().startLeScan(scanSetting, object : BleScanResponse {
            override fun onResult(bean: ScanDeviceBean) {
                val uniqueId = bean.uuid?.ifEmpty { null } 
                    ?: bean.address?.ifEmpty { null } 
                    ?: bean.mac?.ifEmpty { null } 
                    ?: bean.productId?.ifEmpty { null } 
                    ?: return

                Log.d("MiHogar-Escaneo", ">>> Dispositivo BLE encontrado en tiempo real: Name=${bean.name}, MAC=${bean.mac}, UUID=${bean.uuid}, ProductId=${bean.productId}")

                if (discoveredMacs.add(uniqueId)) {
                    runOnUiThread {
                        if (scannedDeviceAdapter.itemCount == 0) {
                            txtpasoGlobal.text = "Dispositivos encontrados"
                            txtpasoSubGlobal.text = "Toca el dispositivo en la lista o pulsa Continuar para vincular por WiFi."
                        }
                        scannedDeviceAdapter.addDevice(bean)
                    }
                }
            }
        })
    }

    private fun iniciarVinculacionConTokenFresco(customName: String) {
        Log.d("MiHogar-Vinculacion", "Solicitando token fresco a la nube Tuya...")
        ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId, object : IThingActivatorGetToken {
            override fun onSuccess(token: String) {
                Log.d("MiHogar-Vinculacion", "Token fresco obtenido exitosamente: $token")
                currentTokenGlobal = token

                val scannedDevice = scannedDeviceBeanGlobal
                if (scannedDevice != null && !scannedDevice.uuid.isNullOrEmpty()) {
                    Log.d("MiHogar-Vinculacion", "Iniciando vinculación para dispositivo BLE seleccionado: ${scannedDevice.uuid}")
                    
                    val builder = ActivatorBuilder()
                        .setSsid(txtssidGlobal.text.toString())
                        .setContext(this@Activity_DispositivoAgregar)
                        .setPassword(txtclaveGlobal.text.toString())
                        .setActivatorModel(ActivatorModelEnum.THING_EZ)
                        .setTimeOut(120)
                        .setToken(token)
                        .setListener(object : IThingSmartActivatorListener {
                            override fun onError(errorCode: String, errorMsg: String) {
                                Log.w("MiHogar-Vinculacion", "Error al vincular dispositivo BLE ($errorCode: $errorMsg). Intentando Modo EZ universal...")
                                iniciarModoEZ(token, customName)
                            }

                            override fun onActiveSuccess(devResp: DeviceBean) {
                                Log.i("MiHogar-Vinculacion", "¡Éxito! Dispositivo vinculado: ${devResp.devId}")
                                finalizarVinculacion(devResp.devId, customName)
                            }

                            override fun onStep(step: String, data: Any) {
                                Log.d("MiHogar-Vinculacion", "Paso BLE: $step - $data")
                            }
                        })

                    mThingActivator = ThingHomeSdk.getActivatorInstance().newActivator(builder)
                    mThingActivator?.start()
                } else {
                    Log.d("MiHogar-Vinculacion", "No hay dispositivo BLE seleccionado. Iniciando Modo EZ (SmartConfig)...")
                    iniciarModoEZ(token, customName)
                }
            }

            override fun onFailure(s: String, s1: String) {
                Log.e("MiHogar-Vinculacion", "Error al obtener token: $s1")
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error al conectar con Tuya Cloud: $s1", Toast.LENGTH_LONG).show()
                    restablecerUIParaReintento()
                }
            }
        })
    }

    private fun iniciarModoEZ(token: String, customName: String) {
        val builder = ActivatorBuilder()
            .setSsid(txtssidGlobal.text.toString())
            .setContext(this@Activity_DispositivoAgregar)
            .setPassword(txtclaveGlobal.text.toString())
            .setActivatorModel(ActivatorModelEnum.THING_EZ)
            .setTimeOut(120)
            .setToken(token)
            .setListener(object : IThingSmartActivatorListener {
                override fun onError(errorCode: String, errorMsg: String) {
                    Log.e("MiHogar-Vinculacion", "Error al vincular en Modo EZ: $errorCode - $errorMsg")
                    runOnUiThread {
                        val alertMsg = "Error al vincular ($errorCode).\n\nVerifica:\n1) Tu WiFi sea de 2.4GHz.\n2) La clave sea correcta.\n3) Si el dispositivo ya estuvo registrado antes, elimínalo primero de la app."
                        Toast.makeText(applicationContext, alertMsg, Toast.LENGTH_LONG).show()
                        restablecerUIParaReintento()
                    }
                }

                override fun onActiveSuccess(devResp: DeviceBean) {
                    Log.i("MiHogar-Vinculacion", "¡Éxito Modo EZ! Dispositivo vinculado: ${devResp.devId}")
                    finalizarVinculacion(devResp.devId, customName)
                }

                override fun onStep(step: String, data: Any) {
                    Log.d("MiHogar-Vinculacion", "Paso Modo EZ: $step - $data")
                }
            })

        mThingActivator = ThingHomeSdk.getActivatorInstance().newActivator(builder)
        mThingActivator?.start()
    }

    private fun finalizarVinculacion(deviceId: String, customName: String) {
        runOnUiThread {
            txtpasoGlobal.text = "Guardando..."
            txtpasoSubGlobal.text = "Configuración completada exitosamente."
        }

        if (customName.isNotEmpty()) {
            ThingHomeSdk.newDeviceInstance(deviceId).renameDevice(customName, object : IResultCallback {
                override fun onError(code: String, error: String) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Dispositivo guardado exitosamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onSuccess() {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "¡Dispositivo guardado exitosamente!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            })
        } else {
            runOnUiThread {
                Toast.makeText(applicationContext, "¡Dispositivo guardado exitosamente!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun restablecerUIParaReintento() {
        txtNombre.isEnabled = true
        animationViewGlobal.visibility = View.GONE
        llEscanearGlobal.visibility = View.VISIBLE
        btnContinuarGlobal.isEnabled = true
        btnContinuarGlobal.visibility = View.VISIBLE
        btnContinuarGlobal.text = "Reintentar"
        txtpasoGlobal.text = "Asignar Nombre"
        txtpasoSubGlobal.text = "Asigne un nombre a su nuevo dispositivo e intente de nuevo."
    }

    override fun onDestroy() {
        super.onDestroy()
        mThingActivator?.stop()
        ThingHomeSdk.getBleOperator().stopLeScan()
    }
}
