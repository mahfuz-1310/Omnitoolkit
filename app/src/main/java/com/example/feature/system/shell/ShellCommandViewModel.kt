package com.example.feature.system.shell

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShellCommandViewModel(application: Application) : AndroidViewModel(application) {

    private val historyStore = ShellHistoryStore(application)

    private val _status = MutableStateFlow(ShellStatus.SHIZUKU_REQUIRED)
    val status: StateFlow<ShellStatus> = _status.asStateFlow()

    private val _commandText = MutableStateFlow("getprop ro.build.version.release")
    val commandText: StateFlow<String> = _commandText.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _lastResult = MutableStateFlow<ShellCommandResult?>(null)
    val lastResult: StateFlow<ShellCommandResult?> = _lastResult.asStateFlow()

    val historyList: StateFlow<List<ShellHistoryItem>> = historyStore.history

    private val _showDangerousConfirmation = MutableStateFlow(false)
    val showDangerousConfirmation: StateFlow<Boolean> = _showDangerousConfirmation.asStateFlow()

    private val _pendingDangerousCommand = MutableStateFlow<String?>(null)
    val pendingDangerousCommand: StateFlow<String?> = _pendingDangerousCommand.asStateFlow()

    private val _showHelpDialog = MutableStateFlow(false)
    val showHelpDialog: StateFlow<Boolean> = _showHelpDialog.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        val currentStatus = ShellExecutor.checkShizukuStatus(getApplication())
        _status.value = currentStatus
    }

    fun updateCommand(text: String) {
        _commandText.value = text
    }

    fun clearCommand() {
        _commandText.value = ""
    }

    fun selectPreset(preset: ShellPreset) {
        _commandText.value = preset.command
    }

    fun requestShizukuPermission() {
        ShellExecutor.requestPermission()
        refreshStatus()
    }

    fun runCurrentCommand(forceConfirmed: Boolean = false) {
        val cmd = _commandText.value.trim()
        if (cmd.isEmpty() || _isRunning.value) return

        refreshStatus()
        if (_status.value != ShellStatus.READY) {
            _showHelpDialog.value = true
            return
        }

        if (!forceConfirmed && ShellExecutor.isPotentiallyDangerous(cmd)) {
            _pendingDangerousCommand.value = cmd
            _showDangerousConfirmation.value = true
            return
        }

        _isRunning.value = true
        viewModelScope.launch {
            try {
                val result = ShellExecutor.executeCommand(cmd)
                _lastResult.value = result

                historyStore.addEntry(
                    ShellHistoryItem(
                        command = result.command,
                        timestamp = result.timestamp,
                        exitCode = result.exitCode,
                        executionTimeMs = result.executionTimeMs
                    )
                )
            } finally {
                _isRunning.value = false
            }
        }
    }

    fun confirmDangerousExecution() {
        val cmd = _pendingDangerousCommand.value ?: return
        _showDangerousConfirmation.value = false
        _pendingDangerousCommand.value = null
        runCurrentCommand(forceConfirmed = true)
    }

    fun dismissDangerousConfirmation() {
        _showDangerousConfirmation.value = false
        _pendingDangerousCommand.value = null
    }

    fun showHelp() {
        _showHelpDialog.value = true
    }

    fun dismissHelp() {
        _showHelpDialog.value = false
    }

    fun deleteHistoryItem(id: String) {
        historyStore.deleteEntry(id)
    }

    fun clearAllHistory() {
        historyStore.clearHistory()
    }

    fun copyOutputToClipboard(context: Context): Boolean {
        val res = _lastResult.value ?: return false
        val content = buildString {
            append("Command: ").append(res.command).append("\n")
            append("Exit Code: ").append(res.exitCode).append("\n")
            append("Execution Time: ").append(res.executionTimeMs).append(" ms\n")
            if (res.stdout.isNotEmpty()) {
                append("\n[STDOUT]\n").append(res.stdout).append("\n")
            }
            if (res.stderr.isNotEmpty()) {
                append("\n[STDERR]\n").append(res.stderr).append("\n")
            }
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Shell Output", content)
        clipboard?.setPrimaryClip(clip)
        return true
    }
}
