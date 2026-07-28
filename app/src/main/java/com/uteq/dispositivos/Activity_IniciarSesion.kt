package com.uteq.dispositivos

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.thingclips.smart.android.user.api.ILoginCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback

class Activity_IniciarSesion : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_sesion)

        val txtCorreo: TextView = findViewById(R.id.txtCorreo)
        val txtClave: TextView = findViewById(R.id.txtClave)
        val correoTextInputLayout: TextInputLayout = findViewById(R.id.correo_text_input_layout)
        val claveTextInputLayout: TextInputLayout = findViewById(R.id.clave_text_input_layout)
        val btnIniciar: Button = findViewById(R.id.btnIniciarSesion)

        btnIniciar.setOnClickListener {
            if (txtCorreo.text.toString().isEmpty() || txtClave.text.toString().isEmpty()) {
                // UI feedback
            } else {
                ThingHomeSdk.getUserInstance().loginWithEmail(
                    "593",
                    txtCorreo.text.toString(),
                    txtClave.text.toString(),
                    object : ILoginCallback {
                        override fun onSuccess(user: User) {
                            // Check shared preferences
                            val prefs = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE)
                            val lastFincaId = prefs.getLong("last_finca_id", 0L)

                            if (lastFincaId != 0L) {
                                val intent = Intent(applicationContext, Activity_FincaDetalle::class.java)
                                intent.putExtra("homeId", lastFincaId)
                                startActivity(intent)
                                finish()
                            } else {
                                // No previous finca, check list
                                ThingHomeSdk.getHomeManagerInstance().queryHomeList(object : IThingGetHomeListCallback {
                                    override fun onSuccess(homeBeans: List<HomeBean>?) {
                                        if (homeBeans != null && homeBeans.isNotEmpty()) {
                                            val intent = Intent(applicationContext, Activity_Fincas::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            val intent = Intent(applicationContext, Activity_FincaAgregar::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }

                                    override fun onError(errorCode: String, errorMsg: String) {
                                        Toast.makeText(applicationContext, "Error Home: \$errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                })
                            }
                        }

                        override fun onError(code: String, error: String) {
                            Toast.makeText(applicationContext, "Error: \$error", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }
}
