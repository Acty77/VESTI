package com.yoshua.vesti.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta clara: azul marino + crema + camel + dorado (estilo boutique)
private val LightColors = lightColorScheme(
    primary = Color(0xFF16324F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3F5),
    onPrimaryContainer = Color(0xFF0C1E33),
    secondary = Color(0xFF8C6A4A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF1E4D3),
    onSecondaryContainer = Color(0xFF2E1D0B),
    tertiary = Color(0xFFC9A24A),
    onTertiary = Color(0xFF2B2100),
    tertiaryContainer = Color(0xFFFFE9B0),
    onTertiaryContainer = Color(0xFF241A00),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFAF7F2),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE7E0D8),
    onSurfaceVariant = Color(0xFF4A4640),
    outline = Color(0xFF7C766E),
    outlineVariant = Color(0xFFCEC8BF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F4EE),
    surfaceContainer = Color(0xFFF3EEE7),
    surfaceContainerHigh = Color(0xFFEEE8E0),
    surfaceContainerHighest = Color(0xFFE9E2D9)
)

// Paleta oscura: mismos tonos, más claros sobre fondo oscuro
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C8F0),
    onPrimary = Color(0xFF0C2A47),
    primaryContainer = Color(0xFF2A4A6B),
    onPrimaryContainer = Color(0xFFD6E3F5),
    secondary = Color(0xFFD9B99A),
    onSecondary = Color(0xFF3B2A17),
    secondaryContainer = Color(0xFF5A4630),
    onSecondaryContainer = Color(0xFFF1E4D3),
    tertiary = Color(0xFFE6C874),
    onTertiary = Color(0xFF3A2D00),
    tertiaryContainer = Color(0xFF554200),
    onTertiaryContainer = Color(0xFFFFE9A8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF14161A),
    onBackground = Color(0xFFE4E2E0),
    surface = Color(0xFF14161A),
    onSurface = Color(0xFFE4E2E0),
    surfaceVariant = Color(0xFF3F4650),
    onSurfaceVariant = Color(0xFFC2C7CF),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF3F4650),
    surfaceContainerLowest = Color(0xFF0F1114),
    surfaceContainerLow = Color(0xFF1A1D21),
    surfaceContainer = Color(0xFF1E2125),
    surfaceContainerHigh = Color(0xFF282B30),
    surfaceContainerHighest = Color(0xFF33363B)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
