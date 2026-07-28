package com.uteq.dispositivos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
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
import com.airbnb.lottie.LottieAnimationView
import com.thingclips.smart.android.ble.api.BleScanResponse
import com.thingclips.smart.android.ble.api.LeScanSetting
import com.thingclips.smart.android.ble.api.ScanDeviceBean
import com.thingclips.smart.android.ble.api.ScanType
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder
import com.thingclips.smart.sdk.api.IMultiModeActivatorListener
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.api.IThingActivator
import com.thingclips.smart.sdk.api.IThingActivatorGetToken
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.sdk.bean.MultiModeActivatorBean
import com.thingclips.smart.sdk.enums.ActivatorModelEnum

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

        val animationView = findViewById<LottieAnimationView>(R.id.animationView)
        animationView.setAnimation(R.raw.carga)
        animationView.loop(true)
        animationView.playAnimation()

        llEscanear.visibility = View.GONE
        animationView.visibility = View.GONE
        btnAtras.visibility = View.GONE
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

        btnContinuar.setOnClickListener {
            if (paso == 1) {
                btnAtras.visibility = View.VISIBLE
                txtpaso.text = "Reinicie el dispositivo"
                txtpasoSub.text = "Mantenga pulsado el botón REINICIAR durante 5 segundos hasta que el indicador parpadee"
                imagen.setImageResource(R.drawable.breakewifi)
                llWifi.visibility = View.GONE
                paso = 2
            } else if (paso == 2) {
                txtpaso.text = "Escaneando dispositivo"
                txtpasoSub.text = "Durante este proceso de escaneo no se desconecte del wifi ni apague el Bluetooth. Esto puede tardar varios segundos"
                imagen.visibility = View.GONE
                animationView.visibility = View.VISIBLE
                paso = 0

                checkPermissionsAndScan()
            } else if (paso == 3) {
                if (scannedDeviceBeanGlobal != null) {
                    animationView.visibility = View.VISIBLE
                    llEscanear.visibility = View.GONE
                    txtpaso.text = "Registrando en la nube..."
                    txtpasoSub.text = "Espera un momento mientras se registra el dispositivo."
                    btnContinuar.isEnabled = false

                    val multiBean = MultiModeActivatorBean().apply {
                        ssid = txtssid.text.toString()
                        pwd = txtclave.text.toString()
                        token = currentTokenGlobal
                        uuid = scannedDeviceBeanGlobal!!.uuid
                        mac = scannedDeviceBeanGlobal!!.mac
                        deviceType = scannedDeviceBeanGlobal!!.deviceType
                        address = scannedDeviceBeanGlobal!!.address
                        homeId = this@Activity_DispositivoAgregar.homeId
                        timeout = 60000
                    }

                    val multiActivator = ThingHomeSdk.getActivator().newMultiModeActivator()
                    multiActivator?.startActivator(multiBean, object : IMultiModeActivatorListener {
                        override fun onSuccess(deviceBean: DeviceBean) {
                            var newName = txtNombre.text.toString().trim()
                            if (newName.isEmpty()) {
                                newName = "Dispositivo Inteligente"
                            }
                            ThingHomeSdk.newDeviceInstance(deviceBean.devId).renameDevice(newName, object : IResultCallback {
                                override fun onError(code: String, error: String) {
                                    runOnUiThread {
                                        Toast.makeText(this@Activity_DispositivoAgregar, "Agregado, pero error al renombrar: $error", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }

                                override fun onSuccess() {
                                    runOnUiThread {
                                        Toast.makeText(this@Activity_DispositivoAgregar, "Agregado exitosamente", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }
                            })
                        }

                        override fun onFailure(code: Int, msg: String, handle: Any?) {
                            runOnUiThread {
                                Toast.makeText(this@Activity_DispositivoAgregar, "Error BLE: $msg", Toast.LENGTH_SHORT).show()
                                animationViewGlobal.visibility = View.GONE
                                paso = 3
                                btnContinuarGlobal.isEnabled = true
                                btnContinuarGlobal.text = "Intentar de nuevo"
                            }
                        }
                    })
                } else if (devId.isNotEmpty()) {
                    // This handles EZ mode where device is already added
                    val newName = txtNombre.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        ThingHomeSdk.newDeviceInstance(devId).renameDevice(newName, object : IResultCallback {
                            override fun onError(code: String, error: String) {
                                runOnUiThread {
                                    Toast.makeText(this@Activity_DispositivoAgregar, "Error al renombrar: $error", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }

                            override fun onSuccess() {
                                runOnUiThread {
                                    Toast.makeText(this@Activity_DispositivoAgregar, "Renombrado exitosamente", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                        })
                    } else {
                        Toast.makeText(applicationContext, "Dispositivo añadido en Tuya", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@Activity_DispositivoAgregar, "No hay dispositivo escaneado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnAtras.setOnClickListener {
            if (paso == 2) {
                btnAtras.visibility = View.GONE
                txtpaso.text = "Selecciona una red WiFi"
                txtpasoSub.text = "Necesitamos detalles de la red WiFi. Completa la informacion para continuar"
                imagen.setImageResource(R.drawable.no_connection_pana)
                llWifi.visibility = View.VISIBLE
                btnContinuar.text = "Continuar"
                paso = 1
            } else if (paso == 3) {
                btnAtras.visibility = View.VISIBLE
                txtpaso.text = "Reinicie el dispositivo"
                txtpasoSub.text = "Mantenga pulsado el botón REINICIAR durante 5 segundos hasta que el indicador parpadee"
                imagen.setImageResource(R.drawable.breakewifi)
                llWifi.visibility = View.GONE
                animationView.visibility = View.GONE
                llEscanear.visibility = View.GONE

                paso = 2
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
        ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId, object : IThingActivatorGetToken {
            override fun onSuccess(token: String) {
                currentTokenGlobal = token
                // Iniciar Wifi EZ mode Activator (legacy support)
                val builder = ActivatorBuilder()
                    .setSsid(txtssidGlobal.text.toString())
                    .setContext(applicationContext)
                    .setPassword(txtclaveGlobal.text.toString())
                    .setActivatorModel(ActivatorModelEnum.THING_EZ)
                    .setTimeOut(60)
                    .setToken(token)
                    .setListener(createActivatorListener())

                mThingActivator = ThingHomeSdk.getActivatorInstance().newMultiActivator(builder)
                mThingActivator?.start()

                // BLE Dual Mode Scanning
                val scanSetting = LeScanSetting.Builder()
                    .setTimeout(60000)
                    .addScanType(ScanType.SINGLE)
                    .build()

                ThingHomeSdk.getBleOperator().startLeScan(scanSetting, object : BleScanResponse {
                    override fun onResult(bean: ScanDeviceBean) {
                        // Parar EZ y BLE scan si se encuentra un dispositivo
                        mThingActivator?.stop()
                        ThingHomeSdk.getBleOperator().stopLeScan()
                        scannedDeviceBeanGlobal = bean

                        runOnUiThread {
                            animationViewGlobal.visibility = View.GONE
                            txtpasoGlobal.text = "Dispositivo detectado"
                            txtpasoSubGlobal.text = "Se detectó un dispositivo. Asigna un nombre y presiona Agregar."
                            txtCategoriaGlobal.text = bean.productId ?: "Bluetooth Device"
                            imagenGlobal.visibility = View.VISIBLE
                            imagenGlobal.setImageResource(R.drawable.dispositivo_switch) // default
                            llEscanearGlobal.visibility = View.VISIBLE
                            btnContinuarGlobal.text = "Agregar"
                            paso = 3
                        }
                    }
                })
            }

            override fun onFailure(s: String, s1: String) {
                Toast.makeText(applicationContext, "Error obteniendo token: $s1", Toast.LENGTH_LONG).show()
                animationViewGlobal.visibility = View.GONE
                paso = 2
                btnContinuarGlobal.text = "Escanear de nuevo"
            }
        })
    }

    private fun createActivatorListener(): IThingSmartActivatorListener {
        return object : IThingSmartActivatorListener {
            override fun onError(errorCode: String, errorMsg: String) {
                Toast.makeText(applicationContext, "Error: $errorCode", Toast.LENGTH_LONG).show()
                animationViewGlobal.visibility = View.GONE
                paso = 2
                btnContinuarGlobal.text = "Escanear de nuevo"
            }

            override fun onActiveSuccess(devResp: DeviceBean) {
                ThingHomeSdk.getBleOperator().stopLeScan() // Ensure stopped
                animationViewGlobal.visibility = View.GONE
                txtpasoGlobal.text = "Dispositivo detectado"
                txtpasoSubGlobal.text = "Se detectó un dispositivo. Verifique la información para continuar"
                txtCategoriaGlobal.text = devResp.categoryCode
                devId = devResp.devId
                imagenGlobal.visibility = View.VISIBLE

                if ("wf_ble_cz" == txtCategoriaGlobal.text.toString()) {
                    imagenGlobal.setImageResource(R.drawable.dispositivo_switch)
                } else if ("wf_ble_kg" == txtCategoriaGlobal.text.toString()) {
                    imagenGlobal.setImageResource(R.drawable.dispositivo_touch)
                } else if ("wf_cz" == txtCategoriaGlobal.text.toString()) {
                    imagenGlobal.setImageResource(R.drawable.dispositivo_tomacorriente)
                }
                llEscanearGlobal.visibility = View.VISIBLE
                btnContinuarGlobal.text = "Agregar"
                paso = 3
            }

            override fun onStep(step: String, data: Any) {
            }
        }
    }
}
