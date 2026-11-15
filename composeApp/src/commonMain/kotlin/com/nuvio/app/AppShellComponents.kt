package com.nuvio.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.CinemaProfileAvatarShape
import com.nuvio.app.core.ui.CinemaRedSpinner
import com.nuvio.app.features.profiles.ProfileTileAvatarSize
import com.nuvio.app.features.settings.AppBrandMark
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.ui.DisintegrationRequest
import com.nuvio.app.core.ui.NativeTabBridge
import com.nuvio.app.core.ui.NuvioAsyncImage
import com.nuvio.app.core.ui.NuvioTokens
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.cloud.CloudLibraryContentType
import com.nuvio.app.features.cloud.CloudLibraryFile
import com.nuvio.app.features.cloud.CloudLibraryItem
import com.nuvio.app.features.home.HomeCatalogSection
import com.nuvio.app.features.home.HomeScreen
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.library.LibraryItem
import com.nuvio.app.features.library.LibraryScreen
import com.nuvio.app.features.library.LibrarySection
import com.nuvio.app.features.library.LibrarySortOption
import com.nuvio.app.features.livetv.LiveTvChannel
import com.nuvio.app.features.livetv.LiveTvScreen
import com.nuvio.app.features.profiles.AvatarRepository
import com.nuvio.app.features.profiles.NuvioProfile
import com.nuvio.app.features.profiles.ProfileBackgroundBackdrop
import com.nuvio.app.features.profiles.ProfileSwitcherTab
import com.nuvio.app.features.profiles.parseHexColor
import com.nuvio.app.features.profiles.profileAvatarImageUrl
import com.nuvio.app.features.search.SearchScreen
import com.nuvio.app.features.settings.AppBrandWordmark
import com.nuvio.app.features.myflowix.MyFlowixScreen
import com.nuvio.app.features.settings.SettingsScreen
import com.nuvio.app.core.ui.PlatformBackHandler
import com.nuvio.app.navigation.LocalNativeNavigationBarHidden
import com.nuvio.app.features.watchprogress.ContinueWatchingItem
import com.nuvio.app.navigation.AppRoute
import com.nuvio.app.navigation.NuvioNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.app_brand_name
import nuvio.composeapp.generated.resources.compose_nav_home
import nuvio.composeapp.generated.resources.nav_my_flowix
import nuvio.composeapp.generated.resources.compose_nav_live_tv
import nuvio.composeapp.generated.resources.compose_nav_profile
import nuvio.composeapp.generated.resources.compose_nav_search
import nuvio.composeapp.generated.resources.sidebar_search
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun rememberGuardedPopBackStack(
    navController: NuvioNavigator,
    route: AppRoute,
    beforePop: () -> Unit = {},
): () -> Unit {
    var popHandled by remember(route) { mutableStateOf(false) }

    return remember(navController, route, popHandled, beforePop) {
        {
            if (!popHandled && navController.currentRoute == route) {
                popHandled = true
                beforePop()
                navController.popBackStack(expectedRoute = route)
            }
        }
    }
}

internal data class AppTabState(
    val searchListState: LazyListState,
    val homeContentGeneration: Int = 0,
    val profileId: Int? = null,
    val searchFocusRequestCount: Int = 0,
    val rootActionsEnabled: Boolean = true,
    val animateHomeCollectionGifs: Boolean = true,
    val libraryDisintegrationRequest: DisintegrationRequest<String>? = null,
    val continueWatchingDisintegrationRequest: DisintegrationRequest<String>? = null,
    val requestedSettingsPageName: String? = null,
)

internal data class AppTabRequests(
    val homeScrollToTopRequests: Flow<Unit>,
    val searchScrollToTopRequests: Flow<Unit>,
    val libraryScrollToTopRequests: Flow<Unit>,
    val liveTvScrollToTopRequests: Flow<Unit>,
    val settingsRootActionRequests: Flow<Unit>,
)

