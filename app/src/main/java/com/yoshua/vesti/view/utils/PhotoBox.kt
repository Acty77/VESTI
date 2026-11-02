package com.yoshua.vesti.view.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/** Muestra la foto (o un botón para tomarla). Al tocar abre la cámara. */
@Composable
fun PhotoBox(
    imageUri: String?, // Ruta Uri de la imagen capturada (null si aún no hay foto)
    circle: Boolean, // Flag: true para foto circular (Usuario), false para rectangular redondeada (Probadores/Productos)
    onTakePhoto: () -> Unit // Callback ejecutado al tocar la caja para lanzar la cámara
) {
    // Determina la figura geométrica según el contexto de uso
    val shape = if (circle) CircleShape else RoundedCornerShape(16.dp)

    // Contenedor principal de tamaño fijo (130dp) recortado con la figura seleccionada
    Box(
        modifier = Modifier
            .size(130.dp)
            .clip(shape) // Recorta los bordes visuales y la zona táctil
            .background(MaterialTheme.colorScheme.surfaceVariant) // Color de fondo grisáceo por defecto
            .clickable(onClick = onTakePhoto), // Detecta la interacción del usuario para tomar la foto
        contentAlignment = Alignment.Center // Centra el ícono o el contenido internamente
    ) {
        if (imageUri != null) {
            // Carga asíncrona de la imagen mediante Coil desde la memoria del dispositivo
            AsyncImage(
                model = imageUri, // Ruta de la foto
                contentDescription = "Foto",
                contentScale = ContentScale.Crop, // Escala la imagen para llenar el contenedor sin deformarla
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Estado por defecto cuando no existe fotografía tomada
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Text("Tomar foto", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}