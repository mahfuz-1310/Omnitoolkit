sed -i '112,135c\
        topBar = {\
            TopAppBar(\
                title = { \
                    Column {\
                        Text("Name Generator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))\
                        Text("Create your perfect name", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)\
                    }\
                },\
                navigationIcon = {\
                    IconButton(onClick = { navController.popBackStack() }) {\
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")\
                    }\
                },\
                actions = {\
                    IconButton(onClick = { showFontSelector = true }) {\
                        Icon(Icons.Default.FontDownload, "Font Style")\
                    }\
                }\
            )\
        },' app/src/main/java/com/example/ui/screens/NameGenScreen.kt
