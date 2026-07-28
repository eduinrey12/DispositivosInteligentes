package com.uteq.dispositivos.ApiService

import com.uteq.dispositivos.Modelo.Aula
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiAula {
    @GET("/api/aulas") // Reemplazar url
    fun get(): List<Aula>

    @GET("/api/aulas") // Reemplazar url
    fun getCall(): Call<List<Aula>>

    @POST("/api/aulas")
    fun post(@Body requestBody: RequestBody): Call<ResponseBody>

    @DELETE("/api/aulas/{id}")
    fun delete(@Path("id") id: Int): Call<Void>
}
