import sys

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerViewModel.kt", "r") as f:
    content = f.read()

marker = "    override fun onCleared() {"

new_methods = """
    fun runDiagnostics(mode: DiagnosticsMode) {
        diagJob?.cancel()
        _diagnosticsState.value = DiagnosticsState(isRunning = true)
        
        diagJob = viewModelScope.launch {
            val existingPassword = repo.getPassword() ?: if (!_routerConfig.value.hasPasswordSaved) _simpleModeState.value.password else null
            val existingUsername = _routerConfig.value.username.ifEmpty { null }
            
            diagnosticsRunner.runDiagnostics(mode, existingPassword, existingUsername).collect { state ->
                _diagnosticsState.value = state
            }
        }
    }

    fun cancelDiagnostics() {
        diagJob?.cancel()
        val current = _diagnosticsState.value
        if (current.isRunning) {
            _diagnosticsState.value = current.copy(isRunning = false, error = "Cancelled by user")
        }
    }
    
    fun resetDiagnostics() {
        cancelDiagnostics()
        _diagnosticsState.value = DiagnosticsState()
    }

"""

if marker in content:
    content = content.replace(marker, new_methods + marker)
    with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerViewModel.kt", "w") as f:
        f.write(content)
    print("Patched VM")
else:
    print("Marker not found")
