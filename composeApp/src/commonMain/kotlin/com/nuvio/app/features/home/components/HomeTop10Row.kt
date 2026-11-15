package com.nuvio.app.features.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.NuvioShelfSection
import com.nuvio.app.core.ui.rememberPosterCardStyleUiState
import com.nuvio.app.features.home.HomeCatalogSection
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.stableKey
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.home_top10_title
import org.jetbrains.compose.resources.stringResource

private const val Top10Count = 10
private val Top10PopularityWords = listOf("top", "trending", "popular", "popolar", "tendenz")

/** The catalogue Home ranks as its Top 10: the first one named for popularity, else the first. */
internal fun List<HomeCatalogSection>.top10Source(): HomeCatalogSection? {
    val candidates = filter { it.items.size >= Top10Count }
    return candidates.firstOrNull { section ->
        Top10PopularityWords.any { word -> section.title.contains(word, ignoreCase = true) }
    } ?: candidates.firstOrNull()
}

/**
 * The first ten titles of a catalogue in its own order, each poster standing beside a tall
 * outlined rank number. The numbers are drawn as strokes so the poster reads first.
 */
@Composable
internal fun HomeTop10Row(
    section: HomeCatalogSection,
    entries: List<MetaPreview>,
    sectionPadding: Dp,
    modifier: Modifier = Modifier,
    onPosterClick: ((MetaPreview) -> Unit)? = null,
    onPosterLongClick: ((MetaPreview) -> Unit)? = null,
) {
    val posterStyle = rememberPosterCardStyleUiState()
    val posterWidth = posterStyle.widthDp.dp
    val posterHeight = posterStyle.heightDp.dp
    val ranked = entries.take(Top10Count).withIndex().toList()
    if (ranked.isEmpty()) return

    NuvioShelfSection(
        title = stringResource(Res.string.home_top10_title, section.title),
        entries = ranked,
        modifier = modifier,
        headerHorizontalPadding = sectionPadding,
        rowContentPadding = PaddingValues(horizontal = sectionPadding),
        itemSpacing = 4.dp,
        key = { (_, item) -> "top10:${item.stableKey()}" },
    ) { (index, item) ->
        val rank = index + 1
        val numberWidth = if (rank == 10) posterWidth * 0.95f else posterWidth * 0.62f
        Box(
            modifier = Modifier
                .width(numberWidth + posterWidth - posterWidth * 0.12f)
                .height(posterHeight),
        ) {
            Text(
                text = rank.toString(),
                style = TextStyle(
                    color = Color.White,
                    fontSize = (posterHeight.value * 0.92f).sp,
                    lineHeight = (posterHeight.value * 0.92f).sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-8).sp,
                    drawStyle = Stroke(width = 5f),
                ),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-4).dp, y = posterHeight * 0.16f),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(posterWidth),
            ) {
                // The rank number already says it; no extra "TOP 10" tag here.
                CompositionLocalProvider(LocalHomeTop10Keys provides emptySet()) {
                    HomePosterCard(
                        item = item,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onPosterClick?.let { { it(item) } },
                        onLongClick = onPosterLongClick?.let { { it(item) } },
                    )
                }
            }
        }
    }
}
