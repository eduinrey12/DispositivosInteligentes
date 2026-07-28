package com.uteq.dispositivos.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object TuyaCloudClient {
    private const val BASE_URL = "https://openapi.tuyaus.com"
    
    // Aquí van tus credenciales del Proyecto Cloud
    private const val CLIENT_ID = "8umutq4nuq4pfkan4unk"
    private const val CLIENT_SECRET = "417ad1be103347638523c7d7d69f398c"

    var accessToken: String? = null

    val api: TuyaCloudApi by lazy {
        val interceptor = TuyaCloudAuthInterceptor(CLIENT_ID, CLIENT_SECRET)
        
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(TuyaCloudApi::class.java)
    }
}
