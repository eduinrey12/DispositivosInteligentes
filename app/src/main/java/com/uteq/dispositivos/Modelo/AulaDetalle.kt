package com.uteq.dispositivos.Modelo

data class AulaDetalle(
    val id_auladetalle: Int,
    val aula: Int,
    val usuario: Int,
    val rol: Int
) {
    fun getId_aula(): Int = aula
    fun getId_usuario(): Int = usuario
    fun getId_rol(): Int = rol
}
