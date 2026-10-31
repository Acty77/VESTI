package com.yoshua.vesti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yoshua.vesti.navegations.AppNavigation
import com.yoshua.vesti.view.theme.AppTheme
import com.yoshua.vesti.viewmodels.LoginViewModel
import com.yoshua.vesti.viewmodels.ProductViewModel
import com.yoshua.vesti.viewmodels.TipoUsuarioViewModel
import com.yoshua.vesti.viewmodels.UserViewModel

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
