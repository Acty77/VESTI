package com.yoshua.vesti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yoshua.vesti.navegations.AppNavigation
import com.yoshua.vesti.view.theme.AppTheme
import com.yoshua.vesti.viewmodels.LoginViewModel
import com.yoshua.vesti.viewmodels.ProbadorViewModel
import com.yoshua.vesti.viewmodels.TipoUsuarioViewModel
import com.yoshua.vesti.viewmodels.UserViewModel
import com.yoshua.vesti.viewmodels.PrendaPerdidaViewModel

class MainActivity : ComponentActivity() {

    // Instanciación de ViewModels mediante el delegado Kotlin 'by viewModels()'.
    // Esto asegura que la instancia esté vinculada al ciclo de vida de la MainActivity,
    // reteniendo su estado internamente tras cambios de configuración (como la rotación de pantalla).
    private val loginViewModel: LoginViewModel by viewModels()
    private val probadorViewModel: ProbadorViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val tipoUsuarioViewModel: TipoUsuarioViewModel by viewModels()
    private val prendaPerdidaViewModel: PrendaPerdidaViewModel by viewModels()

    // Método de ciclo de vida invocado cuando Android crea la Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
            Habilita el diseño de pantalla completa (Edge-to-Edge) permitiendo que la UI
            se extienda por debajo de las barras del sistema (barra de estado y de navegación).
        */
        enableEdgeToEdge()

        // Establece el contenido visual reemplazando los antiguos layouts XML por composables de Jetpack Compose
        setContent {
            // Aplica el tema global personalizado (colores, tipografía y formas de la app)
            AppTheme {
                // Inicia la estructura de navegación e inyecta las instancias compartidas de los ViewModels
                AppNavigation(loginViewModel, probadorViewModel, userViewModel, tipoUsuarioViewModel, prendaPerdidaViewModel)
            }
        }
    }
}