internal data class AppTabActions(
    val onCatalogClick: ((HomeCatalogSection) -> Unit)? = null,
    val onPosterClick: ((MetaPreview) -> Unit)? = null,
    val onPosterLongClick: ((MetaPreview) -> Unit)? = null,
    val onHeroPlayClick: ((MetaPreview) -> Unit)? = null,
    val onHeroMyListClick: ((MetaPreview) -> Unit)? = null,
    val onLibraryPosterClick: ((LibraryItem) -> Unit)? = null,
    val onLibraryCalendarEpisodeClick: ((LibraryItem, Int?, Int?) -> Unit)? = null,
    val onLibraryPosterLongClick: ((LibraryItem, LibrarySection) -> Unit)? = null,
    val onLibrarySectionViewAllClick: ((LibrarySection, LibrarySortOption) -> Unit)? = null,
    val onCloudFilePlay: ((CloudLibraryItem, CloudLibraryFile) -> Unit)? = null,
    val onConnectCloudClick: (() -> Unit)? = null,
    val onDownloadsClick: () -> Unit = {},
    val onContinueWatchingClick: ((ContinueWatchingItem) -> Unit)? = null,
    val onContinueWatchingLongPress: ((ContinueWatchingItem) -> Unit)? = null,
    val onLiveTvChannelClick: (LiveTvChannel) -> Unit = {},
    val onSwitchProfile: (() -> Unit)? = null,
    val onEditProfile: (() -> Unit)? = null,
    val onSettingsPageClick: ((pageName: String, title: String) -> Unit)? = null,
    val onHomescreenSettingsClick: () -> Unit = {},
    val onMetaScreenSettingsClick: () -> Unit = {},
    val onContinueWatchingSettingsClick: () -> Unit = {},
    val onAddonsSettingsClick: () -> Unit = {},
    val onPluginsSettingsClick: () -> Unit = {},
    val onAccountSettingsClick: () -> Unit = {},
    val onSupportersContributorsSettingsClick: () -> Unit = {},
    val onLicensesAttributionsSettingsClick: () -> Unit = {},
    val onWhatsNewSettingsClick: () -> Unit = {},
    val onCheckForUpdatesClick: (() -> Unit)? = null,
    val onTestUpdateBannerClick: (() -> Unit)? = null,
    val onCollectionsSettingsClick: () -> Unit = {},
    val onWatchPartySettingsClick: () -> Unit = {},
    val onFolderClick: ((collectionId: String, folderId: String) -> Unit)? = null,
    val onRequestedSettingsPageConsumed: () -> Unit = {},
    val onInitialHomeContentRendered: () -> Unit = {},
)

