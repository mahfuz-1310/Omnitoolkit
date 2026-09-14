import sys

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "r") as f:
    content = f.read()

start_marker = "            if (!showAdvanced) {\n                // Simple Mode"
end_marker = "            } else {\n                // Advanced Options"

new_ui = """            if (!showAdvanced) {
                // Simple Mode
                // Diagnostics Button
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onOpenDiagnostics) {
                        Text("Remote Diagnostics")
                    }
                }

                if (simpleModeState.status == SimpleModeStatus.IDLE) {
                    Button(
                        onClick = onStartAutoDetect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Auto Detect Router (Recommended)")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Find management IP, detect model, and enable control.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (simpleModeState.status == SimpleModeStatus.DETECTING) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Detecting...")
                    }
                } else {
                    Text(
                        text = "One-Tap Auto Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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

                    Spacer(modifier = Modifier.height(16.dp))

                    if (simpleModeState.status != SimpleModeStatus.ERROR) {
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
                    }

                    if (simpleModeState.status == SimpleModeStatus.ERROR) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = simpleModeState.failureReason ?: "Detection failed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (simpleModeState.status != SimpleModeStatus.ERROR) {
                        Button(
                            onClick = onTestSimple,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = simpleModeState.status != SimpleModeStatus.CONNECTING && simpleModeState.password.isNotEmpty()
                        ) {
                            if (simpleModeState.status == SimpleModeStatus.CONNECTING) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting...")
                            } else {
                                Text("Connect")
                            }
                        }
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
                    Text("Try Advanced options")
                }
"""

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + new_ui + content[end_idx:]
    with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "w") as f:
        f.write(content)
    print("Patched sheet UI")
else:
    print("Markers not found")
