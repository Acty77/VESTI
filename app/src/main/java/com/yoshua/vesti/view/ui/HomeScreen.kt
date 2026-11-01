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
    isAdmin: Boolean, // <-- Pasar estado de Admin
    onNavigate: (String) -> Unit
) {
    // Filtrar accesos directos permitidos según el rol
    val accesosRapidos = menuItems.filter { item ->
        item.route != Routes.HOME && (isAdmin || item.route != Routes.USERS)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hola, $userName", style = MaterialTheme.typography.headlineMedium)
        Text("¿Qué deseas hacer?", style = MaterialTheme.typography.bodyLarge)

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