package com.uteq.dispositivos.ApiService

import com.uteq.dispositivos.Modelo.HistorialDispositivo
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiDispositivoHistorial {
    @GET("/api/historial") // Reemplazar url
    fun get(): List<HistorialDispositivo>

    @POST("/api/historial")
    fun post(@Body requestBody: RequestBody): Call<ResponseBody>

    @DELETE("/api/historial/{id}/")
    fun delete(@Path("id") id: Int): Call<Void>

    @PUT("/api/historial/{id}/")
    fun put(@Path("id") id: Int, @Body requestBody: RequestBody): Call<ResponseBody>
}
