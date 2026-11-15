package com.nuvio.app.core.ui

import androidx.compose.runtime.Composable

/**
 * True when the system asks for less motion (iOS Reduce Motion, Android "Remove animations").
 * Animations then swap travel and scale for plain fades, and loops stand still.
 */
@Composable
internal expect fun rememberReduceMotion(): Boolean
