sed -i '92,106c\
            TopAppBar(\
                title = { Text("Username Generator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },\
                navigationIcon = {\
                    IconButton(onClick = { navController.popBackStack() }) {\
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")\
                    }\
                },\
                actions = {\
                    IconButton(onClick = { showFontSelector = true }) {\
                        Icon(Icons.Default.FontDownload, "Font Style")\
                    }\
                },\
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)\
            )\
' app/src/main/java/com/example/ui/screens/UsernameGenScreen.kt
