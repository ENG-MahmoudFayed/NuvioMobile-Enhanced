package com.nuvio.app.features.myflowix

import androidx.compose.material.icons.outlined.BookmarkBorder
import com.nuvio.app.core.ui.CinemaShapes
import com.nuvio.app.core.ui.CinemaSize
import com.nuvio.app.core.ui.CinemaSpace
import com.nuvio.app.core.ui.CinemaType
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.nuvio.app.AppLoadingProfileAvatar
import com.nuvio.app.core.auth.AuthRepository
import com.nuvio.app.core.ui.CinemaAsyncImage
import com.nuvio.app.core.ui.CinemaColors
import com.nuvio.app.core.ui.CinemaIconAction
import com.nuvio.app.core.ui.CinemaSectionTitle
import com.nuvio.app.core.ui.CinemaProfileAvatarShape
import com.nuvio.app.core.ui.CinemaTopBar
import com.nuvio.app.core.ui.NuvioStatusModal
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.nuvio.app.core.build.AppVersionConfig
import com.nuvio.app.core.ui.NuvioModalBottomSheet
import com.nuvio.app.core.ui.NuvioScreen
import com.nuvio.app.core.ui.NuvioShelfSection
import com.nuvio.app.core.ui.dismissNuvioBottomSheet
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.core.ui.pressScale
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.components.HomeContinueWatchingSection
import com.nuvio.app.features.home.components.HomePosterCard
import com.nuvio.app.features.home.stableKey
import com.nuvio.app.features.library.LibraryRepository
import com.nuvio.app.features.library.LibrarySourceMode
import com.nuvio.app.features.library.toMetaPreview
import com.nuvio.app.features.profiles.ActiveProfileMiniAvatar
import com.nuvio.app.features.profiles.AvatarCatalogItem
import com.nuvio.app.features.profiles.AvatarRepository
import com.nuvio.app.features.profiles.NuvioProfile
import com.nuvio.app.features.profiles.ProfileRepository
import com.nuvio.app.features.watchprogress.ContinueWatchingItem
import com.nuvio.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.nuvio.app.features.watchprogress.WatchProgressEntry
import com.nuvio.app.features.watchprogress.WatchProgressRepository
import com.nuvio.app.features.watchprogress.toContinueWatchingItem
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.catalog.CatalogRepository
import com.nuvio.app.features.catalog.CatalogTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private const val MY_FLOWIX_ROW_LIMIT = 20
private val MyFlowixHorizontalPadding = 16.dp
private val MyFlowixRowShape = RoundedCornerShape(8.dp)

/**
 * The profile tab's landing page: the active profile, downloads, what the profile is watching
 * and its list. Every setting lives one tap away behind the profile menu's App Settings row.
 */
