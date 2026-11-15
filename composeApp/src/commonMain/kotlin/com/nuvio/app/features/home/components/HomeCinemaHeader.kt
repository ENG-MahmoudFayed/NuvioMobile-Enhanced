package com.nuvio.app.features.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.CinemaCategoryPill
import com.nuvio.app.core.ui.CinemaClearPill
import com.nuvio.app.core.ui.CinemaIconAction
import com.nuvio.app.core.ui.CinemaTopBar
import com.nuvio.app.core.ui.NuvioDropdownOption
import com.nuvio.app.core.ui.NuvioDropdownOptionsSheet
import com.nuvio.app.core.ui.dismissNuvioBottomSheet
import com.nuvio.app.core.ui.nuvioConsumePointerEvents
import com.nuvio.app.features.settings.AppBrandMark
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.home_action_downloads
import nuvio.composeapp.generated.resources.home_action_notifications
import nuvio.composeapp.generated.resources.home_filter_all_categories
import nuvio.composeapp.generated.resources.home_filter_categories
import nuvio.composeapp.generated.resources.home_filter_clear
import nuvio.composeapp.generated.resources.home_filter_movies
import nuvio.composeapp.generated.resources.home_filter_shows
import org.jetbrains.compose.resources.stringResource

/** The title row plus the category pills under it. */
internal val HomeCinemaHeaderHeight: Dp = 104.dp
private const val HomeCinemaAllCategoriesKey = ""

internal enum class HomeTypeFilter { Shows, Movies }

internal data class HomeContentFilter(
    val type: HomeTypeFilter? = null,
    val genre: String? = null,
) {
    val isActive: Boolean get() = type != null || genre != null
}

private val ShowTypes = setOf("series", "tv", "show", "anime")

internal fun HomeContentFilter.accepts(type: String, genres: List<String>): Boolean {
    val normalizedType = type.trim().lowercase()
    val typeMatches = when (this.type) {
        null -> true
        HomeTypeFilter.Shows -> normalizedType in ShowTypes
        HomeTypeFilter.Movies -> normalizedType == "movie"
    }
    val genreMatches = genre == null || genres.any { it.equals(genre, ignoreCase = true) }
    return typeMatches && genreMatches
}

/**
 * Home's top bar: the Flowix mark and a large "Home", download and notification icons, and the
 * Shows / Movies / Categories pills. Clear over the hero at the top of the page; a dark bar once
 * the page scrolls, with the pills folding away on the way down and returning on the way up.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeCinemaHeader(
    title: String,
    listState: LazyListState,
    filter: HomeContentFilter,
    genreOptions: List<String>,
    onFilterChange: (HomeContentFilter) -> Unit,
    onDownloadsClick: (() -> Unit)?,
    onNotificationsClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var pillsVisible by remember { mutableStateOf(true) }
    var scrolled by remember { mutableStateOf(false) }
    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val atTop = index == 0 && offset < 24
                scrolled = !atTop
                val delta = when {
                    index > previousIndex -> 1
                    index < previousIndex -> -1
                    else -> offset - previousOffset
                }
                when {
                    atTop -> pillsVisible = true
                    delta > 6 -> pillsVisible = false
                    delta < -6 -> pillsVisible = true
                }
                previousIndex = index
                previousOffset = offset
            }
    }
    val barAlpha by animateFloatAsState(if (scrolled) 1f else 0f, tween(220), label = "home_header_bar")

    var categoriesOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF1A1A1A).copy(alpha = 0.55f + 0.4f * barAlpha),
                    1f to Color(0xFF121212).copy(alpha = 0.92f * barAlpha),
                ),
            )
            .nuvioConsumePointerEvents()
            .padding(top = statusBarTop),
    ) {
        CinemaTopBar(
            title = title,
            leading = { AppBrandMark(modifier = Modifier.size(34.dp)) },
            actions = {
                if (onDownloadsClick != null) {
                    CinemaIconAction(
                        icon = Icons.Outlined.FileDownload,
                        contentDescription = stringResource(Res.string.home_action_downloads),
                        onClick = onDownloadsClick,
                    )
                }
                if (onNotificationsClick != null) {
                    CinemaIconAction(
                        icon = Icons.Outlined.Notifications,
                        contentDescription = stringResource(Res.string.home_action_notifications),
                        onClick = onNotificationsClick,
                    )
                }
            },
        )
        AnimatedVisibility(
            visible = pillsVisible,
            enter = expandVertically(tween(220)) + fadeIn(tween(220)),
            exit = shrinkVertically(tween(220)) + fadeOut(tween(160)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HomeCinemaHeaderHeight - 56.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (filter.isActive) {
                    CinemaClearPill(
                        contentDescription = stringResource(Res.string.home_filter_clear),
                        onClick = { onFilterChange(HomeContentFilter()) },
                    )
                }
                if (filter.type != HomeTypeFilter.Movies) {
                    CinemaCategoryPill(
                        text = stringResource(Res.string.home_filter_shows),
                        selected = filter.type == HomeTypeFilter.Shows,
                        onClick = {
                            onFilterChange(
                                filter.copy(type = if (filter.type == HomeTypeFilter.Shows) null else HomeTypeFilter.Shows),
                            )
                        },
                    )
                }
                if (filter.type != HomeTypeFilter.Shows) {
                    CinemaCategoryPill(
                        text = stringResource(Res.string.home_filter_movies),
                        selected = filter.type == HomeTypeFilter.Movies,
                        onClick = {
                            onFilterChange(
                                filter.copy(type = if (filter.type == HomeTypeFilter.Movies) null else HomeTypeFilter.Movies),
                            )
                        },
                    )
                }
                if (genreOptions.isNotEmpty()) {
                    CinemaCategoryPill(
                        text = filter.genre ?: stringResource(Res.string.home_filter_categories),
                        selected = filter.genre != null,
                        showsDropdown = true,
                        onClick = { categoriesOpen = true },
                    )
                }
                Spacer(Modifier.size(6.dp))
            }
        }
    }

    if (categoriesOpen) {
        val allLabel = stringResource(Res.string.home_filter_all_categories)
        val options = remember(genreOptions, allLabel) {
            listOf(NuvioDropdownOption(HomeCinemaAllCategoriesKey, allLabel)) +
                genreOptions.map { NuvioDropdownOption(it, it) }
        }
        NuvioDropdownOptionsSheet(
            title = stringResource(Res.string.home_filter_categories),
            options = options,
            selectedKey = filter.genre ?: HomeCinemaAllCategoriesKey,
            sheetState = sheetState,
            onDismiss = {
                scope.launch {
                    dismissNuvioBottomSheet(sheetState = sheetState, onDismiss = { categoriesOpen = false })
                }
            },
            onSelected = { option ->
                onFilterChange(filter.copy(genre = option.key.ifBlank { null }))
                scope.launch {
                    dismissNuvioBottomSheet(sheetState = sheetState, onDismiss = { categoriesOpen = false })
                }
            },
        )
    }
}
