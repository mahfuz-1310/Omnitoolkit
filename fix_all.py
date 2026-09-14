import re

with open('overlay.txt', 'r') as f:
    content = f.read()

# Fix 1: Compact View
# Find the Column in the compact view:
old_col_start = """                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {"""
new_col_start = """                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    when (activeTool) {
                        "Calculator" -> {
                            com.example.ui.screens.CalculatorView()
                        }
                        "Fonts" -> {
                            com.example.ui.components.FontStyleGeneratorView()
                        }
                        else -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {"""

# Replace old_col_start. Since this only occurs once in the compact view context:
content = content.replace(old_col_start, new_col_start)

# We need to add the closing braces `} } }` for `when`, `else` and `Column` at the end of the compact view
# The compact view ends before `// Expanded Studio Window`
# Let's find the `} else {` block that transitions to the expanded view
transition = """                }
            }
        }
    } else {
        // Expanded Studio Window"""

new_transition = """                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Expanded Studio Window"""
content = content.replace(transition, new_transition)
print("Compact View Fix applied.")


# Fix 2: Expanded View
# Find the Result Box in the expanded view:
# It starts around line 660, right before '// Controls based on activeTool'
controls_idx = content.find('// Controls based on activeTool')
if controls_idx != -1:
    surface_idx = content.rfind('Surface(\n                        modifier = Modifier.fillMaxWidth(),\n                        shape = RoundedCornerShape(16.dp)', 0, controls_idx)
    
    if surface_idx != -1:
        # Find where it ends
        end_idx = content.rfind('                    }\n                }', surface_idx, controls_idx)
        # End idx will point to the `                    }`
        # Let's add the closing brace properly
        surface_block = content[surface_idx:end_idx]
        indented_surface = "\n".join(["    " + line if line.strip() else line for line in surface_block.split("\n")])
        wrapped_surface = f'if (activeTool !in listOf("Calculator", "Fonts", "Saver")) {{\n{indented_surface}                    }}'
        
        content = content[:surface_idx] + wrapped_surface + content[end_idx:]
        print("Expanded View Fix applied.")
    else:
        print("Could not find Result Box in Expanded View.")
else:
    print("Could not find Controls idx")

with open('app/src/main/java/com/example/ui/components/SystemOverlayContent.kt', 'w') as f:
    f.write(content)
