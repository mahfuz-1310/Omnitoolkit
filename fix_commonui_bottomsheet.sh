sed -i '150,$d' app/src/main/java/com/example/ui/screens/CommonUI.kt
cat << 'BOTTOM' >> app/src/main/java/com/example/ui/screens/CommonUI.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontStyleBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    currentStyle: String,
    onStyleSelected: (String) -> Unit
) {
    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Font Style",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(com.example.utils.FontStyles.styles) { style ->
                        val isSelected = style == currentStyle
                        Surface(
                            onClick = { 
                                onStyleSelected(style)
                                onDismissRequest() 
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        style, 
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        com.example.utils.FontStyles.apply("Zayen Ramirez", style),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        androidx.compose.material.icons.Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}
BOTTOM
