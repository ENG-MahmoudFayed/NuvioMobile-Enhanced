package com.nuvio.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nuvio.app.features.settings.AppBrandMark
import com.nuvio.app.features.settings.AppBrandWordmark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal const val AppIntroMinDurationMs = 1300L

/**
 * The opening frames on pure black: the Flowix mark appears on its own, then gives way to the
 * full wordmark, the way a studio card opens a film.
 */
@Composable
internal fun AppIntroContent(modifier: Modifier = Modifier) {
    val markAlpha = remember { Animatable(0f) }
    val markScale = remember { Animatable(0.82f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val wordmarkScale = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        launch { markAlpha.animateTo(1f, tween(durationMillis = 260, easing = FastOutSlowInEasing)) }
        markScale.animateTo(1f, tween(durationMillis = 420, easing = FastOutSlowInEasing))
        delay(180)
        launch { markAlpha.animateTo(0f, tween(durationMillis = 220)) }
        launch { markScale.animateTo(1.18f, tween(durationMillis = 260, easing = FastOutSlowInEasing)) }
        launch { wordmarkAlpha.animateTo(1f, tween(durationMillis = 260, easing = FastOutSlowInEasing)) }
        wordmarkScale.animateTo(1f, tween(durationMillis = 380, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        AppBrandMark(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer {
                    alpha = markAlpha.value
                    scaleX = markScale.value
                    scaleY = markScale.value
                },
        )
        AppBrandWordmark(
            modifier = Modifier
                .height(56.dp)
                .graphicsLayer {
                    alpha = wordmarkAlpha.value
                    scaleX = wordmarkScale.value
                    scaleY = wordmarkScale.value
                },
        )
    }
}
