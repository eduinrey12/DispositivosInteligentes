package com.uteq.dispositivos.Modelo

data class Dispositivo(
    val id_dispositivo: Int,
    val aula: Aula?,
    val nombre: String?,
    val devId: String?,
    val marca: String?,
    val modelo: String?,
    var estado: Boolean,
    var estado2: Boolean = false
) {
    fun getIdDispositivo(): Int = id_dispositivo
    fun getIdAula(): Aula? = aula
}
