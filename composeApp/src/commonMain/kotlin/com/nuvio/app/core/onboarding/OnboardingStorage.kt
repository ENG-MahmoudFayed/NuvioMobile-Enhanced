package com.nuvio.app.core.onboarding

/**
 * Device-wide "shown once" flags: the profiles intro after the first sign-in, and one-time
 * migrations such as moving existing profiles onto the Cinema look.
 */
internal expect object OnboardingStorage {
    fun isDone(flag: String): Boolean
    fun markDone(flag: String)
}

internal object OnboardingFlags {
    const val ProfilesIntroSeen = "profiles_intro_seen"
    const val CinemaThemeMigrated = "cinema_theme_migrated"
    const val CinemaDetailTabs = "cinema_detail_tabs"
}
