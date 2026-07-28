package com.uteq.dispositivos

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.uteq.dispositivos.ApiService.ApiUrl
import com.uteq.dispositivos.ApiService.ApiUsuario
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Collections

class Activity_IniciarSesion : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_sesion)

        val language = arrayOf("aC", "aC++", "aJava", "a.NET", "iPhone", "Android", "ASP.NET", "PHP")
        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, language)
        
        val actv: AutoCompleteTextView = findViewById(R.id.txtAutoProbar)
        actv.threshold = 1
        actv.setAdapter(adapter)
        actv.setTextColor(Color.RED)

        val retrofit = Retrofit.Builder()
            .baseUrl(ApiUrl.urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiUsuario::class.java)

        val txtProbar: TextView = findViewById(R.id.txtProbar)
        val txtCorreo: TextView = findViewById(R.id.txtCorreo)
        val txtClave: TextView = findViewById(R.id.txtClave)
        val correoTextInputLayout: TextInputLayout = findViewById(R.id.correo_text_input_layout)
        val claveTextInputLayout: TextInputLayout = findViewById(R.id.clave_text_input_layout)
        val btnIniciar: Button = findViewById(R.id.btnIniciarSesion)

        btnIniciar.setOnClickListener {
            if (txtCorreo.text.toString().isEmpty() || txtClave.text.toString().isEmpty()) {
                if (txtCorreo.text.toString().isEmpty()) {
                    correoTextInputLayout.error = "El campo no puede estar vacio"
                    correoTextInputLayout.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                } else {
                    correoTextInputLayout.error = null
                    correoTextInputLayout.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#007224"))
                }
                
                if (txtClave.text.toString().isEmpty()) {
                    claveTextInputLayout.error = "El campo no puede estar vacio"
                    claveTextInputLayout.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                } else {
                    claveTextInputLayout.error = null
                    claveTextInputLayout.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#007224"))
                }
            } else {
                ThingHomeSdk.getUserInstance().loginWithEmail(
                    "593",
                    txtCorreo.text.toString(),
                    txtClave.text.toString(),
                    object : ILoginCallback {
                        override fun onSuccess(user: User) {
                            ThingHomeSdk.getHomeManagerInstance().queryHomeList(object : IThingGetHomeListCallback {
                                override fun onSuccess(homeBeans: List<HomeBean>?) {
                                    if (homeBeans != null && homeBeans.isNotEmpty()) {
                                        val homeId = homeBeans[0].homeId
                                        Toast.makeText(applicationContext, "Bienvenido", Toast.LENGTH_LONG).show()
                                        val intent = Intent(applicationContext, Activity_Facultades::class.java).apply {
                                            putExtra("id_cliente", 1)
                                            putExtra("usuario", "Usuario Prueba")
                                            putExtra("correo", txtCorreo.text.toString())
                                            putExtra("homeId", homeId)
                                        }
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(applicationContext, "Creando un Hogar por defecto...", Toast.LENGTH_LONG).show()
                                        ThingHomeSdk.getHomeManagerInstance().createHome(
                                            "Mi Hogar",
                                            0.0,
                                            0.0,
                                            "",
                                            Collections.singletonList("Sala"),
                                            object : IThingHomeResultCallback {
                                                override fun onSuccess(homeBean: HomeBean) {
                                                    Toast.makeText(applicationContext, "Hogar creado. Bienvenido", Toast.LENGTH_LONG).show()
                                                    val homeId = homeBean.homeId
                                                    val intent = Intent(applicationContext, Activity_Facultades::class.java).apply {
                                                        putExtra("id_cliente", 1)
                                                        putExtra("usuario", "Usuario Prueba")
                                                        putExtra("correo", txtCorreo.text.toString())
                                                        putExtra("homeId", homeId)
                                                    }
                                                    startActivity(intent)
                                                }

                                                override fun onError(errorCode: String, errorMsg: String) {
                                                    Toast.makeText(applicationContext, "Error creando hogar: \$errorMsg", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        )
                                    }
                                }

                                override fun onError(errorCode: String, errorMsg: String) {
                                    Toast.makeText(applicationContext, "Error Home: \$errorMsg", Toast.LENGTH_LONG).show()
                                }
                            })
                        }

                        override fun onError(code: String, error: String) {
                            android.util.Log.e("TuyaLoginError", "Code: \$code | Error: \$error")
                            Toast.makeText(applicationContext, "code: \$code\nerror: \$error", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }
}
