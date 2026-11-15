package com.nuvio.app.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

internal const val NuvioPressedScale = CinemaMotion.PressedScale

/**
 * Shrinks the element and dims it slightly while [interactionSource] is pressed, then springs it
 * back on release. With Reduce Motion only the dimming remains.
 */
@Composable
internal fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = NuvioPressedScale,
): Modifier {
    val reduceMotion = rememberReduceMotion()
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) pressedScale else 1f,
        animationSpec = CinemaMotion.spring(),
        label = "press_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) CinemaMotion.PressedAlpha else 1f,
        animationSpec = CinemaMotion.micro(),
        label = "press_alpha",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}
