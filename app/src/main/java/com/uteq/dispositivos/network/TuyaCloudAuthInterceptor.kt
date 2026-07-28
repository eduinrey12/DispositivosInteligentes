package com.uteq.dispositivos.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TuyaCloudAuthInterceptor(
    private val clientId: String,
    private val clientSecret: String
) : Interceptor {

    // Helper para SHA256
    private fun getSha256(content: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(content.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    // Helper para HMAC-SHA256
    private fun getHmacSHA256(content: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        val hash = mac.doFinal(content.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }.uppercase()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val t = System.currentTimeMillis().toString()
        val nonce = ""

        // Leer el token almacenado (si existe) para inyectarlo
        val accessToken = TuyaCloudClient.accessToken ?: ""

        val method = originalRequest.method
        
        var bodyStr = ""
        val requestBody = originalRequest.body
        if (requestBody != null) {
            val buffer = okio.Buffer()
            requestBody.writeTo(buffer)
            bodyStr = buffer.readUtf8()
        }
        val contentHash = getSha256(bodyStr)
        val headersStr = ""
        
        // Obtener el URL relativo, e.g. /v1.0/token?grant_type=1
        val urlPathAndQuery = originalRequest.url.encodedPath + 
            if (originalRequest.url.encodedQuery != null) "?" + originalRequest.url.encodedQuery else ""

        val isTokenApi = urlPathAndQuery.contains("/v1.0/token")
        val activeToken = if (isTokenApi) "" else (TuyaCloudClient.accessToken ?: "")

        val stringToSign = "$method\n$contentHash\n$headersStr\n$urlPathAndQuery"
        
        // Si hay token y NO es la api de token, se concatena
        val signStr = clientId + activeToken + t + nonce + stringToSign
        val sign = getHmacSHA256(signStr, clientSecret)

        val requestBuilder = originalRequest.newBuilder()
            .header("client_id", clientId)
            .header("sign", sign)
            .header("t", t)
            .header("sign_method", "HMAC-SHA256")
            .header("nonce", nonce)

        if (activeToken.isNotEmpty()) {
            requestBuilder.header("access_token", activeToken)
        }

        val request = requestBuilder.build()
        
        Log.d("TuyaAuth", "URL: $urlPathAndQuery")
        Log.d("TuyaAuth", "SignStr: $signStr")
        Log.d("TuyaAuth", "Sign: $sign")
        
        return chain.proceed(request)
    }
}
