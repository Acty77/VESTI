package com.yoshua.vesti.view.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavBackStackEntry


@Composable
fun AnimacionEntradaFormulario(
    visible: Boolean, // Controla la activación de la animación
    content: @Composable AnimatedVisibilityScope.() -> Unit // Contenido hijo a animar
) {
    // Componente de Jetpack Compose que gestiona la entrada/salida animada del árbol de UI
    AnimatedVisibility(
        visible = visible,
        // Combina desvanecimiento (fadeIn) y desplazamiento vertical suave desde abajo (slideInVertically) de 500ms
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 },
        content = content
    )
}

@Composable
fun Modifier.pulsoYRotacion(): Modifier {
    // Gestor de transiciones infinitas que sobreviven a la recomposición de la UI
    val transition = rememberInfiniteTransition(label = "pulsoYRotacion")

    // Animación continua de escala entre 90% (0.9f) y 110% (1.1f)
    val scale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    // Animación continua de oscilación/rotación leve entre -8 grados y +8 grados
    val rotation by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "rotation"
    )

    // Aplica las transformaciones geométricas directamente a la capa de gráficos (graphicsLayer) para no forzar re-layouts
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationZ = rotation
    }
}


private const val DURACION_NAV = 400 // Duración estándar en milisegundos (400ms)

// Transición al entrar a una nueva pantalla (se desplaza de derecha a izquierda)
val navEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION_NAV)) +
            fadeIn(tween(DURACION_NAV))
}

// Transición de la pantalla que sale al avanzar (se desplaza hacia la izquierda)
val navExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION_NAV)) +
            fadeOut(tween(DURACION_NAV))
}

// Transición al regresar a la pantalla anterior (se desplaza de izquierda a derecha)
val navPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION_NAV)) +
            fadeIn(tween(DURACION_NAV))
}

// Transición de la pantalla destruida al presionar 'Atrás' (se desplaza hacia la derecha)
val navPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION_NAV)) +
            fadeOut(tween(DURACION_NAV))
}