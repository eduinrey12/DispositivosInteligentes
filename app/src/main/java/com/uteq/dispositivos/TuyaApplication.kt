package com.uteq.dispositivos

import android.app.Application
import com.thingclips.smart.home.sdk.ThingHomeSdk

class TuyaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Tuya SDK con las credenciales
        ThingHomeSdk.init(this, "fqkatyqm49kdjn5u8t9w", "sfa3qsqvh9fs987ahc345qj4ej9u8yfm")
    }
}
