package com.nuvio.app.core.onboarding

import android.content.Context
import android.content.SharedPreferences

internal actual object OnboardingStorage {
    private const val PREFS_NAME = "flowix_onboarding"

    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Before initialize() runs (host tests, early startup) every flag reads as done, so nothing
    // one-time fires against storage that cannot remember it.
    actual fun isDone(flag: String): Boolean =
        preferences?.getBoolean(flag, false) ?: true

    actual fun markDone(flag: String) {
        preferences?.edit()?.putBoolean(flag, true)?.apply()
    }
}
