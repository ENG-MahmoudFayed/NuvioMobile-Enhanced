package com.nuvio.app.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.CinemaColors
import com.nuvio.app.core.ui.CinemaEmptyState
import com.nuvio.app.core.ui.CinemaShapes
import com.nuvio.app.core.ui.CinemaSize
import com.nuvio.app.core.ui.CinemaSpace
import com.nuvio.app.core.ui.CinemaType
import com.nuvio.app.core.ui.NuvioScreen
import com.nuvio.app.core.ui.NuvioScreenHeader
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.notifications_empty_message
import nuvio.composeapp.generated.resources.notifications_empty_title
import nuvio.composeapp.generated.resources.notifications_title
import nuvio.composeapp.generated.resources.notifications_whats_new
import nuvio.composeapp.generated.resources.notifications_whats_new_subtitle
import org.jetbrains.compose.resources.stringResource

/**
 * The bell's destination. Flowix keeps no notification inbox yet, so the page says so plainly and
 * points to the release notes, the one stream of news the app already has.
 */
@Composable
internal fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenWhatsNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NuvioScreen(
        modifier = modifier.fillMaxSize(),
        horizontalPadding = 0.dp,
    ) {
        item(key = "notifications_header") {
            NuvioScreenHeader(
                title = stringResource(Res.string.notifications_title),
                onBack = onBack,
                modifier = Modifier.padding(horizontal = CinemaSpace.PageMargin),
            )
        }
        item(key = "notifications_whats_new") {
            WhatsNewRow(onClick = onOpenWhatsNew)
        }
        item(key = "notifications_empty") {
            CinemaEmptyState(
                icon = Icons.Outlined.NotificationsNone,
                title = stringResource(Res.string.notifications_empty_title),
                message = stringResource(Res.string.notifications_empty_message),
                modifier = Modifier.padding(top = CinemaSpace.s32),
            )
        }
    }
}

@Composable
private fun WhatsNewRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = CinemaSpace.PageMargin)
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(CinemaShapes.Card)
            .background(CinemaColors.Surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = CinemaSpace.s16, vertical = CinemaSpace.s12),
        horizontalArrangement = Arrangement.spacedBy(CinemaSpace.s16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CinemaShapes.Pill)
                .background(CinemaColors.Red.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.NewReleases,
                contentDescription = null,
                tint = CinemaColors.Red,
                modifier = Modifier.size(CinemaSize.IconSmall),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CinemaSpace.s4),
        ) {
            Text(
                text = stringResource(Res.string.notifications_whats_new),
                style = CinemaType.cardTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(Res.string.notifications_whats_new_subtitle),
                style = CinemaType.caption,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(CinemaSize.Icon),
        )
    }
}
