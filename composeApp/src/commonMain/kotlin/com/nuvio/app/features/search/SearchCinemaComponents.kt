package com.nuvio.app.features.search

import com.nuvio.app.core.ui.CinemaSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.CinemaAsyncImage
import com.nuvio.app.core.ui.CinemaColors
import com.nuvio.app.core.ui.CinemaShapes
import com.nuvio.app.core.ui.CinemaPlayCircle
import com.nuvio.app.core.ui.CinemaSectionTitle
import com.nuvio.app.core.ui.pressScale
import com.nuvio.app.features.home.MetaPreview
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.action_play
import nuvio.composeapp.generated.resources.compose_search_clear
import nuvio.composeapp.generated.resources.search_cancel
import org.jetbrains.compose.resources.stringResource

private val SearchFieldShape = RoundedCornerShape(12.dp)

/** Filled grey search field with a magnifier, and "Cancel" beside it while it has focus. */
@Composable
internal fun CinemaSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    focused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .clip(SearchFieldShape)
                .background(Color(0xFF333333).copy(alpha = 0.9f))
                .padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = CinemaColors.TextSecondary,
                modifier = Modifier.size(26.dp),
            )
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = CinemaColors.TextSecondary,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    cursorBrush = SolidColor(Color.White),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { onFocusChange(it.isFocused) },
                )
            }
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(role = Role.Button) { onQueryChange("") },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.compose_search_clear),
                        tint = CinemaColors.TextSecondary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = focused,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut(),
        ) {
            Text(
                text = stringResource(Res.string.search_cancel),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button) {
                        onQueryChange("")
                        focusManager.clearFocus()
                    }
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            )
        }
    }
}

/** Section heading for the search page: bold, one line, the full width. */
@Composable
internal fun SearchSectionTitle(text: String, modifier: Modifier = Modifier) {
    CinemaSectionTitle(text = text, modifier = modifier)
}

/** One recommendation: a wide still on the left, the title, and a ringed play button. */
@Composable
internal fun SearchRecommendationRow(
    item: MetaPreview,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onPlayClick: (() -> Unit)? = onClick,
) {
    val interaction = remember { MutableInteractionSource() }
    val artwork = item.landscapePoster ?: item.banner ?: item.poster
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction, pressedScale = 0.98f)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                enabled = onClick != null,
                role = Role.Button,
                onLongClick = onLongClick,
                onClick = { onClick?.invoke() },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CinemaAsyncImage(
            model = artwork,
            contentDescription = null,
            fallbackTitle = item.name,
            modifier = Modifier
                .width(150.dp)
                .aspectRatio(16f / 9f)
                .clip(CinemaShapes.Poster),
        )
        Text(
            text = item.name,
            color = Color.White,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(CinemaSize.MinTouchTarget + 4.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable(enabled = onPlayClick != null, role = Role.Button) { onPlayClick?.invoke() },
            contentAlignment = Alignment.Center,
        ) {
            CinemaPlayCircle(
                size = 42.dp,
                filled = false,
                contentDescription = stringResource(Res.string.action_play),
            )
        }
    }
}

/** Grey placeholder in the shape of a recommendation row, while the catalogue loads. */
@Composable
internal fun SearchRecommendationSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .width(150.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.06f)),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
            )
            Box(
                Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
            )
        }
    }
}