@Composable
internal fun AppTabHost(
    selectedTab: AppScreenTab,
    requests: AppTabRequests,
    state: AppTabState,
    actions: AppTabActions,
    modifier: Modifier = Modifier,
) {
    RootTabHost(
        selectedTab = selectedTab,
        modifier = modifier,
        active = state.rootActionsEnabled,
        profileId = state.profileId,
    ) { tab ->
        when (tab) {
            AppScreenTab.Home -> {
                key(state.homeContentGeneration) {
                    HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        animateCollectionGifs = state.animateHomeCollectionGifs,
                        scrollToTopRequests = requests.homeScrollToTopRequests,
                        onCatalogClick = actions.onCatalogClick,
                        onPosterClick = actions.onPosterClick,
                        onPosterLongClick = actions.onPosterLongClick,
                        onHeroPlayClick = actions.onHeroPlayClick,
                        onHeroMyListClick = actions.onHeroMyListClick,
                        onContinueWatchingClick = actions.onContinueWatchingClick,
                        onContinueWatchingLongPress = actions.onContinueWatchingLongPress,
                        continueWatchingDisintegrationRequest = state.continueWatchingDisintegrationRequest,
                        onFolderClick = actions.onFolderClick,
                        onFirstCatalogRendered = actions.onInitialHomeContentRendered,
                        onDownloadsClick = actions.onDownloadsClick,
                        onNotificationsClick = actions.onWhatsNewSettingsClick,
                    )
                }
            }

            AppScreenTab.Search -> {
                SearchScreen(
                    modifier = Modifier.fillMaxSize(),
                    listState = state.searchListState,
                    onPosterClick = actions.onPosterClick,
                    onPosterLongClick = actions.onPosterLongClick,
                    searchFocusRequestCount = state.searchFocusRequestCount,
                    scrollToTopRequests = requests.searchScrollToTopRequests,
                    onDownloadsClick = actions.onDownloadsClick,
                    onNotificationsClick = actions.onWhatsNewSettingsClick,
                    onPlayClick = actions.onHeroPlayClick,
                )
            }

            AppScreenTab.Library -> {
                LibraryScreen(
                    modifier = Modifier.fillMaxSize(),
                    scrollToTopRequests = requests.libraryScrollToTopRequests,
                    onPosterClick = actions.onLibraryPosterClick,
                    onCalendarEpisodeClick = actions.onLibraryCalendarEpisodeClick,
                    onPosterLongClick = actions.onLibraryPosterLongClick,
                    onSectionViewAllClick = actions.onLibrarySectionViewAllClick,
                    onCloudFilePlay = actions.onCloudFilePlay,
                    onConnectCloudClick = actions.onConnectCloudClick,
                    onCatalogClick = actions.onCatalogClick,
                    onPreviewClick = actions.onPosterClick,
                    onPreviewLongClick = actions.onPosterLongClick,
                    onDownloadsClick = actions.onDownloadsClick,
                    disintegrationRequest = state.libraryDisintegrationRequest,
                )
            }

            AppScreenTab.LiveTv -> {
                LiveTvScreen(
                    scrollToTopRequests = requests.liveTvScrollToTopRequests,
                    onChannelClick = actions.onLiveTvChannelClick,
                )
            }

            AppScreenTab.Settings -> MyFlowixTab(selectedTab, requests, state, actions) { onRootBack ->
                SettingsScreen(
                    modifier = Modifier.fillMaxSize(),
                    onRootBack = onRootBack,
                    rootActionRequests = requests.settingsRootActionRequests,
                    requestedPageName = state.requestedSettingsPageName,
                    onRequestedPageConsumed = actions.onRequestedSettingsPageConsumed,
                    rootActionsEnabled = state.rootActionsEnabled,
                    isSelectedTab = selectedTab == AppScreenTab.Settings,
                    onNavigatePage = actions.onSettingsPageClick,
                    onSwitchProfile = actions.onSwitchProfile,
                    onEditProfile = actions.onEditProfile,
                    onPosterClick = actions.onPosterClick,
                    onHomescreenClick = actions.onHomescreenSettingsClick,
                    onMetaScreenClick = actions.onMetaScreenSettingsClick,
                    onContinueWatchingClick = actions.onContinueWatchingSettingsClick,
                    onAddonsClick = actions.onAddonsSettingsClick,
                    onPluginsClick = actions.onPluginsSettingsClick,
                    onAccountClick = actions.onAccountSettingsClick,
                    onSupportersContributorsClick = actions.onSupportersContributorsSettingsClick,
                    onLicensesAttributionsClick = actions.onLicensesAttributionsSettingsClick,
                    onWhatsNewClick = actions.onWhatsNewSettingsClick,
                    onCheckForUpdatesClick = actions.onCheckForUpdatesClick,
                    onTestUpdateBannerClick = actions.onTestUpdateBannerClick,
                    onCollectionsClick = actions.onCollectionsSettingsClick,
                    onWatchPartyClick = actions.onWatchPartySettingsClick,
                )
            }
        }
    }
}

private enum class MyFlowixPane { Home, Settings, Library }

/**
 * The profile tab opens on My Flowix; App Settings and the full library slide in over it and
 * return with back, so every setting stays reachable from one place.
 */
