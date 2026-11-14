package com.nuvio.app.core.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

internal const val NuvioPressedScale = 0.95f

/** Shrinks the element while [interactionSource] is pressed and springs it back on release. */
@Composable
internal fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = NuvioPressedScale,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "press_scale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
