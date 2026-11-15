package com.nuvio.app.features.myflowix

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
    onSwitchProfile: (() -> Unit)? = null,
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
                avatars = avatars,
                onProfileClick = { showProfileMenu = true },
                onDownloadsClick = onDownloadsClick,
            )
        }
        item(key = "my_flowix_downloads") {
            Row(
                modifier = Modifier
                    .padding(horizontal = MyFlowixHorizontalPadding)
                    .fillMaxWidth()
                    .clip(MyFlowixRowShape)
                    .background(tokens.colors.surface)
                    .clickable(role = Role.Button, onClick = onDownloadsClick)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = null,
                    tint = tokens.colors.textPrimary,
                    modifier = Modifier.size(24.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(Res.string.compose_settings_root_downloads_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.colors.textPrimary,
                    )
                    Text(
                        text = stringResource(Res.string.my_flowix_downloads_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.colors.textSecondary,
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
                    modifier = Modifier.padding(top = 4.dp),
                    onItemClick = onContinueWatchingClick,
                    onItemLongPress = onContinueWatchingLongPress,
                )
            }
        }
        item(key = "my_flowix_my_list") {
            if (myList.isEmpty()) {
                MyListEmptyRow(onClick = onOpenLibrary)
            } else {
                NuvioShelfSection(
                    title = stringResource(Res.string.home_hero_my_list),
                    entries = myList,
                    modifier = Modifier.padding(top = 4.dp),
                    headerHorizontalPadding = MyFlowixHorizontalPadding,
                    rowContentPadding = PaddingValues(horizontal = MyFlowixHorizontalPadding),
                    itemSpacing = 8.dp,
                    onViewAllClick = onOpenLibrary,
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
            onEditProfile = onEditProfile,
        )
    }
}

@Composable
private fun MyFlowixHeader(
    profile: NuvioProfile?,
    avatars: List<AvatarCatalogItem>,
    onProfileClick: () -> Unit,
    onDownloadsClick: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = MyFlowixHorizontalPadding, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .heightIn(min = 48.dp)
                .clip(MyFlowixRowShape)
                .clickable(role = Role.Button, onClick = onProfileClick),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActiveProfileMiniAvatar(profile = profile, avatars = avatars, selected = false, size = 32)
            Text(
                text = profile.displayName(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = tokens.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = stringResource(Res.string.my_flowix_profiles),
                tint = tokens.colors.textPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(tokens.shapes.avatar)
                .clickable(role = Role.Button, onClick = onDownloadsClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = stringResource(Res.string.compose_settings_root_downloads_title),
                tint = tokens.colors.textPrimary,
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
            .padding(horizontal = MyFlowixHorizontalPadding)
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .clickable(role = Role.Button, onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.home_hero_my_list),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = tokens.colors.textPrimary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.my_flowix_see_all),
                    style = MaterialTheme.typography.bodyLarge,
                    color = tokens.colors.textPrimary,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = tokens.colors.textPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Text(
            text = stringResource(Res.string.my_flowix_my_list_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.colors.textSecondary,
        )
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
        Text(
            text = stringResource(Res.string.my_flowix_recently_watched),
            modifier = Modifier.padding(horizontal = MyFlowixHorizontalPadding),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = tokens.colors.textPrimary,
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
                        .width(220.dp)
                        .pressScale(interactionSource)
                        .clip(RoundedCornerShape(4.dp))
                        .background(tokens.colors.surface)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Button,
                        ) { onClick(entry) },
                ) {
                    AsyncImage(
                        model = entry.episodeThumbnail?.takeIf { it.isNotBlank() }
                            ?: entry.background?.takeIf { it.isNotBlank() }
                            ?: entry.poster,
                        contentDescription = entry.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(tokens.colors.surfaceCard),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.colors.textPrimary,
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
    onEditProfile: (() -> Unit)?,
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
        showDragHandle = false,
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = tokens.colors.textPrimary,
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
                    .clip(RoundedCornerShape(16.dp))
                    .background(tokens.colors.surfaceCard)
                    .padding(vertical = 20.dp),
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ActiveProfileMiniAvatar(profile = activeProfile, avatars = avatars, selected = false, size = 96)
                    Text(
                        text = activeProfile.displayName(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = tokens.colors.textPrimary,
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    otherProfiles.take(3).forEach { profile ->
                        ProfileChoice(label = profile.displayName(), onClick = { dismissThen(onSwitchProfile) }) {
                            ActiveProfileMiniAvatar(profile = profile, avatars = avatars, selected = false, size = 64)
                        }
                    }
                    ProfileChoice(
                        label = stringResource(Res.string.compose_profile_add_profile),
                        onClick = { dismissThen(onSwitchProfile) },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(tokens.colors.surfaceCard),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Add, null, tint = tokens.colors.textPrimary, modifier = Modifier.size(28.dp))
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .heightIn(min = 44.dp)
                        .clip(tokens.shapes.chip)
                        .background(tokens.colors.surfaceCard)
                        .clickable(role = Role.Button) { dismissThen(onSwitchProfile) }
                        .padding(horizontal = 18.dp),
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
                icon = Icons.Rounded.Settings,
                label = stringResource(Res.string.my_flowix_app_settings),
                onClick = { dismissThen(onOpenAppSettings) },
            )
            MenuRow(
                icon = Icons.Rounded.Person,
                label = stringResource(Res.string.settings_account),
                onClick = { dismissThen(onOpenAccount) },
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
            .width(72.dp)
            .clip(MyFlowixRowShape)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        avatar()
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.nuvio.colors.textPrimary,
            maxLines = 1,
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
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(tokens.colors.surfaceCard)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tokens.colors.textPrimary, modifier = Modifier.size(24.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = tokens.colors.textPrimary,
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
