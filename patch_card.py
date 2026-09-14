import sys

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "r") as f:
    content = f.read()

import_marker = "import androidx.compose.ui.text.input.PasswordVisualTransformation"
new_imports = "\nimport com.example.feature.system.wifi.diagnostics.RemoteDiagnosticsSheet"

if new_imports not in content:
    content = content.replace(import_marker, import_marker + new_imports)


state_marker = "    val simpleModeState by viewModel.simpleModeState.collectAsStateWithLifecycle()"
new_states = """
    val diagnosticsState by viewModel.diagnosticsState.collectAsStateWithLifecycle()
    var showDiagnostics by remember { mutableStateOf(false) }
"""

if "showDiagnostics" not in content:
    content = content.replace(state_marker, state_marker + new_states)


sheet_marker = "    if (isRouterSheetOpen) {"
new_sheet = """
    if (showDiagnostics) {
        RemoteDiagnosticsSheet(
            state = diagnosticsState,
            onRun = { mode -> viewModel.runDiagnostics(mode) },
            onCancel = { viewModel.cancelDiagnostics() },
            onDismiss = { 
                viewModel.resetDiagnostics()
                showDiagnostics = false 
            }
        )
    }

"""

if "RemoteDiagnosticsSheet(" not in content:
    content = content.replace(sheet_marker, new_sheet + sheet_marker)

button_marker = "                // Simple Mode"
new_button = """                // Diagnostics Button
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showDiagnostics = true }) {
                        Text("Remote Diagnostics")
                    }
                }
"""
if "Remote Diagnostics" not in content:
    content = content.replace(button_marker, button_marker + "\n" + new_button)

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "w") as f:
    f.write(content)

print("Patched WifiControllerCard")
