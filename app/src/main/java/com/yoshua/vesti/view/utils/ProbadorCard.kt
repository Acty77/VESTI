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
    numero: Int,
    estado: EstadoProbador,
    segundosEnUso: Long,
    onAccion: () -> Unit,
    onToggleServicio: () -> Unit, // <-- Nuevo callback para cambiar servicio
    modifier: Modifier = Modifier
) {
    val colores = coloresDeEstado(estado)
    val fondo by animateColorAsState(targetValue = colores.fondo, label = "fondoProbador")
    val texto by animateColorAsState(targetValue = colores.texto, label = "textoProbador")

    val etiqueta = when (estado) {
        EstadoProbador.LIBRE -> "Libre"
        EstadoProbador.OCUPADO -> "Ocupado"
        EstadoProbador.EXCEDIDO -> "Excedido"
        EstadoProbador.FUERA_DE_SERVICIO -> "Fuera de Servicio"
    }

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
            // Header: Título + Botón rápido de activación/bloqueo de servicio
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
                        text = "Probador %02d".format(numero),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón rápido para alternar Fuera de Servicio
                IconButton(onClick = onToggleServicio) {
                    Icon(
                        imageVector = if (estado == EstadoProbador.FUERA_DE_SERVICIO)
                            Icons.Outlined.PlayCircleOutline
                        else
                            Icons.Outlined.Block,
                        contentDescription = "Alternar estado de servicio",
                        tint = texto
                    )
                }
            }

            // Etiqueta visual de estado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(texto.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(iconoEstado, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = when (estado) {
                    EstadoProbador.LIBRE -> "Disponible para usar"
                    EstadoProbador.FUERA_DE_SERVICIO -> "Desactivado por mantenimiento"
                    else -> "En uso hace ${formatearTiempo(segundosEnUso)}"
                },
                style = MaterialTheme.typography.bodyMedium
            )

            // Botón de acción contextual según el estado
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

/** 725 segundos -> "12:05" */
private fun formatearTiempo(segundos: Long): String {
    val minutos = segundos / 60
    val resto = segundos % 60
    return "%d:%02d".format(minutos, resto)
}

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