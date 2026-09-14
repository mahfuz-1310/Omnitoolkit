import sys

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "r") as f:
    lines = f.readlines()

new_lines = []
decl_line = ""
for line in lines:
    if "val simpleModeState by viewModel.simpleModeState.collectAsStateWithLifecycle()" in line:
        decl_line = line
        break

if not decl_line:
    sys.exit("Decl not found")

lines.remove(decl_line)
for i, line in enumerate(lines):
    if "val routerConfig by viewModel.routerConfig.collectAsStateWithLifecycle()" in line:
        lines.insert(i + 1, decl_line)
        break

with open("app/src/main/java/com/example/feature/system/wifi/WifiControllerCard.kt", "w") as f:
    f.writelines(lines)

print("Fixed scope")