@Composable
internal fun MyFlowixScreen(
    modifier: Modifier = Modifier,
    scrollToTopRequests: Flow<Unit> = emptyFlow(),
    onOpenAppSettings: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenLibrary: () -> Unit,
    onDownloadsClick: () -> Unit,
    onNotificationsClick: (() -> Unit)? = null,
    onSwitchProfile: (() -> Unit)? = null,
    onSelectProfile: ((NuvioProfile) -> Unit)? = null,
    onEditProfile: (() -> Unit)? = null,
    onPosterClick: ((MetaPreview) -> Unit)? = null,
    onPosterLongClick: ((MetaPreview) -> Unit)? = null,
    onContinueWatchingClick: ((ContinueWatchingItem) -> Unit)? = null,
    onContinueWatchingLongPress: ((ContinueWatchingItem) -> Unit)? = null,
) {
    val tokens = MaterialTheme.nuvio
    val profileState by ProfileRepository.state.collectAsStateWithLifecycle()
    val avatars by AvatarRepository.avatars.collectAsStateWithLifecycle()
    val watchProgressUiState by remember {
        WatchProgressRepository.ensureLoaded()
        WatchProgressRepository.uiState
    }.collectAsStateWithLifecycle()
    val continueWatchingPreferences by remember {
        ContinueWatchingPreferencesRepository.ensureLoaded()
        ContinueWatchingPreferencesRepository.uiState
    }.collectAsStateWithLifecycle()
    val libraryUiState by remember {
        LibraryRepository.ensureLoaded()
        LibraryRepository.uiState
    }.collectAsStateWithLifecycle()

    val addonUiState by remember {
        AddonRepository.initialize()
        AddonRepository.uiState
    }.collectAsStateWithLifecycle()
    val catalogUiState by remember {
        CatalogRepository.uiState
    }.collectAsStateWithLifecycle()

    val firstMovieCatalog = remember(addonUiState.addons) {
        addonUiState.addons
            .filter { it.enabled && it.manifest != null }
            .flatMap { addon ->
                addon.manifest!!.catalogs
                    .filter { catalog -> catalog.type == "movie" }
                    .map { catalog -> addon to catalog }
            }
            .firstOrNull()
    }

    LaunchedEffect(firstMovieCatalog) {
        if (firstMovieCatalog != null) {
            val (addon, catalog) = firstMovieCatalog
            CatalogRepository.load(
                target = CatalogTarget.Addon(
                    manifestUrl = addon.manifestUrl,
                    contentType = catalog.type,
                    catalogId = catalog.id,
                ),
            )
        }
    }

    val recentEntries = remember(watchProgressUiState.entries, watchProgressUiState.hiddenContentIds) {
        watchProgressUiState.entries
            .filterNot { it.parentMetaId in watchProgressUiState.hiddenContentIds }
            .sortedByDescending { it.lastUpdatedEpochMs }
            .distinctBy { it.parentMetaId }
    }
    val continueWatchingItems = remember(recentEntries) {
        recentEntries
            .filter { it.isResumable && it.lastPositionMs > 0L }
            .take(MY_FLOWIX_ROW_LIMIT)
            .map { it.toContinueWatchingItem() }
    }
    val recentlyWatched = remember(recentEntries) { recentEntries.take(MY_FLOWIX_ROW_LIMIT) }
    val myList = remember(libraryUiState.sourceMode, libraryUiState.items, libraryUiState.sections) {
        val items = if (libraryUiState.sourceMode == LibrarySourceMode.LOCAL) {
            libraryUiState.items
        } else {
            libraryUiState.sections.flatMap { it.items }
        }
        items.map { it.toMetaPreview() }.distinctBy { it.stableKey() }.take(MY_FLOWIX_ROW_LIMIT)
    }

    var showProfileMenu by rememberSaveable { mutableStateOf(false) }
    var showSignOutConfirm by rememberSaveable { mutableStateOf(false) }
    val signOutScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    LaunchedEffect(scrollToTopRequests) {
        scrollToTopRequests.collect { listState.animateScrollToItem(0) }
    }

    NuvioScreen(
        modifier = modifier,
        horizontalPadding = 0.dp,
        listState = listState,
    ) {
        item(key = "my_flowix_header") {
            MyFlowixHeader(
                profile = profileState.activeProfile,
                onProfileClick = { showProfileMenu = true },
                onDownloadsClick = onDownloadsClick,
                onNotificationsClick = onNotificationsClick,
            )
        }
        item(key = "my_flowix_downloads") {
            Row(
                modifier = Modifier
                    .padding(horizontal = MyFlowixHorizontalPadding)
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .clip(MyFlowixRowShape)
                    .background(CinemaColors.Card)
                    .border(1.dp, CinemaColors.CardBorder, MyFlowixRowShape)
                    .clickable(role = Role.Button, onClick = onDownloadsClick)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.compose_settings_root_downloads_title),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = stringResource(Res.string.my_flowix_downloads_description),
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        color = CinemaColors.TextSecondary,
                    )
                }
            }
        }
        if (continueWatchingItems.isNotEmpty()) {
            item(key = "my_flowix_continue_watching") {
                HomeContinueWatchingSection(
                    items = continueWatchingItems,
                    style = continueWatchingPreferences.style,
                    dataSourceKey = watchProgressUiState.source,
                    useEpisodeThumbnails = continueWatchingPreferences.useEpisodeThumbnails,
                    blurNextUp = continueWatchingPreferences.blurNextUp,
                    modifier = Modifier.padding(top = 12.dp),
                    onItemClick = onContinueWatchingClick,
                    onItemLongPress = onContinueWatchingLongPress,
                    onItemInfoClick = onPosterClick?.let { openDetails ->
                        { item ->
                            openDetails(
                                MetaPreview(
                                    id = item.parentMetaId,
                                    type = item.parentMetaType,
                                    name = item.title,
                                    poster = item.poster,
                                ),
                            )
                        }
                    },
                )
            }
        }
        item(key = "my_flowix_my_list") {
            if (myList.isEmpty()) {
                MyListEmptyRow(onClick = onOpenLibrary)
            } else {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SeeAllHeader(
                        title = stringResource(Res.string.home_hero_my_list),
                        onSeeAll = onOpenLibrary,
                    )
                    NuvioShelfSection(
                        title = "",
                        entries = myList,
                        rowContentPadding = PaddingValues(horizontal = MyFlowixHorizontalPadding),
                        itemSpacing = 8.dp,
                        key = { item -> item.stableKey() },
                    ) { item ->
                        HomePosterCard(
                            item = item,
                            onClick = onPosterClick?.let { { it(item) } },
                            onLongClick = onPosterLongClick?.let { { it(item) } },
                        )
                    }
                }
            }
        }
        if (firstMovieCatalog != null && catalogUiState.items.isNotEmpty()) {
            item(key = "my_flowix_new_releases") {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SeeAllHeader(
                        title = stringResource(Res.string.my_flowix_new_releases),
                        onSeeAll = null,
                    )
                    NuvioShelfSection(
                        title = "",
                        entries = catalogUiState.items.take(20),
                        rowContentPadding = PaddingValues(horizontal = MyFlowixHorizontalPadding),
                        itemSpacing = 8.dp,
                        key = { item -> item.stableKey() },
                    ) { item ->
                        HomePosterCard(
                            item = item,
                            onClick = onPosterClick?.let { { it(item) } },
                            onLongClick = onPosterLongClick?.let { { it(item) } },
                        )
                    }
                }
            }
        }
        if (recentlyWatched.isNotEmpty()) {
            item(key = "my_flowix_recently_watched") {
                RecentlyWatchedRow(
                    entries = recentlyWatched,
                    onClick = { entry ->
                        onPosterClick?.invoke(
                            MetaPreview(
                                id = entry.parentMetaId,
                                type = entry.parentMetaType,
                                name = entry.title,
                                poster = entry.poster,
                            ),
                        )
                    },
                )
            }
        }
    }

    if (showProfileMenu) {
        MyFlowixProfileMenu(
            profiles = profileState.profiles,
            activeProfile = profileState.activeProfile,
            avatars = avatars,
            onDismiss = { showProfileMenu = false },
            onOpenAppSettings = onOpenAppSettings,
            onOpenAccount = onOpenAccount,
            onSwitchProfile = onSwitchProfile,
            onSelectProfile = onSelectProfile,
            onEditProfile = onEditProfile,
            onSignOut = { showSignOutConfirm = true },
        )
    }

    NuvioStatusModal(
        title = stringResource(Res.string.settings_account_sign_out_confirm_title),
        message = stringResource(Res.string.settings_account_sign_out_confirm_message),
        isVisible = showSignOutConfirm,
        confirmText = stringResource(Res.string.settings_account_sign_out),
        dismissText = stringResource(Res.string.action_cancel),
        onConfirm = {
            showSignOutConfirm = false
            signOutScope.launch { AuthRepository.signOut() }
        },
        onDismiss = { showSignOutConfirm = false },
    )
}

