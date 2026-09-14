import sys

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "r") as f:
    content = f.read()

# 1. Update the status text in the header
status_text_old = """                            text = when(routerConfig.status) {
                                ConnectionStatus.NOT_CONFIGURED -> "Not configured"
                                ConnectionStatus.CONNECTED -> "Connected"
                                ConnectionStatus.ERROR -> "Error"
                            }"""

status_text_new = """                            text = when(routerConfig.status) {
                                ConnectionStatus.NOT_CONFIGURED -> if (simpleModeState.status == SimpleModeStatus.DETECTING) "Detecting..." else "Not configured"
                                ConnectionStatus.CONNECTED -> "Connected"
                                ConnectionStatus.ERROR -> "Error"
                            }"""
content = content.replace(status_text_old, status_text_new)

# 2. Add the banner below the header
header_end = """                        )
                    }
                }
            }"""

banner_new = """                        )
                    }
                }
            }
            
            if (routerConfig.status == ConnectionStatus.NOT_CONFIGURED) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto Detect Router available", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Set up in one tap.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Button(
                            onClick = { 
                                isRouterSheetOpen = true 
                                viewModel.startAutoDetect()
                            }
                        ) {
                            Text("One-Tap Auto Setup")
                        }
                    }
                }
            } else if (routerConfig.status == ConnectionStatus.CONNECTED) {
                Text(
                    text = "Connected to ${routerConfig.model} • One-Tap setup complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }"""
content = content.replace(header_end, banner_new)

# 3. Add onStartAutoDetect to RouterControllerSheet signature
sig_old = "    onOpenDiagnostics: () -> Unit\n) {"
sig_new = "    onOpenDiagnostics: () -> Unit,\n    onStartAutoDetect: () -> Unit\n) {"
content = content.replace(sig_old, sig_new)

call_old = "            onOpenDiagnostics = { showDiagnostics = true }\n        )"
call_new = "            onOpenDiagnostics = { showDiagnostics = true },\n            onStartAutoDetect = { viewModel.startAutoDetect() }\n        )"
content = content.replace(call_old, call_new)

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "w") as f:
    f.write(content)

print("Patched basic UI elements in WifiControllerCard")
