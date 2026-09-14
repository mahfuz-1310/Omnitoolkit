import sys

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "r") as f:
    content = f.read()

start_marker = "            if (!showAdvanced) {\n                // Simple Mode"
end_marker = "            } else {\n                // Advanced Options"

new_ui = """            if (!showAdvanced) {
                // Simple Mode
                Text(
                    text = "Simple Mode (password only)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = simpleModeState.password,
                    onValueChange = onUpdateSimplePassword,
                    label = { Text("Admin Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (simpleModeState.manualUsernameRequired) {
                    OutlinedTextField(
                        value = simpleModeState.manualUsername,
                        onValueChange = onUpdateSimpleManualUsername,
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = simpleModeState.remember,
                        onCheckedChange = onUpdateSimpleRemember
                    )
                    Text("Remember Password")
                }
                Spacer(modifier = Modifier.height(8.dp))

                val dispIp = simpleModeState.managementIp ?: defaultGateway
                Text(
                    text = "Detected management IP: ${dispIp.ifEmpty { "Unknown" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (simpleModeState.detectedModel != null) {
                    Text(
                        text = "Detected Model: ${simpleModeState.detectedModel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (simpleModeState.status == SimpleModeStatus.ERROR) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = simpleModeState.failureReason ?: "Couldn't log in with password only. Try Advanced options.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    if (simpleModeState.openPorts.isNotEmpty()) {
                        Text(
                            text = "Open ports: ${simpleModeState.openPorts.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onTestSimple,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = simpleModeState.status != SimpleModeStatus.CONNECTING && simpleModeState.password.isNotEmpty()
                ) {
                    if (simpleModeState.status == SimpleModeStatus.CONNECTING) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connecting...")
                    } else {
                        Text("Test & Enable")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { 
                        showAdvanced = true 
                        if (simpleModeState.managementIp != null) ip = simpleModeState.managementIp
                        if (simpleModeState.password.isNotEmpty()) password = simpleModeState.password
                        if (simpleModeState.usernameTried != null) username = simpleModeState.usernameTried
                        else if (simpleModeState.manualUsername.isNotEmpty()) username = simpleModeState.manualUsername
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Advanced options")
                }
"""

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx == -1 or end_idx == -1:
    print("Markers not found")
    sys.exit(1)

new_content = content[:start_idx] + new_ui + content[end_idx:]
with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "w") as f:
    f.write(new_content)

print("Patched Simple Mode UI")
