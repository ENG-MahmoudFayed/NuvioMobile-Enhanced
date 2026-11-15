package com.nuvio.app.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The building blocks of the Cinema look: black ground, white and grey pills, one red action,
 * rounded-square profile avatars. Screens take their colours from here so the look stays one
 * system instead of a hex value per file.
 */
internal object CinemaColors {
    val Red = Color(0xFFE50914)
    val RedPressed = Color(0xFFB20710)
    val Pill = Color(0xFF2B2B2B)
    val PillOnImage = Color(0xB36D6D6E)
    val PillBorder = Color(0x33FFFFFF)
    val Field = Color(0xFF161616)
    val FieldBorder = Color(0xFF5A5A5A)
    val TextSecondary = Color(0xFFB3B3B3)
    val TextMuted = Color(0xFF8C8C8C)
    val Sheet = Color(0xFF232323)
    val SheetRow = Color(0xFF2B2B2B)
    val Card = Color(0xFF141414)
    val CardBorder = Color(0xFF333333)
    val ProgressTrack = Color(0xFF5A5A5A)
}

internal val CinemaProfileAvatarShape = RoundedCornerShape(percent = 12)
internal val CinemaBlockButtonShape = RoundedCornerShape(4.dp)
internal val CinemaCategoryPillShape = RoundedCornerShape(12.dp)

internal enum class CinemaPillStyle { White, Grey, GreyOnImage }

/** Full-round button: white "Play", grey "Download", translucent "+ My List" over artwork. */
@Composable
internal fun CinemaPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: CinemaPillStyle = CinemaPillStyle.White,
    height: Dp = 48.dp,
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
            .clip(CircleShape)
            .background(if (enabled) container else container.copy(alpha = 0.5f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text,
            color = content,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
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
            .pressScale(interaction, pressedScale = 0.98f)
            .height(54.dp)
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
                modifier = Modifier.size(22.dp),
            )
        } else {
            Text(text = text, color = content, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (trailingIcon != null) {
                Spacer(Modifier.width(12.dp))
                Icon(trailingIcon, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
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
        label = "cinema_pill_border",
    )
    Row(
        modifier = modifier
            .pressScale(interaction)
            .heightIn(min = 40.dp)
            .clip(CinemaCategoryPillShape)
            .background(if (selected) Color.White.copy(alpha = 0.18f) else CinemaColors.PillOnImage.copy(alpha = 0.45f))
            .border(1.dp, border, CinemaCategoryPillShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        if (showsDropdown) {
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
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
            .size(40.dp)
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
        Icon(Icons.Rounded.Close, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(20.dp))
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
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onTitleClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
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
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (titleTrailing != null) {
                Spacer(Modifier.width(6.dp))
                titleTrailing()
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/** A plain white icon in a thumb-sized target, for the top bar. */
@Composable
internal fun CinemaIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 28.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .pressScale(interaction)
            .size(48.dp)
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

/** White-ringed play button laid over artwork or beside a search result. */
@Composable
internal fun CinemaPlayCircle(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    filled: Boolean = true,
) {
    Canvas(modifier = modifier.size(size)) {
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

/** Red spinner with a fading tail, shown while a profile loads. */
@Composable
internal fun CinemaRedSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    strokeWidth: Dp = 8.dp,
) {
    val transition = rememberInfiniteTransition(label = "cinema_spinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "cinema_spinner_rotation",
    )
    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotation),
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
                label = "cinema_dot_color",
            )
            val dotSize by animateDpAsState(if (index == current) 10.dp else 9.dp, label = "cinema_dot_size")
            Box(
                Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

/** Thin red progress line over a grey track, as under Continue Watching posters. */
@Composable
internal fun CinemaProgressLine(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(CinemaColors.ProgressTrack),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
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
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            .background(CinemaColors.Red)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
