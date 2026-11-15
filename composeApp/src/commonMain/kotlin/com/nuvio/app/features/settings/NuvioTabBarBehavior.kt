package com.nuvio.app.features.settings

import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.settings_tab_bar_behavior_off
import nuvio.composeapp.generated.resources.settings_tab_bar_behavior_off_description
import nuvio.composeapp.generated.resources.settings_tab_bar_behavior_static
import nuvio.composeapp.generated.resources.settings_tab_bar_behavior_static_description
import org.jetbrains.compose.resources.StringResource

/**
 * How the iOS 26 Liquid Glass tab bar behaves. Replaces the old on/off toggle so there is a single
 * source of truth — [OFF] is what used to be "Liquid Glass disabled".
 *
 * The bar never shrinks or hides while a page scrolls: the old "auto_hide" and "morphed" choices
 * collapsed it into a single selected-tab pill, and both now read back as [STATIC].
 *
 * The [key] is what gets published to Swift through NativeTabBridge, so these strings must stay in
 * sync with `NuvioTabBarBehavior` in the iOS app.
 */
enum class NuvioTabBarBehavior(
    val key: String,
    val labelRes: StringResource,
    val descriptionRes: StringResource,
) {
    OFF(
        key = "off",
        labelRes = Res.string.settings_tab_bar_behavior_off,
        descriptionRes = Res.string.settings_tab_bar_behavior_off_description,
    ),
    STATIC(
        key = "static",
        labelRes = Res.string.settings_tab_bar_behavior_static,
        descriptionRes = Res.string.settings_tab_bar_behavior_static_description,
    ),
    ;

    val isEnabled: Boolean
        get() = this != OFF

    companion object {
        val Default: NuvioTabBarBehavior = STATIC

        private val retiredScrollKeys = setOf("auto_hide", "morphed")

        fun fromKey(key: String?): NuvioTabBarBehavior? {
            if (key != null && retiredScrollKeys.any { it.equals(key, ignoreCase = true) }) return STATIC
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
        }

        /**
         * Migration path for profiles saved before this became a choice: the old boolean only told
         * us whether Liquid Glass was on, so an enabled bar adopts the default.
         */
        fun fromLegacyEnabled(enabled: Boolean?): NuvioTabBarBehavior = when (enabled) {
            null -> Default
            true -> Default
            false -> OFF
        }
    }
}
