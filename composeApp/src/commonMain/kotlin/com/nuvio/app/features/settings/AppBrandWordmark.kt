package com.nuvio.app.features.settings

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.MaterialTheme
import com.nuvio.app.core.ui.appTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun AppBrandWordmark(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    icon: AppIconOption? = null,
) {
    val state by remember {
        AppIconRepository.ensureLoaded()
        AppIconRepository.state
    }.collectAsStateWithLifecycle()
    val resource: DrawableResource = icon?.wordmarkResource
        ?: MaterialTheme.appTheme.wordmarkResource(state.selected)

    Crossfade(
        targetState = resource,
        animationSpec = tween(durationMillis = 350),
        label = "AppBrandWordmarkCrossfade",
        modifier = modifier,
    ) { targetResource ->
        Image(
            painter = painterResource(targetResource),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
        )
    }
}

/** The app icon on its own, as a rounded square: the brand mark beside page titles and on the splash. */
@Composable
internal fun AppBrandMark(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val state by remember {
        AppIconRepository.ensureLoaded()
        AppIconRepository.state
    }.collectAsStateWithLifecycle()
    Image(
        painter = painterResource(state.selected.previewResource),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(percent = 22)),
    )
}
