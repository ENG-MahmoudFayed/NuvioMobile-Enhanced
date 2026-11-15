package com.nuvio.app.features.profiles

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.nuvio.app.core.ui.CinemaMotion
import com.nuvio.app.core.ui.rememberReduceMotion
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.rounded.Check
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.CinemaColors
import com.nuvio.app.features.auth.PosterMosaic
import com.nuvio.app.core.ui.CinemaProfileAvatarShape
import com.nuvio.app.features.tmdb.TmdbTrendingArt
import com.nuvio.app.features.tmdb.TmdbTrendingArtwork
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.ui.NuvioAsyncImage
import com.nuvio.app.core.ui.NuvioBackButton
import com.nuvio.app.core.ui.NuvioToastHost
import com.nuvio.app.core.ui.LocalVerticalBarPadding
import com.nuvio.app.features.membership.CosmeticEntitlement
import com.nuvio.app.features.settings.AppBrandWordmark
import com.nuvio.app.features.settings.SupporterBadgeIfPresent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileSelectionScreen(
    onProfileSelected: (NuvioProfile, Offset) -> Unit,
    onEditProfile: (NuvioProfile) -> Unit,
    onAddProfile: () -> Unit,
    onBack: (() -> Unit)? = null,
    interactionEnabled: Boolean = true,
    activeProfileIndex: Int? = null,
    contentVisible: Boolean = true,
    onSignInWithAccount: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val profileState by ProfileRepository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    // Paired with the tapped avatar's on-screen center at the moment of the tap, so once the PIN
    // is verified the caller can still glide the transition emblem out from that exact spot.
    var pendingPinSelection by remember { mutableStateOf<Pair<NuvioProfile, Offset>?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var biometricAuthenticating by remember { mutableStateOf(false) }

    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(20f) }
    val manageAlpha = remember { Animatable(0f) }
    // Drives the fade-out of everything below the header once a profile is tapped (Netflix-style
    // transition into AppLoadingContent). A plain Animatable rather than AnimatedVisibility's
    // built-in enter/exit so the supporter badge next to the wordmark — rendered as a sibling, not
    // a descendant, of whatever this is applied to — can sit outside it and stay fully visible.
    val contentFadeAlpha = remember { Animatable(1f) }
    val onProfileClick: (NuvioProfile, Offset) -> Unit = onProfileClick@{ profile, tapCenter ->
        if (!interactionEnabled || biometricAuthenticating) return@onProfileClick

        if (
            !isEditMode &&
            profile.profileIndex != activeProfileIndex &&
            profile.profileIndex == 1 &&
            profile.pinEnabled
        ) {
            biometricAuthenticating = true
            scope.launch {
                val result = try {
                    ProfileBiometricAuth.authenticate(profile.profileIndex, profile.userId)
                } catch (e: CancellationException) {
                    biometricAuthenticating = false
                    throw e
                } catch (_: Exception) {
                    // A platform failure is not authentication. Follow the existing PIN fallback.
                    ProfileBiometricResult.Failed
                }
                when (result) {
                    ProfileBiometricResult.Success -> {
                        biometricAuthenticating = false
                        onProfileSelected(profile, tapCenter)
                    }
                    ProfileBiometricResult.FallbackRequested -> {
                        biometricAuthenticating = false
                        pendingPinSelection = profile to tapCenter
                    }
                    ProfileBiometricResult.Invalidated,
                    -> {
                        biometricAuthenticating = false
                        pendingPinSelection = profile to tapCenter
                    }
                    ProfileBiometricResult.Unavailable,
                    ProfileBiometricResult.NotConfigured,
                    -> {
                        biometricAuthenticating = false
                        pendingPinSelection = profile to tapCenter
                    }

                    ProfileBiometricResult.Failed,
                    -> {
                        biometricAuthenticating = false
                        pendingPinSelection = profile to tapCenter
                    }
                    ProfileBiometricResult.Cancelled -> {
                        biometricAuthenticating = false
                    }
                }
            }
        } else {
            routeProfileSelection(
                profile = profile,
                isEditMode = isEditMode,
                activeProfileIndex = activeProfileIndex,
                onEditProfile = onEditProfile,
                onActiveProfileSelected = { scope.launch { showAlreadyActiveProfileToast(it) } },
                onPinRequired = { pendingPinSelection = it to tapCenter },
                onProfileSelected = { onProfileSelected(it, tapCenter) },
            )
        }
    }

    LaunchedEffect(Unit) {
        AvatarRepository.refreshAvatars()
    }

    LaunchedEffect(Unit) {
        launch { titleAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch { titleOffset.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
        delay(300)
        manageAlpha.animateTo(1f, tween(500))
    }

    LaunchedEffect(contentVisible) {
        contentFadeAlpha.animateTo(if (contentVisible) 1f else 0f, tween(250))
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val backgroundProfile = profileState.activeProfile ?: profileState.profiles.firstOrNull()
    var heroPosters by remember { mutableStateOf<List<TmdbTrendingArt>>(emptyList()) }
    LaunchedEffect(Unit) { heroPosters = TmdbTrendingArtwork.load() }
    // Where the first row of avatars is centred: the grey sheet's curved edge runs through it.
    var sheetTopPx by remember { mutableStateOf<Float?>(null) }
    var containerTopPx by remember { mutableStateOf(0f) }
    val avatarHalfPx = with(LocalDensity.current) { ProfileTileAvatarSize.toPx() / 2f }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onGloballyPositioned { containerTopPx = it.positionInRoot().y },
    ) {
        val isTabletLayout = maxWidth >= 768.dp
        val heroHeight = maxHeight * if (isTabletLayout) 0.58f else 0.62f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight),
        ) {
            ProfileBackgroundBackdrop(
                profile = backgroundProfile,
                modifier = Modifier.fillMaxSize(),
                fallback = {
                    if (heroPosters.isNotEmpty()) {
                        PosterMosaic(
                            posters = heroPosters,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = 0.7f },
                        )
                    }
                },
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            0f to Color.Transparent,
                            0.65f to Color.Black.copy(alpha = 0.35f),
                            1f to Color.Black,
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.55f),
                            0.18f to Color.Transparent,
                            0.6f to Color.Black.copy(alpha = 0.25f),
                            1f to Color.Black,
                        ),
                    ),
            )
            // Over the poster wall the wordmark sits in the middle; with no posters to show it
            // grows to fill the empty half on its own.
            val wordmarkHeight = when {
                heroPosters.isEmpty() -> if (isTabletLayout) 72.dp else 60.dp
                isTabletLayout -> 48.dp
                else -> 40.dp
            }
            // Not wrapped in `contentFadeAlpha` like the rest of this screen below: the
            // wordmark and the Supporter badge next to it both stay fully visible through the
            // tap-to-centre transition, while everything underneath fades away.
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = statusBarTop)
                    .graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffset.value
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppBrandWordmark(modifier = Modifier.height(wordmarkHeight))
                SupporterBadgeIfPresent(height = wordmarkHeight)
            }
        }

        sheetTopPx?.let { top ->
            Canvas(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = contentFadeAlpha.value },
            ) {
                val arc = size.width * 0.05f
                val path = Path().apply {
                    moveTo(0f, top + arc)
                    quadraticTo(size.width / 2f, top - arc, size.width, top + arc)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        0f to Color(0xFF3A3A3A),
                        0.35f to Color(0xFF2E2E2E),
                        1f to Color(0xFF262626),
                        startY = top - arc,
                        endY = size.height,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height((heroHeight - 54.dp).coerceAtLeast(160.dp)))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentFadeAlpha.value },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.profile_choose_your_profile),
                    color = CinemaColors.TextSecondary,
                    fontSize = 19.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffset.value
                    },
                )
                Spacer(Modifier.height(14.dp))

                val profiles = profileState.profiles
                val tiles = buildList<ProfileTile> {
                    profiles.forEach { add(ProfileTile.Profile(it)) }
                    if (profiles.size < MAX_PROFILES) add(ProfileTile.Add)
                    if (profiles.isNotEmpty()) add(ProfileTile.Edit)
                }
                val columns = if (isTabletLayout) tiles.size.coerceAtLeast(1) else 2
                tiles.chunked(columns).forEachIndexed { rowIndex, rowTiles ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            if (isTabletLayout) 28.dp else 36.dp,
                            Alignment.CenterHorizontally,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp)
                            .then(
                                if (rowIndex == 0) {
                                    Modifier.onGloballyPositioned { coordinates ->
                                        val top = coordinates.positionInRoot().y - containerTopPx + avatarHalfPx
                                        if (sheetTopPx != top) sheetTopPx = top
                                    }
                                } else {
                                    Modifier
                                },
                            ),
                    ) {
                        rowTiles.forEachIndexed { columnIndex, tile ->
                            val animDelay = (rowIndex * columns + columnIndex) * 50
                            when (tile) {
                                is ProfileTile.Profile -> ProfileAvatarCard(
                                    profile = tile.profile,
                                    isEditMode = isEditMode,
                                    animDelay = animDelay,
                                    enabled = interactionEnabled,
                                    onClick = { tapCenter -> onProfileClick(tile.profile, tapCenter) },
                                )
                                ProfileTile.Add -> ProfileActionTile(
                                    icon = Icons.Rounded.Add,
                                    label = stringResource(Res.string.profile_add_short),
                                    animDelay = animDelay,
                                    enabled = interactionEnabled,
                                    onClick = onAddProfile,
                                )
                                ProfileTile.Edit -> ProfileActionTile(
                                    icon = if (isEditMode) Icons.Rounded.Check else Icons.Rounded.Edit,
                                    label = stringResource(
                                        if (isEditMode) Res.string.action_done else Res.string.action_edit,
                                    ),
                                    animDelay = animDelay,
                                    enabled = interactionEnabled,
                                    highlighted = isEditMode,
                                    onClick = { isEditMode = !isEditMode },
                                )
                            }
                        }
                    }
                }

                if (onSignInWithAccount != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(Res.string.settings_account_sign_in_with_account),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .graphicsLayer { alpha = manageAlpha.value }
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(enabled = interactionEnabled, onClick = onSignInWithAccount)
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                    )
                }

                Spacer(modifier = Modifier.height(navigationBottom + 32.dp))
            }
        }

        if (onBack != null && interactionEnabled && contentVisible) {
            NuvioBackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(LocalVerticalBarPadding.current)
                    .padding(start = 16.dp, top = statusBarTop + 8.dp),
            )
        }

        NuvioToastHost(modifier = Modifier.align(Alignment.TopCenter))
    }

    pendingPinSelection?.let { (profile, tapCenter) ->
        PinEntryDialog(
            profileName = profile.name,
            onVerify = { pin -> ProfileRepository.verifyPin(profile.profileIndex, pin) },
            onVerified = {
                pendingPinSelection = null
                if (interactionEnabled && profile.profileIndex != activeProfileIndex) {
                    onProfileSelected(profile, tapCenter)
                }
            },
            onDismiss = { pendingPinSelection = null },
        )
    }
}

