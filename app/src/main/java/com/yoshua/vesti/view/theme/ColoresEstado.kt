package com.yoshua.vesti.view.theme

import androidx.compose.ui.graphics.Color
import com.yoshua.vesti.repository.models.EstadoProbador

/** Colores de una tarjeta de probador: fondo suave y texto oscuro. */
data class ColoresEstado(val fondo: Color, val texto: Color)

fun coloresDeEstado(estado: EstadoProbador): ColoresEstado = when (estado) {
    EstadoProbador.LIBRE -> ColoresEstado(fondo = Color(0xFFE6F4EA), texto = Color(0xFF1B5E20))
    EstadoProbador.OCUPADO -> ColoresEstado(fondo = Color(0xFFFFEFD6), texto = Color(0xFF8A4B00))
    EstadoProbador.EXCEDIDO -> ColoresEstado(fondo = Color(0xFFFCE4E4), texto = Color(0xFF9B1C1C))
}