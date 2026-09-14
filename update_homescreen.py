import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

# Find the start of the All-In-One scanner
start_str = "// ==================== ALL-IN-ONE SYSTEM & HARDWARE SCANNER ===================="
start_idx = content.find(start_str)

# Find the end of the All-In-One scanner (right before FEATURED BANNER)
end_str = "// ==================== FEATURED BANNER ===================="
end_idx = content.find(end_str)

if start_idx != -1 and end_idx != -1:
    new_scanner_card = """// ==================== SECURITY SCAN ====================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        SoundEffectManager.playClick()
                        navController.navigate(Screen.SecurityScan.route)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Security Scan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tap to run a deep security scan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        """
    
    updated_content = content[:start_idx] + new_scanner_card + content[end_idx:]
    with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
        f.write(updated_content)
    print("Updated HomeScreen.kt successfully")
else:
    print(f"Could not find indices. Start: {start_idx}, End: {end_idx}")
