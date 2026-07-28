package com.uteq.dispositivos.ApiService

import com.uteq.dispositivos.Modelo.FacultadCompartida
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiFacultadCompartida {
    @GET("/api/fcompartidos") // Reemplazar url
    fun getCall(): Call<List<FacultadCompartida>>

    @POST("/api/fcompartidos")
    fun post(@Body requestBody: RequestBody): Call<ResponseBody>

    @DELETE("/api/fcompartidos/{id}")
    fun delete(@Path("id") id: Int): Call<Void>
}
