package com.yoshua.vesti.navegations

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
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
import com.yoshua.vesti.view.ui.HomeScreen
import com.yoshua.vesti.view.ui.LoginScreen
import com.yoshua.vesti.view.ui.ProbadorFormScreen
import com.yoshua.vesti.view.ui.ProbadorListScreen
import com.yoshua.vesti.view.ui.UserFormScreen
import com.yoshua.vesti.view.ui.UserListScreen
import com.yoshua.vesti.view.utils.navEnterTransition
import com.yoshua.vesti.view.utils.navExitTransition
import com.yoshua.vesti.view.utils.navPopEnterTransition
import com.yoshua.vesti.view.utils.navPopExitTransition
import com.yoshua.vesti.viewmodels.LoginViewModel
import com.yoshua.vesti.viewmodels.ProbadorViewModel
import com.yoshua.vesti.viewmodels.TipoUsuarioViewModel
import com.yoshua.vesti.viewmodels.UserViewModel
import kotlinx.coroutines.launch

/** Rutas de navegación */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROBADORES = "probadores"
    const val PROBADOR_FORM = "probador_form?id={id}"
    const val USERS = "users"
    const val USER_FORM = "user_form?id={id}"

    fun probadorForm(id: Int = 0) = "probador_form?id=$id"
    fun userForm(id: Int = 0) = "user_form?id=$id"
}

data class MenuItem(val route: String, val label: String, val icon: ImageVector)

/** Opciones del menú lateral */
val menuItems = listOf(
    MenuItem(Routes.HOME, "Inicio", Icons.Outlined.Home),
    MenuItem(Routes.PROBADORES, "Probadores", Icons.Outlined.DoorFront),
    MenuItem(Routes.USERS, "Usuarios", Icons.Outlined.People)
)

private fun titleFor(route: String?): String = when (route) {
    Routes.HOME -> "Inicio"
    Routes.PROBADORES -> "Probadores"
    Routes.PROBADOR_FORM -> "Probador"
    Routes.USERS -> "Usuarios"
    Routes.USER_FORM -> "Usuario"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    probadorViewModel: ProbadorViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // 1. Verificación de rol: 1 es Administrador
    val isAdmin = loginState.loggedUser?.tipoUsuarioId == 1

    // 2. Filtro correcto del menú: Si es admin ve todo, si no, se oculta 'Usuarios'
    val visibleMenuItems = menuItems.filter { item ->
        if (isAdmin) true else item.route != Routes.USERS
    }

    val isLogin = currentRoute == null || currentRoute == Routes.LOGIN
    val isTopLevel = visibleMenuItems.any { it.route == currentRoute }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = false }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isLogin,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = loginState.loggedUser?.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    text = loginState.loggedUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                visibleMenuItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTopLevel(item.route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        loginViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isLogin) {
                    TopAppBar(
                        title = { Text(titleFor(currentRoute)) },
                        navigationIcon = {
                            if (isTopLevel) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            } else {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                loginViewModel = loginViewModel,
                probadorViewModel = probadorViewModel,
                userViewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                currentUserId = loginState.loggedUser?.id,
                currentUserName = loginState.loggedUser?.name ?: "",
                isAdmin = isAdmin, // <-- Se pasa la propiedad de Admin
                onNavigateTopLevel = ::navigateTopLevel
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    loginViewModel: LoginViewModel,
    probadorViewModel: ProbadorViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    currentUserId: Int?,
    currentUserName: String,
    isAdmin: Boolean, // <-- Recibe el flag de Admin
    onNavigateTopLevel: (String) -> Unit
) {
    val idArg = listOf(navArgument("id") { type = NavType.IntType; defaultValue = 0 })

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,
        enterTransition = navEnterTransition,
        exitTransition = navExitTransition,
        popEnterTransition = navPopEnterTransition,
        popExitTransition = navPopExitTransition
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(loginViewModel) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }
        composable(Routes.HOME) {
            HomeScreen(
                userName = currentUserName,
                isAdmin = isAdmin, // <-- Pasa el rol a HomeScreen
                onNavigate = onNavigateTopLevel
            )
        }
        composable(Routes.PROBADORES) {
            ProbadorListScreen(
                viewModel = probadorViewModel,
                onAdd = { navController.navigate(Routes.probadorForm()) },
                onEdit = { id -> navController.navigate(Routes.probadorForm(id)) }
            )
        }
        composable(Routes.PROBADOR_FORM, arguments = idArg) { entry ->
            ProbadorFormScreen(
                viewModel = probadorViewModel,
                probadorId = entry.arguments?.getInt("id") ?: 0,
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.USERS) {
            UserListScreen(
                viewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                currentUserId = currentUserId,
                isAdmin = isAdmin, // <-- Pasa el rol a UserListScreen
                onAdd = { navController.navigate(Routes.userForm()) },
                onEdit = { id -> navController.navigate(Routes.userForm(id)) }
            )
        }
        composable(Routes.USER_FORM, arguments = idArg) { entry ->
            UserFormScreen(
                viewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                userId = entry.arguments?.getInt("id") ?: 0,
                onDone = { navController.popBackStack() }
            )
        }
    }
}