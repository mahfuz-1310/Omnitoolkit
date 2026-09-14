sed -i '$d' app/src/main/java/com/example/ui/screens/NameGenScreen.kt
cat << 'SHEET' >> app/src/main/java/com/example/ui/screens/NameGenScreen.kt
    }
    
    FontStyleBottomSheet(
        visible = showFontSelector,
        onDismissRequest = { showFontSelector = false },
        currentStyle = fontStyle,
        onStyleSelected = { viewModel.setFontStyle(it) }
    )
}
SHEET
