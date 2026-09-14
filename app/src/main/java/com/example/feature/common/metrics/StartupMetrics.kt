package com.example.feature.common.metrics

import android.os.SystemClock
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StartupData(
    val appProcessStartMs: Long = 0L,
    val activityCreateMs: Long = 0L,
    val firstComposeMs: Long = 0L,
    val firstDrawMs: Long = 0L,
    val ttidMs: Long = 0L,
    val ttffMs: Long = 0L
)

object StartupMetrics {
    private const val TAG = "StartupMetrics"
    
    // Captured as soon as this class or process starts
    private val appProcessStart = System.currentTimeMillis()
    private val appProcessStartNanos = SystemClock.elapsedRealtimeNanos()

    private var activityCreateTime: Long = 0L
    private var firstComposeTime: Long = 0L
    private var firstDrawTime: Long = 0L
    private var fullContentTime: Long = 0L

    private val _startupData = MutableStateFlow(StartupData())
    val startupData: StateFlow<StartupData> = _startupData.asStateFlow()

    fun onActivityCreate() {
        if (activityCreateTime == 0L) {
            activityCreateTime = System.currentTimeMillis()
            updateData()
        }
    }

    fun onFirstCompose() {
        if (firstComposeTime == 0L) {
            firstComposeTime = System.currentTimeMillis()
            updateData()
        }
    }

    fun onFirstDraw() {
        if (firstDrawTime == 0L) {
            firstDrawTime = System.currentTimeMillis()
            updateData()
            val ttid = (firstDrawTime - appProcessStart).coerceAtLeast(0)
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "⚡ TTID Measured: ${ttid}ms | ActivityCreate: ${activityCreateTime - appProcessStart}ms | FirstCompose: ${firstComposeTime - appProcessStart}ms")
            }
        }
    }

    fun onFullContentRendered() {
        if (fullContentTime == 0L) {
            fullContentTime = System.currentTimeMillis()
            updateData()
            val ttff = (fullContentTime - appProcessStart).coerceAtLeast(0)
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "⚡ TTFF Measured: ${ttff}ms")
            }
        }
    }

    private fun updateData() {
        val ttid = if (firstDrawTime > 0) (firstDrawTime - appProcessStart).coerceAtLeast(0) else 0L
        val ttff = if (fullContentTime > 0) (fullContentTime - appProcessStart).coerceAtLeast(0) else 0L
        _startupData.value = StartupData(
            appProcessStartMs = appProcessStart,
            activityCreateMs = if (activityCreateTime > 0) (activityCreateTime - appProcessStart).coerceAtLeast(0) else 0L,
            firstComposeMs = if (firstComposeTime > 0) (firstComposeTime - appProcessStart).coerceAtLeast(0) else 0L,
            firstDrawMs = if (firstDrawTime > 0) (firstDrawTime - appProcessStart).coerceAtLeast(0) else 0L,
            ttidMs = ttid,
            ttffMs = ttff
        )
    }
}
