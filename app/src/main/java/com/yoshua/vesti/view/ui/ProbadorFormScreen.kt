package com.yoshua.vesti.view.ui

import com.yoshua.vesti.view.utils.AnimacionEntradaFormulario
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.yoshua.vesti.viewmodels.ProbadorViewModel

@Composable
fun ProbadorFormScreen(viewModel: ProbadorViewModel, probadorId: Int, onDone: () -> Unit) {
    var numero by rememberSaveable { mutableStateOf("") }
    var ubicacion by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }

    // Carga el probador a editar mediante una corrutina (función suspend del ViewModel)
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

    // ANIMACIÓN 1 (view/utils/Animations.kt): fade + deslizamiento vertical
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
                if (probadorId > 0) "Editar Probador" else "Nuevo Probador",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = numero, onValueChange = { numero = it },
                label = { Text("Número") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ubicacion, onValueChange = { ubicacion = it },
                label = { Text("Ubicación") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    val n = numero.toIntOrNull()
                    when {
                        n == null || n <= 0 -> error = "Número inválido"
                        ubicacion.isBlank() -> error = "La ubicación es obligatoria"
                        else -> {
                            viewModel.saveProbador(probadorId, n, ubicacion.trim()) { guardado ->
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
            ) { Text("Guardar Probador") }

            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}