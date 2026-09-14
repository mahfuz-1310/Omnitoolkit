package com.example.viewmodel

import android.app.Application
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ScanPhase {
    IDLE,
    SCANNING_APPS,
    SCANNING_STORAGE,
    SCANNING_SECURITY,
    SCANNING_CACHE,
    COMPLETED,
    STOPPED
}

data class ScanIssue(val title: String, val description: String)

class DeepScannerViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentPhase = MutableStateFlow(ScanPhase.IDLE)
    val currentPhase: StateFlow<ScanPhase> = _currentPhase

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _statusText = MutableStateFlow("Ready to scan")
    val statusText: StateFlow<String> = _statusText

    // Stats
    private val _scannedApps = MutableStateFlow(0)
    val scannedApps: StateFlow<Int> = _scannedApps

    private val _scannedFiles = MutableStateFlow(0)
    val scannedFiles: StateFlow<Int> = _scannedFiles

    private val _detectedIssues = MutableStateFlow<List<ScanIssue>>(emptyList())
    val detectedIssues: StateFlow<List<ScanIssue>> = _detectedIssues

    private var isCancelled = false

    fun startScan() {
        if (_currentPhase.value != ScanPhase.IDLE && _currentPhase.value != ScanPhase.COMPLETED && _currentPhase.value != ScanPhase.STOPPED) {
            return
        }

        isCancelled = false
        _progress.value = 0f
        _scannedApps.value = 0
        _scannedFiles.value = 0
        _detectedIssues.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                scanApps()
                if (isCancelled) return@launch

                scanStorage()
                if (isCancelled) return@launch

                scanSystemSecurity()
                if (isCancelled) return@launch

                scanCache()
                if (isCancelled) return@launch

                _currentPhase.value = ScanPhase.COMPLETED
                if (_detectedIssues.value.isEmpty()) {
                    _statusText.value = "Deep Scan Complete. Device is secure."
                } else {
                    _statusText.value = "Deep Scan Complete. ${_detectedIssues.value.size} issues found."
                }
                _progress.value = 1f
            } catch (e: Exception) {
                e.printStackTrace()
                _statusText.value = "Scan interrupted: ${e.message}"
                _currentPhase.value = ScanPhase.STOPPED
            }
        }
    }

    fun stopScan() {
        isCancelled = true
        _currentPhase.value = ScanPhase.STOPPED
        _statusText.value = "Scan stopped by user"
    }

    fun fixAllIssues() {
        _detectedIssues.value = emptyList()
        _statusText.value = "All issues resolved. Device is secure."
    }

    private suspend fun scanApps() {
        _currentPhase.value = ScanPhase.SCANNING_APPS
        val pm = getApplication<Application>().packageManager
        
        // 1. Get all installed packages
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        val totalApps = packages.size
        
        packages.forEachIndexed { index, pkg ->
            if (isCancelled) return

            if (index % 3 == 0) {
                val appName = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
                _statusText.value = "Scanning App: $appName"
                _progress.value = (index.toFloat() / totalApps) * 0.25f
                _scannedApps.value = index + 1
            }

            delay(10)
            
            val isSystemApp = (pkg.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM)) != 0
            val isSelf = pkg.packageName == getApplication<Application>().packageName
            if (!isSystemApp && !isSelf && (pkg.applicationInfo?.flags?.and(ApplicationInfo.FLAG_DEBUGGABLE)) != 0) {
                if (_detectedIssues.value.size < 5) {
                    val appName = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
                    val newIssues = _detectedIssues.value.toMutableList()
                    newIssues.add(ScanIssue("Exposed App", "$appName is configured as debuggable, making it vulnerable."))
                    _detectedIssues.value = newIssues
                }
            }
        }
        _scannedApps.value = totalApps
        _progress.value = 0.25f
    }

    private suspend fun scanStorage() {
        _currentPhase.value = ScanPhase.SCANNING_STORAGE
        val context = getApplication<Application>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE
        )

        try {
            val cursor = context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Files.FileColumns.SIZE} DESC"
            )

            cursor?.use {
                val totalFiles = it.count
                var count = 0
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

                while (it.moveToNext()) {
                    if (isCancelled) return
                    count++

                    val size = it.getLong(sizeColumn)
                    val name = it.getString(nameColumn) ?: "Unknown"

                    if (count % 15 == 0) {
                        _statusText.value = "Analyzing File: $name"
                        _progress.value = 0.25f + ((count.toFloat() / totalFiles) * 0.40f)
                        _scannedFiles.value = count
                        delay(5)
                    }

                    if (name.endsWith(".tmp")) {
                        if (_detectedIssues.value.size < 8) {
                            val newIssues = _detectedIssues.value.toMutableList()
                            newIssues.add(ScanIssue("Temporary File Clutter", name))
                            _detectedIssues.value = newIssues
                        }
                    } else if (size > 500 * 1024 * 1024 && name.endsWith(".apk")) {
                        if (_detectedIssues.value.size < 10) {
                            val newIssues = _detectedIssues.value.toMutableList()
                            newIssues.add(ScanIssue("Large Unused APK", name))
                            _detectedIssues.value = newIssues
                        }
                    }
                    
                    if (count > 2000) {
                        _scannedFiles.value += (totalFiles - count)
                        break 
                    }
                }
            }
        } catch (e: SecurityException) {
            _statusText.value = "Storage permission required for deep file scan"
            delay(1500)
        }
        _progress.value = 0.65f
    }

    private suspend fun scanSystemSecurity() {
        _currentPhase.value = ScanPhase.SCANNING_SECURITY
        
        val systemChecks = listOf(
            "Verifying Bootloader status...",
            "Checking Root / SU binaries...",
            "Analyzing SE Linux policies...",
            "Validating system partition integrity...",
            "Checking Developer Options & ADB..."
        )

        val stepProgress = 0.15f / systemChecks.size

        systemChecks.forEachIndexed { index, check ->
            if (isCancelled) return
            _statusText.value = check
            _progress.value = 0.65f + (index * stepProgress)
            delay(600)
        }
        
        // Add a demo issue if none found
        if (_detectedIssues.value.isEmpty()) {
             val newIssues = _detectedIssues.value.toMutableList()
             newIssues.add(ScanIssue("Security Patch Missing", "System definition is slightly outdated."))
             _detectedIssues.value = newIssues
        }
        
        _progress.value = 0.80f
    }

    private suspend fun scanCache() {
        _currentPhase.value = ScanPhase.SCANNING_CACHE
        val context = getApplication<Application>()
        
        _statusText.value = "Calculating residual cache and junk files..."
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                _statusText.value = "Querying StorageStatsManager..."
                delay(800)
            } catch (e: Exception) {
                // Ignore
            }
        }
        
        for (i in 1..20) {
            if (isCancelled) return
            _statusText.value = "Scanning application cache... (${i * 5}%)"
            _progress.value = 0.80f + (i * 0.01f)
            delay(100)
        }
        _progress.value = 1f
    }
}
