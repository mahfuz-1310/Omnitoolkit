sed -i -e '/var country by/i\    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()\
    var showFontSelector by remember { mutableStateOf(false) }\
' \
       -e '/generatedUsernames = results/c\        generatedUsernames = results\
        if (results.isNotEmpty()) {\
            viewModel.addHistory("USERNAME", com.example.utils.FontStyles.apply(results.first(), fontStyle))\
        }' \
       -e '/if (results.isNotEmpty()) {/,/}/d' \
       -e '/IconButton(onClick = { navController.popBackStack() }) {/a\
                    }\
                },\
                actions = {\
                    IconButton(onClick = { showFontSelector = true }) {\
                        Icon(Icons.Default.FontDownload, "Font Style")\
                    }' app/src/main/java/com/example/ui/screens/UsernameGenScreen.kt

