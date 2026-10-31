package com.yoshua.vesti.viewmodels

/**
 * Reglas de validación compartidas por los ViewModels.
 */
object Validaciones {

    private const val LETRA = "[A-Za-zÁÉÍÓÚáéíóúÜüÑñ]"

    /**
     * Solo letras mayúsculas y minúsculas (incluye ñ, Ñ y vocales con tilde o diéresis),
     * con UN espacio entre palabras. Ej. válidos: "Juan Pérez", "Jefe Ventas", "Administrador".
     * Inválidos: " Juan", "Juan ", "Juan  Pérez", "Juan3", "Jefe-Ventas".
     */
    private val SOLO_LETRAS_Y_ESPACIOS = Regex("^$LETRA+( $LETRA+)*$")

    fun esSoloLetras(valor: String): Boolean = SOLO_LETRAS_Y_ESPACIOS.matches(valor)

    // Formato básico de email: texto@dominio.extensión
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /** Devuelve el mensaje de error del email, o null si es válido. */
    fun errorEmail(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu email"
        !EMAIL.matches(valor.trim()) -> "El email no tiene un formato válido"
        else -> null
    }

    /** Devuelve el mensaje de error de la contraseña, o null si es válida. */
    fun errorPassword(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu contraseña"
        valor.length < 4 -> "Debe tener al menos 4 caracteres"
        else -> null
    }
}