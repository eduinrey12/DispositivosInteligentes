package com.uteq.dispositivos.Modelo

data class Facultad(
    val id_facultad: Int,
    val nombre: String?,
    val estado: Boolean,
    val usuario: Usuario?,
    val cantidad_aula: Int,
    val cantidad_dispositivos: Int,
    val cantidad_dispositivos_activo: Int
) {
    fun getId_usuario(): Usuario? = usuario
}
