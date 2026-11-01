package com.yoshua.vesti.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yoshua.vesti.view.utils.AnimacionEntradaFormulario
import com.yoshua.vesti.viewmodels.ProbadorViewModel

@Composable
fun ProbadorFormScreen(
    viewModel: ProbadorViewModel,
    probadorId: Int,
    onDone: () -> Unit
) {
    var numero by rememberSaveable { mutableStateOf("") }
    var ubicacion by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) } // Previene doble envío

    // Carga inicial al editar
    LaunchedEffect(probadorId) {
        if (probadorId > 0 && !loaded) {
            viewModel.getProbador(probadorId)?.let {
                numero = it.numero.toString()
                ubicacion = it.ubicacion
            }
        }
        loaded = true
        visible = true
    }

    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (probadorId > 0) "Editar Probador" else "Nuevo Probador",
                style = MaterialTheme.typography.headlineMedium
            )

            // Campo Número con resaltado de error
            OutlinedTextField(
                value = numero,
                onValueChange = { newValue ->
                    // Filtra para permitir solo dígitos numéricos
                    if (newValue.all { it.isDigit() }) {
                        numero = newValue
                        error = null // Limpia error al escribir
                    }
                },
                label = { Text("Número del Probador") },
                singleLine = true,
                isError = error?.contains("Número") == true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Ubicación con resaltado de error
            OutlinedTextField(
                value = ubicacion,
                onValueChange = {
                    ubicacion = it
                    error = null // Limpia error al escribir
                },
                label = { Text("Ubicación (ej: Sector A - Nivel 1)") },
                singleLine = true,
                isError = error?.contains("ubicación") == true,
                modifier = Modifier.fillMaxWidth()
            )

            // Mensaje de error general
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Botón Principal de Guardado
            Button(
                enabled = !isSaving,
                onClick = {
                    val n = numero.toIntOrNull()
                    when {
                        n == null || n <= 0 -> error = "Número inválido"
                        ubicacion.isBlank() -> error = "La ubicación es obligatoria"
                        else -> {
                            isSaving = true
                            viewModel.saveProbador(probadorId, n, ubicacion.trim()) { guardado ->
                                isSaving = false
                                if (guardado) {
                                    onDone()
                                } else {
                                    error = "Ya existe un probador con ese número"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text("Guardar Probador")
            }

            OutlinedButton(
                onClick = onDone,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}