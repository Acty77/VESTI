package com.yoshua.vesti.repository.models

/** Los tres estados posibles de un probador. */
enum class EstadoProbador { LIBRE, OCUPADO, EXCEDIDO }

/**
 * Regla central de VESTI: decide el estado según cuánto tiempo lleva ocupado el probador.
 * inicioMillis es la hora (en milisegundos) en que comenzó el uso; es null si está libre.
 */
fun calcularEstado(inicioMillis: Long?, ahoraMillis: Long, umbralMinutos: Int): EstadoProbador {
    if (inicioMillis == null) return EstadoProbador.LIBRE
    val minutosEnUso = (ahoraMillis - inicioMillis) / 60_000L
    return if (minutosEnUso >= umbralMinutos) EstadoProbador.EXCEDIDO else EstadoProbador.OCUPADO
}