internal val ProfileTileAvatarSize = 110.dp

private sealed interface ProfileTile {
    data class Profile(val profile: NuvioProfile) : ProfileTile
    data object Add : ProfileTile
    data object Edit : ProfileTile
}

private class TileEntrance(
    val alpha: Animatable<Float, AnimationVector1D>,
    val scale: Animatable<Float, AnimationVector1D>,
    val offset: Animatable<Float, AnimationVector1D>,
)

@Composable
private fun rememberTileEntrance(animDelay: Int): TileEntrance {
    val reduceMotion = rememberReduceMotion()
    val entrance = remember {
        TileEntrance(Animatable(0f), Animatable(0.85f), Animatable(30f))
    }
    LaunchedEffect(Unit) {
        delay(animDelay.toLong() + 120)
        launch { entrance.alpha.animateTo(1f, tween(CinemaMotion.StandardMillis, easing = FastOutSlowInEasing)) }
        if (reduceMotion) {
            entrance.scale.snapTo(1f)
            entrance.offset.snapTo(0f)
        } else {
            launch { entrance.scale.animateTo(1f, CinemaMotion.spring()) }
            launch { entrance.offset.animateTo(0f, CinemaMotion.spring()) }
        }
    }
    return entrance
}

/** A profile as a rounded-square picture with its name in bold underneath. */
@Composable
private fun ProfileAvatarCard(
    profile: NuvioProfile,
    isEditMode: Boolean,
    animDelay: Int,
    enabled: Boolean,
    onClick: (Offset) -> Unit,
) {
    val avatarColor = remember(profile.avatarColorHex) {
        parseHexColor(profile.avatarColorHex)
    }
    val avatars by AvatarRepository.avatars.collectAsStateWithLifecycle()
    val avatarItem = remember(profile.avatarId, avatars) {
        profile.avatarId?.let { id -> avatars.find { it.id == id } }
    }
    val avatarImageUrl = remember(profile.avatarUrl, avatarItem) {
        profileAvatarImageUrl(profile, avatarItem)
    }
    val entrance = rememberTileEntrance(animDelay)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.95f else 1f

    // Kept up to date on every layout pass so the tap handler below can hand back exactly where
    // on screen this avatar sits — that's where AppLoadingContent glides its emblem in from.
    var avatarCenterInWindow by remember { mutableStateOf(Offset.Zero) }
    val haptics = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(ProfileTileAvatarSize + 16.dp)
            .graphicsLayer {
                alpha = entrance.alpha.value
                scaleX = entrance.scale.value * pressScale
                scaleY = entrance.scale.value * pressScale
                translationY = entrance.offset.value
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = stringResource(
                    if (isEditMode) {
                        Res.string.profile_edit_accessibility
                    } else {
                        Res.string.profile_select_accessibility
                    },
                ),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick(avatarCenterInWindow)
                },
            )
            .semantics {
                role = Role.Button
            },
    ) {
        Box(
            modifier = Modifier
                .size(ProfileTileAvatarSize)
                .onGloballyPositioned { coordinates ->
                    avatarCenterInWindow = coordinates.positionInWindow() + Offset(
                        coordinates.size.width / 2f,
                        coordinates.size.height / 2f,
                    )
                }
                .clip(CinemaProfileAvatarShape)
                .background(avatarItem?.bgColor?.let { parseHexColor(it) } ?: avatarColor),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarImageUrl != null) {
                NuvioAsyncImage(
                    imageUrl = avatarImageUrl,
                    contentDescription = avatarItem?.displayName ?: profile.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    animateIfPossible = true,
                )
            } else if (profile.name.isNotBlank()) {
                Text(
                    text = profile.name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp),
                )
            }

            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = profile.name.ifBlank {
                    stringResource(Res.string.profile_label_number, profile.profileIndex)
                },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (profile.pinEnabled && !isEditMode) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = stringResource(Res.string.profile_locked_accessibility),
                    tint = CinemaColors.TextSecondary,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}

/** The grey "Add" and "Edit" squares that sit in the grid beside the profiles. */
@Composable
private fun ProfileActionTile(
    icon: ImageVector,
    label: String,
    animDelay: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    highlighted: Boolean = false,
) {
    val entrance = rememberTileEntrance(animDelay)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.95f else 1f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(ProfileTileAvatarSize + 16.dp)
            .graphicsLayer {
                alpha = entrance.alpha.value
                scaleX = entrance.scale.value * pressScale
                scaleY = entrance.scale.value * pressScale
                translationY = entrance.offset.value
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(ProfileTileAvatarSize)
                .clip(CinemaProfileAvatarShape)
                .background(if (highlighted) Color(0xFF5A5A5A) else Color(0xFF474747)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
