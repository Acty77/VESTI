package com.yoshua.vesti.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshua.vesti.repository.models.Probador
import com.yoshua.vesti.repository.models.calcularEstado
import com.yoshua.vesti.view.utils.ProbadorCard
import com.yoshua.vesti.viewmodels.ProbadorViewModel
import kotlinx.coroutines.delay

@Composable
fun ProbadorListScreen(
    viewModel: ProbadorViewModel,
    isAdmin: Boolean, // Controla la visibilidad de los botones de edición, eliminación y creación (+)
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    // Observa en tiempo real la lista de probadores emitida desde el Flow de Room
    val probadores by viewModel.probadores.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<Probador?>(null) }

    // Reloj interno: Genera un pulso cada 1 segundo (1000ms) para provocar la recomposición de los cronómetros
    var ahoraMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L) // Pausa asíncrona sin bloquear el hilo principal
            ahoraMillis = System.currentTimeMillis() // Actualiza la variable gatillando el recalculo de tiempo
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (probadores.isEmpty()) {
            // Estado vacío de la lista
            Text(
                text = if (isAdmin) "No hay probadores. Presiona + para agregar." else "No hay probadores registrados.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // LazyColumn recicla las vistas renderizando solo los elementos visibles en pantalla
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 'key = { it.id }' optimiza el rendimiento del reciclado al asociar cada tarjeta a la PK de Room
                items(probadores, key = { it.id }) { probador ->
                    // Función pura de dominio que calcula el estado actual (LIBRE, OCUPADO, EXCEDIDO, FUERA_DE_SERVICIO)
                    val estado = calcularEstado(probador, ahoraMillis, umbralMinutos = 10)
                    // Cálculo preciso en segundos transcurridos desde que inició el uso
                    val segundosEnUso = if (probador.inicioMillis != null) {
                        (ahoraMillis - probador.inicioMillis) / 1000L
                    } else 0L

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            // Tarjeta reusable que gestiona la UI de colores e íconos dinámicos del probador
                            ProbadorCard(
                                numero = probador.numero,
                                estado = estado,
                                segundosEnUso = segundosEnUso,
                                onAccion = { viewModel.toggleEstadoProbador(probador) },
                                onToggleServicio = { viewModel.toggleEnServicio(probador) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Pie de tarjeta con ubicación y botones restringidos para Admin
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Ubicación: ${probador.ubicacion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Aplica RBAC: Solo el Administrador puede editar o eliminar probadores
                                if (isAdmin) {
                                    Row {
                                        IconButton(onClick = { onEdit(probador.id) }) {
                                            Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                                        }
                                        IconButton(onClick = { toDelete = probador }) {
                                            Icon(
                                                Icons.Outlined.Delete,
                                                contentDescription = "Eliminar",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Botón flotante para agregar nuevos probadores (Exclusivo Admin)
        if (isAdmin) {
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar probador")
            }
        }
    }

    // Diálogo modal de confirmación antes de borrar un registro de la base de datos
    toDelete?.let { probador ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar probador") },
            text = { Text("¿Deseas eliminar el probador ${"%02d".format(probador.numero)}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProbador(probador) // Ejecuta la eliminación en el ViewModel
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}