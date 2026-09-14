package com.example.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

data class LocationPreset(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

object FakeGpsManager {
    private const val TAG = "FakeGpsManager"
    private const val PREFS_NAME = "fake_gps_prefs"
    private const val KEY_LAT = "fake_lat"
    private const val KEY_LNG = "fake_lng"
    private const val KEY_ALT = "fake_alt"
    private const val KEY_PRESET_NAME = "fake_preset_name"

    val popularPresets = listOf(
        LocationPreset("Tokyo Tower", "Japan", 35.6586, 139.7454),
        LocationPreset("Times Square, NYC", "USA", 40.7580, -73.9855),
        LocationPreset("Eiffel Tower, Paris", "France", 48.8584, 2.2945),
        LocationPreset("Big Ben, London", "UK", 51.5007, -0.1246),
        LocationPreset("Burj Khalifa, Dubai", "UAE", 25.1972, 55.2744),
        LocationPreset("Sydney Opera House", "Australia", -33.8568, 151.2153),
        LocationPreset("Golden Gate Bridge, SF", "USA", 37.8199, -122.4783),
        LocationPreset("Colosseum, Rome", "Italy", 41.8902, 12.4922),
        LocationPreset("Marina Bay Sands", "Singapore", 1.2834, 103.8607)
    )

    private val _isMockingActive = MutableStateFlow(false)
    val isMockingActive: StateFlow<Boolean> = _isMockingActive.asStateFlow()

    private val _currentLat = MutableStateFlow(35.6586)
    val currentLat: StateFlow<Double> = _currentLat.asStateFlow()

    private val _currentLng = MutableStateFlow(139.7454)
    val currentLng: StateFlow<Double> = _currentLng.asStateFlow()

    private val _updateCounter = MutableStateFlow(0)
    val updateCounter: StateFlow<Int> = _updateCounter.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private var mockJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _currentLat.value = prefs.getFloat(KEY_LAT, 35.6586f).toDouble()
        _currentLng.value = prefs.getFloat(KEY_LNG, 139.7454f).toDouble()
    }

    fun saveCoordinates(context: Context, lat: Double, lng: Double, presetName: String = "Custom") {
        _currentLat.value = lat
        _currentLng.value = lng
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_LAT, lat.toFloat())
            .putFloat(KEY_LNG, lng.toFloat())
            .putString(KEY_PRESET_NAME, presetName)
            .apply()
    }

    fun getSavedPresetName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PRESET_NAME, "Tokyo Tower") ?: "Tokyo Tower"
    }

    /**
     * Checks if the app is currently allowed to add test providers (i.e. selected as Mock Location App).
     */
    fun isMockLocationAppAllowed(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        val testProvider = "gps_mock_test_probe"
        return try {
            try { locationManager.removeTestProvider(testProvider) } catch (_: Exception) {}
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.addTestProvider(
                    testProvider,
                    false, false, false, false, false, false, false,
                    android.location.provider.ProviderProperties.POWER_USAGE_LOW,
                    android.location.provider.ProviderProperties.ACCURACY_FINE
                )
            } else {
                @Suppress("DEPRECATION")
                locationManager.addTestProvider(
                    testProvider,
                    false, false, false, false, false, false, false,
                    Criteria.POWER_LOW,
                    Criteria.ACCURACY_FINE
                )
            }
            try { locationManager.removeTestProvider(testProvider) } catch (_: Exception) {}
            true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Attempts to start injecting mock locations.
     * Returns true if successfully started, false if SecurityException / not mock app.
     */
    @SuppressLint("WrongConstant", "MissingPermission")
    fun startMockLocation(context: Context, lat: Double, lng: Double): Boolean {
        _lastError.value = null
        saveCoordinates(context, lat, lng)
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: run {
                _lastError.value = "LocationManager not available on this device."
                return false
            }

        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

        // 1. Setup Providers
        for (provider in providers) {
            try {
                try {
                    locationManager.removeTestProvider(provider)
                } catch (ignored: Exception) { }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    locationManager.addTestProvider(
                        provider,
                        false,
                        false,
                        false,
                        false,
                        true,
                        true,
                        true,
                        android.location.provider.ProviderProperties.POWER_USAGE_LOW,
                        android.location.provider.ProviderProperties.ACCURACY_FINE
                    )
                } else {
                    @Suppress("DEPRECATION")
                    locationManager.addTestProvider(
                        provider,
                        false,
                        false,
                        false,
                        false,
                        true,
                        true,
                        true,
                        Criteria.POWER_LOW,
                        Criteria.ACCURACY_FINE
                    )
                }
                locationManager.setTestProviderEnabled(provider, true)
            } catch (e: SecurityException) {
                Log.w(TAG, "SecurityException: App is not selected as Mock Location app in Developer Options: ${e.message}")
                _lastError.value = "Please select this app under 'Select mock location app' in Developer Options"
                _isMockingActive.value = false
                return false
            } catch (e: Exception) {
                Log.w(TAG, "Warning initializing test provider $provider: ${e.message}")
            }
        }

        // 2. Start continuous periodic updates
        mockJob?.cancel()
        _isMockingActive.value = true
        _updateCounter.value = 0

        mockJob = scope.launch {
            while (isActive && _isMockingActive.value) {
                try {
                    injectLocation(locationManager, providers, lat, lng)
                    _updateCounter.value += 1
                } catch (e: SecurityException) {
                    _lastError.value = "Mock permission revoked in Developer Options."
                    _isMockingActive.value = false
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Error injecting mock location: ${e.message}")
                }
                delay(1000)
            }
        }

        return true
    }

    private fun injectLocation(
        locationManager: LocationManager,
        providers: List<String>,
        lat: Double,
        lng: Double
    ) {
        val now = System.currentTimeMillis()
        val elapsedNanos = SystemClock.elapsedRealtimeNanos()

        for (provider in providers) {
            val location = Location(provider).apply {
                latitude = lat
                longitude = lng
                altitude = 25.0
                time = now
                elapsedRealtimeNanos = elapsedNanos
                accuracy = 2.5f
                speed = 0.0f
                bearing = 0.0f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bearingAccuracyDegrees = 0.1f
                    verticalAccuracyMeters = 0.5f
                    speedAccuracyMetersPerSecond = 0.1f
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    elapsedRealtimeUncertaintyNanos = 0.0
                }
            }
            try {
                locationManager.setTestProviderLocation(provider, location)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set location for $provider: ${e.message}")
            }
        }
    }

    fun stopMockLocation(context: Context) {
        mockJob?.cancel()
        mockJob = null
        _isMockingActive.value = false

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return

        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        for (provider in providers) {
            try {
                locationManager.setTestProviderEnabled(provider, false)
                locationManager.removeTestProvider(provider)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to remove test provider $provider: ${e.message}")
            }
        }
    }

    fun openDeveloperSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }
}
