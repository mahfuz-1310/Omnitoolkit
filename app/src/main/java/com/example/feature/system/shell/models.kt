package com.example.feature.system.shell

import java.util.UUID

/**
 * Represents the current Shizuku service and permission status for Shell execution.
 */
enum class ShellStatus(val label: String) {
    READY("Ready"),
    SHIZUKU_REQUIRED("Shizuku Required"),
    PERMISSION_DENIED("Permission Denied"),
    STOPPED_BY_YOU("Stopped by You")
}

/**
 * Result of a shell command execution via Shizuku.
 */
data class ShellCommandResult(
    val command: String,
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val executionTimeMs: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isSuccess: Boolean get() = exitCode == 0
}

/**
 * Saved command history item.
 */
data class ShellHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val command: String,
    val timestamp: Long,
    val exitCode: Int,
    val executionTimeMs: Long
)

/**
 * Preset diagnostic shell commands for rapid execution.
 */
data class ShellPreset(
    val name: String,
    val command: String,
    val description: String,
    val category: String = "Diagnostics"
)

object ShellPresets {
    val list = listOf(
        ShellPreset(
            name = "whoami",
            command = "whoami",
            description = "Current shell user identity"
        ),
        ShellPreset(
            name = "id",
            command = "id",
            description = "User ID and group memberships"
        ),
        ShellPreset(
            name = "OS Version",
            command = "getprop ro.build.version.release",
            description = "Android OS release version"
        ),
        ShellPreset(
            name = "Device Model",
            command = "getprop ro.product.model",
            description = "Hardware product model name"
        ),
        ShellPreset(
            name = "List Packages",
            command = "pm list packages",
            description = "List all installed packages on device"
        ),
        ShellPreset(
            name = "Animation Scale",
            command = "settings get global window_animation_scale",
            description = "Global window animation scale factor"
        ),
        ShellPreset(
            name = "Battery Telemetry",
            command = "dumpsys battery",
            description = "Battery temperature, voltage and status snapshot"
        ),
        ShellPreset(
            name = "Kernel Architecture",
            command = "uname -a",
            description = "Linux kernel architecture and release"
        )
    )
}
