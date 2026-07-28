package com.uteq.dispositivos.ApiService

import com.uteq.dispositivos.Modelo.Facultad
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiFacultad {
    @GET("/api/facultades") // Reemplazar url
    fun get(): List<Facultad>

    @GET("/api/facultades") // Reemplazar url
    fun getCall(): Call<List<Facultad>>

    @POST("/api/facultades")
    fun post(@Body requestBody: RequestBody): Call<ResponseBody>

    @DELETE("/api/facultades/delete/{id}")
    fun delete(@Path("id") id: Int): Call<Void>
}
