package com.uteq.dispositivos

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.uteq.dispositivos.Adaptador.IrRemotoPruebaAdapter
import com.uteq.dispositivos.network.TuyaCloudClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Activity_IrRemotosPrueba : AppCompatActivity() {

    private var devId: String? = null
    private var categoryId: Int = 0
    private var brandId: Int = 0
    private var brandName: String? = null
    private lateinit var rcvRemotosPrueba: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ir_remotos_prueba)

        devId = intent.getStringExtra("devId")
        categoryId = intent.getIntExtra("categoryId", 0)
        brandId = intent.getIntExtra("brandId", 0)
        brandName = intent.getStringExtra("brandName")

        if (devId == null || categoryId == 0 || brandId == 0) {
            Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.txtTituloPrueba).text = "Probando $brandName"

        rcvRemotosPrueba = findViewById(R.id.rcvRemotosPrueba)
        rcvRemotosPrueba.layoutManager = LinearLayoutManager(this)

        descargarIndices()
    }

    private fun descargarIndices() {
        Toast.makeText(this, "Cargando opciones de control...", Toast.LENGTH_SHORT).show()

        TuyaCloudClient.api.getRemoteIndexes(devId!!, categoryId, brandId).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val resultElement = body.get("result")
                    var jsonArray: com.google.gson.JsonArray? = null
                    
                    if (resultElement != null) {
                        if (resultElement.isJsonArray) {
                            jsonArray = resultElement.asJsonArray
                        } else if (resultElement.isJsonObject) {
                            val resultObj = resultElement.asJsonObject
                            if (resultObj.has("remote_index_list")) {
                                jsonArray = resultObj.getAsJsonArray("remote_index_list")
                            } else {
                                // sometimes they just return result: { ... } ??
                            }
                        }
                    }
                    
                    if (jsonArray != null) {
                        val lista = mutableListOf<JsonObject>()
                        for (i in 0 until jsonArray.size()) {
                            val item = jsonArray.get(i)
                            if (item.isJsonObject) {
                                lista.add(item.asJsonObject)
                            } else {
                                val obj = JsonObject()
                                obj.addProperty("remote_index", item.asInt)
                                lista.add(obj)
                            }
                        }

                        val adapter = IrRemotoPruebaAdapter(lista,
                            onProbarClick = { remoteIndex ->
                                probarBotonPower(remoteIndex)
                            },
                            onVincularClick = { remoteIndex ->
                                vincularRemoto(remoteIndex)
                            }
                        )
                        rcvRemotosPrueba.adapter = adapter
                    } else {
                        Toast.makeText(this@Activity_IrRemotosPrueba, "Respuesta inesperada: $body", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@Activity_IrRemotosPrueba, "Error HTTP: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@Activity_IrRemotosPrueba, "Error red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun probarBotonPower(remoteIndex: Int) {
        Toast.makeText(this, "Enviando comando Power...", Toast.LENGTH_SHORT).show()
        val body = JsonObject()
        body.addProperty("categoryId", categoryId)
        body.addProperty("remoteIndex", remoteIndex)
        // La key universal para encender suele ser "Power" o "power"
        body.addProperty("key", "power") 

        TuyaCloudClient.api.testCommand(devId!!, body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@Activity_IrRemotosPrueba, "Comando enviado. ¿Se encendió/apagó?", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@Activity_IrRemotosPrueba, "Fallo comando: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@Activity_IrRemotosPrueba, "Error red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun vincularRemoto(remoteIndex: Int) {
        Toast.makeText(this, "Vinculando...", Toast.LENGTH_SHORT).show()
        val body = JsonObject()
        body.addProperty("categoryId", categoryId)
        body.addProperty("brandId", brandId)
        body.addProperty("remoteIndex", remoteIndex)
        body.addProperty("remoteName", brandName)

        TuyaCloudClient.api.saveRemote(devId!!, body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.has("success") && res.get("success").asBoolean) {
                        Toast.makeText(this@Activity_IrRemotosPrueba, "Vinculado exitosamente!", Toast.LENGTH_LONG).show()
                        // Aquí regresamos a la pantalla de Detalles o abrimos el control
                        finish() 
                    } else {
                        Toast.makeText(this@Activity_IrRemotosPrueba, "No se pudo vincular: $res", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@Activity_IrRemotosPrueba, "Error HTTP: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@Activity_IrRemotosPrueba, "Error red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
