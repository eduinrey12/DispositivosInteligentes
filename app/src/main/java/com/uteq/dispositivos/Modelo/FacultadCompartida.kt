package com.uteq.dispositivos.Modelo

data class FacultadCompartida(
    val id_facultadCompartida: Int,
    val facultad: Facultad?,
    val usuario: Usuario?,
    val estado: Boolean
) {
    fun getIdFacultadCompartida() = id_facultadCompartida
    fun getIdFacultad() = facultad
    fun getIdUsuario() = usuario
}
