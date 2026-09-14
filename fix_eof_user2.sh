sed -i '$d' app/src/main/java/com/example/ui/screens/UsernameGenScreen.kt
cat << 'SHEET' >> app/src/main/java/com/example/ui/screens/UsernameGenScreen.kt
    FontStyleBottomSheet(
        visible = showFontSelector,
        onDismissRequest = { showFontSelector = false },
        currentStyle = fontStyle,
        onStyleSelected = { viewModel.setFontStyle(it) }
    )
}
SHEET
