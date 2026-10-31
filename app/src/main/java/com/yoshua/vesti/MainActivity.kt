package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.app.navegations.AppNavigation
import com.example.app.view.theme.AppTheme
import com.example.app.viewmodels.LoginViewModel
import com.example.app.viewmodels.ProductViewModel
import com.example.app.viewmodels.TipoUsuarioViewModel
import com.example.app.viewmodels.UserViewModel

class MainActivity : ComponentActivity() {

    // ViewModels (capa VM de MVVM) con alcance de la Activity
    private val loginViewModel: LoginViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val tipoUsuarioViewModel: TipoUsuarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppNavigation(loginViewModel, productViewModel, userViewModel, tipoUsuarioViewModel)
            }
        }
    }
}
