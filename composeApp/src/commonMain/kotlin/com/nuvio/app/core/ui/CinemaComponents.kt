package com.nuvio.app.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/*
 * The building blocks of the Cinema look: black ground, white and grey pills, one red action,
 * rounded-square profile avatars. Values come from CinemaTheme.kt.
 */

internal val CinemaProfileAvatarShape = CinemaShapes.ProfileAvatar
internal val CinemaBlockButtonShape = CinemaShapes.Pill
internal val CinemaCategoryPillShape = RoundedCornerShape(CinemaRadius.Card)

internal enum class CinemaPillStyle { White, Grey, GreyOnImage }

/** Full-round button: white "Play", grey "Download", translucent "+ My List" over artwork. */
@Composable
internal fun CinemaPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: CinemaPillStyle = CinemaPillStyle.White,
    height: Dp = CinemaSize.ButtonHeight,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val container = when (style) {
        CinemaPillStyle.White -> Color.White
        CinemaPillStyle.Grey -> CinemaColors.Pill
        CinemaPillStyle.GreyOnImage -> CinemaColors.PillOnImage
    }
    val content = if (style == CinemaPillStyle.White) Color.Black else Color.White
    Row(
        modifier = modifier
            .pressScale(interaction)
            .height(height)
            .clip(CinemaShapes.Pill)
            .background(if (enabled) container else container.copy(alpha = 0.5f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = CinemaSpace.s16),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(CinemaSize.Icon))
            Spacer(Modifier.width(CinemaSpace.s8))
        }
        Text(
            text = text,
            style = CinemaType.button,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The one strong action on a screen: red "Get Started" / "Sign In", or white "Next". */
@Composable
internal fun CinemaBlockButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    white: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingIcon: ImageVector? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val container = if (white) Color.White else CinemaColors.Red
    val content = if (white) Color.Black else Color.White
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .height(CinemaSize.ButtonHeight)
            .clip(CinemaBlockButtonShape)
            .background(if (enabled) container else container.copy(alpha = 0.55f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = content,
                strokeWidth = 2.dp,
                modifier = Modifier.size(CinemaSize.IconSmall),
            )
        } else {
            Text(text = text, style = CinemaType.button, color = content)
            if (trailingIcon != null) {
                Spacer(Modifier.width(CinemaSpace.s12))
                Icon(trailingIcon, contentDescription = null, tint = content, modifier = Modifier.size(CinemaSize.Icon))
            }
        }
    }
}

/** "Shows", "Movies", "Categories ▾" under the Home title. */
@Composable
internal fun CinemaCategoryPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    showsDropdown: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val border by animateColorAsState(
        if (selected) Color.White else CinemaColors.PillBorder,
        animationSpec = CinemaMotion.micro(),
        label = "cinema_pill_border",
    )
    Row(
        modifier = modifier
            .pressScale(interaction)
            .heightIn(min = CinemaSize.MinTouchTarget)
            .clip(CinemaCategoryPillShape)
            .background(if (selected) Color.White.copy(alpha = 0.18f) else CinemaColors.PillOnImage.copy(alpha = 0.45f))
            .border(1.dp, border, CinemaCategoryPillShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = CinemaSpace.s16, vertical = CinemaSpace.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = CinemaType.cardTitle,
            maxLines = 1,
        )
        if (showsDropdown) {
            Spacer(Modifier.width(CinemaSpace.s4))
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(CinemaSize.IconSmall),
            )
        }
    }
}

/** The round × that clears a selected category, shown left of the pills. */
@Composable
internal fun CinemaClearPill(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .pressScale(interaction)
            .size(CinemaSize.MinTouchTarget)
            .clip(CircleShape)
            .background(CinemaColors.PillOnImage.copy(alpha = 0.45f))
            .border(1.dp, CinemaColors.PillBorder, CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Close, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(CinemaSize.IconSmall))
    }
}

/** Page header: optional brand mark, a large title, and icon actions on the right. */
@Composable
internal fun CinemaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    onTitleClickLabel: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = CinemaSpace.PageMargin, end = CinemaSpace.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onTitleClick != null) {
                        Modifier
                            .heightIn(min = CinemaSize.MinTouchTarget)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClickLabel = onTitleClickLabel,
                                role = Role.Button,
                                onClick = onTitleClick,
                            )
                    } else {
                        Modifier
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(CinemaSpace.s12))
            }
            Text(
                text = title,
                style = CinemaType.screenTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (titleTrailing != null) {
                Spacer(Modifier.width(CinemaSpace.s4))
                titleTrailing()
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/** A plain white icon in a thumb-sized target, for top bars and cards. */
@Composable
internal fun CinemaIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 26.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .pressScale(interaction)
            .size(CinemaSize.MinTouchTarget + 4.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size))
    }
}

/** Section heading on one line that shrinks down to 85% before it truncates. */
@Composable
internal fun CinemaSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    val style = CinemaType.sectionTitle
    BasicText(
        text = text,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(
            minFontSize = style.fontSize * 0.85f,
            maxFontSize = style.fontSize,
            stepSize = 0.5.sp,
        ),
        modifier = modifier,
    )
}

/**
 * A remote picture with a dark placeholder while it loads, a 0.2 s fade once it arrives, and the
 * title written in its place if it never does. The picture is cropped, never stretched.
 */
