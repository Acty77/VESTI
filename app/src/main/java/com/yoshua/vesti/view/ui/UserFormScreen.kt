package com.yoshua.vesti.view.ui // Ubicación en la capa de vista de la aplicación

// Importaciones para utilidades personalizadas (Cámara, Animaciones y Cuadro de foto)
import com.yoshua.vesti.view.utils.CameraCapture
import com.yoshua.vesti.view.utils.AnimacionEntradaFormulario
import com.yoshua.vesti.view.utils.PhotoBox
// Utilidad nativa de Android para validar patrones de expresiones regulares (Email)
import android.util.Patterns
// Importaciones de maquetación en Jetpack Compose
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
// Importaciones de Material 3 para menús desplegables (Dropdown), botones y campos de texto
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// Importación de ViewModels requeridos (Usuarios y Roles/Tipos)
import com.yoshua.vesti.viewmodels.TipoUsuarioViewModel
import com.yoshua.vesti.viewmodels.UserViewModel

/**
 * Formulario de Usuario con soporte para captura de fotografía (Cámara),
 * asignación de rol mediante menú desplegable (FK a tipo_usuarios) y validaciones de campo.
 */
@OptIn(ExperimentalMaterial3Api::class) // Necesario por usar ExposedDropdownMenuBox de Material 3
@Composable
fun UserFormScreen(
    viewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    userId: Int, // id = 0 para nuevo usuario; id > 0 para editar existente
    onDone: () -> Unit
) {
    // Suscripción reactiva a la lista de tipos/roles de usuario registrados en la BD
    val tipos by tipoUsuarioViewModel.tipos.collectAsStateWithLifecycle()
    // Estados locales persistentes para los campos del formulario
    var tipoUsuarioId by rememberSaveable { mutableIntStateOf(0) }
    var tipoExpanded by remember { mutableStateOf(false) } // Controla si el desplegable está abierto
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) } // Ruta o URI de la fotografía tomada
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var showCamera by rememberSaveable { mutableStateOf(false) } // Modalm/Vista de la cámara
    var loaded by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }
    // Escucha de mensajes devueltos por la base de datos (ej: email duplicado)
    val dbMessage by viewModel.message.collectAsStateWithLifecycle()

    // Limpia mensajes del ViewModel al destruir el Composable para evitar interferencias futuras
    DisposableEffect(Unit) { onDispose { viewModel.clearMessage() } }

    // Carga inicial asíncrona en modo edición
    LaunchedEffect(userId) {
        if (userId > 0 && !loaded) {
            viewModel.getUser(userId)?.let {
                name = it.name
                email = it.email
                password = it.password
                imageUri = it.profileImageUri
                tipoUsuarioId = it.tipoUsuarioId
            }
        }
        loaded = true
        visible = true
    }

    // Intercepción de flujo: Si activa la cámara, despliega la interfaz de captura sobre la pantalla
    if (showCamera) {
        CameraCapture(
            carpeta = "usuarios",
            onImageCaptured = { uri ->
                imageUri = uri.toString() // Almacena la ruta de la imagen generada
                showCamera = false // Cierra la cámara
            },
            onClose = { showCamera = false }
        )
        return // Cancela la renderización del resto del formulario mientras la cámara está abierta
    }

    // Animación personalizada de entrada para el formulario
    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Permite desplazamiento al desplegar teclado
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (userId > 0) "Editar Usuario" else "Nuevo Usuario",
                style = MaterialTheme.typography.headlineMedium
            )

            // Contenedor visual para capturar o previsualizar la foto circular del usuario
            PhotoBox(imageUri = imageUri, circle = true, onTakePhoto = { showCamera = true })

            // Campo de texto: Nombre completo
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nombre") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            // Campo de texto: Email
            OutlinedTextField(
                value = email, onValueChange = { email = it; viewModel.clearMessage() },
                label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            // Campo de texto: Contraseña enmascarada
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // Menú desplegable para la Clave Foránea (FK) de TipoUsuario
            ExposedDropdownMenuBox(
                expanded = tipoExpanded,
                onExpandedChange = { tipoExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    // Muestra el nombre del rol correspondiente al id seleccionado
                    value = tipos.firstOrNull { it.id == tipoUsuarioId }?.tipoUsuario ?: "",
                    onValueChange = {},
                    readOnly = true, // Deshabilita escritura directa
                    label = { Text("Tipo de usuario") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                // Opciones del menú extraídas de Room
                ExposedDropdownMenu(
                    expanded = tipoExpanded,
                    onDismissRequest = { tipoExpanded = false }
                ) {
                    tipos.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.tipoUsuario) },
                            onClick = {
                                tipoUsuarioId = tipo.id // Actualiza la FK elegida
                                tipoExpanded = false
                            }
                        )
                    }
                }
            }

            // Muestra mensaje de error local o proveniente de Room
            (error ?: dbMessage)?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            // Guardado validado con reglas de negocio
            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = "El nombre es obligatorio"
                        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> error = "Email inválido"
                        password.length < 4 -> error = "La contraseña debe tener al menos 4 caracteres"
                        tipos.none { it.id == tipoUsuarioId } -> error = "Seleccione un tipo de usuario"
                        else -> {
                            error = null
                            viewModel.saveUser(userId, name.trim(), email.trim(), password, imageUri, tipoUsuarioId, onDone)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar Usuario") }

            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}