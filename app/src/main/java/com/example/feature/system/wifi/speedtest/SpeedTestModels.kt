package com.example.feature.system.wifi.speedtest

enum class SpeedTestPhase {
    IDLE,
    PING,
    DOWNLOAD,
    UPLOAD,
    COMPLETED,
    ERROR
}

data class SpeedTestState(
    val phase: SpeedTestPhase = SpeedTestPhase.IDLE,
    val pingMs: Long = 0,
    val jitterMs: Long = 0,
    val downloadMbps: Double = 0.0,
    val uploadMbps: Double = 0.0,
    val currentSpeedMbps: Double = 0.0,
    val progress: Float = 0f,
    val errorMessage: String? = null
)
