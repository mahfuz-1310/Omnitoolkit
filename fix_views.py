import re

with open('app/src/main/java/com/example/ui/components/SystemOverlayContent.kt', 'r') as f:
    content = f.read()

controls_idx = content.find('// Controls based on activeTool')
if controls_idx != -1:
    surface_idx = content.rfind('Surface(\n                        modifier = Modifier.fillMaxWidth(),\n                        shape = RoundedCornerShape(16.dp)', 0, controls_idx)
    
    if surface_idx != -1:
        # We need to find where this Surface ends. It ends exactly before '// Controls based on activeTool'
        # Well, there are a few spaces and newlines.
        end_idx = content.rfind('                    }\n                }\n\n                // Controls based on activeTool', surface_idx, controls_idx + 100)
        if end_idx == -1:
            end_idx = content.rfind('                    }\n                }', surface_idx, controls_idx)
            
        print("Surface idx:", surface_idx)
        print("End idx:", end_idx)
        
        surface_block = content[surface_idx:end_idx]
        
        indented_surface = "\n".join(["    " + line if line.strip() else line for line in surface_block.split("\n")])
        wrapped_surface = f'if (activeTool !in listOf("Calculator", "Fonts", "Saver")) {{\n{indented_surface}                    }}'
        
        content = content[:surface_idx] + wrapped_surface + content[end_idx:]
        print("Fixed expanded view Result Box")

with open('app/src/main/java/com/example/ui/components/SystemOverlayContent.kt', 'w') as f:
    f.write(content)

