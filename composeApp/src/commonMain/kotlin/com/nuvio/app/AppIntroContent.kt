package com.nuvio.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.CinemaColors
import com.nuvio.app.core.ui.rememberReduceMotion
import com.nuvio.app.features.settings.AppBrandMark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** How long the intro holds the screen at most; data keeps loading underneath meanwhile. */
internal const val AppIntroMinDurationMs = 1100L

/** Same size as the iOS launch screen logo, so the hand-over shows no jump. */
private val IntroMarkSize = 80.dp

/**
 * The first frame matches the static launch screen: the Flowix mark alone in the middle of black.
 * It then grows a little while a red glow blooms behind it, and fades out toward the profile
 * picker. Under Reduce Motion the mark simply fades.
 */
@Composable
internal fun AppIntroContent(modifier: Modifier = Modifier) {
    val reduceMotion = rememberReduceMotion()
    val markScale = remember { Animatable(1f) }
    val glowAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            launch { glowAlpha.animateTo(1f, tween(durationMillis = 450, easing = FastOutSlowInEasing)) }
            markScale.animateTo(1.08f, tween(durationMillis = 550, easing = FastOutSlowInEasing))
        } else {
            delay(550)
        }
        delay(200)
        contentAlpha.animateTo(0f, tween(durationMillis = 300))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(IntroMarkSize * 3)
                .graphicsLayer { alpha = glowAlpha.value * contentAlpha.value }
                .background(
                    Brush.radialGradient(
                        0f to CinemaColors.Red.copy(alpha = 0.35f),
                        0.5f to CinemaColors.Red.copy(alpha = 0.12f),
                        1f to Color.Transparent,
                    ),
                ),
        )
        AppBrandMark(
            modifier = Modifier
                .size(IntroMarkSize)
                .graphicsLayer {
                    alpha = contentAlpha.value
                    scaleX = markScale.value
                    scaleY = markScale.value
                },
        )
    }
}
