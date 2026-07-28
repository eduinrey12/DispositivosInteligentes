package com.uteq.dispositivos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.thingclips.smart.home.sdk.ThingHomeSdk

class Activity_Bienvenida : AppCompatActivity() {

    private val context: Context = this

    override fun onDestroy() {
        super.onDestroy()
        ThingHomeSdk.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.LOCATION_HARDWARE,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.CONTROL_LOCATION_UPDATES,
            Manifest.permission.INSTALL_LOCATION_PROVIDER,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.OVERRIDE_WIFI_CONFIG,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
            Manifest.permission.MANAGE_WIFI_INTERFACES,
            Manifest.permission.MANAGE_WIFI_NETWORK_SELECTION,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.BIND_COMPANION_DEVICE_SERVICE,
            Manifest.permission.UPDATE_DEVICE_STATS,
            Manifest.permission.BIND_MIDI_DEVICE_SERVICE,
            Manifest.permission.REQUEST_OBSERVE_COMPANION_DEVICE_PRESENCE,
            Manifest.permission.USE_ICC_AUTH_WITH_DEVICE_IDENTIFIER,
            Manifest.permission.BIND_DEVICE_ADMIN
        )

        val ungrantedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }

        val btnIniciar = findViewById<Button>(R.id.btnIniciarSesion)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrarse)

        btnIniciar.setOnClickListener {
            val intent = Intent(this@Activity_Bienvenida, Activity_IniciarSesion::class.java)
            startActivity(intent)
        }

        btnRegistrar.setOnClickListener {
            val intent = Intent(this@Activity_Bienvenida, Activity_Registrar::class.java)
            startActivity(intent)
        }
    }
}
