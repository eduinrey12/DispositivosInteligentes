package com.uteq.dispositivos

object ReflectionHelper {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val c = Class.forName("com.thingclips.smart.sdk.enums.ActivatorModelEnum")
            for (enumConstant in c.enumConstants) {
                println("Enum: $enumConstant")
            }
            val c2 = Class.forName("com.thingclips.smart.sdk.api.IThingActivatorInstance")
            for (m in c2.methods) {
                println("Method: $m")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
