package com.yoshua.vesti.view.ui // Paquete que contiene la capa de presentación (UI) de la aplicación

// Importaciones de Compose para el diseño de maquetación en columna, tarjetas e íconos
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// Importación de las rutas y la lista global de ítems del menú
import com.yoshua.vesti.navegations.Routes
import com.yoshua.vesti.navegations.menuItems

/**
 * Pantalla principal (Panel de bienvenida).
 * Muestra accesos directos dinámicos en función del rol del usuario (Admin o Vendedor).
 */
@Composable
fun HomeScreen(
    userName: String, // Nombre del usuario autenticado que viene desde el StateFlow del Login
    isAdmin: Boolean, // Flag que indica si el usuario posee rol de Administrador (tipoUsuarioId == 1)
    onNavigate: (String) -> Unit // Callback para ejecutar la navegación hacia la ruta seleccionada
) {
    // Filtrado de seguridad y UI: Oculta la ruta HOME de las tarjetas y restringe USERS si no es admin
    val accesosRapidos = menuItems.filter { item ->
        item.route != Routes.HOME && (isAdmin || item.route != Routes.USERS)
    }

    // Contenedor vertical principal que organiza los elementos de bienvenida y tarjetas
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp), // Ocupa toda la pantalla con un margen de 16dp
        verticalArrangement = Arrangement.spacedBy(12.dp) // Otorga una separación uniforme de 12dp entre elementos
    ) {
        // Mensaje de saludo personalizado con el nombre extraído del usuario logueado
        Text("Hola, $userName", style = MaterialTheme.typography.headlineMedium)
        Text("¿Qué deseas hacer?", style = MaterialTheme.typography.bodyLarge)

        // Iteración sobre la lista filtrada de accesos rápidos para renderizar una tarjeta por cada opción
        accesosRapidos.forEach { item ->
            Card(
                onClick = { onNavigate(item.route) }, // Al hacer clic, navega a la pantalla correspondiente
                modifier = Modifier.fillMaxWidth() // La tarjeta abarca todo el ancho disponible
            ) {
                // Disposición horizontal interna de la tarjeta: Ícono + Espaciador + Texto
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente al centro el ícono y texto
                ) {
                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp)) // Separación horizontal entre el ícono y la etiqueta
                    Text(item.label, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}