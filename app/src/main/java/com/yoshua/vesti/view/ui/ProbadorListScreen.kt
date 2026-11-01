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
    isAdmin: Boolean,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val probadores by viewModel.probadores.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<Probador?>(null) }

    // Actualiza el reloj interno cada 1 segundo para recalcular el tiempo transcurrido
    var ahoraMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            ahoraMillis = System.currentTimeMillis()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (probadores.isEmpty()) {
            Text(
                text = if (isAdmin) "No hay probadores. Presiona + para agregar." else "No hay probadores registrados.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(probadores, key = { it.id }) { probador ->
                    val estado = calcularEstado(probador, ahoraMillis, umbralMinutos = 10)
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
                            // Integración completa con ProbadorCard
                            ProbadorCard(
                                numero = probador.numero,
                                estado = estado,
                                segundosEnUso = segundosEnUso,
                                onAccion = { viewModel.toggleEstadoProbador(probador) },
                                onToggleServicio = { viewModel.toggleEnServicio(probador) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Detalles de ubicación y opciones administrativas
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

    toDelete?.let { probador ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar probador") },
            text = { Text("¿Deseas eliminar el probador ${"%02d".format(probador.numero)}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProbador(probador)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}