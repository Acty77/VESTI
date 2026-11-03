package com.yoshua.vesti.view.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yoshua.vesti.view.utils.CameraCapture

@Composable
fun PrendaPerdidaScreen(
    onGuardar: (numProbador: Int, descripcion: String, fotoUri: String?) -> Unit
) {
    var numProbador by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var lastPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var mensajeError by rememberSaveable { mutableStateOf<String?>(null) }

    if (showCamera) {
        CameraCapture(
            carpeta = "prendas_perdidas",
            onImageCaptured = { uri: Uri ->
                lastPhoto = uri.toString()
                showCamera = false
            },
            onClose = { showCamera = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Registro de Prendas Olvidadas", style = MaterialTheme.typography.headlineMedium)

            // Foto previa o botón para capturar
            if (lastPhoto != null) {
                AsyncImage(
                    model = lastPhoto,
                    contentDescription = "Foto de la prenda",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            OutlinedButton(
                onClick = { showCamera = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (lastPhoto == null) "Fotografiar Prenda" else "Cambiar Foto")
            }

            // Campo de número de probador
            OutlinedTextField(
                value = numProbador,
                onValueChange = { if (it.all { char -> char.isDigit() }) numProbador = it },
                label = { Text("Número de Probador") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo de descripción de la prenda
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (ej. Polera azul marca X, Talla M)") },
                modifier = Modifier.fillMaxWidth()
            )

            mensajeError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Botón de registro
            Button(
                onClick = {
                    val num = numProbador.toIntOrNull()
                    when {
                        num == null -> mensajeError = "Ingresa un número de probador válido"
                        descripcion.isBlank() -> mensajeError = "Ingresa una descripción"
                        else -> {
                            onGuardar(num, descripcion.trim(), lastPhoto)
                            numProbador = ""
                            descripcion = ""
                            lastPhoto = null
                            mensajeError = "¡Prenda registrada con éxito!"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Registro")
            }
        }
    }
}