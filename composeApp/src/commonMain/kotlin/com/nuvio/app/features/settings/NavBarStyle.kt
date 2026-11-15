package com.nuvio.app.features.settings

import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.settings_nav_bar_style_expanded
import nuvio.composeapp.generated.resources.settings_nav_bar_style_classic
import nuvio.composeapp.generated.resources.settings_nav_bar_position_bottom
import nuvio.composeapp.generated.resources.settings_nav_bar_position_top
import org.jetbrains.compose.resources.StringResource

enum class NavBarStyle(
    val key: String,
    val labelRes: StringResource,
) {
    EXPANDED("expanded", Res.string.settings_nav_bar_style_expanded),
    CLASSIC("classic", Res.string.settings_nav_bar_style_classic),
    ;

    companion object {
        /** The bar never shrinks on scroll: the old "adaptive" and "compact" styles read back as [EXPANDED]. */
        fun fromKey(key: String?): NavBarStyle =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: EXPANDED
    }
}

/** Where the floating pill sits. CLASSIC ignores it. */
enum class NavBarPosition(
    val key: String,
    val labelRes: StringResource,
) {
    BOTTOM("bottom", Res.string.settings_nav_bar_position_bottom),
    TOP("top", Res.string.settings_nav_bar_position_top),
    ;

    companion object {
        fun fromKey(key: String?): NavBarPosition =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: BOTTOM
    }
}
