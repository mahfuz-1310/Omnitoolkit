package com.example.feature.system.apps

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NameGenApplication
import com.example.feature.system.apps.actions.AppActions
import com.example.feature.system.apps.capability.AppManagerCapability
import com.example.feature.system.apps.capability.Capabilities
import com.example.feature.system.apps.model.AppEnabledState
import com.example.feature.system.apps.model.AppItem
import com.example.feature.system.apps.util.SafeBlocklist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext

    private val _capability = MutableStateFlow(AppManagerCapability.NONE)
    val capability: StateFlow<AppManagerCapability> = _capability.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    val disabledApps: StateFlow<List<AppItem>> = _installedApps.map { list ->
        list.filter { !it.isEnabled }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isReverting = MutableStateFlow(false)
    val isReverting: StateFlow<Boolean> = _isReverting.asStateFlow()

    private val _revertProgressCount = MutableStateFlow(0)
    val revertProgressCount: StateFlow<Int> = _revertProgressCount.asStateFlow()

    private val _revertTotalCount = MutableStateFlow(0)
    val revertTotalCount: StateFlow<Int> = _revertTotalCount.asStateFlow()

    private val _lastRevertResult = MutableStateFlow<Pair<Int, Int>?>(null)
    val lastRevertResult: StateFlow<Pair<Int, Int>?> = _lastRevertResult.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _historyCount = MutableStateFlow(0)
    val historyCount: StateFlow<Int> = _historyCount.asStateFlow()

    companion object {
        private var memoryCachedApps: List<AppItem>? = null
        private val iconMemoryCache = java.util.concurrent.ConcurrentHashMap<String, android.graphics.drawable.Drawable>()
    }

    init {
        refreshCapability()
        observeHistoryCount()
        memoryCachedApps?.let {
            _installedApps.value = it
        }
    }

    fun onVisible() {
        if (_installedApps.value.isNotEmpty() || _isLoading.value) {
            return
        }
        memoryCachedApps?.let {
            _installedApps.value = it
            return
        }
        loadApps()
    }

    fun refreshCapability() {
        _capability.value = Capabilities.detectCapability(context)
    }

    fun requestShizukuPermission() {
        val requested = Capabilities.requestShizukuPermission()
        if (requested) {
            viewModelScope.launch {
                _snackbarMessage.emit("Requested Shizuku permission. Please grant in the prompt.")
            }
        } else {
            viewModelScope.launch {
                _snackbarMessage.emit("Shizuku is not installed or service is not running.")
            }
        }
        refreshCapability()
    }

    private fun observeHistoryCount() {
        try {
            val app = getApplication<NameGenApplication>()
            viewModelScope.launch {
                app.database.appHistoryDao().getAllHistory().collect { list ->
                    _historyCount.value = list.size
                }
            }
        } catch (_: Exception) {}
    }

    fun loadApps() {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            refreshCapability()
            try {
                val pm = context.packageManager
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.MATCH_ALL or PackageManager.MATCH_DISABLED_COMPONENTS
                } else {
                    PackageManager.GET_UNINSTALLED_PACKAGES or PackageManager.GET_DISABLED_COMPONENTS
                }

                val packages = kotlinx.coroutines.withTimeoutOrNull(12000L) {
                    pm.getInstalledPackages(flags)
                } ?: emptyList()

                val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
                val accumulatedList = mutableListOf<AppItem>()
                val chunkSize = 75

                packages.chunked(chunkSize).forEach { chunk ->
                    val chunkItems = chunk.mapNotNull { pkgInfo ->
                        try {
                            val appInfo = pkgInfo.applicationInfo ?: return@mapNotNull null
                            val pkgName = pkgInfo.packageName ?: return@mapNotNull null
                            val appName = pm.getApplicationLabel(appInfo).toString()
                            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                            val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                pkgInfo.longVersionCode
                            } else {
                                @Suppress("DEPRECATION")
                                pkgInfo.versionCode.toLong()
                            }

                            val installTime = pkgInfo.firstInstallTime
                            val updateTime = pkgInfo.lastUpdateTime
                            val firstInstallFormatted = dateFormat.format(Date(installTime))
                            val lastUpdateFormatted = dateFormat.format(Date(updateTime))

                            val enabledSetting = try {
                                pm.getApplicationEnabledSetting(pkgName)
                            } catch (_: Exception) {
                                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                            }

                            val isEnabled = when (enabledSetting) {
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> false
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
                                else -> appInfo.enabled
                            }

                            val enabledState = when (enabledSetting) {
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> AppEnabledState.DISABLED
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> AppEnabledState.DISABLED_USER
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> AppEnabledState.SUSPENDED
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> AppEnabledState.ENABLED
                                else -> if (appInfo.enabled) AppEnabledState.ENABLED else AppEnabledState.DISABLED
                            }

                            val isLaunchable = pm.getLaunchIntentForPackage(pkgName) != null
                            val isSafeBlocklisted = SafeBlocklist.isSafeBlocklisted(pkgName, context)
                            val icon = iconMemoryCache[pkgName]

                            AppItem(
                                appName = appName,
                                packageName = pkgName,
                                versionName = pkgInfo.versionName ?: "1.0",
                                versionCode = vCode,
                                firstInstallTime = installTime,
                                lastUpdateTime = updateTime,
                                firstInstallTimeFormatted = firstInstallFormatted,
                                lastUpdateTimeFormatted = lastUpdateFormatted,
                                isSystemApp = isSystem,
                                isLaunchable = isLaunchable,
                                isEnabled = isEnabled,
                                enabledState = enabledState,
                                isSafeBlocklisted = isSafeBlocklisted,
                                icon = icon
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }

                    accumulatedList.addAll(chunkItems)
                    val currentSorted = accumulatedList.sortedWith(
                        compareBy({ it.isSafeBlocklisted }, { it.isSystemApp }, { it.appName.lowercase() })
                    )
                    _installedApps.value = currentSorted
                }

                val finalList = accumulatedList.sortedWith(
                    compareBy({ it.isSafeBlocklisted }, { it.isSystemApp }, { it.appName.lowercase() })
                )
                memoryCachedApps = finalList
                _installedApps.value = finalList

                // Load icons asynchronously in background without blocking main thread or first frame
                launch(Dispatchers.IO) {
                    finalList.chunked(30).forEach { batch ->
                        var hasNewIcons = false
                        batch.forEach { item ->
                            if (item.icon == null && !iconMemoryCache.containsKey(item.packageName)) {
                                try {
                                    val iconDrawable = pm.getApplicationIcon(item.packageName)
                                    iconMemoryCache[item.packageName] = iconDrawable
                                    hasNewIcons = true
                                } catch (_: Exception) {}
                            }
                        }
                        if (hasNewIcons) {
                            _installedApps.value = _installedApps.value.map { current ->
                                val cachedIcon = iconMemoryCache[current.packageName]
                                if (current.icon == null && cachedIcon != null) {
                                    current.copy(icon = cachedIcon)
                                } else {
                                    current
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _snackbarMessage.emit("Failed to load apps: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAppEnabled(app: AppItem) {
        val targetEnabled = !app.isEnabled
        val cap = _capability.value

        if (cap == AppManagerCapability.NONE) {
            viewModelScope.launch {
                _snackbarMessage.emit("Requires Shizuku or Device Owner to enable/disable.")
            }
            return
        }

        if (!targetEnabled && app.isSafeBlocklisted) {
            viewModelScope.launch {
                _snackbarMessage.emit("Cannot disable system-critical app: ${app.appName}")
            }
            return
        }

        // Optimistic UI update
        val previousApps = _installedApps.value
        _installedApps.value = previousApps.map {
            if (it.packageName == app.packageName) it.copy(isEnabled = targetEnabled) else it
        }

        viewModelScope.launch(Dispatchers.IO) {
            val result = if (targetEnabled) {
                AppActions.enableApp(app.packageName, cap, context)
            } else {
                AppActions.disableApp(app.packageName, cap, context)
            }

            result.fold(
                onSuccess = {
                    val statusText = if (targetEnabled) "Enabled" else "Disabled"
                    _snackbarMessage.emit("$statusText ${app.appName}")
                    loadApps()
                },
                onFailure = { error ->
                    // Rollback
                    _installedApps.value = previousApps
                    _snackbarMessage.emit("Failed to toggle ${app.appName}: ${error.message}")
                }
            )
        }
    }

    fun uninstallApp(app: AppItem) {
        if (app.isSafeBlocklisted && app.isSystemApp) {
            viewModelScope.launch {
                _snackbarMessage.emit("Cannot uninstall system/critical package: ${app.appName}")
            }
            return
        }

        val result = AppActions.uninstallApp(app.packageName, _capability.value, context)
        result.fold(
            onSuccess = {
                viewModelScope.launch {
                    _snackbarMessage.emit("Uninstall requested for ${app.appName}")
                }
            },
            onFailure = { error ->
                viewModelScope.launch {
                    _snackbarMessage.emit("Uninstall failed: ${error.message}")
                }
            }
        )
    }

    fun revertAllDisabled() {
        val toEnable = _installedApps.value.filter { !it.isEnabled && !it.isSafeBlocklisted }
        if (toEnable.isEmpty()) {
            viewModelScope.launch {
                _snackbarMessage.emit("No disabled apps to enable.")
            }
            return
        }

        executeRevert(toEnable.map { it.packageName })
    }

    fun revertSelected(packageNames: List<String>) {
        if (packageNames.isEmpty()) {
            viewModelScope.launch {
                _snackbarMessage.emit("No apps selected.")
            }
            return
        }

        executeRevert(packageNames)
    }

    private fun executeRevert(packages: List<String>) {
        if (_isReverting.value) return
        _isReverting.value = true
        _revertTotalCount.value = packages.size
        _revertProgressCount.value = 0
        _lastRevertResult.value = null

        viewModelScope.launch(Dispatchers.IO) {
            var successCount = 0
            var failureCount = 0
            val cap = _capability.value

            packages.forEachIndexed { index, pkg ->
                val res = AppActions.enableApp(pkg, cap, context)
                if (res.isSuccess) {
                    successCount++
                } else {
                    failureCount++
                }
                _revertProgressCount.value = index + 1
            }

            _lastRevertResult.value = Pair(successCount, failureCount)
            _isReverting.value = false
            loadApps()

            val msg = if (failureCount == 0) {
                "Enabled $successCount apps successfully"
            } else {
                "Enabled $successCount apps ($failureCount failed)"
            }
            _snackbarMessage.emit(msg)
        }
    }
}
