package com.uteq.dispositivos

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.uteq.dispositivos.Adaptador.AulaAdapter
import com.uteq.dispositivos.ApiService.ApiAula
import com.uteq.dispositivos.ApiService.ApiUrl
import com.uteq.dispositivos.Modelo.Aula
import com.uteq.dispositivos.Modelo.Facultad
import com.uteq.dispositivos.Modelo.Usuario
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

class Activity_Aulas : AppCompatActivity() {

    private lateinit var rcvAula: RecyclerView
    private lateinit var adapterAula: AulaAdapter
    private var id_cliente = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aulas)

        id_cliente = intent.getIntExtra("id_cliente", 0)
        val homeId = intent.getLongExtra("homeId", 190971144L)

        rcvAula = findViewById(R.id.rcvDispositivos)
        rcvAula.layoutManager = GridLayoutManager(this, 3)

        actualizarDatos()

        val btnAgregar = findViewById<ImageButton>(R.id.btnAgregarAula)
        btnAgregar.setOnClickListener { showDialogAgregarAula() }

        val btnActualizar = findViewById<ImageButton>(R.id.btnActualizar)
        btnActualizar.setOnClickListener { actualizarDatos() }
    }

    fun actualizarDatos() {
        val idFacultad = intent.getIntExtra("idFacultad", 0)
        val datosNuevo: MutableList<Aula> = ArrayList()
        val dummyUser = Usuario(1, "Test", "Test", "Test")
        val dummyFacultad = Facultad(1, "Dummy", true, dummyUser, 1, 1, 1)
        datosNuevo.add(Aula(1, "Aula Prueba (Local)", dummyFacultad, true, 1, 1))

        adapterAula = AulaAdapter(datosNuevo) { id: Int, precionado: Boolean, it: View ->
            val aula = datosNuevo.find { a -> a.id_aula == id }
            if (aula != null) {
                if (precionado) {
                    val popupMenu = PopupMenu(applicationContext, it)
                    popupMenu.inflate(R.menu.menu_facultad)
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        if (menuItem.itemId == R.id.op_detalle) {
                            showDialogDetalle(aula.nombre, aula.cantidad_dispositivos, aula.cantidad_dispositivos_activo)
                            true
                        } else if (menuItem.itemId == R.id.op_eliminar) {
                            showDialogEliminar(aula.id_aula, aula.nombre)
                            true
                        } else {
                            false
                        }
                    }
                    popupMenu.show()
                } else {
                    val intent = Intent(applicationContext, Activity_Dispositivos::class.java)
                    intent.putExtra("idAula", aula.id_aula)
                    intent.putExtra("id_cliente", id_cliente)
                    intent.putExtra("homeId", getIntent().getLongExtra("homeId", 190971144L))
                    startActivity(intent)
                }
            }
        }
        rcvAula.adapter = adapterAula
    }

    private fun showDialogAgregarAula() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_facultades)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        
        val txtEncabezado = dialog.findViewById<TextView>(R.id.txtEncabezadoDialog)
        val imgLogo = dialog.findViewById<ImageView>(R.id.imgLogo)
        val txtNombre = dialog.findViewById<TextView>(R.id.txtMotivoDialog)
        val llfoto = dialog.findViewById<LinearLayout>(R.id.llFoto)
        val btnAgregar = dialog.findViewById<Button>(R.id.btnAgregarDialog)
        val txt_i_nombre = dialog.findViewById<TextInputLayout>(R.id.motivo_text_input_layout)
        
        llfoto.visibility = View.GONE
        txtEncabezado.text = "Agregar Aula"
        imgLogo.setImageResource(R.drawable.logo_uteq)

        btnAgregar.setOnClickListener {
            if (txtNombre.text.toString().isEmpty()) {
                txt_i_nombre.error = "El campo no puede estar vacio"
                txt_i_nombre.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
            } else {
                try {
                    val idFacultad = intent.getIntExtra("idFacultad", 0)
                    val facultadJson = JSONObject()
                    val jsonObject = JSONObject()
                    facultadJson.put("id_facultad", idFacultad)
                    jsonObject.put("nombre", txtNombre.text.toString())
                    jsonObject.put("facultad", facultadJson)

                    val retrofit = Retrofit.Builder()
                        .baseUrl(ApiUrl.urlUbicMedic)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val apiService = retrofit.create(ApiAula::class.java)
                    val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
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

    private fun showDialogEliminar(idEliminar: Int, nombre: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_eliminar)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        
        val txtEncabezado = dialog.findViewById<TextView>(R.id.txtvEncabezado)
        val txtEliminar = dialog.findViewById<TextView>(R.id.txtAvisoBloquear)
        val btnEliminar = dialog.findViewById<Button>(R.id.btnFinalizarSi)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnFinalizarNo)
        
        txtEncabezado.text = "¿Quieres eliminar la $nombre?"
        txtEliminar.text = "Cuando eliminas una aula se eliminaran los dispositivos que tengan"

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnEliminar.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl(ApiUrl.urlUbicMedic)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiAula::class.java)
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

    private fun showDialogDetalle(aula: String?, dispositivo: Int, activo: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_detalle_facultad)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        
        val txtEncabezado = dialog.findViewById<TextView>(R.id.txtvEncabezado)
        val llAulas = dialog.findViewById<LinearLayout>(R.id.llAulas)
        val txtAvisoDispositivos = dialog.findViewById<TextView>(R.id.txtAvisoDispositivo)
        val txtAvisoDispositivosActivo = dialog.findViewById<TextView>(R.id.txtAvisoDispositivoActivo)
        
        llAulas.visibility = View.GONE
        txtEncabezado.text = aula
        txtAvisoDispositivos.text = "Cuenta con $dispositivo dispositivos"
        txtAvisoDispositivosActivo.text = "Cuenta con $activo dispositivos activos"

        dialog.show()
    }
}