@Composable
private fun MyFlowixTab(
    selectedTab: AppScreenTab,
    requests: AppTabRequests,
    state: AppTabState,
    actions: AppTabActions,
    settings: @Composable (onRootBack: () -> Unit) -> Unit,
) {
    var paneName by rememberSaveable { mutableStateOf(MyFlowixPane.Home.name) }
    val pane = MyFlowixPane.entries.firstOrNull { it.name == paneName } ?: MyFlowixPane.Home
    val showHome = { paneName = MyFlowixPane.Home.name }

    LaunchedEffect(state.requestedSettingsPageName) {
        if (state.requestedSettingsPageName != null) paneName = MyFlowixPane.Settings.name
    }
    PlatformBackHandler(
        enabled = pane != MyFlowixPane.Home && selectedTab == AppScreenTab.Settings && state.rootActionsEnabled,
        onBack = showHome,
    )

    // These panes sit at the tab root, where no native bar offers a back button.
    CompositionLocalProvider(LocalNativeNavigationBarHidden provides true) {
        when (pane) {
            MyFlowixPane.Home -> MyFlowixScreen(
                modifier = Modifier.fillMaxSize(),
                scrollToTopRequests = requests.settingsRootActionRequests,
                onOpenAppSettings = { paneName = MyFlowixPane.Settings.name },
                onOpenAccount = actions.onAccountSettingsClick,
                onOpenLibrary = { paneName = MyFlowixPane.Library.name },
                onDownloadsClick = actions.onDownloadsClick,
                onNotificationsClick = actions.onWhatsNewSettingsClick,
                onSwitchProfile = actions.onSwitchProfile,
                onEditProfile = actions.onEditProfile,
                onPosterClick = actions.onPosterClick,
                onPosterLongClick = actions.onPosterLongClick,
                onContinueWatchingClick = actions.onContinueWatchingClick,
                onContinueWatchingLongPress = actions.onContinueWatchingLongPress,
            )

            MyFlowixPane.Settings -> settings(showHome)

            MyFlowixPane.Library -> LibraryScreen(
                modifier = Modifier.fillMaxSize(),
                scrollToTopRequests = requests.libraryScrollToTopRequests,
                onPosterClick = actions.onLibraryPosterClick,
                onCalendarEpisodeClick = actions.onLibraryCalendarEpisodeClick,
                onPosterLongClick = actions.onLibraryPosterLongClick,
                onSectionViewAllClick = actions.onLibrarySectionViewAllClick,
                onCloudFilePlay = actions.onCloudFilePlay,
                onConnectCloudClick = actions.onConnectCloudClick,
                onCatalogClick = actions.onCatalogClick,
                onPreviewClick = actions.onPosterClick,
                onPreviewLongClick = actions.onPosterLongClick,
                onDownloadsClick = actions.onDownloadsClick,
                disintegrationRequest = state.libraryDisintegrationRequest,
                onBack = showHome,
            )
        }
    }
}

