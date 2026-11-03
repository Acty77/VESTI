package com.yoshua.vesti.navegations

// Importaciones para diseño en Compose, íconos y menú desplegable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// Importaciones de las pantallas visuales (UI)
import com.yoshua.vesti.view.ui.HomeScreen
import com.yoshua.vesti.view.ui.LoginScreen
import com.yoshua.vesti.view.ui.PrendaPerdidaScreen
import com.yoshua.vesti.view.ui.ProbadorFormScreen
import com.yoshua.vesti.view.ui.ProbadorListScreen
import com.yoshua.vesti.view.ui.UserFormScreen
import com.yoshua.vesti.view.ui.UserListScreen
// Importación de transiciones animadas entre pantallas
import com.yoshua.vesti.view.utils.navEnterTransition
import com.yoshua.vesti.view.utils.navExitTransition
import com.yoshua.vesti.view.utils.navPopEnterTransition
import com.yoshua.vesti.view.utils.navPopExitTransition
// Importación de ViewModels
import com.yoshua.vesti.viewmodels.LoginViewModel
import com.yoshua.vesti.viewmodels.PrendaPerdidaViewModel
import com.yoshua.vesti.viewmodels.ProbadorViewModel
import com.yoshua.vesti.viewmodels.TipoUsuarioViewModel
import com.yoshua.vesti.viewmodels.UserViewModel
import kotlinx.coroutines.launch

/** Rutas de navegación */
// Objeto Singleton que centraliza las cadenas de texto de las rutas para evitar errores ortográficos ("magic strings")
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROBADORES = "probadores"
    const val PROBADOR_FORM = "probador_form?id={id}"
    const val USERS = "users"
    const val USER_FORM = "user_form?id={id}"
    const val PRENDAS_PERDIDAS = "prendas_perdidas"

    fun probadorForm(id: Int = 0) = "probador_form?id=$id"
    fun userForm(id: Int = 0) = "user_form?id=$id"
}

// Clase de datos inmutable que representa cada ítem dentro del menú lateral deslizable
data class MenuItem(val route: String, val label: String, val icon: ImageVector)

/** Opciones del menú lateral */
// Lista estática que define las opciones globales navegables de la aplicación
val menuItems = listOf(
    MenuItem(Routes.HOME, "Inicio", Icons.Outlined.Home), // Opción Inicio
    MenuItem(Routes.PROBADORES, "Probadores", Icons.Outlined.DoorFront), // Opción Probadores
    MenuItem(Routes.USERS, "Usuarios", Icons.Outlined.People), // Opción Usuarios (Restringida para Vendedor)
    MenuItem(Routes.PRENDAS_PERDIDAS, "Prendas Perdidas", Icons.Outlined.Checkroom) // Opción Prendas Perdidas (Ícono de perchero)
)

