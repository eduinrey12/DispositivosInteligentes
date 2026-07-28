package com.uteq.dispositivos.Modelo

data class Aula(
    val id_aula: Int,
    val nombre: String?,
    val facultad: Facultad?,
    val estado: Boolean,
    val cantidad_dispositivos: Int,
    val cantidad_dispositivos_activo: Int
) {
    fun getId_facultad(): Facultad? = facultad
}
