sed -i -e '/val styledResults = results.map/d' \
       -e '/generatedResults = styledResults/c\        generatedResults = results' \
       -e '/if (styledResults.isNotEmpty()) {/c\        if (results.isNotEmpty()) {' \
       -e '/viewModel.addHistory("NAME", styledResults.first().name)/c\            viewModel.addHistory("NAME", com.example.utils.FontStyles.apply(results.first().name, fontStyle))' app/src/main/java/com/example/ui/screens/NameGenScreen.kt
