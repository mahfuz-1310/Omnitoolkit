import sys

with open("app/src/main/java/com/example/feature/system/wifi/RouterControl/PasswordOnlyConnector.kt", "r") as f:
    content = f.read()

sig = "    suspend fun connectWithPasswordOnly("
new_func = """
    suspend fun detectRouterEnvironment(defaultGateway: String): Pair<String, Pair<RouterModel, String>> = withContext(Dispatchers.IO) {
        val (managementIp, openPorts) = discoverManagementIp(defaultGateway)
        val detectedModel = detectModel(managementIp, openPorts)
        Pair(managementIp, detectedModel)
    }

"""

if "detectRouterEnvironment" not in content:
    content = content.replace(sig, new_func + sig)
    with open("app/src/main/java/com/example/feature/system/wifi/RouterControl/PasswordOnlyConnector.kt", "w") as f:
        f.write(content)
    print("Patched PasswordOnlyConnector")
else:
    print("Already patched PasswordOnlyConnector")
