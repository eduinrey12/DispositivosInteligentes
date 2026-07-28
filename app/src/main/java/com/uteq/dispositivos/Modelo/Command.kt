package com.uteq.dispositivos.Modelo

import com.google.gson.annotations.SerializedName

data class Command(
    @SerializedName("code")
    var code: String?,
    @SerializedName("value")
    var value: Boolean
)