@Composable
private fun MyFlowixHeader(
    profile: NuvioProfile?,
    onProfileClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onNotificationsClick: (() -> Unit)?,
) {
    CinemaTopBar(
        title = profile.displayName(),
        modifier = Modifier.padding(top = 4.dp),
        leading = {
            if (profile != null) {
                AppLoadingProfileAvatar(profile = profile, modifier = Modifier.size(40.dp))
            }
        },
        titleTrailing = {
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = stringResource(Res.string.my_flowix_profiles),
                tint = Color.White,
                modifier = Modifier.size(34.dp),
            )
        },
        onTitleClick = onProfileClick,
        actions = {
            CinemaIconAction(
                icon = Icons.Outlined.FileDownload,
                contentDescription = stringResource(Res.string.compose_settings_root_downloads_title),
                onClick = onDownloadsClick,
            )
            if (onNotificationsClick != null) {
                CinemaIconAction(
                    icon = Icons.Outlined.Notifications,
                    contentDescription = stringResource(Res.string.home_action_notifications),
                    onClick = onNotificationsClick,
                )
            }
        },
    )
}

/** A row title with "See All ›" at the far end, as above My List. */
@Composable
private fun SeeAllHeader(
    title: String,
    onSeeAll: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = MyFlowixHorizontalPadding, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CinemaSectionTitle(
            text = title,
            modifier = Modifier.weight(1f),
        )
        if (onSeeAll != null) Row(
            modifier = Modifier
                .heightIn(min = 44.dp)
                .clip(MyFlowixRowShape)
                .clickable(role = Role.Button, onClick = onSeeAll)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.my_flowix_see_all),
                fontSize = 18.sp,
                color = Color.White,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun MyListEmptyRow(onClick: () -> Unit) {
    val tokens = MaterialTheme.nuvio
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SeeAllHeader(
            title = stringResource(Res.string.home_hero_my_list),
            onSeeAll = null,
        )
        Row(
            modifier = Modifier
                .padding(horizontal = MyFlowixHorizontalPadding)
                .fillMaxWidth()
                .clip(MyFlowixRowShape)
                .background(CinemaColors.Surface)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(CinemaSpace.s16),
            horizontalArrangement = Arrangement.spacedBy(CinemaSpace.s16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CinemaShapes.Pill)
                    .background(CinemaColors.SurfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = CinemaColors.TextSecondary,
                    modifier = Modifier.size(CinemaSize.Icon),
                )
            }
            Text(
                text = stringResource(Res.string.my_flowix_my_list_empty),
                style = CinemaType.body.copy(color = CinemaColors.TextSecondary),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RecentlyWatchedRow(
    entries: List<WatchProgressEntry>,
    onClick: (WatchProgressEntry) -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CinemaSectionTitle(
            text = stringResource(Res.string.my_flowix_recently_watched),
            modifier = Modifier.padding(horizontal = MyFlowixHorizontalPadding),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = MyFlowixHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(entries, key = { it.parentMetaId }) { entry ->
                val interactionSource = remember { MutableInteractionSource() }
                val label = if (entry.seasonNumber != null && entry.episodeNumber != null) {
                    val badge = stringResource(
                        Res.string.streams_episode_badge,
                        entry.seasonNumber,
                        entry.episodeNumber,
                    )
                    entry.episodeTitle?.takeIf { it.isNotBlank() }?.let { "$badge  $it" } ?: badge
                } else {
                    entry.title
                }
                Column(
                    modifier = Modifier
                        .width(264.dp)
                        .pressScale(interactionSource)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CinemaColors.Sheet)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Button,
                        ) { onClick(entry) },
                ) {
                    CinemaAsyncImage(
                        model = entry.episodeThumbnail?.takeIf { it.isNotBlank() }
                            ?: entry.background?.takeIf { it.isNotBlank() }
                            ?: entry.poster,
                        contentDescription = entry.title,
                        fallbackTitle = entry.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    )
                    Text(
                        text = label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 18.dp),
                        fontSize = 17.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyFlowixProfileMenu(
    profiles: List<NuvioProfile>,
    activeProfile: NuvioProfile?,
    avatars: List<AvatarCatalogItem>,
    onDismiss: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenAccount: () -> Unit,
    onSwitchProfile: (() -> Unit)?,
    onSelectProfile: ((NuvioProfile) -> Unit)?,
    onEditProfile: (() -> Unit)?,
    onSignOut: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    fun dismissThen(action: () -> Unit = {}) {
        coroutineScope.launch {
            dismissNuvioBottomSheet(sheetState = sheetState, onDismiss = onDismiss)
            action()
        }
    }
    val otherProfiles = profiles.filter { it.profileIndex != activeProfile?.profileIndex }

    NuvioModalBottomSheet(
        onDismissRequest = { dismissThen() },
        sheetState = sheetState,
        containerColor = CinemaColors.Sheet,
        showDragHandle = true,
        fullHeight = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(
                    text = stringResource(Res.string.my_flowix_profiles),
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                MenuCircleButton(
                    icon = Icons.Rounded.Close,
                    label = stringResource(Res.string.action_close),
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = { dismissThen() },
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF2E2E2E))
                    .padding(vertical = 24.dp),
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (activeProfile != null) {
                        AppLoadingProfileAvatar(profile = activeProfile, modifier = Modifier.size(116.dp))
                    }
                    Text(
                        text = activeProfile.displayName(),
                        fontSize = 38.sp,
                        lineHeight = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (onEditProfile != null) {
                    MenuCircleButton(
                        icon = Icons.Rounded.Edit,
                        label = stringResource(Res.string.my_flowix_edit_profile),
                        modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp),
                        onClick = { dismissThen(onEditProfile) },
                    )
                }
            }

            if (onSwitchProfile != null) {
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(CinemaSpace.s8),
                ) {
                    otherProfiles.take(3).forEach { profile ->
                        // A PIN-locked profile goes through the picker, which asks for the PIN.
                        val open: () -> Unit = if (profile.pinEnabled || onSelectProfile == null) {
                            { onSwitchProfile() }
                        } else {
                            { onSelectProfile(profile) }
                        }
                        ProfileChoice(label = profile.displayName(), onClick = { dismissThen(open) }) {
                            AppLoadingProfileAvatar(profile = profile, modifier = Modifier.size(72.dp))
                        }
                    }
                    ProfileChoice(
                        label = stringResource(Res.string.compose_profile_add_profile),
                        onClick = { dismissThen(onSwitchProfile) },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CinemaProfileAvatarShape)
                                .background(Color(0xFF3A3A3A)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(34.dp))
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .heightIn(min = 44.dp)
                        .clip(tokens.shapes.chip)
                        .background(Color(0xFF474747))
                        .clickable(role = Role.Button) { dismissThen(onSwitchProfile) }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.my_flowix_manage_profiles),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.colors.textPrimary,
                    )
                }
            }

            MenuRow(
                icon = Icons.Outlined.Settings,
                label = stringResource(Res.string.my_flowix_app_settings),
                onClick = { dismissThen(onOpenAppSettings) },
            )
            MenuRow(
                icon = Icons.Outlined.Person,
                label = stringResource(Res.string.settings_account),
                onClick = { dismissThen(onOpenAccount) },
            )
            MenuRow(
                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                label = stringResource(Res.string.settings_account_sign_out),
                onClick = { dismissThen(onSignOut) },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    Res.string.compose_about_version_format,
                    AppVersionConfig.VERSION_NAME,
                    AppVersionConfig.VERSION_CODE.toString(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun ProfileChoice(
    label: String,
    onClick: () -> Unit,
    avatar: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(84.dp)
            .clip(MyFlowixRowShape)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        avatar()
        Text(
            text = label,
            style = CinemaType.caption.copy(color = Color.White),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(CinemaColors.SheetRow)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = tokens.colors.textPrimary,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun MenuCircleButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(tokens.shapes.avatar)
            .background(tokens.colors.overlaySelected)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tokens.colors.textPrimary, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun NuvioProfile?.displayName(): String =
    this?.name?.takeIf { it.isNotBlank() }
        ?: this?.let { stringResource(Res.string.profile_label_number, it.profileIndex) }
        ?: stringResource(Res.string.nav_my_flowix)