@Composable
internal fun TabletFloatingTopBar(
    selectedTab: AppScreenTab,
    showLiveTv: Boolean,
    onTabSelected: (AppScreenTab) -> Unit,
    onProfileSelected: (NuvioProfile) -> Unit,
    onAddProfileRequested: () -> Unit,
    modifier: Modifier = Modifier,
    topInset: Dp? = null,
) {
    val tokens = MaterialTheme.nuvio
    val statusBarPadding = topInset ?: (WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + NuvioTokens.Space.s10)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = statusBarPadding, bottom = tokens.spacing.controlGap),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            color = tokens.colors.surface.copy(alpha = tokens.opacity.visible - tokens.opacity.subtle),
            shape = tokens.shapes.chip,
            tonalElevation = tokens.elevation.playerControls,
            shadowElevation = tokens.elevation.overlay,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = NuvioTokens.Space.s10, vertical = tokens.spacing.controlGap),
                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabletTopPillItem(
                    label = stringResource(Res.string.compose_nav_home),
                    selected = selectedTab == AppScreenTab.Home,
                    onClick = { onTabSelected(AppScreenTab.Home) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = stringResource(Res.string.compose_nav_home),
                            modifier = Modifier.size(NuvioTokens.Space.s18),
                            tint = if (selectedTab == AppScreenTab.Home) {
                                tokens.colors.textPrimary
                            } else {
                                tokens.colors.textMuted
                            },
                        )
                    },
                )
                TabletTopPillItem(
                    label = stringResource(Res.string.compose_nav_search),
                    selected = selectedTab == AppScreenTab.Search,
                    onClick = { onTabSelected(AppScreenTab.Search) },
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.sidebar_search),
                            contentDescription = stringResource(Res.string.compose_nav_search),
                            modifier = Modifier.size(NuvioTokens.Space.s18),
                            tint = if (selectedTab == AppScreenTab.Search) {
                                tokens.colors.textPrimary
                            } else {
                                tokens.colors.textMuted
                            },
                        )
                    },
                )
                if (showLiveTv) {
                    TabletTopPillItem(
                        label = stringResource(Res.string.compose_nav_live_tv),
                        selected = selectedTab == AppScreenTab.LiveTv,
                        onClick = { onTabSelected(AppScreenTab.LiveTv) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Tv,
                                contentDescription = stringResource(Res.string.compose_nav_live_tv),
                                modifier = Modifier.size(18.dp),
                                tint = if (selectedTab == AppScreenTab.LiveTv) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        },
                    )
                }
                Surface(
                    color = if (selectedTab == AppScreenTab.Settings) {
                        tokens.colors.overlaySelected
                    } else {
                        tokens.colors.surface
                    },
                    shape = tokens.shapes.chip,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = tokens.spacing.listGap, vertical = tokens.spacing.controlGap),
                        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileSwitcherTab(
                            selected = selectedTab == AppScreenTab.Settings,
                            onClick = { onTabSelected(AppScreenTab.Settings) },
                            onProfileSelected = onProfileSelected,
                            onAddProfileRequested = onAddProfileRequested,
                        )
                        Text(
                            text = stringResource(Res.string.nav_my_flowix),
                            modifier = Modifier.clickable { onTabSelected(AppScreenTab.Settings) },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == AppScreenTab.Settings) {
                                tokens.colors.textPrimary
                            } else {
                                tokens.colors.textMuted
                            },
                        )
                    }
                }
            }
        }
    }
}

internal fun ContinueWatchingItem.isCloudLibraryContinueWatchingItem(): Boolean =
    parentMetaType.equals(CloudLibraryContentType, ignoreCase = true)

@Composable
private fun TabletTopPillItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    Surface(
        color = if (selected) tokens.colors.overlaySelected else tokens.colors.surface,
        shape = tokens.shapes.chip,
        tonalElevation = if (selected) tokens.elevation.raised else tokens.elevation.flat,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = tokens.components.chipHorizontalPadding, vertical = NuvioTokens.Space.s10),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) {
                    tokens.colors.textPrimary
                } else {
                    tokens.colors.textMuted
                },
            )
        }
    }
}

private val LoadingAvatarSize = 200.dp
private val LoadingAvatarLift = 70.dp
private val LoadingSpinnerDrop = 130.dp

/**
 * The profile-loading screen: the chosen avatar, large and square-cornered, above a red spinner on
 * black. When a profile was tapped the avatar glides there from the tile the finger left.
 */
