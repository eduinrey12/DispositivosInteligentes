package com.uteq.dispositivos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thingclips.smart.android.user.api.ILogoutCallback
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback

class Activity_Fincas : AppCompatActivity() {

    private lateinit var rcvFincas: RecyclerView
    private var adapterFinca: FincaAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fincas)

        rcvFincas = findViewById(R.id.rcvFincas)
        rcvFincas.layoutManager = LinearLayoutManager(this)

        val btnAgregarFinca: Button = findViewById(R.id.btnAgregarFinca)
        btnAgregarFinca.setOnClickListener {
            startActivity(Intent(this, Activity_FincaAgregar::class.java))
        }

        val btnCerrarSesion: ImageButton = findViewById(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            ThingHomeSdk.getUserInstance().logout(object : ILogoutCallback {
                override fun onSuccess() {
                    // Clear preferences
                    val prefs = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()
                    
                    val intent = Intent(this@Activity_Fincas, Activity_IniciarSesion::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                override fun onError(code: String?, error: String?) {
                    Toast.makeText(this@Activity_Fincas, "Error: \$error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        cargarFincas()
    }

    private fun cargarFincas() {
        ThingHomeSdk.getHomeManagerInstance().queryHomeList(object : IThingGetHomeListCallback {
            override fun onSuccess(homeBeans: List<HomeBean>?) {
                if (homeBeans != null) {
                    adapterFinca = FincaAdapter(homeBeans) { homeId ->
                        // Save preference
                        val prefs = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE)
                        prefs.edit().putLong("last_finca_id", homeId).apply()

                        // Go to Detalle
                        val intent = Intent(this@Activity_Fincas, Activity_FincaDetalle::class.java)
                        intent.putExtra("homeId", homeId)
                        startActivity(intent)
                    }
                    rcvFincas.adapter = adapterFinca
                }
            }

            override fun onError(errorCode: String?, errorMsg: String?) {
                Toast.makeText(this@Activity_Fincas, "Error: \$errorMsg", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
