package com.uteq.dispositivos.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface TuyaCloudApi {
    @GET("/v1.0/token?grant_type=1")
    fun getToken(): Call<JsonObject>

    @GET("/v2.0/infrareds/{device_id}/categories")
    fun getCategories(@Path("device_id") deviceId: String): Call<JsonObject>

    @GET("/v2.0/infrareds/{device_id}/categories/{category_id}/brands")
    fun getBrands(
        @Path("device_id") deviceId: String,
        @Path("category_id") categoryId: Int
    ): Call<JsonObject>

    @GET("/v2.0/infrareds/{device_id}/categories/{category_id}/brands/{brand_id}/remote-indexs")
    fun getRemoteIndexes(
        @Path("device_id") deviceId: String,
        @Path("category_id") categoryId: Int,
        @Path("brand_id") brandId: Int
    ): Call<JsonObject>

    @retrofit2.http.POST("/v2.0/infrareds/{device_id}/testing/command")
    fun testCommand(
        @Path("device_id") deviceId: String,
        @retrofit2.http.Body body: JsonObject
    ): Call<JsonObject>

    @retrofit2.http.POST("/v2.0/infrareds/{device_id}/remotes")
    fun saveRemote(
        @Path("device_id") deviceId: String,
        @retrofit2.http.Body body: JsonObject
    ): Call<JsonObject>

    @GET("/v2.0/cloud/thing/last/{uuid}")
    fun getDeviceInfoByUuid(@Path("uuid") uuid: String): Call<JsonObject>
}
