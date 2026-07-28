package com.uteq.dispositivos.Modelo

data class Usuario(
    val id_usuario: Int,
    val usuario: String?,
    val email: String?,
    val contraseña: String?
) {
    fun getIdUsuario() = id_usuario
}
