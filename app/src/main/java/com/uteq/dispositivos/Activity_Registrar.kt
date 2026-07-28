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
import com.thingclips.sdk.home.bean.InviteMessageBean
import com.thingclips.smart.android.user.api.ILoginCallback
import com.thingclips.smart.android.user.api.ILogoutCallback
import com.thingclips.smart.android.user.api.IRegisterCallback
import com.thingclips.smart.android.user.bean.User
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.api.IThingDataCallback
import java.util.regex.Pattern

class Activity_Registrar : AppCompatActivity() {

    private var code: String = ""

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

        ThingHomeSdk.getUserInstance().loginWithEmail(
            "593", "ereyb@uteq.edu.ec", "12345", object : ILoginCallback {
                override fun onSuccess(user: User) {
                    ThingHomeSdk.getMemberInstance().getInvitationMessage(190971144, object : IThingDataCallback<InviteMessageBean> {
                        override fun onSuccess(result: InviteMessageBean) {
                            Log.i("Codigo: ", result.invitationCode)
                            code = result.invitationCode
                            ThingHomeSdk.getUserInstance().logout(object : ILogoutCallback {
                                override fun onSuccess() {}
                                override fun onError(errorCode: String, errorMsg: String) {}
                            })
                        }

                        override fun onError(errorCode: String, errorMessage: String) {
                            Toast.makeText(applicationContext, "code: \$errorCode error:\$errorMessage", Toast.LENGTH_SHORT).show()
                            Log.i("Error: ", errorMessage)
                        }
                    })
                }

                override fun onError(code: String, error: String) {
                    Toast.makeText(applicationContext, "code: \$code error:\$error", Toast.LENGTH_SHORT).show()
                }
            }
        )

        btnAtras.setOnClickListener {
            txtTitulo.text = "Registrar"
            llDatos.visibility = View.VISIBLE
            llVerificar.visibility = View.GONE
        }

        btnRegistrar.setOnClickListener {
            if (txtUsuario.text.toString().isEmpty() || txtCorreo.text.toString().isEmpty() || txtClave.text.toString().isEmpty() || txtClaveConfirmar.text.toString().isEmpty()) {
                if (txtUsuario.text.toString().isEmpty()) {
                    txtInputUsuario.error = "El campo no puede estar vacio"
                    txtInputUsuario.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                } else {
                    txtInputUsuario.error = null
                    txtInputUsuario.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#007224"))
                    txtInputUsuario.isErrorEnabled = false
                }

                if (txtCorreo.text.toString().isEmpty()) {
                    txtInputCorreo.error = "El campo no puede estar vacio"
                    txtInputCorreo.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                } else {
                    txtInputCorreo.error = null
                    txtInputCorreo.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#007224"))
                    txtInputCorreo.isErrorEnabled = false
                }

                if (txtClave.text.toString().isEmpty()) {
                    txtInputClave.error = "El campo no puede estar vacio"
                    txtInputClave.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                } else {
                    txtInputClave.error = null
                    txtInputClave.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#007224"))
                    txtInputClave.isErrorEnabled = false
                }

                if (txtClaveConfirmar.text.toString().isEmpty()) {
                    txtInputConfirmar.error = "El campo no puede estar vacio"
                    txtInputConfirmar.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                } else {
                    txtInputConfirmar.error = null
                    txtInputConfirmar.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#007224"))
                    txtInputConfirmar.isErrorEnabled = false
                }
            } else {
                if (txtClave.text.toString() != txtClaveConfirmar.text.toString()) {
                    txtError.visibility = View.VISIBLE
                } else {
                    try {
                        txtTitulo.text = "Introducir código de verificación"
                        txtNotificar.text = "El código de verificación se ha enviado a su correo: \${txtCorreo.text}"
                        llDatos.visibility = View.GONE
                        llVerificar.visibility = View.VISIBLE
                        txtError.visibility = View.GONE

                        ThingHomeSdk.getUserInstance().sendVerifyCodeWithUserName(
                            txtCorreo.text.toString(), "", "593", 1, object : IResultCallback {
                                override fun onError(code: String, error: String) {
                                    Toast.makeText(applicationContext, "code: \$code error:\$error", Toast.LENGTH_SHORT).show()
                                }

                                override fun onSuccess() {
                                    Toast.makeText(applicationContext, "El código de verificación se envio exitosamente.", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(applicationContext, "code: \$code error:\$error", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess() {
                        ThingHomeSdk.getUserInstance().registerAccountWithEmail(
                            "593", txtCorreo.text.toString(), txtClave.text.toString(), txtVerificar.text.toString(), object : IRegisterCallback {
                                override fun onSuccess(user: User) {
                                    try {
                                        ThingHomeSdk.getHomeManagerInstance().joinHomeByInviteCode(
                                            code, object : IResultCallback {
                                                override fun onError(code: String, error: String) {
                                                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
                                                    Log.i("Error: ", "\$code, \$error")
                                                    val intent = Intent(applicationContext, Activity_IniciarSesion::class.java)
                                                    startActivity(intent)
                                                }

                                                override fun onSuccess() {
                                                    Toast.makeText(applicationContext, "Se registró correctamente en Tuya", Toast.LENGTH_LONG).show()
                                                    val intent = Intent(applicationContext, Activity_IniciarSesion::class.java)
                                                    startActivity(intent)
                                                }
                                            }
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                override fun onError(code: String, error: String) {
                                    Toast.makeText(applicationContext, "code: \$code error:\$error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            )
        }

        txtCorreo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val expresionRegular = "^[a-zA-Z0-9._%+-]+@(gmail\\.com|uteq\\.edu\\.ec)$"
                val pattern = Pattern.compile(expresionRegular)
                val matcher = pattern.matcher(s.toString())

                if (!matcher.matches()) {
                    txtInputCorreo.error = "Correo no valido."
                    txtInputCorreo.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                } else {
                    txtInputCorreo.error = null
                    txtInputCorreo.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#007224"))
                    txtInputCorreo.isErrorEnabled = false
                }
            }
        })
    }
}
