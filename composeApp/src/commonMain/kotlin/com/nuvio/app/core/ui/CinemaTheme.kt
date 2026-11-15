package com.nuvio.app.core.ui

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The one place the Cinema look keeps its shared values. Screens take colours, spacing, corners,
 * motion and text styles from here instead of writing numbers inline.
 */
internal object CinemaColors {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF141414)
    val SurfaceElevated = Color(0xFF232323)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB3B3B3)
    val TextMuted = Color(0xFF8C8C8C)
    val Red = Color(0xFFE50914)
    val RedPressed = Color(0xFFB20710)

    val Pill = Color(0xFF2B2B2B)
    val PillOnImage = Color(0xB36D6D6E)
    val PillBorder = Color(0x33FFFFFF)
    val Field = Color(0xFF161616)
    val FieldBorder = Color(0xFF5A5A5A)
    val Sheet = SurfaceElevated
    val SheetRow = Pill
    val Card = Surface
    val CardBorder = Color(0xFF333333)
    val ProgressTrack = Color(0xFF5A5A5A)
    val Scrim = Color(0xCC000000)
}

internal object CinemaSpace {
    val s4 = 4.dp
    val s8 = 8.dp
    val s12 = 12.dp
    val s16 = 16.dp
    val s24 = 24.dp
    val s32 = 32.dp

    /** Left and right margin of every page. */
    val PageMargin = s16

    /** Space kept free under the last item of a list, above the floating tab bar. */
    val ListEndClearance = s16
}

internal object CinemaRadius {
    val Poster = 6.dp
    val Card = 12.dp
    val Sheet = 28.dp
    val Field = 4.dp
}

internal object CinemaShapes {
    val Poster = RoundedCornerShape(CinemaRadius.Poster)
    val Card = RoundedCornerShape(CinemaRadius.Card)
    val Pill = CircleShape
    val Field = RoundedCornerShape(CinemaRadius.Field)
    val ProfileAvatar = RoundedCornerShape(percent = 12)
}

internal object CinemaSize {
    /** Height of primary, secondary and block buttons. */
    val ButtonHeight = 48.dp

    /** Smallest touch target for anything tappable. */
    val MinTouchTarget = 44.dp

    val IconSmall = 20.dp
    val Icon = 24.dp
}

internal object CinemaMotion {
    const val MicroMillis = 180
    const val StandardMillis = 320
    const val SkeletonSweepMillis = 1200
    const val PressedScale = 0.96f
    const val PressedAlpha = 0.85f

    fun <T> micro(): FiniteAnimationSpec<T> = tween(MicroMillis)

    fun <T> standard(): FiniteAnimationSpec<T> = tween(StandardMillis)

    /** A spring that settles in about 0.35 s with a little give (damping ~0.8). */
    fun <T> spring(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = 320f)
}

/** Named text styles. Titles are semibold with natural spacing, never tightened. */
internal object CinemaType {
    val screenTitle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
            color = CinemaColors.TextPrimary,
        )

    val sectionTitle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
            color = CinemaColors.TextPrimary,
        )

    val cardTitle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 16.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
            color = CinemaColors.TextPrimary,
        )

    val body: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 15.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            color = CinemaColors.TextPrimary,
        )

    val caption: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            color = CinemaColors.TextSecondary,
        )

    val button: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        )
}
