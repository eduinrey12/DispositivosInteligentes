package com.uteq.dispositivos.ApiService

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.POST

interface ApiDispositivoTuya {
    @POST("breake_encendido/")
    fun offBreake(): Call<ResponseBody>

    @POST("breake_apagado/")
    fun onBreake(): Call<ResponseBody>

    @POST("switch_1_encendido/")
    fun offSwitch1(): Call<ResponseBody>

    @POST("switch_1_apagado/")
    fun onSwitch1(): Call<ResponseBody>

    @POST("switch_2_encendido/")
    fun offSwitch2(): Call<ResponseBody>

    @POST("switch_2_apagado/")
    fun onSwitch2(): Call<ResponseBody>

    @POST("tomacorriente_1_encendido/")
    fun offToma1(): Call<ResponseBody>

    @POST("tomacorriente_2_encendido/")
    fun offToma2(): Call<ResponseBody>

    @POST("tomacorriente_1_apagado/")
    fun onToma1(): Call<ResponseBody>

    @POST("tomacorriente_2_apagado/")
    fun onToma2(): Call<ResponseBody>

    @POST("smart_touch_apagado/")
    fun onWifi(): Call<ResponseBody>

    @POST("smart_touch_encendido/")
    fun offWifi(): Call<ResponseBody>
}
