with open('app/src/main/java/com/example/ui/components/SystemOverlayContent.kt', 'r') as f:
    content = f.read()

import re

# find the block inside the else of the compact view
search_str = '                        else -> {\n                            Column(\n                                modifier = Modifier\n                                    .fillMaxWidth()\n                                    .verticalScroll(rememberScrollState())\n                            ) {'
replace_str = '                        else -> {\n                            if (activeTool !in listOf("Saver", "Calculator", "Fonts")) {\n                                Column(\n                                    modifier = Modifier\n                                        .fillMaxWidth()\n                                        .verticalScroll(rememberScrollState())\n                                ) {'

if search_str in content:
    content = content.replace(search_str, replace_str)
    
    # Need to add a closing brace for the if
    # Let's just find the end of that Column.
    # It ends with:
    #                     }
    #                 }
    #             }
    #         }
    #     } else {
    
    # We'll just do a regex replace
    content = re.sub(r'(                            }\n                        }\n                    }\n                }\n            }\n        }\n    } else {\n        // Expanded Studio Window)', r'                                }\n\0', content)
    print("Wrapped else block in compact view")

with open('app/src/main/java/com/example/ui/components/SystemOverlayContent.kt', 'w') as f:
    f.write(content)

