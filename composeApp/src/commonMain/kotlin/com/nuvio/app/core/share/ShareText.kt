package com.nuvio.app.core.share

import androidx.compose.runtime.Composable

/** Opens the platform share sheet with a line of text, such as a title and its link. */
@Composable
internal expect fun rememberShareText(): (String) -> Unit
