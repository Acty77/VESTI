package com.example.app.view.ui

import com.example.app.view.utils.CameraCapture
import com.example.app.view.utils.AnimacionEntradaFormulario
import com.example.app.view.utils.PhotoBox
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
import com.example.app.viewmodels.ProductViewModel

@Composable
fun ProductFormScreen(viewModel: ProductViewModel, productId: Int, onDone: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }

    // Carga el producto a editar mediante una corrutina (función suspend del ViewModel)
    LaunchedEffect(productId) {
        if (productId > 0 && !loaded) {
            viewModel.getProduct(productId)?.let {
                name = it.name
                price = it.price.toString()
                stock = it.stock.toString()
                imageUri = it.imageUri
            }
        }
        loaded = true
        visible = true
    }

    if (showCamera) {
        CameraCapture(
            carpeta = "productos",
            onImageCaptured = { uri ->
                imageUri = uri.toString()
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
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
                if (productId > 0) "Editar Producto" else "Nuevo Producto",
                style = MaterialTheme.typography.headlineMedium
            )

            PhotoBox(imageUri = imageUri, circle = false, onTakePhoto = { showCamera = true })

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nombre") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = price, onValueChange = { price = it },
                label = { Text("Precio") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = stock, onValueChange = { stock = it },
                label = { Text("Stock") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    val p = price.replace(',', '.').toDoubleOrNull()
                    val s = stock.toIntOrNull()
                    when {
                        name.isBlank() -> error = "El nombre es obligatorio"
                        p == null || p < 0 -> error = "Precio inválido"
                        s == null || s < 0 -> error = "Stock inválido"
                        else -> {
                            viewModel.saveProduct(productId, name.trim(), p, s, imageUri)
                            onDone()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar Producto") }

            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