@Composable
internal fun CinemaAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackTitle: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: Color = CinemaColors.Surface,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    var failed by remember(model) { mutableStateOf(model == null) }
    val alpha = remember(model) { Animatable(0f) }
    var loaded by remember(model) { mutableStateOf(false) }
    LaunchedEffect(loaded) {
        if (loaded) alpha.animateTo(1f, tween(200))
    }
    Box(
        modifier = modifier.background(placeholderColor),
        contentAlignment = Alignment.Center,
    ) {
        if (model != null && !failed) {
            AsyncImage(
                model = model,
                contentDescription = contentDescription,
                contentScale = contentScale,
                onSuccess = { loaded = true },
                onError = { failed = true },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha.value },
            )
        }
        if (failed && !fallbackTitle.isNullOrBlank()) {
            Text(
                text = fallbackTitle,
                style = CinemaType.cardTitle.copy(color = CinemaColors.TextSecondary),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(CinemaSpace.s12),
            )
        }
        overlay()
    }
}

/** White-ringed play button laid over artwork or beside a search result. */
@Composable
internal fun CinemaPlayCircle(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    filled: Boolean = true,
    contentDescription: String? = null,
) {
    Canvas(
        modifier = modifier
            .size(size)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            ),
    ) {
        val stroke = this.size.minDimension * 0.05f
        val radius = this.size.minDimension / 2f - stroke / 2f
        if (filled) {
            drawCircle(color = Color.Black.copy(alpha = 0.45f), radius = radius)
        }
        drawCircle(color = Color.White, radius = radius, style = Stroke(width = stroke))
        val triangle = this.size.minDimension * 0.30f
        val cx = center.x + triangle * 0.12f
        val path = Path().apply {
            moveTo(cx - triangle / 2.4f, center.y - triangle / 2f)
            lineTo(cx + triangle / 1.8f, center.y)
            lineTo(cx - triangle / 2.4f, center.y + triangle / 2f)
            close()
        }
        drawPath(path, Color.White)
    }
}

/** Red arc with a fading tail, turning once a second, shown while a profile loads. */
@Composable
internal fun CinemaRedSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
) {
    val reduceMotion = rememberReduceMotion()
    val transition = rememberInfiniteTransition(label = "cinema_spinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "cinema_spinner_rotation",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "cinema_spinner_pulse",
    )
    Canvas(
        modifier = modifier
            .size(size)
            .rotate(if (reduceMotion) 0f else rotation)
            .graphicsLayer { alpha = if (reduceMotion) pulse else 1f },
    ) {
        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2f
        drawArc(
            brush = Brush.sweepGradient(
                0f to Color.Transparent,
                0.25f to CinemaColors.Red.copy(alpha = 0.15f),
                0.75f to CinemaColors.Red,
                1f to CinemaColors.Red,
            ),
            startAngle = 0f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(this.size.width - strokePx, this.size.height - strokePx),
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
    }
}

/** Page dots under a pager: white for the current page, grey for the rest. */
@Composable
internal fun CinemaPagerDots(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val color by animateColorAsState(
                if (index == current) Color.White else Color(0xFF6E6E6E),
                animationSpec = CinemaMotion.micro(),
                label = "cinema_dot_color",
            )
            val dotSize by animateDpAsState(
                if (index == current) 10.dp else 8.dp,
                animationSpec = CinemaMotion.micro(),
                label = "cinema_dot_size",
            )
            Box(
                Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

/**
 * Thin red progress line over a grey track, as under Continue Watching posters. It fills from
 * the left the first time it appears.
 */
@Composable
internal fun CinemaProgressLine(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    rounded: Boolean = true,
) {
    val reduceMotion = rememberReduceMotion()
    val target = progress.coerceIn(0f, 1f)
    val fill = remember { Animatable(if (reduceMotion) target else 0f) }
    LaunchedEffect(target) {
        fill.animateTo(target, CinemaMotion.standard())
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(if (rounded) CircleShape else RoundedCornerShape(0.dp))
            .background(CinemaColors.ProgressTrack),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fill.value)
                .height(height)
                .background(CinemaColors.Red),
        )
    }
}

/** Small red tag at the foot of a poster: "New Episode", "Recently Added". */
@Composable
internal fun CinemaPosterTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        style = CinemaType.caption.copy(color = Color.White),
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            .background(CinemaColors.Red)
            .padding(horizontal = CinemaSpace.s8, vertical = 3.dp),
    )
}

/** Grey block that a list shows while its rows load; it carries the shared shimmer. */
@Composable
internal fun CinemaSkeletonBlock(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = CinemaShapes.Poster,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(rememberCinemaShimmerBrush()),
    )
}

/**
 * The loading shimmer: a soft highlight sweeping left to right every 1.2 s. With Reduce Motion it
 * stays still.
 */
@Composable
internal fun rememberCinemaShimmerBrush(): Brush {
    val reduceMotion = rememberReduceMotion()
    val base = CinemaColors.Surface
    val highlight = Color(0xFF262626)
    if (reduceMotion) return Brush.linearGradient(listOf(base, base))
    val transition = rememberInfiniteTransition(label = "cinema_shimmer")
    val sweep by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = CinemaMotion.SkeletonSweepMillis, easing = LinearEasing),
            RepeatMode.Restart,
        ),
        label = "cinema_shimmer_sweep",
    )
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(sweep, 0f),
        end = Offset(sweep + 400f, 0f),
    )
}

/** Empty state: an outline icon, one bold line and a softer hint, centred. */
@Composable
internal fun CinemaEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CinemaSpace.s32, vertical = CinemaSpace.s32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CinemaSpace.s12),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(CinemaColors.SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = CinemaColors.TextSecondary, modifier = Modifier.size(34.dp))
        }
        Text(text = title, style = CinemaType.sectionTitle, textAlign = TextAlign.Center)
        Text(text = message, style = CinemaType.body.copy(color = CinemaColors.TextSecondary), textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(CinemaSpace.s4))
            CinemaPillButton(
                text = actionLabel,
                onClick = onAction,
                style = CinemaPillStyle.White,
                modifier = Modifier.defaultMinSize(minWidth = 140.dp),
            )
        }
    }
}
