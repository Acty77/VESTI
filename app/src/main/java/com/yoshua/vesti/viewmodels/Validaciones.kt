package com.yoshua.vesti.viewmodels

object Validaciones {
    private const val LETRA = "[A-Za-zÁÉÍÓÚáéíóúÜüÑñ]"

    private val SOLO_LETRAS_Y_ESPACIOS = Regex("^$LETRA+( $LETRA+)*$")

    // Evalúa si una cadena cumple con la regla de solo letras y espacios válidos
    fun esSoloLetras(valor: String): Boolean = SOLO_LETRAS_Y_ESPACIOS.matches(valor)

    // Expresión Regular para el formato estándar de correo electrónico (usuario@dominio.ext)
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /** Valida el campo correo electrónico retornando el mensaje descriptivo del error o null si es válido */
    fun errorEmail(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu email"
        !EMAIL.matches(valor.trim()) -> "El email no tiene un formato válido"
        else -> null
    }

    /** Valida la longitud mínima requerida para la contraseña (mínimo 4 caracteres) */
    fun errorPassword(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu contraseña"
        valor.length < 4 -> "Debe tener al menos 4 caracteres"
        else -> null
    }
}