sed -i -e '/val animationsEnabled: StateFlow<Boolean> = settingsRepository.animationsFlow.stateIn(/i\
    val fontStyle: StateFlow<String> = settingsRepository.fontStyleFlow.stateIn(\
        scope = viewModelScope,\
        started = SharingStarted.WhileSubscribed(5000),\
        initialValue = "Default"\
    )\
' \
       -e '/fun setHistorySearchQuery(query: String) {/i\
    fun setFontStyle(style: String) {\
        viewModelScope.launch {\
            settingsRepository.setFontStyle(style)\
        }\
    }\
' app/src/main/java/com/example/ui/MainViewModel.kt
