package com.yoshua.vesti.view.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshua.vesti.repository.models.User
import com.yoshua.vesti.viewmodels.TipoUsuarioViewModel
import com.yoshua.vesti.viewmodels.UserViewModel

@Composable
fun UserListScreen(
    viewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    currentUserId: Int?,
    isAdmin: Boolean, // <-- Nuevo parámetro de rol
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<User?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (users.isEmpty()) {
            Text("No hay usuarios registrados.", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, style = MaterialTheme.typography.titleMedium)
                                Text(user.email, style = MaterialTheme.typography.bodyMedium)
                            }

                            // SOLO EL ADMIN PUEDE EDITAR Y ELIMINAR
                            if (isAdmin) {
                                IconButton(onClick = { onEdit(user.id) }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                                }
                                // Evitar que el Admin se elimine a sí mismo
                                if (user.id != currentUserId) {
                                    IconButton(onClick = { toDelete = user }) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SOLO EL ADMIN VE EL BOTÓN FLOTANTE (+) PARA AGREGAR
        if (isAdmin) {
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar usuario")
            }
        }
    }

    toDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Deseas eliminar al usuario ${user.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUser(user)
                    toDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}