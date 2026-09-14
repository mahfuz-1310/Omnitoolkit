sed -i -e '/text = heroResult,/c\                                text = com.example.utils.FontStyles.apply(heroResult, fontStyle),' \
       -e '/ClipData.newPlainText("Username", heroResult))/c\                                        clipboard.setPrimaryClip(ClipData.newPlainText("Username", com.example.utils.FontStyles.apply(heroResult, fontStyle)))' \
       -e '/val isFav by viewModel.isFavorite("USERNAME", heroResult)/c\                                val isFav by viewModel.isFavorite("USERNAME", com.example.utils.FontStyles.apply(heroResult, fontStyle)).collectAsStateWithLifecycle(initialValue = false)' \
       -e '/if(isFav) viewModel.removeFavorite("USERNAME", heroResult)/c\                                        if(isFav) viewModel.removeFavorite("USERNAME", com.example.utils.FontStyles.apply(heroResult, fontStyle))' \
       -e '/else viewModel.addFavorite("USERNAME", heroResult)/c\                                        else viewModel.addFavorite("USERNAME", com.example.utils.FontStyles.apply(heroResult, fontStyle))' \
       -e '/name = result,/c\                        name = com.example.utils.FontStyles.apply(result, fontStyle),' app/src/main/java/com/example/ui/screens/UsernameGenScreen.kt

cat << 'SHEET' >> app/src/main/java/com/example/ui/screens/UsernameGenScreen.kt
    }
    
    FontStyleBottomSheet(
        visible = showFontSelector,
        onDismissRequest = { showFontSelector = false },
        currentStyle = fontStyle,
        onStyleSelected = { viewModel.setFontStyle(it) }
    )
}
SHEET
