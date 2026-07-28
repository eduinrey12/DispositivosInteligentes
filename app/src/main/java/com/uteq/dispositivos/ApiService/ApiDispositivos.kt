package com.uteq.dispositivos.ApiService

import com.uteq.dispositivos.Modelo.Dispositivo
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiDispositivos {
    @GET("/api/dispositivos") // Reemplazar url
    fun get(): List<Dispositivo>

    @GET("/api/dispositivos") // Reemplazar url
    fun getCall(): Call<List<Dispositivo>>

    @POST("/api/dispositivos")
    fun post(@Body requestBody: RequestBody): Call<ResponseBody>

    @DELETE("/api/dispositivos/delete/{id}")
    fun delete(@Path("id") id: Int): Call<Void>

    @DELETE("/api/dispositivos/{devId}")
    fun deleteDevId(@Path("devId") devId: String): Call<Void>

    @PUT("/api/dispositivos/{id}/estado")
    fun put(@Path("id") id: Long, @Body nuevoEstado: Boolean): Call<Dispositivo>
}
