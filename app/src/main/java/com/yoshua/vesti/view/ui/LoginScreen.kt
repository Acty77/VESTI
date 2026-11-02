package com.yoshua.vesti.view.ui // Ubicación dentro de las vistas de la aplicación

// Importaciones de Compose para animaciones, layouts, campos de texto y validaciones
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// Importación del modificador animado personalizado y del ViewModel de autenticación
import com.yoshua.vesti.view.utils.pulsoYRotacion
import com.yoshua.vesti.viewmodels.LoginViewModel

/**
 * Vista de inicio de sesión con validaciones en tiempo real, encriptación/ocultamiento de clave y animaciones.
 */
@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    // Escucha de forma reactiva el estado unificado de UI expuesto por el LoginViewModel
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Copias locales de las variables de error para garantizar Smart Casts nulos seguros en Kotlin
    val emailError = state.emailError
    val passwordError = state.passwordError

    // Efecto secundario: Monitorea si el usuario se ha autenticado con éxito para desencadenar la navegación a Home
    LaunchedEffect(state.loggedUser) {
        if (state.loggedUser != null) onLoginSuccess()
    }

    // Estructura vertical centrada para el formulario de acceso
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo: Contenedor circular con el icono Checkroom
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(112.dp)
                // Modificador personalizado que aplica una animación de pulso y rotación al logo
                .pulsoYRotacion()
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Checkroom,
                contentDescription = "Logo de VESTI",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "VESTI",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Gestión de probadores", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Campo de entrada de texto para el Correo Electrónico
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange, // Notifica al ViewModel cada cambio de texto
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
            trailingIcon = if (emailError != null) { // Muestra ícono de alerta solo si existe un error de validación
                { Icon(Icons.Outlined.Error, contentDescription = "Error en el email") }
            } else null,
            isError = emailError != null, // Destaca el borde del campo en color rojo si hay error
            supportingText = if (emailError != null) { // Despliega el texto descriptivo del error bajo el campo
                { Text(emailError) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Teclado optimizado para email
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Campo de entrada de texto para la Contraseña
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            trailingIcon = {
                // Botón interactivo para alternar la visibilidad del texto de la clave
                IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (state.passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (state.passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            isError = passwordError != null,
            supportingText = if (passwordError != null) {
                { Text(passwordError) }
            } else null,
            // Alterna la máscara del texto entre caracteres normales o puntos de contraseña de forma nativa
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Banner informativo para errores globales de autenticación (ej. contraseña incorrecta)
        state.error?.let { mensaje ->
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(mensaje, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        Spacer(Modifier.height(24.dp))
        // Botón de acción para procesar el ingreso
        Button(
            onClick = viewModel::login,
            enabled = !state.isLoading, // Se deshabilita durante la carga para evitar peticiones duplicadas
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (state.isLoading) {
                // Muestra spinner de carga mientras se consulta a la base de datos
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Ingresar")
            }
        }
        Spacer(Modifier.height(16.dp))
        // Leyenda para orientar en la evaluación del proyecto
        Text(
            "Usuario de prueba: admin@duoc.cl / admin123",
            style = MaterialTheme.typography.bodySmall
        )
    }
}