sed -i '143c\
        FontStyleBottomSheet(\
            visible = showFontSelector,\
            onDismissRequest = { showFontSelector = false },\
            currentStyle = fontStyle,\
            onStyleSelected = { viewModel.setFontStyle(it) }\
        )\
    }\
' app/src/main/java/com/example/ui/screens/NicknameGenScreen.kt
