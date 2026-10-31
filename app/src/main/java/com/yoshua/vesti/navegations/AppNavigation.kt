package com.example.app.navegations

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.app.view.utils.navEnterTransition
import com.example.app.view.utils.navExitTransition
import com.example.app.view.utils.navPopEnterTransition
import com.example.app.view.utils.navPopExitTransition
import com.example.app.view.ui.CameraScreen
import com.example.app.view.ui.HomeScreen
import com.example.app.view.ui.LoginScreen
import com.example.app.view.ui.ProductFormScreen
import com.example.app.view.ui.ProductListScreen
import com.example.app.view.ui.TipoUsuarioScreen
import com.example.app.view.ui.UserFormScreen
import com.example.app.view.ui.UserListScreen
import com.example.app.viewmodels.LoginViewModel
import com.example.app.viewmodels.ProductViewModel
import com.example.app.viewmodels.TipoUsuarioViewModel
import com.example.app.viewmodels.UserViewModel
import kotlinx.coroutines.launch

/** Rutas de navegación */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PRODUCTS = "products"
    const val PRODUCT_FORM = "product_form?id={id}"
    const val USERS = "users"
    const val USER_FORM = "user_form?id={id}"
    const val CAMERA = "camera"
    const val TIPOS = "tipos_usuario"

    fun productForm(id: Int = 0) = "product_form?id=$id"
    fun userForm(id: Int = 0) = "user_form?id=$id"
}

data class MenuItem(val route: String, val label: String, val icon: ImageVector)

/** Opciones del menú lateral */
val menuItems = listOf(
    MenuItem(Routes.HOME, "Inicio", Icons.Default.Home),
    MenuItem(Routes.PRODUCTS, "Productos", Icons.Default.ShoppingCart),
    MenuItem(Routes.USERS, "Usuarios", Icons.Default.People),
    MenuItem(Routes.TIPOS, "Tipos de usuario", Icons.Default.Badge),
    MenuItem(Routes.CAMERA, "Cámara", Icons.Default.PhotoCamera)
)

private fun titleFor(route: String?): String = when (route) {
    Routes.HOME -> "Inicio"
    Routes.PRODUCTS -> "Productos"
    Routes.PRODUCT_FORM -> "Producto"
    Routes.USERS -> "Usuarios"
    Routes.USER_FORM -> "Usuario"
    Routes.CAMERA -> "Cámara"
    Routes.TIPOS -> "Tipos de usuario"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    val isLogin = currentRoute == null || currentRoute == Routes.LOGIN
    val isTopLevel = menuItems.any { it.route == currentRoute }

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
                    loginState.loggedUser?.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    loginState.loggedUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                menuItems.forEach { item ->
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
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                loginViewModel = loginViewModel,
                productViewModel = productViewModel,
                userViewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                currentUserId = loginState.loggedUser?.id,
                currentUserName = loginState.loggedUser?.name ?: "",
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
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    currentUserId: Int?,
    currentUserName: String,
    onNavigateTopLevel: (String) -> Unit
) {
    val idArg = listOf(navArgument("id") { type = NavType.IntType; defaultValue = 0 })

    // ANIMACIÓN 3 (view/utils/Animations.kt): deslizamiento horizontal entre pantallas
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
            HomeScreen(userName = currentUserName, onNavigate = onNavigateTopLevel)
        }
        composable(Routes.PRODUCTS) {
            ProductListScreen(
                viewModel = productViewModel,
                onAdd = { navController.navigate(Routes.productForm()) },
                onEdit = { id -> navController.navigate(Routes.productForm(id)) }
            )
        }
        composable(Routes.PRODUCT_FORM, arguments = idArg) { entry ->
            ProductFormScreen(
                viewModel = productViewModel,
                productId = entry.arguments?.getInt("id") ?: 0,
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.USERS) {
            UserListScreen(
                viewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                currentUserId = currentUserId,
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
        composable(Routes.CAMERA) {
            CameraScreen()
        }
        composable(Routes.TIPOS) {
            TipoUsuarioScreen(tipoUsuarioViewModel)
        }
    }
}
