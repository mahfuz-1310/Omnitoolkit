package com.example.feature.system.apps

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.feature.system.apps.model.AppEnabledState
import com.example.feature.system.apps.model.AppItem
import com.example.feature.system.apps.util.SafeBlocklist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testSafeBlocklist_protectsCriticalSystemApps() {
        assertTrue(SafeBlocklist.isSafeBlocklisted("android", context))
        assertTrue(SafeBlocklist.isSafeBlocklisted("com.android.systemui", context))
        assertTrue(SafeBlocklist.isSafeBlocklisted("com.android.packageinstaller", context))
        assertTrue(SafeBlocklist.isSafeBlocklisted("com.google.android.packageinstaller", context))
        assertTrue(SafeBlocklist.isSafeBlocklisted("com.android.settings", context))
        assertTrue(SafeBlocklist.isSafeBlocklisted("com.android.vending", context))
        assertTrue(SafeBlocklist.isSafeBlocklisted(context.packageName, context))
    }

    @Test
    fun testSafeBlocklist_allowsNormalApps() {
        assertFalse(SafeBlocklist.isSafeBlocklisted("com.example.thirdpartyapp", context))
        assertFalse(SafeBlocklist.isSafeBlocklisted("org.wikipedia", context))
        assertFalse(SafeBlocklist.isSafeBlocklisted("com.spotify.music", context))
    }

    @Test
    fun testAppItem_creationAndProperties() {
        val app = AppItem(
            appName = "Demo App",
            packageName = "com.demo.app",
            versionName = "2.1.0",
            versionCode = 42L,
            firstInstallTime = 1000L,
            lastUpdateTime = 2000L,
            firstInstallTimeFormatted = "01/01/2026 - 10:00",
            lastUpdateTimeFormatted = "01/02/2026 - 12:00",
            isSystemApp = false,
            isLaunchable = true,
            isEnabled = true,
            enabledState = AppEnabledState.ENABLED,
            isSafeBlocklisted = false,
            icon = null
        )

        assertEquals("Demo App", app.appName)
        assertEquals("com.demo.app", app.packageName)
        assertTrue(app.isEnabled)
        assertEquals(AppEnabledState.ENABLED, app.enabledState)
        assertFalse(app.isSafeBlocklisted)
    }
}
