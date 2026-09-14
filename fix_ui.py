import sys

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "r") as f:
    content = f.read()

sig1 = "    onTestSimple: () -> Unit"
sig2 = "    onTestSimple: () -> Unit,\n    onOpenDiagnostics: () -> Unit"

if sig2 not in content:
    content = content.replace(sig1, sig2)

call1 = "            onTestSimple = { viewModel.testAndEnableWithPasswordOnly() }"
call2 = "            onTestSimple = { viewModel.testAndEnableWithPasswordOnly() },\n            onOpenDiagnostics = { showDiagnostics = true }"

if call2 not in content:
    content = content.replace(call1, call2)

button1 = "onClick = { showDiagnostics = true }"
button2 = "onClick = onOpenDiagnostics"

if button2 not in content:
    content = content.replace(button1, button2)

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "w") as f:
    f.write(content)

print("Fixed UI")
