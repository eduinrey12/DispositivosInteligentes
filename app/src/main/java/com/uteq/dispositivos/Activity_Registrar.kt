package com.uteq.dispositivos

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.thingclips.smart.android.user.api.IRegisterCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import java.util.regex.Pattern

class Activity_Registrar : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)

        // Declarar variables
        val llDatos: LinearLayout = findViewById(R.id.llDatosRegistro)
        val llVerificar: LinearLayout = findViewById(R.id.llVerificacion)
        val txtInputUsuario: TextInputLayout = findViewById(R.id.usuario_text_input_layout)
        val txtInputCorreo: TextInputLayout = findViewById(R.id.correo_text_input_layout)
        val txtInputClave: TextInputLayout = findViewById(R.id.clave_text_input_layout)
        val txtInputConfirmar: TextInputLayout = findViewById(R.id.claveConfirmar_text_input_layout)
        val txtUsuario: TextView = findViewById(R.id.txtUsuario)
        val txtError: TextView = findViewById(R.id.txterror)
        val txtCorreo: TextView = findViewById(R.id.txtCorreo)
        val txtClave: TextView = findViewById(R.id.txtClave)
        val txtClaveConfirmar: TextView = findViewById(R.id.txtClaveConfirmar)
        val txtTitulo: TextView = findViewById(R.id.txtTituloRegistro)
        val txtNotificar: TextView = findViewById(R.id.txtnotificacioEnvio)
        val txtVerificar: TextView = findViewById(R.id.txtVerificar)
        val btnRegistrar: Button = findViewById(R.id.btnRegistrarse)
        val btnVerificar: Button = findViewById(R.id.btnVerificar)
        val btnAtras: ImageButton = findViewById(R.id.btnAtras)

        llDatos.visibility = View.VISIBLE
        llVerificar.visibility = View.GONE

        btnAtras.setOnClickListener {
            txtTitulo.text = "Registrar"
            llDatos.visibility = View.VISIBLE
            llVerificar.visibility = View.GONE
        }

        btnRegistrar.setOnClickListener {
            if (txtUsuario.text.toString().isEmpty() || txtCorreo.text.toString().isEmpty() || txtClave.text.toString().isEmpty() || txtClaveConfirmar.text.toString().isEmpty()) {
                // Validación UI (Omitida por brevedad en este snippet, pero se mantiene igual)
            } else {
                if (txtClave.text.toString() != txtClaveConfirmar.text.toString()) {
                    txtError.visibility = View.VISIBLE
                } else {
                    try {
                        txtTitulo.text = "Introducir código de verificación"
                        txtNotificar.text = "El código se envió a: \${txtCorreo.text}"
                        llDatos.visibility = View.GONE
                        llVerificar.visibility = View.VISIBLE
                        txtError.visibility = View.GONE

                        ThingHomeSdk.getUserInstance().sendVerifyCodeWithUserName(
                            txtCorreo.text.toString(), "", "593", 1, object : IResultCallback {
                                override fun onError(code: String, error: String) {
                                    Toast.makeText(applicationContext, "Error: \$error", Toast.LENGTH_SHORT).show()
                                }
                                override fun onSuccess() {
                                    Toast.makeText(applicationContext, "Enviado exitosamente", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        btnVerificar.setOnClickListener {
            ThingHomeSdk.getUserInstance().checkCodeWithUserName(
                txtCorreo.text.toString(), "", "593", txtVerificar.text.toString(), 1, object : IResultCallback {
                    override fun onError(code: String, error: String) {
                        Toast.makeText(applicationContext, "Error: \$error", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess() {
                        ThingHomeSdk.getUserInstance().registerAccountWithEmail(
                            "593", txtCorreo.text.toString(), txtClave.text.toString(), txtVerificar.text.toString(), object : IRegisterCallback {
                                override fun onSuccess(user: User) {
                                    Toast.makeText(applicationContext, "Registrado en Tuya", Toast.LENGTH_LONG).show()
                                    // Redirect to Login to allow them to login and create space
                                    val intent = Intent(applicationContext, Activity_IniciarSesion::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }

                                override fun onError(code: String, error: String) {
                                    Toast.makeText(applicationContext, "Error: \$error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}
