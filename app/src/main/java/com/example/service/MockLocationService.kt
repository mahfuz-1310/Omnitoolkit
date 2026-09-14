package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.utils.FakeGpsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground Service responsible for continuous injection of Mock GPS & Network coordinates.
 * Keeps the mock provider alive and updates ongoing status notification.
 */
class MockLocationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var isServiceRunning = false

    companion object {
        const val CHANNEL_ID = "mock_location_service_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "ACTION_START_MOCK_LOCATION"
        const val ACTION_STOP = "ACTION_STOP_MOCK_LOCATION"
        const val ACTION_UPDATE_COORDS = "ACTION_UPDATE_COORDS"

        const val EXTRA_LAT = "EXTRA_LATITUDE"
        const val EXTRA_LNG = "EXTRA_LONGITUDE"
        const val EXTRA_NAME = "EXTRA_LOCATION_NAME"

        fun start(context: Context, lat: Double, lng: Double, name: String = "Custom Location") {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_LAT, lat)
                putExtra(EXTRA_LNG, lng)
                putExtra(EXTRA_NAME, name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun updateLocation(context: Context, lat: Double, lng: Double, name: String = "Custom Location") {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_UPDATE_COORDS
                putExtra(EXTRA_LAT, lat)
                putExtra(EXTRA_LNG, lng)
                putExtra(EXTRA_NAME, name)
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val lat = intent.getDoubleExtra(EXTRA_LAT, FakeGpsManager.currentLat.value)
                val lng = intent.getDoubleExtra(EXTRA_LNG, FakeGpsManager.currentLng.value)
                val name = intent.getStringExtra(EXTRA_NAME) ?: FakeGpsManager.getSavedPresetName(this)
                startMocking(lat, lng, name)
            }
            ACTION_UPDATE_COORDS -> {
                val lat = intent.getDoubleExtra(EXTRA_LAT, FakeGpsManager.currentLat.value)
                val lng = intent.getDoubleExtra(EXTRA_LNG, FakeGpsManager.currentLng.value)
                val name = intent.getStringExtra(EXTRA_NAME) ?: FakeGpsManager.getSavedPresetName(this)
                if (isServiceRunning) {
                    FakeGpsManager.startMockLocation(this, lat, lng)
                    val notification = buildNotification(lat, lng, name)
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    manager?.notify(NOTIFICATION_ID, notification)
                }
            }
            ACTION_STOP -> {
                stopMocking()
            }
        }
        return START_STICKY
    }

    private fun startMocking(lat: Double, lng: Double, name: String) {
        val success = FakeGpsManager.startMockLocation(this, lat, lng)
        if (!success) {
            stopSelf()
            return
        }

        isServiceRunning = true
        val notification = buildNotification(lat, lng, name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Monitor state changes to keep notification & lifecycle in sync
        serviceScope.launch {
            FakeGpsManager.isMockingActive.collect { active ->
                if (!active && isServiceRunning) {
                    stopMocking()
                }
            }
        }
    }

    private fun stopMocking() {
        isServiceRunning = false
        FakeGpsManager.stopMockLocation(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(lat: Double, lng: Double, name: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MockLocationService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fake GPS Active • $name")
            .setContentText("Lat: %.5f, Lng: %.5f (Broadcasting to OS)".format(lat, lng))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingOpen)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Spoofing", pendingStop)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mock Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent status notification while Mock GPS Location is active."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        FakeGpsManager.stopMockLocation(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
