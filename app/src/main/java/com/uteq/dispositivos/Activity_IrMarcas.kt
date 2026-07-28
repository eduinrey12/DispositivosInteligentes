package com.uteq.dispositivos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.uteq.dispositivos.Adaptador.IrMarcaAdapter
import com.uteq.dispositivos.network.TuyaCloudClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Activity_IrMarcas : AppCompatActivity() {

    private var devId: String? = null
    private var categoryId: Int = 0
    private lateinit var rcvMarcasIr: RecyclerView
    private lateinit var etBuscarMarca: EditText
    
    private val listaMarcasOriginal = mutableListOf<JsonObject>()
    private lateinit var adapter: IrMarcaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ir_marcas)

        devId = intent.getStringExtra("devId")
        categoryId = intent.getIntExtra("categoryId", 0)

        if (devId == null || categoryId == 0) {
            Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rcvMarcasIr = findViewById(R.id.rcvMarcasIr)
        rcvMarcasIr.layoutManager = LinearLayoutManager(this)
        
        etBuscarMarca = findViewById(R.id.etBuscarMarca)
        
        adapter = IrMarcaAdapter(listaMarcasOriginal) { brandId, nombre ->
            // Ir a la siguiente pantalla para probar los controles de la marca
            val intent = android.content.Intent(this@Activity_IrMarcas, Activity_IrRemotosPrueba::class.java)
            intent.putExtra("devId", devId)
            intent.putExtra("categoryId", categoryId)
            intent.putExtra("brandId", brandId)
            intent.putExtra("brandName", nombre)
            startActivity(intent)
        }
        rcvMarcasIr.adapter = adapter

        etBuscarMarca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtradas = listaMarcasOriginal.filter { 
                    it.get("brand_name").asString.lowercase().contains(query) 
                }
                adapter.actualizar(filtradas)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        descargarMarcas()
    }

    private fun descargarMarcas() {
        Toast.makeText(this, "Cargando marcas...", Toast.LENGTH_SHORT).show()

        TuyaCloudClient.api.getBrands(devId!!, categoryId).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val result = body.getAsJsonArray("result")
                    
                    if (result != null) {
                        listaMarcasOriginal.clear()
                        for (i in 0 until result.size()) {
                            listaMarcasOriginal.add(result.get(i).asJsonObject)
                        }
                        adapter.actualizar(listaMarcasOriginal)
                    }
                } else {
                    Toast.makeText(this@Activity_IrMarcas, "Error HTTP: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@Activity_IrMarcas, "Error red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
