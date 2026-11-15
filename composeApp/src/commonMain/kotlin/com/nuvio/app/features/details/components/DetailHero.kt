package com.nuvio.app.features.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.nuvio.app.features.details.formatMetaReleaseLineForDetails
import com.nuvio.app.features.details.formatRuntimeForDisplay
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.heroStretchHeight
import com.nuvio.app.core.ui.heroStretchZoom
import com.nuvio.app.features.details.MetaDetails
import com.nuvio.app.features.player.MiniPlayerController
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.nuvio.app.core.ui.LocalVerticalBarPadding
import com.nuvio.app.core.ui.verticalBarWidth

@Composable
fun DetailHero(
    meta: MetaDetails,
    isTablet: Boolean = false,
    scrollOffset: () -> Int = { 0 },
    stretchPx: () -> Float = { 0f },
    contentMaxWidth: Dp = 560.dp,
    onHeightChanged: (Int) -> Unit = {},
    heroTrailerSourceUrl: String? = null,
    heroTrailerSourceAudioUrl: String? = null,
    heroTrailerReady: Boolean = false,
    heroTrailerPlayWhenReady: () -> Boolean = { false },
    heroTrailerMuted: Boolean = true,
    heroGradientColor: Color? = null,
    onBackdropLoaded: (Painter, ImageBitmap?) -> Unit = { _, _ -> },
    onHeroTrailerMuteToggle: () -> Unit = {},
    onHeroTrailerReady: () -> Unit = {},
    onHeroTrailerEnded: () -> Unit = {},
    onHeroTrailerError: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (!isTablet) {
            PhoneCinemaHero(
                meta = meta,
                imageHeight = (maxWidth - verticalBarWidth()) * (9f / 16f),
                stretchPx = stretchPx,
                onHeightChanged = onHeightChanged,
                heroTrailerSourceUrl = heroTrailerSourceUrl,
                heroTrailerSourceAudioUrl = heroTrailerSourceAudioUrl,
                heroTrailerReady = heroTrailerReady,
                heroTrailerPlayWhenReady = heroTrailerPlayWhenReady,
                heroTrailerMuted = heroTrailerMuted,
                onBackdropLoaded = onBackdropLoaded,
                onHeroTrailerMuteToggle = onHeroTrailerMuteToggle,
                onHeroTrailerReady = onHeroTrailerReady,
                onHeroTrailerEnded = onHeroTrailerEnded,
                onHeroTrailerError = onHeroTrailerError,
            )
            return@BoxWithConstraints
        }
        val heroHeight = detailHeroHeight(maxWidth - verticalBarWidth(), isTablet)
        val trailerAlpha by animateFloatAsState(
            targetValue = if (heroTrailerReady) 1f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "detail_hero_trailer_alpha",
        )
        val muteIconSize = if (isTablet) 20.dp else 22.dp
        val heroChromeTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            8.dp +
            ((40.dp - muteIconSize) / 2)
        val bottomGradientColor = heroGradientColor ?: MaterialTheme.colorScheme.background
        var logoLoadError by remember(meta.id, meta.logo) {
            mutableStateOf(false)
        }
        val logoUrl = meta.logo?.takeIf { it.isNotBlank() }

        val heroBaseHeightPx = with(LocalDensity.current) { heroHeight.roundToPx() }
        LaunchedEffect(heroBaseHeightPx) { onHeightChanged(heroBaseHeightPx) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heroStretchHeight(heroHeight, stretchPx)
                .graphicsLayer {
                    clip = true
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                val imageUrl = meta.background ?: meta.poster
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = meta.name,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(heroHeight)
                            .heroStretchZoom(stretchPx)
                            .graphicsLayer {
                                translationY = scrollOffset() * 0.5f
                                scaleX = 1.08f
                                scaleY = 1.08f
                        },
                        alignment = if (isTablet) Alignment.TopCenter else Alignment.Center,
                        contentScale = ContentScale.Crop,
                        onSuccess = { state ->
                            onBackdropLoaded(
                                state.painter,
                                loadedBackdropImageBitmap(state.result),
                            )
                        },
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                    )
                }
                if (heroTrailerSourceUrl != null) {
                    HeroTrailerPlayerSurface(
                        sourceUrl = heroTrailerSourceUrl,
                        sourceAudioUrl = heroTrailerSourceAudioUrl,
                        playWhenReady = heroTrailerPlayWhenReady() && !MiniPlayerController.isActive,
                        muted = heroTrailerMuted,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(heroHeight)
                            .heroStretchZoom(stretchPx)
                            .graphicsLayer {
                                alpha = trailerAlpha
                                translationY = scrollOffset() * 0.5f
                                scaleX = 1.08f
                                scaleY = 1.08f
                            },
                        onReady = onHeroTrailerReady,
                        onEnded = onHeroTrailerEnded,
                        onError = onHeroTrailerError,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                enabled = heroTrailerReady,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onHeroTrailerMuteToggle,
                            ),
                    )
                    AnimatedContent(
                        targetState = heroTrailerMuted,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(LocalVerticalBarPadding.current)
                            .padding(
                                top = heroChromeTopPadding,
                                end = if (isTablet) 32.dp else 22.dp,
                            )
                            .graphicsLayer {
                                alpha = trailerAlpha * 0.72f
                            },
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(120)) + scaleIn(
                                initialScale = 0.82f,
                                animationSpec = tween(160),
                            )) togetherWith (fadeOut(animationSpec = tween(90)) + scaleOut(
                                targetScale = 1.12f,
                                animationSpec = tween(100),
                            ))
                        },
                        label = "detail_hero_trailer_mute_icon",
                    ) { muted ->
                        Icon(
                            imageVector = if (muted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(muteIconSize),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTablet) 360.dp else 320.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.00f to Color.Transparent,
                                    0.16f to bottomGradientColor.copy(alpha = 0.04f),
                                    0.32f to bottomGradientColor.copy(alpha = 0.14f),
                                    0.50f to bottomGradientColor.copy(alpha = 0.34f),
                                    0.68f to bottomGradientColor.copy(alpha = 0.62f),
                                    0.84f to bottomGradientColor.copy(alpha = 0.84f),
                                    1.00f to bottomGradientColor,
                                ),
                            ),
                        ),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isTablet) 32.dp else 18.dp)
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (logoUrl != null && !logoLoadError) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = stringResource(Res.string.detail_logo_content_description, meta.name),
                            modifier = Modifier
                                .fillMaxWidth(if (isTablet) 0.56f else 0.6f)
                                .widthIn(max = contentMaxWidth)
                                .height(if (isTablet) 72.dp else 80.dp),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Fit,
                            onError = { logoLoadError = true },
                        )
                    } else {
                        Text(
                            text = meta.name,
                            style = if (isTablet) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                    }

                    if (meta.genres.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = meta.genres.take(3).joinToString(" \u2022 "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

private fun detailHeroHeight(maxWidth: Dp, isTablet: Boolean): Dp =
    if (!isTablet) {
        (maxWidth * 1.33f).coerceIn(420.dp, 760.dp)
    } else {
        (maxWidth * 0.42f).coerceIn(300.dp, 420.dp)
    }

/**
 * The phone details header: the backdrop (or trailer) as a 16:9 card with rounded top corners
 * just under the status bar, then the title in bold on the left and a line of facts under it.
 */
@Composable
private fun PhoneCinemaHero(
    meta: MetaDetails,
    imageHeight: Dp,
    stretchPx: () -> Float,
    onHeightChanged: (Int) -> Unit,
    heroTrailerSourceUrl: String?,
    heroTrailerSourceAudioUrl: String?,
    heroTrailerReady: Boolean,
    heroTrailerPlayWhenReady: () -> Boolean,
    heroTrailerMuted: Boolean,
    onBackdropLoaded: (Painter, ImageBitmap?) -> Unit,
    onHeroTrailerMuteToggle: () -> Unit,
    onHeroTrailerReady: () -> Unit,
    onHeroTrailerEnded: () -> Unit,
    onHeroTrailerError: () -> Unit,
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val reportedHeightPx = with(LocalDensity.current) { (statusBarTop + 6.dp + imageHeight).roundToPx() }
    LaunchedEffect(reportedHeightPx) { onHeightChanged(reportedHeightPx) }
    val trailerAlpha by animateFloatAsState(
        targetValue = if (heroTrailerReady) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "detail_phone_trailer_alpha",
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(statusBarTop + 6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heroStretchHeight(imageHeight, stretchPx)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(Color(0xFF141414)),
        ) {
            val imageUrl = meta.background ?: meta.poster
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = meta.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .heroStretchZoom(stretchPx),
                    contentScale = ContentScale.Crop,
                    onSuccess = { state ->
                        onBackdropLoaded(
                            state.painter,
                            loadedBackdropImageBitmap(state.result),
                        )
                    },
                )
            }
            if (heroTrailerSourceUrl != null) {
                HeroTrailerPlayerSurface(
                    sourceUrl = heroTrailerSourceUrl,
                    sourceAudioUrl = heroTrailerSourceAudioUrl,
                    playWhenReady = heroTrailerPlayWhenReady() && !MiniPlayerController.isActive,
                    muted = heroTrailerMuted,
                    modifier = Modifier
                        .fillMaxSize()
                        .heroStretchZoom(stretchPx)
                        .graphicsLayer { alpha = trailerAlpha },
                    onReady = onHeroTrailerReady,
                    onEnded = onHeroTrailerEnded,
                    onError = onHeroTrailerError,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            enabled = heroTrailerReady,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onHeroTrailerMuteToggle,
                        ),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(34.dp)
                        .graphicsLayer { alpha = trailerAlpha }
                        .clip(RoundedCornerShape(17.dp))
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (heroTrailerMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = meta.name,
                color = Color.White,
                fontSize = 27.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            DetailHeroFactsRow(meta = meta)
        }
    }
}

/** "2013  16+  5 Seasons  IMDb 9.5" under the phone title. */
@Composable
internal fun DetailHeroFactsRow(meta: MetaDetails) {
    val year = formatMetaReleaseLineForDetails(meta)
    val ageBadge = meta.ageRating?.trim()?.takeIf { it.isNotBlank() }
    val seasonCount = remember(meta.videos) {
        meta.videos.mapNotNull { it.season }.filter { it > 0 }.distinct().size
    }
    val lengthText = when {
        seasonCount == 1 -> stringResource(Res.string.details_season_single)
        seasonCount > 1 -> stringResource(Res.string.details_seasons_count, seasonCount)
        else -> formatRuntimeForDisplay(meta.runtime)
    }
    val imdbRating = meta.imdbRating?.takeIf { raw -> raw.toDoubleOrNull()?.let { it > 0.0 } == true }
    if (year == null && ageBadge == null && lengthText == null && imdbRating == null) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        year?.let { Text(it, color = Color.White, fontSize = 18.sp) }
        ageBadge?.let { DetailHeroMetaBadge(text = it) }
        lengthText?.let { Text(it, color = Color.White, fontSize = 18.sp) }
        imdbRating?.let {
            Text("IMDb $it", color = ImdbYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
