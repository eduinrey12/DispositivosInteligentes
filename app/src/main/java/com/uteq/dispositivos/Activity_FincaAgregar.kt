package com.uteq.dispositivos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import java.util.Collections

class Activity_FincaAgregar : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finca_agregar)

        val txtNombre: TextInputEditText = findViewById(R.id.txtNombreNuevaFinca)
        val btnCrear: Button = findViewById(R.id.btnCrearFinca)

        btnCrear.setOnClickListener {
            val nombre = txtNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                txtNombre.error = "Ingrese un nombre"
                return@setOnClickListener
            }

            ThingHomeSdk.getHomeManagerInstance().createHome(
                nombre,
                0.0,
                0.0,
                "",
                Collections.emptyList(),
                object : IThingHomeResultCallback {
                    override fun onSuccess(homeBean: HomeBean?) {
                        Toast.makeText(this@Activity_FincaAgregar, "Espacio Creado", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@Activity_FincaAgregar, Activity_Fincas::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }

                    override fun onError(errorCode: String?, errorMsg: String?) {
                        Toast.makeText(this@Activity_FincaAgregar, "Error: \$errorMsg", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}
