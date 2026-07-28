package com.uteq.dispositivos

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.uteq.dispositivos.Adaptador.IrCategoriaAdapter
import com.uteq.dispositivos.network.TuyaCloudClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Activity_IrCategorias : AppCompatActivity() {

    private var devId: String? = null
    private lateinit var rcvCategoriasIr: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ir_categorias)

        devId = intent.getStringExtra("devId")
        if (devId == null) {
            Toast.makeText(this, "Error: ID de dispositivo nulo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rcvCategoriasIr = findViewById(R.id.rcvCategoriasIr)
        rcvCategoriasIr.layoutManager = LinearLayoutManager(this)

        cargarCategorias()
    }

    private fun cargarCategorias() {
        Toast.makeText(this, "Obteniendo token...", Toast.LENGTH_SHORT).show()

        TuyaCloudClient.api.getToken().enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val result = body.getAsJsonObject("result")
                    if (result != null && result.has("access_token")) {
                        val token = result.get("access_token").asString
                        TuyaCloudClient.accessToken = token
                        descargarCategoriasV2()
                    } else {
                        Toast.makeText(this@Activity_IrCategorias, "Error token: $body", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Activity_IrCategorias, "Error HTTP token: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@Activity_IrCategorias, "Fallo red token: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun descargarCategoriasV2() {
        TuyaCloudClient.api.getCategories(devId!!).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val result = body.getAsJsonArray("result")
                    
                    if (result != null) {
                        val adapter = IrCategoriaAdapter(result, devId!!) { categoryId, nombre ->
                            val intent = android.content.Intent(this@Activity_IrCategorias, Activity_IrMarcas::class.java)
                            intent.putExtra("devId", devId)
                            intent.putExtra("categoryId", categoryId)
                            intent.putExtra("categoryName", nombre)
                            startActivity(intent)
                        }
                        rcvCategoriasIr.adapter = adapter
                    }
                } else {
                    Toast.makeText(this@Activity_IrCategorias, "Error categorías: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@Activity_IrCategorias, "Fallo red categorías: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
