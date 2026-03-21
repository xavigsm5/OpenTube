package com.opentube.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animatable para controlar el progreso de 0f a 1f
    val progress = remember { Animatable(0f) }
    // Animatable para el movimiento del triángulo (offset X)
    val triangleOffset = remember { Animatable(-50f) } // Empieza más a la izquierda

    LaunchedEffect(Unit) {
        // Animaciones rápidas concurrentes
        launch {
            triangleOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutLinearInEasing
                )
            )
        }
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = LinearEasing
            )
        )
        // Al terminar la animación rápida, navegar a la siguiente pantalla
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)), // Fondo negro puro/oscuro como en YouTube
        contentAlignment = Alignment.Center
    ) {
        // Contenedor visual más pequeño para coincidir con la reducción de tamaño de la imagen
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(220.dp) // Ancho fijo más compacto en lugar de ocupar toda la pantalla
                .height(80.dp)
        ) {
            // Icono de Play ("Triángulo blanco") que se mueve
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "OpenTube Logo",
                tint = Color.White,
                modifier = Modifier
                    .size(42.dp)
                    .offset(x = triangleOffset.value.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Barra de progreso roja
            LinearProgressIndicator(
                progress = progress.value,
                modifier = Modifier
                    .weight(1f) // Ocupa el resto del pequeño contenedor
                    .height(4.dp), // Altura delgada
                color = Color.Red,
                trackColor = Color(0xFF333333), // Gris oscuro de fondo
                strokeCap = StrokeCap.Round
            )
        }
    }
}
