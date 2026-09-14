sed -i -e '/var selectedCategory/i\    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()\
    var showFontSelector by remember { mutableStateOf(false) }\
' \
       -e '/generatedNicknames = List/c\            val results = List(generateCount.toInt()) { NicknameGenerator.generateNickname(selectedCategory) }\
            generatedNicknames = results\
            if(results.isNotEmpty()) viewModel.addHistory("NICKNAME", com.example.utils.FontStyles.apply(results.first(), fontStyle))' \
       -e '/TopAppBar(/,/)/c\            TopAppBar(\
                title = { Text("Nickname Generator", style = MaterialTheme.typography.titleMedium) },\
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
            )' \
       -e '/text = name/c\                                text = com.example.utils.FontStyles.apply(name, fontStyle)' \
       -e '/ClipData.newPlainText("Nickname", name))/c\                                            clipboard.setPrimaryClip(ClipData.newPlainText("Nickname", com.example.utils.FontStyles.apply(name, fontStyle)))' \
       -e '/val isFav by viewModel.isFavorite("NICKNAME", name)/c\                                    val isFav by viewModel.isFavorite("NICKNAME", com.example.utils.FontStyles.apply(name, fontStyle)).collectAsStateWithLifecycle(initialValue = false)' \
       -e '/if (isFav) viewModel.removeFavorite("NICKNAME", name)/c\                                            if (isFav) viewModel.removeFavorite("NICKNAME", com.example.utils.FontStyles.apply(name, fontStyle))' \
       -e '/else viewModel.addFavorite("NICKNAME", name)/c\                                            else viewModel.addFavorite("NICKNAME", com.example.utils.FontStyles.apply(name, fontStyle))' app/src/main/java/com/example/ui/screens/NicknameGenScreen.kt

cat << 'SHEET' >> app/src/main/java/com/example/ui/screens/NicknameGenScreen.kt
    FontStyleBottomSheet(
        visible = showFontSelector,
        onDismissRequest = { showFontSelector = false },
        currentStyle = fontStyle,
        onStyleSelected = { viewModel.setFontStyle(it) }
    )
}
SHEET
