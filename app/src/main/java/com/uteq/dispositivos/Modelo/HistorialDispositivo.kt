package com.uteq.dispositivos.Modelo

data class HistorialDispositivo(
    val id_historial: Int,
    val dispositivo: Int,
    val usuario: Int,
    val accion: String?,
    val fecha: String?
) {
    fun getIdHistorial() = id_historial
    fun getIdDispositivo() = dispositivo
    fun getIdUsuario() = usuario
}
