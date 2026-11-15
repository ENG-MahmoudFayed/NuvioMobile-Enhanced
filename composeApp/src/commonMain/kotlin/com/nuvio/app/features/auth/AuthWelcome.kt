package com.nuvio.app.features.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.CinemaAsyncImage
import com.nuvio.app.core.ui.CinemaBlockButton
import com.nuvio.app.core.ui.CinemaColors
import com.nuvio.app.core.ui.CinemaPagerDots
import com.nuvio.app.core.ui.pressScale
import com.nuvio.app.core.ui.rememberReduceMotion
import com.nuvio.app.features.settings.AppBrandWordmark
import com.nuvio.app.features.tmdb.TmdbTrendingArt
import com.nuvio.app.features.tmdb.TmdbTrendingArtwork
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.auth_welcome_caption
import nuvio.composeapp.generated.resources.auth_welcome_get_started
import nuvio.composeapp.generated.resources.auth_welcome_point_offline
import nuvio.composeapp.generated.resources.auth_welcome_point_profiles
import nuvio.composeapp.generated.resources.auth_welcome_point_progress
import nuvio.composeapp.generated.resources.auth_welcome_ready_title
import nuvio.composeapp.generated.resources.auth_welcome_wall_subtitle
import nuvio.composeapp.generated.resources.auth_welcome_wall_title
import nuvio.composeapp.generated.resources.compose_auth_sign_in
import org.jetbrains.compose.resources.stringResource

private const val WelcomePageCount = 2
private val WelcomeBottomReserve = 196.dp

/**
 * The first screen for someone without an account: a wall of this week's trending posters, two
 * pages of what Flowix does, and one red action. "Sign In" sits top right for returning viewers.
 */
@Composable
internal fun AuthWelcomeScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    menu: @Composable () -> Unit = {},
) {
    var posters by remember { mutableStateOf<List<TmdbTrendingArt>>(emptyList()) }
    LaunchedEffect(Unit) { posters = TmdbTrendingArtwork.load() }

    val pagerState = rememberPagerState(pageCount = { WelcomePageCount })
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF1B1519),
                    0.55f to Color(0xFF0A0809),
                    1f to Color(0xFF3D0A10),
                ),
            ),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> WelcomeWallPage(
                    posters = posters,
                    topPadding = statusBarTop + 72.dp,
                    bottomPadding = navigationBottom + WelcomeBottomReserve,
                )
                else -> WelcomeReadyPage(
                    posters = posters,
                    bottomPadding = navigationBottom + WelcomeBottomReserve,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarTop + 8.dp, start = 24.dp, end = 16.dp)
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBrandWordmark(modifier = Modifier.height(30.dp))
            Spacer(Modifier.weight(1f))
            menu()
            Spacer(Modifier.width(4.dp))
            WelcomeSignInButton(onClick = onSignIn)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = navigationBottom + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CinemaPagerDots(count = WelcomePageCount, current = pagerState.currentPage)
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(Res.string.auth_welcome_caption),
                color = CinemaColors.TextSecondary,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            CinemaBlockButton(
                text = stringResource(Res.string.auth_welcome_get_started),
                onClick = onGetStarted,
            )
        }
    }
}

@Composable
private fun WelcomeSignInButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Text(
        text = stringResource(Res.string.compose_auth_sign_in),
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF4A4A4A).copy(alpha = 0.85f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 9.dp),
    )
}

/** Page one: the poster wall in a rounded frame, with the promise set over its foot. */
@Composable
private fun WelcomeWallPage(
    posters: List<TmdbTrendingArt>,
    topPadding: Dp,
    bottomPadding: Dp,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding, bottom = bottomPadding - 40.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                .background(Color(0xFF141014)),
        ) {
            PosterMosaic(posters = posters, modifier = Modifier.fillMaxSize())
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.45f to Color.Transparent,
                            0.72f to Color(0xFF141014).copy(alpha = 0.88f),
                            1f to Color(0xFF141014),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.auth_welcome_wall_title),
                    color = Color.White,
                    fontSize = 32.sp,
                    lineHeight = 37.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.auth_welcome_wall_subtitle),
                    color = CinemaColors.TextSecondary,
                    fontSize = 17.sp,
                    lineHeight = 23.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Page two: the wall behind everything, dimmed, and what an account gives you. */
@Composable
private fun WelcomeReadyPage(
    posters: List<TmdbTrendingArt>,
    bottomPadding: Dp,
) {
    Box(Modifier.fillMaxSize()) {
        PosterMosaic(
            posters = posters.drop(9) + posters.take(9),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.45f },
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.25f),
                        0.45f to Color(0xFF0E0A0C).copy(alpha = 0.85f),
                        0.75f to Color(0xFF14080B),
                        1f to Color(0xFF3D0A10),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .padding(start = 32.dp, end = 32.dp, bottom = bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.auth_welcome_ready_title),
                color = Color.White,
                fontSize = 32.sp,
                lineHeight = 37.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                WelcomePoint(stringResource(Res.string.auth_welcome_point_offline))
                WelcomePoint(stringResource(Res.string.auth_welcome_point_progress))
                WelcomePoint(stringResource(Res.string.auth_welcome_point_profiles))
            }
        }
    }
}

@Composable
private fun WelcomePoint(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = CinemaColors.Red,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            color = Color(0xFFE6E6E6),
            fontSize = 19.sp,
            lineHeight = 24.sp,
        )
    }
}

/**
 * Three staggered columns of posters drifting slowly upward and back, so the wall reads as a
 * catalogue rather than a still. With no posters it draws the same grid as plain tiles.
 */
@Composable
internal fun PosterMosaic(
    posters: List<TmdbTrendingArt>,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReduceMotion()
    val drift by rememberInfiniteTransition(label = "poster_mosaic").animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(14_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "poster_mosaic_drift",
    )
    Row(
        modifier = modifier.padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(3) { column ->
            val stagger = when (column) {
                1 -> (-56).dp
                else -> (-8).dp
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = stagger)
                    .graphicsLayer {
                        val travel = 28.dp.toPx()
                        translationY = if (column == 1) drift * travel else -drift * travel
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(5) { row ->
                    val art = posters.getOrNull(column + row * 3)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if ((row + column) % 2 == 0) Color(0xFF241E22) else Color(0xFF1C1719)),
                    ) {
                        if (art != null) {
                            CinemaAsyncImage(
                                model = art.posterUrl,
                                contentDescription = null,
                                placeholderColor = Color.Transparent,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }
}
