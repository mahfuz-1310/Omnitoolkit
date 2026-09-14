import sys

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerViewModel.kt", "r") as f:
    content = f.read()

# Replace SimpleModeStatus
status_old = "enum class SimpleModeStatus {\n    IDLE, CONNECTING, CONNECTED, ERROR\n}"
status_new = "enum class SimpleModeStatus {\n    IDLE, DETECTING, NEED_PASSWORD, CONNECTING, CONNECTED, ERROR\n}"

if status_old in content:
    content = content.replace(status_old, status_new)

# Add startAutoDetect
sig = "    fun testAndEnableWithPasswordOnly() {"
new_func = """
    fun startAutoDetect() {
        val gateway = networkInfo.value.gateway
        if (gateway.isEmpty()) {
            _simpleModeState.value = _simpleModeState.value.copy(
                status = SimpleModeStatus.ERROR,
                failureReason = "No gateway available"
            )
            return
        }
        
        _simpleModeState.value = _simpleModeState.value.copy(status = SimpleModeStatus.DETECTING)
        viewModelScope.launch {
            try {
                val (ip, modelInfo) = passwordOnlyConnector.detectRouterEnvironment(gateway)
                _simpleModeState.value = _simpleModeState.value.copy(
                    managementIp = ip,
                    detectedModel = modelInfo.second,
                    status = SimpleModeStatus.NEED_PASSWORD
                )
            } catch (e: Exception) {
                _simpleModeState.value = _simpleModeState.value.copy(
                    status = SimpleModeStatus.ERROR,
                    failureReason = e.message ?: "Detection failed"
                )
            }
        }
    }

"""

if "fun startAutoDetect()" not in content:
    content = content.replace(sig, new_func + sig)

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerViewModel.kt", "w") as f:
    f.write(content)

print("Patched VM")
