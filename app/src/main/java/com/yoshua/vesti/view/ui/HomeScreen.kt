package com.yoshua.vesti.view.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yoshua.vesti.navegations.Routes
import com.yoshua.vesti.navegations.menuItems

@Composable
fun HomeScreen(
    userName: String,
    isAdmin: Boolean,
    onNavigate: (String) -> Unit
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
                onClick = { onNavigate(item.route) },
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(item.label, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}