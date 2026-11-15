package com.nuvio.app.features.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.CinemaBlockButton
import com.nuvio.app.core.ui.CinemaColors
import com.nuvio.app.core.ui.CinemaPagerDots
import com.nuvio.app.features.settings.AppBrandWordmark
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.profiles_intro_got_it
import nuvio.composeapp.generated.resources.profiles_intro_household
import nuvio.composeapp.generated.resources.profiles_intro_label
import nuvio.composeapp.generated.resources.profiles_intro_next
import nuvio.composeapp.generated.resources.profiles_intro_own_space
import nuvio.composeapp.generated.resources.profiles_intro_progress
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private class IntroPage(
    val text: StringResource,
    val art: DrawScope.() -> Unit,
)

private val IntroPages = listOf(
    IntroPage(Res.string.profiles_intro_progress) { drawResumePhone() },
    IntroPage(Res.string.profiles_intro_own_space) { drawProfileCards() },
    IntroPage(Res.string.profiles_intro_household) { drawHousehold() },
)

/**
 * Shown once, right after the first sign-in: three pages on what a profile is for, each with an
 * illustration drawn from the app's own pieces (a resume bar, profile cards, a household of
 * avatars), then "Got It" carries on to the profile picker.
 */
@Composable
internal fun ProfilesIntroScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { IntroPages.size })
    val scope = rememberCoroutineScope()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val lastPage = pagerState.currentPage == IntroPages.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = statusBarTop + 48.dp, bottom = navigationBottom + 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppBrandWordmark(modifier = Modifier.height(40.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { index ->
            val page = IntroPages[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(
                        modifier = Modifier
                            .widthIn(max = 380.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        onDraw = page.art,
                    )
                }
                Text(
                    text = stringResource(Res.string.profiles_intro_label),
                    color = CinemaColors.TextSecondary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(page.text),
                    color = Color.White,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(max = 460.dp)
                        .height(110.dp)
                        .semantics { heading() },
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        CinemaPagerDots(count = IntroPages.size, current = pagerState.currentPage)
        Spacer(Modifier.height(36.dp))
        CinemaBlockButton(
            text = stringResource(if (lastPage) Res.string.profiles_intro_got_it else Res.string.profiles_intro_next),
            white = true,
            onClick = {
                if (lastPage) {
                    onFinished()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier
                .widthIn(max = 560.dp)
                .padding(horizontal = 24.dp),
        )
    }
}

private val GlowRed = Color(0xFFB0121D)
private val GlowViolet = Color(0xFF5B2A86)
private val ScreenBlue = Color(0xFF2A4BD7)

private fun DrawScope.drawGlow(center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            0f to GlowRed.copy(alpha = 0.55f),
            0.45f to GlowViolet.copy(alpha = 0.30f),
            1f to Color.Transparent,
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

/** A phone held at an angle, playing a title with its red resume bar part-way along. */
private fun DrawScope.drawResumePhone() {
    val w = size.width
    drawGlow(Offset(w * 0.5f, w * 0.52f), w * 0.48f)
    rotate(degrees = -9f, pivot = Offset(w * 0.5f, w * 0.5f)) {
        val phoneSize = Size(w * 0.42f, w * 0.76f)
        val phoneTopLeft = Offset(w * 0.5f - phoneSize.width / 2f, w * 0.12f)
        drawRoundRect(
            color = Color(0xFF1B2A8C),
            topLeft = phoneTopLeft,
            size = phoneSize,
            cornerRadius = CornerRadius(w * 0.05f),
        )
        val inset = w * 0.018f
        val screenTopLeft = phoneTopLeft + Offset(inset, inset)
        val screenSize = Size(phoneSize.width - inset * 2, phoneSize.height - inset * 2)
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color(0xFFC2344B), ScreenBlue, Color(0xFF3B82F6)),
                start = screenTopLeft,
                end = screenTopLeft + Offset(screenSize.width, screenSize.height),
            ),
            topLeft = screenTopLeft,
            size = screenSize,
            cornerRadius = CornerRadius(w * 0.04f),
        )
        val posterSize = Size(screenSize.width * 0.62f, screenSize.width * 0.62f * 1.5f)
        val posterTopLeft = Offset(
            screenTopLeft.x + (screenSize.width - posterSize.width) / 2f,
            screenTopLeft.y + screenSize.height * 0.14f,
        )
        drawRoundRect(
            color = Color(0xFF0D0F24).copy(alpha = 0.75f),
            topLeft = posterTopLeft,
            size = posterSize,
            cornerRadius = CornerRadius(w * 0.015f),
        )
        val playCenter = posterTopLeft + Offset(posterSize.width / 2f, posterSize.height / 2f)
        val playRadius = posterSize.width * 0.2f
        drawCircle(Color.White, radius = playRadius, center = playCenter, style = Stroke(width = w * 0.01f))
        val tri = playRadius * 0.9f
        drawPath(
            Path().apply {
                moveTo(playCenter.x - tri * 0.35f, playCenter.y - tri * 0.5f)
                lineTo(playCenter.x + tri * 0.55f, playCenter.y)
                lineTo(playCenter.x - tri * 0.35f, playCenter.y + tri * 0.5f)
                close()
            },
            Color.White,
        )
        val barY = posterTopLeft.y + posterSize.height + w * 0.04f
        drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(posterTopLeft.x, barY),
            end = Offset(posterTopLeft.x + posterSize.width, barY),
            strokeWidth = w * 0.014f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = CinemaColors.Red,
            start = Offset(posterTopLeft.x, barY),
            end = Offset(posterTopLeft.x + posterSize.width * 0.62f, barY),
            strokeWidth = w * 0.014f,
            cap = StrokeCap.Round,
        )
    }
}

/** Three profile cards fanned out, each with its own colour, avatar and list rows. */
private fun DrawScope.drawProfileCards() {
    val w = size.width
    drawGlow(Offset(w * 0.5f, w * 0.5f), w * 0.5f)
    val cards = listOf(
        Triple(-14f, Color(0xFF2563EB), Offset(-w * 0.2f, w * 0.04f)),
        Triple(10f, Color(0xFFDB2777), Offset(w * 0.2f, w * 0.04f)),
        Triple(0f, Color(0xFFF59E0B), Offset(0f, -w * 0.02f)),
    )
    val cardSize = Size(w * 0.38f, w * 0.54f)
    cards.forEach { (angle, colour, shift) ->
        val center = Offset(w * 0.5f, w * 0.5f) + shift
        rotate(degrees = angle, pivot = center) {
            val topLeft = center - Offset(cardSize.width / 2f, cardSize.height / 2f)
            drawRoundRect(
                color = Color(0xFF1E1E22),
                topLeft = topLeft,
                size = cardSize,
                cornerRadius = CornerRadius(w * 0.035f),
            )
            val avatar = cardSize.width * 0.46f
            drawRoundRect(
                color = colour,
                topLeft = topLeft + Offset((cardSize.width - avatar) / 2f, cardSize.height * 0.1f),
                size = Size(avatar, avatar),
                cornerRadius = CornerRadius(avatar * 0.16f),
            )
            repeat(3) { row ->
                val y = topLeft.y + cardSize.height * (0.62f + row * 0.11f)
                drawRoundRect(
                    color = Color.White.copy(alpha = if (row == 0) 0.75f else 0.28f),
                    topLeft = Offset(topLeft.x + cardSize.width * 0.14f, y),
                    size = Size(cardSize.width * (if (row == 0) 0.72f else 0.56f - row * 0.08f), w * 0.022f),
                    cornerRadius = CornerRadius(w * 0.011f),
                )
            }
        }
    }
}

/** A household: six avatar tiles in two rows, one for each profile an account can hold. */
private fun DrawScope.drawHousehold() {
    val w = size.width
    drawGlow(Offset(w * 0.5f, w * 0.5f), w * 0.5f)
    val colours = listOf(
        Color(0xFF2563EB), Color(0xFFDB2777), Color(0xFFF59E0B),
        Color(0xFF10B981), Color(0xFF8B5CF6), Color(0xFFE50914),
    )
    val tile = w * 0.22f
    val gap = w * 0.06f
    val gridWidth = tile * 3 + gap * 2
    val start = Offset((w - gridWidth) / 2f, w * 0.22f)
    colours.forEachIndexed { index, colour ->
        val column = index % 3
        val row = index / 3
        val topLeft = start + Offset(column * (tile + gap), row * (tile + gap * 1.6f))
        drawRoundRect(
            color = colour,
            topLeft = topLeft,
            size = Size(tile, tile),
            cornerRadius = CornerRadius(tile * 0.16f),
        )
        translate(topLeft.x, topLeft.y) {
            drawCircle(Color.White.copy(alpha = 0.9f), radius = tile * 0.13f, center = Offset(tile / 2f, tile * 0.4f))
            drawArc(
                color = Color.White.copy(alpha = 0.9f),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(tile * 0.25f, tile * 0.62f),
                size = Size(tile * 0.5f, tile * 0.5f),
                style = Stroke(width = tile * 0.06f, cap = StrokeCap.Round),
            )
        }
        drawRoundRect(
            color = Color.White.copy(alpha = 0.55f),
            topLeft = Offset(topLeft.x + tile * 0.2f, topLeft.y + tile + gap * 0.35f),
            size = Size(tile * 0.6f, w * 0.018f),
            cornerRadius = CornerRadius(w * 0.009f),
        )
    }
}
