sed -i -e '/val hasCustomInput/i\    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()\
    var showFontSelector by remember { mutableStateOf(false) }\
' \
       -e '/generatedResults = results/c\        val styledResults = results.map { it.copy(name = com.example.utils.FontStyles.apply(it.name, fontStyle)) }\
        generatedResults = styledResults\
        if (styledResults.isNotEmpty()) {\
            viewModel.addHistory("NAME", styledResults.first().name)\
        }' \
       -e '/if (results.isNotEmpty()) {/,/}/d' \
       -e '/IconButton(onClick = { navController.popBackStack() }) {/a\
                    }\
                },\
                actions = {\
                    IconButton(onClick = { showFontSelector = true }) {\
                        Icon(Icons.Default.FontDownload, "Font Style")\
                    }' app/src/main/java/com/example/ui/screens/NameGenScreen.kt

