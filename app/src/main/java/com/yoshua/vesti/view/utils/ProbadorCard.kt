package com.yoshua.vesti.view.utils

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yoshua.vesti.repository.models.EstadoProbador
import com.yoshua.vesti.view.theme.AppTheme
import com.yoshua.vesti.view.theme.coloresDeEstado

@Composable
fun ProbadorCard(
    numero: Int, // Número asignado al probador
    estado: EstadoProbador, // Estado dinámico (LIBRE, OCUPADO, EXCEDIDO, FUERA_DE_SERVICIO)
    segundosEnUso: Long, // Tiempo acumulado en segundos de uso
    onAccion: () -> Unit, // Callback para alternar estado de uso (Iniciar / Marcar Libre)
    onToggleServicio: () -> Unit, // Callback para alternar fuera de servicio / mantenimiento
    modifier: Modifier = Modifier
) {
    // Obtiene la combinación de colores (fondo y texto) asociada al estado actual
    val colores = coloresDeEstado(estado)
    // Transición animada para suavizar el cambio de color de la tarjeta al cambiar de estado
    val fondo by animateColorAsState(targetValue = colores.fondo, label = "fondoProbador")
    val texto by animateColorAsState(targetValue = colores.texto, label = "textoProbador")

    // Mapeo del Enum a texto en español para la etiqueta visual
    val etiqueta = when (estado) {
        EstadoProbador.LIBRE -> "Libre"
        EstadoProbador.OCUPADO -> "Ocupado"
        EstadoProbador.EXCEDIDO -> "Excedido"
        EstadoProbador.FUERA_DE_SERVICIO -> "Fuera de Servicio"
    }

    // Mapeo del Enum al ícono descriptivo correspondiente
    val iconoEstado: ImageVector = when (estado) {
        EstadoProbador.LIBRE -> Icons.Outlined.CheckCircle
        EstadoProbador.OCUPADO -> Icons.Outlined.Timer
        EstadoProbador.EXCEDIDO -> Icons.Outlined.Warning
        EstadoProbador.FUERA_DE_SERVICIO -> Icons.Outlined.Build
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = texto)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Título del probador con formato a 2 dígitos + Botón rápido de mantenimiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.MeetingRoom, contentDescription = null)
                    Text(
                        text = "Probador %02d".format(numero), // Formatea el entero '1' a "01"
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón rápido para alternar el estado "Fuera de Servicio"
                IconButton(onClick = onToggleServicio) {
                    Icon(
                        imageVector = if (estado == EstadoProbador.FUERA_DE_SERVICIO)
                            Icons.Outlined.PlayCircleOutline // Ícono para reactivar
                        else
                            Icons.Outlined.Block, // Ícono para bloquear/desactivar
                        contentDescription = "Alternar estado de servicio",
                        tint = texto
                    )
                }
            }

            // Chip/Etiqueta visual indicadora del estado actual
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(texto.copy(alpha = 0.12f), RoundedCornerShape(50)) // Fondo translúcido
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(iconoEstado, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Muestra el detalle del tiempo o la descripción del estado
            Text(
                text = when (estado) {
                    EstadoProbador.LIBRE -> "Disponible para usar"
                    EstadoProbador.FUERA_DE_SERVICIO -> "Desactivado por mantenimiento"
                    else -> "En uso hace ${formatearTiempo(segundosEnUso)}" // Muestra cronómetro formato mm:ss
                },
                style = MaterialTheme.typography.bodyMedium
            )

            // Botón de acción contextual según el estado operativo del probador
            if (estado != EstadoProbador.FUERA_DE_SERVICIO) {
                FilledTonalButton(
                    onClick = onAccion,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = texto, contentColor = fondo)
                ) {
                    Text(if (estado == EstadoProbador.LIBRE) "Iniciar uso" else "Marcar libre")
                }
            } else {
                OutlinedButton(
                    onClick = onToggleServicio,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = texto)
                ) {
                    Text("Reactivar Probador")
                }
            }
        }
    }
}

/** Función privada que convierte segundos acumulados a formato digital legible "12:05" */
private fun formatearTiempo(segundos: Long): String {
    val minutos = segundos / 60
    val resto = segundos % 60
    return "%d:%02d".format(minutos, resto)
}

// Vista previa interactiva de Compose dentro de Android Studio
@Preview(showBackground = true)
@Composable
private fun ProbadorCardPreview() {
    AppTheme {
        var estado by remember { mutableStateOf(EstadoProbador.LIBRE) }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProbadorCard(
                numero = 1,
                estado = estado,
                segundosEnUso = 725,
                onAccion = {
                    estado = if (estado == EstadoProbador.LIBRE) EstadoProbador.OCUPADO else EstadoProbador.LIBRE
                },
                onToggleServicio = {
                    estado = if (estado == EstadoProbador.FUERA_DE_SERVICIO) EstadoProbador.LIBRE else EstadoProbador.FUERA_DE_SERVICIO
                }
            )
            ProbadorCard(
                numero = 2,
                estado = EstadoProbador.FUERA_DE_SERVICIO,
                segundosEnUso = 0,
                onAccion = {},
                onToggleServicio = {}
            )
        }
    }
}