// Función privada pura que mapea la ruta actual a un título legible para mostrar en la barra superior (TopAppBar)
private fun titleFor(route: String?): String = when (route) {
    Routes.HOME -> "Inicio"
    Routes.PROBADORES -> "Probadores"
    Routes.PROBADOR_FORM -> "Probador"
    Routes.USERS -> "Usuarios"
    Routes.USER_FORM -> "Usuario"
    Routes.PRENDAS_PERDIDAS -> "Prendas Perdidas"
    else -> "" // Si es Login o ruta desconocida, no muestra título
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel, // Inyección del ViewModel de Login para controlar el estado del usuario y la sesión
    probadorViewModel: ProbadorViewModel, // ViewModel para los probadores
    userViewModel: UserViewModel, // ViewModel para la gestión de usuarios
    tipoUsuarioViewModel: TipoUsuarioViewModel, // ViewModel para los roles de usuario
    prendaPerdidaViewModel: PrendaPerdidaViewModel // ViewModel para el registro de prendas perdidas
) {
    val navController = rememberNavController() // Mantiene y sobrevive el estado de la pila de navegación de Android
    val drawerState = rememberDrawerState(DrawerValue.Closed) // Guarda el estado del menú lateral (abierto o cerrado)
    val scope = rememberCoroutineScope() // Alcance de corrutinas vinculado al ciclo de vida del Composable para abrir/cerrar el drawer
    val backStackEntry by navController.currentBackStackEntryAsState() // Suscripción reactiva a la pantalla actual en la pila de navegación
    val currentRoute = backStackEntry?.destination?.route // Extrae la cadena de texto de la ruta en la que se encuentra el usuario
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle() // Escucha reactiva e ininterrumpida del estado del usuario logueado

    // 1. Verificación de rol: 1 es Administrador (Aplica la regla de negocio RBAC)
    val isAdmin = loginState.loggedUser?.tipoUsuarioId == 1

    // 2. Filtro correcto del menú: Si es admin ve todo, si no es admin, se oculta la opción 'Usuarios'
    val visibleMenuItems = menuItems.filter { item ->
        if (isAdmin) true else item.route != Routes.USERS
    }

    // Determina si la pantalla actual es Login (para ocultar barras y menú)
    val isLogin = currentRoute == null || currentRoute == Routes.LOGIN
    // Determina si la ruta actual pertenece al nivel principal (pantallas que muestran el ícono de hamburguesa)
    val isTopLevel = visibleMenuItems.any { it.route == currentRoute }

    // Función interna para navegar entre opciones principales limpiando la pila para evitar acumulación excesiva de memoria
    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = false } // Remueve las pantallas anteriores hasta HOME
            launchSingleTop = true // Evita duplicar la misma pantalla si se presiona varias veces seguidas
        }
    }

    // Componente principal de menú lateral deslizable de Material 3
    ModalNavigationDrawer(
        drawerState = drawerState, // Vincula el estado abierto/cerrado
        gesturesEnabled = !isLogin, // Deshabilita los gestos de deslizamiento en la pantalla de Login
        drawerContent = {
            ModalDrawerSheet { // Hoja visual del contenido del menú lateral
                Spacer(Modifier.height(16.dp)) // Espaciador vertical
                // Muestra el nombre del usuario logueado dinámicamente
                Text(
                    text = loginState.loggedUser?.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                // Muestra el correo electrónico del usuario logueado
                Text(
                    text = loginState.loggedUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp)) // Línea divisora visual

                // Renderiza dinámicamente los ítems del menú permitidos según el rol
                visibleMenuItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) }, // Nombre de la opción
                        icon = { Icon(item.icon, contentDescription = null) }, // Ícono de la opción
                        selected = currentRoute == item.route, // Marca como seleccionado si coincide con la ruta actual
                        onClick = {
                            scope.launch { drawerState.close() } // Cierra el menú asíncronamente
                            navigateTopLevel(item.route) // Executa la navegación
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp)) // Segunda línea divisora

                // Botón independiente para cerrar la sesión activa del usuario
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() } // Cierra el menú lateral
                        loginViewModel.logout() // Limpia los datos de la sesión activa en el ViewModel
                        navController.navigate(Routes.LOGIN) { // Redirige al login eliminando todo el historial
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        // Estructura contenedora del diseño visual básico (TopBar + Contenido)
        Scaffold(
            topBar = {
                if (!isLogin) { // Solo muestra la barra superior si NO está en la pantalla de Login
                    TopAppBar(
                        title = { Text(titleFor(currentRoute)) }, // Muestra el título correspondiente a la vista actual
                        navigationIcon = {
                            if (isTopLevel) {
                                // Si es pantalla principal, muestra el ícono de hamburguesa para abrir el menú
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            } else {
                                // Si es un formulario o pantalla secundaria, muestra la flecha para volver atrás
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors( // Aplica los colores definidos en el tema de la app
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        ) { innerPadding -> // Recibe el espacio dejado por la TopBar para no traslapar el contenido
            AppNavHost(
                navController = navController, // Control de navegación
                modifier = Modifier.padding(innerPadding), // Ajusta los márgenes interiores según el Scaffold
                loginViewModel = loginViewModel,
                probadorViewModel = probadorViewModel,
                userViewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                prendaPerdidaViewModel = prendaPerdidaViewModel, // <-- Pasa el ViewModel a AppNavHost
                currentUserId = loginState.loggedUser?.id, // ID del usuario logueado
                currentUserName = loginState.loggedUser?.name ?: "", // Nombre del usuario logueado
                isAdmin = isAdmin, // <-- Se pasa la propiedad de Admin para aplicar restricciones en las pantallas
                onNavigateTopLevel = ::navigateTopLevel // Referencia a la función de navegación principal
            )
        }
    }
}

// Composable privado que contiene la configuración del grafo de navegación (NavHost)
@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    loginViewModel: LoginViewModel,
    probadorViewModel: ProbadorViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    prendaPerdidaViewModel: PrendaPerdidaViewModel, // <-- Recibe PrendaPerdidaViewModel
    currentUserId: Int?,
    currentUserName: String,
    isAdmin: Boolean, // <-- Recibe el flag de Admin
    onNavigateTopLevel: (String) -> Unit
) {
    // Configuración del argumento de ruta 'id' asignando el tipo entero y valor por defecto 0
    val idArg = listOf(navArgument("id") { type = NavType.IntType; defaultValue = 0 })

    // Grafo de navegación que contiene el registro de todas las pantallas accesibles en la app
    NavHost(
        navController = navController, // Controlador de la pila de pantallas
        startDestination = Routes.LOGIN, // Pantalla de inicio obligatoria
        modifier = modifier,
        enterTransition = navEnterTransition, // Animación al entrar a una pantalla
        exitTransition = navExitTransition, // Animación al salir de una pantalla
        popEnterTransition = navPopEnterTransition, // Animación al volver hacia una pantalla
        popExitTransition = navPopExitTransition // Animación al destruirse una pantalla al volver atrás
    ) {
        // Registro de la ruta de Login
        composable(Routes.LOGIN) {
            LoginScreen(loginViewModel) {
                navController.navigate(Routes.HOME) { // Tras login exitoso, navega a Home eliminando la vista de Login
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }
        // Registro de la ruta de Inicio (Home)
        composable(Routes.HOME) {
            HomeScreen(
                userName = currentUserName,
                isAdmin = isAdmin, // <-- Pasa el rol a HomeScreen para adaptar la bienvenida y opciones
                onNavigate = onNavigateTopLevel
            )
        }
        // Registro de la ruta de Listado de Probadores
        composable(Routes.PROBADORES) {
            ProbadorListScreen(
                viewModel = probadorViewModel,
                isAdmin = isAdmin, // <-- Pasa el rol a ProbadorListScreen para ocultar o mostrar acciones de Admin
                onAdd = { navController.navigate(Routes.probadorForm()) }, // Redirige a crear probador
                onEdit = { id -> navController.navigate(Routes.probadorForm(id)) } // Redirige a editar probador
            )
        }
        // Registro de la ruta del Formulario de Probador (Soporta creación o edición mediante el parámetro 'id')
        composable(Routes.PROBADOR_FORM, arguments = idArg) { entry ->
            ProbadorFormScreen(
                viewModel = probadorViewModel,
                probadorId = entry.arguments?.getInt("id") ?: 0, // Extrae el id enviado en la ruta o usa 0 por defecto
                onDone = { navController.popBackStack() } // Vuelve al listado al terminar la operación
            )
        }
        // Registro de la ruta del Listado de Usuarios
        composable(Routes.USERS) {
            UserListScreen(
                viewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                currentUserId = currentUserId,
                isAdmin = isAdmin, // <-- Pasa el rol a UserListScreen
                onAdd = { navController.navigate(Routes.userForm()) }, // Redirige a crear usuario
                onEdit = { id -> navController.navigate(Routes.userForm(id)) } // Redirige a editar usuario
            )
        }
        // Registro de la ruta del Formulario de Usuario (Soporta creación o edición)
        composable(Routes.USER_FORM, arguments = idArg) { entry ->
            UserFormScreen(
                viewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                userId = entry.arguments?.getInt("id") ?: 0, // Extrae el id o usa 0
                onDone = { navController.popBackStack() } // Vuelve al listado de usuarios al terminar
            )
        }
        // Registro de la ruta del Registro de Prendas Perdidas
        composable(Routes.PRENDAS_PERDIDAS) {
            PrendaPerdidaScreen(
                onGuardar = { num, desc, foto ->
                    prendaPerdidaViewModel.guardarPrenda(num, desc, foto) {
                        navController.popBackStack() // Vuelve a la pantalla anterior tras guardar con éxito en la BD
                    }
                }
            )
        }
    }
}