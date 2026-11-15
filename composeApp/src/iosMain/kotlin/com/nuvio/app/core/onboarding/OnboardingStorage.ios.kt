package com.nuvio.app.core.onboarding

import platform.Foundation.NSUserDefaults

internal actual object OnboardingStorage {
    private const val KEY_PREFIX = "flowix_onboarding_"

    actual fun isDone(flag: String): Boolean =
        NSUserDefaults.standardUserDefaults.boolForKey(KEY_PREFIX + flag)

    actual fun markDone(flag: String) {
        NSUserDefaults.standardUserDefaults.setBool(true, forKey = KEY_PREFIX + flag)
    }
}
