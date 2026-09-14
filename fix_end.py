with open('app/src/main/java/com/example/ui/components/SystemOverlayContent.kt', 'r') as f:
    content = f.read()

# Let's find the Text("Regen"...) part in the compact view
idx = content.find('Text("Regen", fontSize = 9.sp, fontWeight = FontWeight.Bold)')
if idx != -1:
    end_idx = content.find('    } else {', idx)
    
    if end_idx != -1:
        # replace everything from idx to end_idx + 12
        new_end = """Text("Regen", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                }
                }
                }
            }
        }
    } else {"""
        
        content = content[:idx] + new_end + content[end_idx + 12:]
        print("Fixed!")

with open('app/src/main/java/com/example/ui/components/SystemOverlayContent.kt', 'w') as f:
    f.write(content)
