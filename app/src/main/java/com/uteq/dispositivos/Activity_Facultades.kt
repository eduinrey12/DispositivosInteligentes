package com.uteq.dispositivos

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.uteq.dispositivos.Adaptador.FacultadAdapter
import com.uteq.dispositivos.ApiService.ApiFacultad
import com.uteq.dispositivos.ApiService.ApiFacultadCompartida
import com.uteq.dispositivos.ApiService.ApiUrl
import com.uteq.dispositivos.Modelo.Facultad
import com.uteq.dispositivos.Modelo.Usuario
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Activity_Facultades : AppCompatActivity() {

    private lateinit var rcvFacultad: RecyclerView
    private var adapterFacultad: FacultadAdapter? = null
    private var foto: ImageView? = null

    private var nfacultad = ""
    private var correo = ""

    private var correos: MutableList<String> = mutableListOf()
    private var usuarios: MutableList<Usuario> = mutableListOf()
    private var id_usuario = 0
    private var id_facultad = 0
    private var homeId: Long = 190971144

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facultades)

        id_usuario = intent.getIntExtra("id_cliente", 0)
        correo = intent.getStringExtra("correo") ?: ""
        homeId = intent.getLongExtra("homeId", 190971144)

        rcvFacultad = findViewById(R.id.rcvDispositivos)
        rcvFacultad.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        usuarios = ArrayList()
        correos = ArrayList()
        correos.add("dummy@dummy.com")

        actualizarDatos()

        val addButton: ImageButton = findViewById(R.id.btnAgregarFacultad)
        addButton.setOnClickListener {
            showDialogAgregarFacultad()
        }

        val updateButton: ImageButton = findViewById(R.id.btnActualizar)
        updateButton.setOnClickListener {
            actualizarDatos()
        }
    }

    fun actualizarDatos() {
        val datosNuevo = ArrayList<Facultad>()
        val dummyUser = Usuario(1, "Usuario Prueba", "prueba@test.com", "123")
        datosNuevo.add(Facultad(1, "Facultad Prueba (Local)", true, dummyUser, 1, 1, 1))

        adapterFacultad = FacultadAdapter(datosNuevo) { id: Int, precionado: Boolean, it: View ->
            val facultad = datosNuevo.firstOrNull { it.id_facultad == id }
            if (facultad != null) {
                id_facultad = facultad.id_facultad
                nfacultad = facultad.nombre ?: ""
                if (precionado) {
                    val popupMenu = PopupMenu(applicationContext, it)
                    popupMenu.inflate(R.menu.menu_facultad)
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.op_detalle -> {
                                showDialogDetalle((facultad.nombre ?: ""), facultad.cantidad_aula, facultad.cantidad_dispositivos, facultad.cantidad_dispositivos_activo)
                                true
                            }
                            R.id.op_eliminar -> {
                                showDialogEliminar(facultad.id_facultad, (facultad.nombre ?: ""))
                                true
                            }
                            R.id.op_compartir -> {
                                showDialogCompartir()
                                true
                            }
                            else -> false
                        }
                    }
                    popupMenu.show()
                } else {
                    val intent = Intent(applicationContext, Activity_Aulas::class.java).apply {
                        putExtra("idFacultad", facultad.id_facultad)
                        putExtra("id_cliente", id_usuario)
                        putExtra("homeId", homeId)
                    }
                    startActivity(intent)
                }
            }
        }
        rcvFacultad.adapter = adapterFacultad
    }

    private fun showDialogAgregarFacultad() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_facultades)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        
        val imgLogo: ImageView = dialog.findViewById(R.id.imgLogo)
        val txtNombre: TextView = dialog.findViewById(R.id.txtMotivoDialog)
        val btnAgregar: Button = dialog.findViewById(R.id.btnAgregarDialog)
        val btnFoto: Button = dialog.findViewById(R.id.btnSelecionarFoto)
        val txt_i_nombre: TextInputLayout = dialog.findViewById(R.id.motivo_text_input_layout)
        foto = dialog.findViewById(R.id.imgFotoSelecion)
        imgLogo.setImageResource(R.drawable.logo_uteq)

        btnFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1001)
        }

        btnAgregar.setOnClickListener {
            if (txtNombre.text.toString().isEmpty()) {
                txt_i_nombre.error = "El campo no puede estar vacio"
                txt_i_nombre.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
            } else {
                try {
                    val jsonObject = JSONObject()
                    val usuarioJson = JSONObject()
                    usuarioJson.put("id_usuario", id_usuario)
                    jsonObject.put("nombre", txtNombre.text.toString())
                    jsonObject.put("usuario", usuarioJson)

                    val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

                    val retrofit = Retrofit.Builder()
                        .baseUrl(ApiUrl.urlUbicMedic)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val apiService = retrofit.create(ApiFacultad::class.java)

                    val call = apiService.post(requestBody)
                    call.enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                Toast.makeText(applicationContext, "Se registro correctamente", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        }
                    })

                    actualizarDatos()
                    dialog.dismiss()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        dialog.show()
    }

    private fun showDialogEliminar(idEliminar: Int, facultad: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_eliminar)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val txtEncabezado: TextView = dialog.findViewById(R.id.txtvEncabezado)
        val txtEliminar: TextView = dialog.findViewById(R.id.txtAvisoBloquear)
        val btnEliminar: Button = dialog.findViewById(R.id.btnFinalizarSi)
        val btnCancelar: Button = dialog.findViewById(R.id.btnFinalizarNo)

        txtEncabezado.text = "¿Quieres eliminar la facultad de \$facultad?"
        txtEliminar.text = "Cuando eliminas una facultad se eliminaran las aulas y dispositivos que tengan"

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnEliminar.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl(ApiUrl.urlUbicMedic)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiFacultad::class.java)

            val call = apiService.delete(idEliminar)

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        dialog.dismiss()
                        actualizarDatos()
                        Toast.makeText(applicationContext, "Se elimino la facultad", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                }
            })
        }
        dialog.show()
    }

    private fun showDialogDetalle(facultad: String, aula: Int, dispositivo: Int, activo: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_detalle_facultad)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        
        val txtEncabezado: TextView = dialog.findViewById(R.id.txtvEncabezado)
        val txtAvisoAula: TextView = dialog.findViewById(R.id.txtAvisoAulas)
        val txtAvisoDispositivos: TextView = dialog.findViewById(R.id.txtAvisoDispositivo)
        val txtAvisoDispositivosActivo: TextView = dialog.findViewById(R.id.txtAvisoDispositivoActivo)
        
        txtEncabezado.text = facultad
        txtAvisoAula.text = "Cuenta con \$aula aulas"
        txtAvisoDispositivos.text = "Cuenta con \$dispositivo dispositivos"
        txtAvisoDispositivosActivo.text = "Cuenta con \$activo dispositivos activos"

        dialog.show()
    }

    private fun showDialogCompartir() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_facultad_compartir)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        
        val notificar: CheckBox = dialog.findViewById(R.id.cbNotificar)
        val btnCompartir: Button = dialog.findViewById(R.id.btnCompartirDialog)
        val txtCorreo: AutoCompleteTextView = dialog.findViewById(R.id.txtAgregarPersona)
        val txtComentario: TextView = dialog.findViewById(R.id.editTextAgregarMensaje)
        val titulo: TextView = dialog.findViewById(R.id.txtEncabezadoDialog)
        
        titulo.text = String.format("Compartir \"%s\"", nfacultad)
        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, correos)
        txtCorreo.threshold = 1
        txtCorreo.setAdapter(adapter)
        txtCorreo.setTextColor(Color.BLACK)
        
        btnCompartir.setOnClickListener {
            if (txtCorreo.text.toString().isEmpty()) {
                txtCorreo.error = "El campo no puede estar vacio"
            } else {
                try {
                    var usuario_id = 0
                    var comprobar = false
                    var nUsuario = ""
                    for (usuario in usuarios) {
                        if (usuario.email == txtCorreo.text.toString()) {
                            usuario_id = usuario.id_usuario
                            nUsuario = usuario.usuario ?: ""
                            comprobar = true
                        }
                    }
                    if (comprobar) {
                        val jsonObject = JSONObject()
                        val usuarioJson = JSONObject()
                        val facultadJson = JSONObject()
                        facultadJson.put("id_facultad", id_facultad)
                        usuarioJson.put("id_usuario", usuario_id)

                        jsonObject.put("estado", true)
                        jsonObject.put("usuario", usuarioJson)
                        jsonObject.put("facultad", facultadJson)

                        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

                        val retrofit = Retrofit.Builder()
                            .baseUrl(ApiUrl.urlUbicMedic)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()

                        val apiService = retrofit.create(ApiFacultadCompartida::class.java)

                        val call = apiService.post(requestBody)
                        val finalNUsuario = nUsuario
                        call.enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                if (response.isSuccessful) {
                                    var mensaje = """
                                        Estimado [Nombre del Usuario],
                                        
                                        Esperamos que te encuentres bien. Queremos informarte que se te ha compartido un edificio en nuestra aplicación móvil:
                                        
                                        Nombre del Encargado: [Nombre del Encargado]
                                        Nombre del Edificio: [Nombre del Edificio]
                                        [comentario]
                                        
                                        Atentamente,
                                        [Dispositivos]
                                    """.trimIndent()
                                    mensaje = mensaje.replace("[Nombre del Encargado]", correo)
                                    mensaje = mensaje.replace("[Nombre del Usuario]", finalNUsuario)
                                    mensaje = mensaje.replace("[Nombre del Edificio]", nfacultad)
                                    mensaje = mensaje.replace("[comentario]", txtComentario.text.toString())
                                    
                                    val enviarCorreo = Enviar_Correo(correo, txtCorreo.text.toString(), "Edificio compartido", mensaje)
                                    enviarCorreo.execute()
                                    Toast.makeText(applicationContext, "Se compartio la facultad", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            }
                        })

                        actualizarDatos()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(applicationContext, "Correo invalido", Toast.LENGTH_LONG).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            if (notificar.isChecked) {
                // handle check
            } else {
                // handle uncheck
            }
        }
        dialog.show()
    }
}
