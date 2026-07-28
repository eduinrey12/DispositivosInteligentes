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
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback
import com.thingclips.smart.sdk.api.IResultCallback
import com.thingclips.smart.sdk.api.IThingDevice
import com.uteq.dispositivos.Adaptador.DispositivoAdapter
import com.uteq.dispositivos.Modelo.Aula
import com.uteq.dispositivos.Modelo.Dispositivo
import com.uteq.dispositivos.Modelo.Facultad
import com.uteq.dispositivos.Modelo.Usuario

class Activity_Dispositivos : AppCompatActivity() {

    private lateinit var rcvDispositivo: RecyclerView
    private var adapterDispositivo: DispositivoAdapter? = null
    private var foto: ImageView? = null

    override fun onRestart() {
        super.onRestart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispositivos)

        val idAula = intent.getIntExtra("idAula", 0)
        val homeId = intent.getLongExtra("homeId", 190971144L)

        rcvDispositivo = findViewById(R.id.rcvDispositivos)
        rcvDispositivo.layoutManager = GridLayoutManager(this, 2)

        actualizarDatos()

        val btnAgregar: ImageButton = findViewById(R.id.btnAgregarDispositivo)
        btnAgregar.setOnClickListener {
            val intent = Intent(applicationContext, Activity_DispositivoAgregar::class.java)
            intent.putExtra("idAula", idAula)
            intent.putExtra("homeId", homeId)
            startActivity(intent)
        }

        val btnActualizar: ImageButton = findViewById(R.id.btnActualizar)
        btnActualizar.setOnClickListener {
            actualizarDatos()
        }

        val animationView: LottieAnimationView = findViewById(R.id.animationView)
        animationView.setAnimation(R.raw.candadopepa)
        animationView.loop(true)
        animationView.playAnimation()
        animationView.visibility = View.GONE
    }

    fun actualizarDatos() {
        val idAula = intent.getIntExtra("idAula", 0)
        val homeId = intent.getLongExtra("homeId", 190971144)

        ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(object : IThingHomeResultCallback {
            override fun onSuccess(homeBean: HomeBean) {
                val deviceBeans = homeBean.deviceList
                val datosNuevo = ArrayList<Dispositivo>()
                val dummyUser = Usuario(1, "Test", "Test", "Test")
                val dummyFacultad = Facultad(1, "Dummy", true, dummyUser, 1, 1, 1)
                val dummyAula = Aula(idAula, "Aula Prueba", dummyFacultad, true, 1, 1)

                if (deviceBeans != null) {
                    for (i in deviceBeans.indices) {
                        val d = deviceBeans[i]
                        var estado = false
                        if (d.dps != null && d.dps.containsKey("1")) {
                            val state = d.dps["1"]
                            if (state is Boolean) {
                                estado = state
                            }
                        }
                        datosNuevo.add(Dispositivo(i + 1, dummyAula, d.name, d.devId, "Tuya", "Generic", estado))
                    }
                }

                adapterDispositivo = DispositivoAdapter(datosNuevo) { id: Int, posicion: Int, op: Int, it: View ->
                    val dispositivo = datosNuevo.firstOrNull { it.id_dispositivo == id }
                    if (dispositivo != null) {
                        when (op) {
                            0 -> {
                                val popupMenu = PopupMenu(applicationContext, it)
                                popupMenu.inflate(R.menu.menu_facultad)
                                popupMenu.setOnMenuItemClickListener { menuItem ->
                                    if (menuItem.itemId == R.id.op_eliminar) {
                                        showDialogEliminar(dispositivo.id_dispositivo, dispositivo.nombre ?: "")
                                        true
                                    } else {
                                        false
                                    }
                                }
                                popupMenu.show()
                            }
                            1, 2 -> {
                                val mDevice: IThingDevice = ThingHomeSdk.newDeviceInstance(dispositivo.devId)
                                try {
                                    val newState = !dispositivo.estado
                                    dispositivo.estado = newState
                                    mDevice.publishDps("{\"1\": \$newState}", object : IResultCallback {
                                        override fun onError(code: String, error: String) {}
                                        override fun onSuccess() {}
                                    })
                                } catch (e: Exception) {
                                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                                }
                                adapterDispositivo?.notifyItemChanged(posicion)
                            }
                            3 -> {
                                val mDevice3: IThingDevice = ThingHomeSdk.newDeviceInstance(dispositivo.devId)
                                try {
                                    val newState = !dispositivo.estado2
                                    dispositivo.estado2 = newState
                                    mDevice3.publishDps("{\"2\": \$newState}", object : IResultCallback {
                                        override fun onError(code: String, error: String) {}
                                        override fun onSuccess() {}
                                    })
                                } catch (e: Exception) {
                                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                                }
                                adapterDispositivo?.notifyItemChanged(posicion)
                            }
                        }
                    }
                }
                rcvDispositivo.adapter = adapterDispositivo
            }

            override fun onError(errorCode: String, errorMsg: String) {
                Toast.makeText(applicationContext, "Error Tuya: \$errorMsg", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDialogAgregarDispositivos() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_dispositivos)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        val txtEncabezado: TextView = dialog.findViewById(R.id.txtEncabezadoDialog)
        val imgLogo: ImageView = dialog.findViewById(R.id.imgLogo)
        val txtNombre: TextView = dialog.findViewById(R.id.txtMotivoDialog)
        val btnAgregar: Button = dialog.findViewById(R.id.btnAgregarDialog)
        val txt_i_nombre: TextInputLayout = dialog.findViewById(R.id.motivo_text_input_layout)
        val spnMarca: Spinner = dialog.findViewById(R.id.spnMarca)
        
        val itemsMarca = listOf("Meross", "Tuya")
        val adapterMarca = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsMarca)
        adapterMarca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnMarca.adapter = adapterMarca
        
        val spnModelo: Spinner = dialog.findViewById(R.id.spnModelo)
        val itemsModelo = listOf("ON_I_OFF_1", "ON_I_OFF_2", "IOT-BASED", "Smart Touch", "Tomacorriente", "mss110")
        val adapterModelo = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsModelo)
        adapterModelo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnModelo.adapter = adapterModelo
        
        txtEncabezado.text = "Agregar Dispositivo"
        val btnFoto: Button = dialog.findViewById(R.id.btnSelecionarFoto)
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
                Toast.makeText(applicationContext, "Omitiendo guardado en backend por ahora.", Toast.LENGTH_LONG).show()
                actualizarDatos()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showDialogEliminar(idEliminar: Int, nombre: String) {
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
        
        txtEncabezado.text = "¿Quieres eliminar el dispositivo: \$nombre?"
        txtEliminar.text = "Cuando eliminas una aula se eliminaran los dispositivos que tengan"

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnEliminar.setOnClickListener {
            dialog.dismiss()
            actualizarDatos()
            Toast.makeText(applicationContext, "Omitiendo eliminación en backend.", Toast.LENGTH_LONG).show()
        }

        dialog.show()
    }

    private fun showDialogBloqueo(dispositivo: String, usuario: String, fecha: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_bloqueo_dispositivo)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        
        val txtEncabezado: TextView = dialog.findViewById(R.id.txtvEncabezado)
        val txtAvisoUsuario: TextView = dialog.findViewById(R.id.txtAvisoUsuario)
        val txtAvisoFecha: TextView = dialog.findViewById(R.id.txtAvisoFecha)
        
        txtEncabezado.text = dispositivo
        txtAvisoUsuario.text = "\$usuario bloqueo este dispositivo"
        txtAvisoFecha.text = "Fue bloqueado el \$fecha"

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        ThingHomeSdk.onDestroy()
    }
}