@Composable
internal fun AppLoadingContent(
    modifier: Modifier = Modifier,
    profile: NuvioProfile? = null,
    entryOrigin: Offset? = null,
    exitTowardProfileTab: Boolean = false,
    onExitFinished: () -> Unit = {},
) {
    val emblemScale = remember { Animatable(0.9f) }
    val emblemAlpha = remember { Animatable(0f) }
    val emblemOffsetX = remember { Animatable(0f) }
    val emblemOffsetY = remember { Animatable(0f) }
    val spinnerAlpha = remember { Animatable(0f) }
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(entryOrigin) {
        if (entryOrigin != null) {
            // Tap-to-center: this emblem *is* the avatar the user just tapped, already on screen
            // at that spot and at the tile's size, so it glides and grows into place rather than
            // fading in out of nowhere.
            val size = snapshotFlow { containerSize }.first { it != IntSize.Zero }
            val liftPx = with(density) { LoadingAvatarLift.toPx() }
            emblemAlpha.snapTo(1f)
            emblemScale.snapTo(ProfileTileAvatarSize / LoadingAvatarSize)
            emblemOffsetX.snapTo(entryOrigin.x - size.width / 2f)
            emblemOffsetY.snapTo(entryOrigin.y - (size.height / 2f - liftPx))
            val travelDuration = 550
            launch { emblemOffsetX.animateTo(0f, tween(travelDuration, easing = FastOutSlowInEasing)) }
            launch { emblemScale.animateTo(1f, tween(travelDuration, easing = FastOutSlowInEasing)) }
            launch { spinnerAlpha.animateTo(1f, tween(240, delayMillis = travelDuration - 120)) }
            emblemOffsetY.animateTo(0f, tween(travelDuration, easing = FastOutSlowInEasing))
        } else {
            launch { emblemAlpha.animateTo(1f, tween(260, easing = FastOutSlowInEasing)) }
            launch { spinnerAlpha.animateTo(1f, tween(320, delayMillis = 120)) }
            emblemScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }

    LaunchedEffect(exitTowardProfileTab) {
        if (!exitTowardProfileTab) return@LaunchedEffect
        launch { spinnerAlpha.animateTo(0f, tween(160)) }
        launch { emblemScale.animateTo(1.06f, tween(260, easing = FastOutSlowInEasing)) }
        emblemAlpha.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
        onExitFinished()
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .onSizeChanged { containerSize = it },
        contentAlignment = Alignment.Center,
    ) {
        val emblemModifier = Modifier
            .offset(y = -LoadingAvatarLift)
            .graphicsLayer {
                scaleX = emblemScale.value
                scaleY = emblemScale.value
                alpha = emblemAlpha.value
                translationX = emblemOffsetX.value
                translationY = emblemOffsetY.value
            }
        if (profile != null) {
            AppLoadingProfileAvatar(
                profile = profile,
                modifier = emblemModifier.size(LoadingAvatarSize),
            )
        } else {
            AppBrandMark(
                contentDescription = stringResource(Res.string.app_brand_name),
                modifier = emblemModifier.size(LoadingAvatarSize * 0.6f),
            )
        }
        CinemaRedSpinner(
            modifier = Modifier
                .offset(y = LoadingSpinnerDrop)
                .graphicsLayer { alpha = spinnerAlpha.value },
        )
    }
}

@Composable
internal fun AppLoadingProfileAvatar(
    profile: NuvioProfile,
    modifier: Modifier = Modifier,
) {
    val avatarColor = remember(profile.avatarColorHex) { parseHexColor(profile.avatarColorHex) }
    val avatars by remember { AvatarRepository.avatars }.collectAsStateWithLifecycle()
    val avatarItem = remember(profile.avatarId, avatars) {
        profile.avatarId?.let { id -> avatars.find { it.id == id } }
    }
    val avatarImageUrl = remember(profile.avatarUrl, avatarItem) {
        profileAvatarImageUrl(profile, avatarItem)
    }
    val backgroundColor = avatarItem?.bgColor?.let(::parseHexColor) ?: avatarColor

    BoxWithConstraints(
        modifier = modifier
            .clip(CinemaProfileAvatarShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        val initialSize = (maxWidth.value * 0.42f).sp
        when {
            avatarImageUrl != null -> NuvioAsyncImage(
                imageUrl = avatarImageUrl,
                contentDescription = avatarItem?.displayName ?: profile.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                // Keep an animated avatar (GIF) playing through the loading transition instead of
                // freezing on its first frame.
                animateIfPossible = true,
            )
            profile.name.isNotBlank() -> Text(
                text = profile.name.take(1).uppercase(),
                color = Color.White,
                fontSize = initialSize,
                fontWeight = FontWeight.Bold,
            )
            else -> Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.48f),
            )
        }
    }
}

@Composable
internal fun AppLaunchOverlay(
    profile: NuvioProfile?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .zIndex(NuvioTokens.Z.dialog)
            .background(Color.Black),
    ) {
        AppLoadingContent(modifier = Modifier.fillMaxSize(), profile = profile)
    }
}
