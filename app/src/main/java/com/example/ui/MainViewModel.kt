package com.example.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.SettingsRepository
import com.example.data.db.FavoriteItem
import com.example.data.db.HistoryItem
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.HistoryRepository
import com.example.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainViewModel(
    private val historyRepository: HistoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // === Shared Generator State ===
    private val _activeTool = MutableStateFlow("Name")
    val activeTool: StateFlow<String> = _activeTool.asStateFlow()

    private val _selectedMode = MutableStateFlow("Full Name")
    val selectedMode: StateFlow<String> = _selectedMode.asStateFlow()

    private val _selectedGender = MutableStateFlow("Male")
    val selectedGender: StateFlow<String> = _selectedGender.asStateFlow()

    private val _selectedCountry = MutableStateFlow("International")
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()

    private val _selectedStyle = MutableStateFlow("Modern")
    val selectedStyle: StateFlow<String> = _selectedStyle.asStateFlow()

    private val _selectedCount = MutableStateFlow(25)
    val selectedCount: StateFlow<Int> = _selectedCount.asStateFlow()

    private val _selectedNameLength = MutableStateFlow("Medium")
    val selectedNameLength: StateFlow<String> = _selectedNameLength.asStateFlow()

    private val _includeMiddle = MutableStateFlow(true)
    val includeMiddle: StateFlow<Boolean> = _includeMiddle.asStateFlow()

    private val _noRepeat = MutableStateFlow(true)
    val noRepeat: StateFlow<Boolean> = _noRepeat.asStateFlow()

    private val _customFirst = MutableStateFlow("")
    val customFirst: StateFlow<String> = _customFirst.asStateFlow()

    private val _customMiddle = MutableStateFlow("")
    val customMiddle: StateFlow<String> = _customMiddle.asStateFlow()

    private val _customLast = MutableStateFlow("")
    val customLast: StateFlow<String> = _customLast.asStateFlow()

    private val _lastNameCategory = MutableStateFlow("Random")
    val lastNameCategory: StateFlow<String> = _lastNameCategory.asStateFlow()

    private val _initialsOnly = MutableStateFlow(false)
    val initialsOnly: StateFlow<Boolean> = _initialsOnly.asStateFlow()

    private val _excludeDuplicates = MutableStateFlow(true)
    val excludeDuplicates: StateFlow<Boolean> = _excludeDuplicates.asStateFlow()

    private val _useRareNames = MutableStateFlow(false)
    val useRareNames: StateFlow<Boolean> = _useRareNames.asStateFlow()

    private val _includeMeaning = MutableStateFlow(false)
    val includeMeaning: StateFlow<Boolean> = _includeMeaning.asStateFlow()

    private val _generatedNames = MutableStateFlow<List<NameResult>>(emptyList())
    val generatedNames: StateFlow<List<NameResult>> = _generatedNames.asStateFlow()

    private val _currentResultText = MutableStateFlow("Zayen Arlo Ramirez")
    val currentResultText: StateFlow<String> = _currentResultText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isFloatingWindowFocusable = MutableStateFlow(true)
    val isFloatingWindowFocusable: StateFlow<Boolean> = _isFloatingWindowFocusable.asStateFlow()

    fun setFloatingWindowFocusable(focusable: Boolean) {
        _isFloatingWindowFocusable.value = focusable
    }

    private val _floatingToastMessage = MutableStateFlow<String?>(null)
    val floatingToastMessage: StateFlow<String?> = _floatingToastMessage.asStateFlow()

    fun showFloatingToast(message: String) {
        _floatingToastMessage.value = message
        viewModelScope.launch {
            kotlinx.coroutines.delay(1800)
            if (_floatingToastMessage.value == message) {
                _floatingToastMessage.value = null
            }
        }
    }

    // Tool specific options
    private val _usernameStyle = MutableStateFlow("Gaming")
    val usernameStyle: StateFlow<String> = _usernameStyle.asStateFlow()

    private val _usernameAddNum = MutableStateFlow(true)
    val usernameAddNum: StateFlow<Boolean> = _usernameAddNum.asStateFlow()

    private val _usernameAddUnder = MutableStateFlow(false)
    val usernameAddUnder: StateFlow<Boolean> = _usernameAddUnder.asStateFlow()

    private val _usernameAddDot = MutableStateFlow(false)
    val usernameAddDot: StateFlow<Boolean> = _usernameAddDot.asStateFlow()

    private val _usernameShort = MutableStateFlow(false)
    val usernameShort: StateFlow<Boolean> = _usernameShort.asStateFlow()

    private val _usernamePrefix = MutableStateFlow(false)
    val usernamePrefix: StateFlow<Boolean> = _usernamePrefix.asStateFlow()

    private val _usernameSuffix = MutableStateFlow(false)
    val usernameSuffix: StateFlow<Boolean> = _usernameSuffix.asStateFlow()

    private val _passwordLength = MutableStateFlow(16f)
    val passwordLength: StateFlow<Float> = _passwordLength.asStateFlow()

    private val _passwordUpper = MutableStateFlow(true)
    val passwordUpper: StateFlow<Boolean> = _passwordUpper.asStateFlow()

    private val _passwordLower = MutableStateFlow(true)
    val passwordLower: StateFlow<Boolean> = _passwordLower.asStateFlow()

    private val _passwordNums = MutableStateFlow(true)
    val passwordNums: StateFlow<Boolean> = _passwordNums.asStateFlow()

    private val _passwordSyms = MutableStateFlow(true)
    val passwordSyms: StateFlow<Boolean> = _passwordSyms.asStateFlow()

    private val _passwordExcludeSimilar = MutableStateFlow(false)
    val passwordExcludeSimilar: StateFlow<Boolean> = _passwordExcludeSimilar.asStateFlow()

    private val _passwordExcludeAmbiguous = MutableStateFlow(false)
    val passwordExcludeAmbiguous: StateFlow<Boolean> = _passwordExcludeAmbiguous.asStateFlow()

    private val _stylishTextInput = MutableStateFlow(com.example.utils.AppConstants.APP_NAME)
    val stylishTextInput: StateFlow<String> = _stylishTextInput.asStateFlow()

    private val _stylishFont = MutableStateFlow("Bubble")
    val stylishFont: StateFlow<String> = _stylishFont.asStateFlow()

    private val _nicknameCategory = MutableStateFlow("Cute")
    val nicknameCategory: StateFlow<String> = _nicknameCategory.asStateFlow()

    private val _nicknameCount = MutableStateFlow(10f)
    val nicknameCount: StateFlow<Float> = _nicknameCount.asStateFlow()

    private val _bioCategory = MutableStateFlow("Gamer")
    val bioCategory: StateFlow<String> = _bioCategory.asStateFlow()

    private val _bioLength = MutableStateFlow("Short")
    val bioLength: StateFlow<String> = _bioLength.asStateFlow()

    private val _bioEmoji = MutableStateFlow(true)
    val bioEmoji: StateFlow<Boolean> = _bioEmoji.asStateFlow()

    private val _builderBaseName = MutableStateFlow("")
    val builderBaseName: StateFlow<String> = _builderBaseName.asStateFlow()

    private val _builderPrefix = MutableStateFlow("")
    val builderPrefix: StateFlow<String> = _builderPrefix.asStateFlow()

    private val _builderSuffix = MutableStateFlow("")
    val builderSuffix: StateFlow<String> = _builderSuffix.asStateFlow()

    private val _builderAddNumbers = MutableStateFlow(false)
    val builderAddNumbers: StateFlow<Boolean> = _builderAddNumbers.asStateFlow()

    private val _builderAddUnderscore = MutableStateFlow(false)
    val builderAddUnderscore: StateFlow<Boolean> = _builderAddUnderscore.asStateFlow()

    private val _builderAddDot = MutableStateFlow(false)
    val builderAddDot: StateFlow<Boolean> = _builderAddDot.asStateFlow()

    private val _builderRandomLetters = MutableStateFlow(false)
    val builderRandomLetters: StateFlow<Boolean> = _builderRandomLetters.asStateFlow()

    private val _textSaverInput = MutableStateFlow("")
    val textSaverInput: StateFlow<String> = _textSaverInput.asStateFlow()

    private val _smartQuery = MutableStateFlow("popular modern name")
    val smartQuery: StateFlow<String> = _smartQuery.asStateFlow()

    private val _mixerName1 = MutableStateFlow("Alex")
    val mixerName1: StateFlow<String> = _mixerName1.asStateFlow()

    private val _mixerName2 = MutableStateFlow("Jordan")
    val mixerName2: StateFlow<String> = _mixerName2.asStateFlow()

    private val _mixerName3 = MutableStateFlow("")
    val mixerName3: StateFlow<String> = _mixerName3.asStateFlow()

    private val _mixerResults = MutableStateFlow<List<String>>(emptyList())
    val mixerResults: StateFlow<List<String>> = _mixerResults.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery

    private val _favoriteSearchQuery = MutableStateFlow("")
    val favoriteSearchQuery: StateFlow<String> = _favoriteSearchQuery

    val allHistory: StateFlow<List<HistoryItem>> = historyRepository.allHistory
        .combine(_historySearchQuery) { history, query ->
            if (query.isBlank()) history
            else history.filter { it.content.contains(query, ignoreCase = true) || it.type.contains(query, ignoreCase = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allFavorites: StateFlow<List<FavoriteItem>> = favoriteRepository.allFavorites
        .combine(_favoriteSearchQuery) { favorites, query ->
            if (query.isBlank()) favorites
            else favorites.filter { it.content.contains(query, ignoreCase = true) || it.type.contains(query, ignoreCase = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val smartSuggestions: StateFlow<List<String>> = historyRepository.allHistory
        .map { history ->
            val typeCounts = history.groupingBy { it.type }.eachCount()
            val sortedTypes = typeCounts.entries.sortedByDescending { it.value }.map { it.key }
            if (sortedTypes.isEmpty()) {
                listOf("NAME", "USERNAME", "PASSWORD") // Default suggestions
            } else {
                sortedTypes.take(3)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("NAME", "USERNAME", "PASSWORD")
        )

    val savedList: StateFlow<List<String>> = settingsRepository.savedListFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedText: StateFlow<String> = settingsRepository.savedTextFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val darkMode: StateFlow<Boolean?> = settingsRepository.darkModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    val whiteTheme: StateFlow<Boolean> = settingsRepository.whiteThemeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    val hapticFeedback: StateFlow<Boolean> = settingsRepository.hapticFeedbackFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val soundEffects: StateFlow<Boolean> = settingsRepository.soundEffectsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val appIcon: StateFlow<String> = settingsRepository.appIconFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Default Blue"
    )

    init {
        viewModelScope.launch {
            hapticFeedback.collect { enabled ->
                SoundEffectManager.isHapticEnabled = enabled
            }
        }
        viewModelScope.launch {
            soundEffects.collect { enabled ->
                SoundEffectManager.isSoundEnabled = enabled
            }
        }
    }
    
    val fontStyle: StateFlow<String> = settingsRepository.fontStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Default"
    )

    val animationsEnabled: StateFlow<Boolean> = settingsRepository.animationsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val floatingModeEnabled: StateFlow<Boolean> = settingsRepository.floatingModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val rememberFloatingPosition: StateFlow<Boolean> = settingsRepository.rememberFloatingPositionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val defaultFloatingSize: StateFlow<String> = settingsRepository.defaultFloatingSizeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Compact"
    )

    val floatingPosX: StateFlow<Float> = settingsRepository.floatingPosXFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 50f
    )

    val floatingPosY: StateFlow<Float> = settingsRepository.floatingPosYFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 150f
    )

    private val initialUiColor: Long = runBlocking {
        try {
            settingsRepository.uiColorFlow.first()
        } catch (e: Exception) {
            0xFF6C63FFL
        }
    }

    private val initialButtonColor: Long = runBlocking {
        try {
            settingsRepository.buttonColorFlow.first()
        } catch (e: Exception) {
            0xFF6C63FFL
        }
    }

    val uiColor: StateFlow<Long> = settingsRepository.uiColorFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialUiColor
    )

    val buttonColor: StateFlow<Long> = settingsRepository.buttonColorFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialButtonColor
    )
    
    fun setFontStyle(style: String) {
        viewModelScope.launch {
            settingsRepository.setFontStyle(style)
        }
    }

    fun setFloatingMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFloatingMode(enabled)
        }
    }

    fun setRememberFloatingPosition(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRememberFloatingPosition(enabled)
        }
    }

    fun setDefaultFloatingSize(size: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultFloatingSize(size)
        }
    }

    fun setFloatingPosition(x: Float, y: Float) {
        viewModelScope.launch {
            settingsRepository.setFloatingPosition(x, y)
        }
    }

    fun setUiColor(color: Long) {
        viewModelScope.launch {
            settingsRepository.setUiColor(color)
        }
    }

    fun setButtonColor(color: Long) {
        viewModelScope.launch {
            settingsRepository.setButtonColor(color)
        }
    }

    fun resetColors() {
        viewModelScope.launch {
            settingsRepository.resetColors()
        }
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }
    
    fun setFavoriteSearchQuery(query: String) {
        _favoriteSearchQuery.value = query
    }

    fun addHistory(type: String, content: String) {
        viewModelScope.launch {
            historyRepository.insert(type, content)
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            historyRepository.deleteById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }

    fun addFavorite(type: String, content: String) {
        viewModelScope.launch {
            favoriteRepository.insert(type, content)
        }
    }

    fun removeFavorite(type: String, content: String) {
        viewModelScope.launch {
            favoriteRepository.remove(type, content)
        }
    }

    fun deleteFavoriteItem(id: Int) {
        viewModelScope.launch {
            favoriteRepository.deleteById(id)
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            favoriteRepository.clearAll()
        }
    }

    fun isFavorite(type: String, content: String): StateFlow<Boolean> {
        return favoriteRepository.isFavorite(type, content).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    }

    fun setDarkMode(isDark: Boolean?) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(isDark)
        }
    }

    fun setWhiteTheme(isWhite: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWhiteTheme(isWhite)
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedback(enabled)
            SoundEffectManager.isHapticEnabled = enabled
        }
    }

    fun setSoundEffects(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEffects(enabled)
            SoundEffectManager.isSoundEnabled = enabled
        }
    }

    fun setAppIcon(iconName: String) {
        viewModelScope.launch {
            settingsRepository.setAppIcon(iconName)
        }
    }

    fun setAnimations(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAnimations(enabled)
        }
    }

    // === Generator State Setters ===
    fun setActiveTool(tool: String) {
        _activeTool.value = tool
    }

    fun setMode(mode: String) {
        _selectedMode.value = mode
    }

    fun setGender(gender: String) {
        _selectedGender.value = gender
    }

    fun setCountry(country: String) {
        _selectedCountry.value = country
    }

    fun setStyle(style: String) {
        _selectedStyle.value = style
    }

    fun setCount(count: Int) {
        _selectedCount.value = count
    }

    fun setNameLength(length: String) {
        _selectedNameLength.value = length
    }

    fun setIncludeMiddle(include: Boolean) {
        _includeMiddle.value = include
    }

    fun setNoRepeat(noRepeat: Boolean) {
        _noRepeat.value = noRepeat
    }

    fun setCustomFirst(name: String) {
        _customFirst.value = name
    }

    fun setCustomMiddle(name: String) {
        _customMiddle.value = name
    }

    fun setCustomLast(name: String) {
        _customLast.value = name
    }

    fun setLastNameCategory(cat: String) {
        _lastNameCategory.value = cat
    }

    fun setInitialsOnly(enabled: Boolean) {
        _initialsOnly.value = enabled
    }

    fun setExcludeDuplicates(enabled: Boolean) {
        _excludeDuplicates.value = enabled
    }

    fun setUseRareNames(enabled: Boolean) {
        _useRareNames.value = enabled
    }

    fun setIncludeMeaning(enabled: Boolean) {
        _includeMeaning.value = enabled
    }

    fun setUsernameStyle(style: String) {
        _usernameStyle.value = style
    }

    fun setUsernameAddNum(add: Boolean) {
        _usernameAddNum.value = add
    }

    fun setUsernameAddUnder(add: Boolean) {
        _usernameAddUnder.value = add
    }

    fun setUsernameAddDot(add: Boolean) {
        _usernameAddDot.value = add
    }

    fun setUsernameShort(short: Boolean) {
        _usernameShort.value = short
    }

    fun setUsernamePrefix(prefix: Boolean) {
        _usernamePrefix.value = prefix
    }

    fun setUsernameSuffix(suffix: Boolean) {
        _usernameSuffix.value = suffix
    }

    fun setPasswordLength(len: Float) {
        _passwordLength.value = len
    }

    fun setPasswordUpper(v: Boolean) {
        _passwordUpper.value = v
    }

    fun setPasswordLower(v: Boolean) {
        _passwordLower.value = v
    }

    fun setPasswordNums(v: Boolean) {
        _passwordNums.value = v
    }

    fun setPasswordSyms(v: Boolean) {
        _passwordSyms.value = v
    }

    fun setPasswordExcludeSimilar(v: Boolean) {
        _passwordExcludeSimilar.value = v
    }

    fun setPasswordExcludeAmbiguous(v: Boolean) {
        _passwordExcludeAmbiguous.value = v
    }

    fun setStylishTextInput(text: String) {
        _stylishTextInput.value = text
    }

    fun setStylishFont(font: String) {
        _stylishFont.value = font
    }

    fun setCurrentResultText(text: String) {
        _currentResultText.value = text
    }

    fun setNicknameCategory(cat: String) {
        _nicknameCategory.value = cat
    }

    fun setNicknameCount(count: Float) {
        _nicknameCount.value = count
    }

    fun setBioCategory(cat: String) {
        _bioCategory.value = cat
    }

    fun setBioLength(len: String) {
        _bioLength.value = len
    }

    fun setBioEmoji(emoji: Boolean) {
        _bioEmoji.value = emoji
    }

    fun setBuilderBaseName(name: String) {
        _builderBaseName.value = name
    }

    fun setBuilderPrefix(prefix: String) {
        _builderPrefix.value = prefix
    }

    fun setBuilderSuffix(suffix: String) {
        _builderSuffix.value = suffix
    }

    fun setBuilderAddNumbers(add: Boolean) {
        _builderAddNumbers.value = add
    }

    fun setBuilderAddUnderscore(add: Boolean) {
        _builderAddUnderscore.value = add
    }

    fun setBuilderAddDot(add: Boolean) {
        _builderAddDot.value = add
    }

    fun setBuilderRandomLetters(random: Boolean) {
        _builderRandomLetters.value = random
    }

    fun setTextSaverInput(text: String) {
        _textSaverInput.value = text
    }

    fun saveText() {
        val text = _textSaverInput.value
        if (text.isNotBlank()) {
            viewModelScope.launch {
                settingsRepository.addSavedText(text)
                _textSaverInput.value = ""
            }
        }
    }

    fun deleteSavedItem(index: Int) {
        viewModelScope.launch {
            settingsRepository.deleteSavedTextAt(index)
        }
    }

    fun clearSavedText() {
        viewModelScope.launch {
            settingsRepository.clearSavedList()
            _textSaverInput.value = ""
        }
    }

    fun setSmartQuery(query: String) {
        _smartQuery.value = query
    }

    fun setMixerInputs(n1: String, n2: String, n3: String = "") {
        _mixerName1.value = n1
        _mixerName2.value = n2
        _mixerName3.value = n3
    }

    fun mixNames(n1: String, n2: String, n3: String = "") {
        _mixerName1.value = n1
        _mixerName2.value = n2
        _mixerName3.value = n3
        viewModelScope.launch(Dispatchers.Default) {
            val results = NameMixer.mix(n1, n2, n3)
            withContext(Dispatchers.Main) {
                _mixerResults.value = results
                if (results.isNotEmpty()) {
                    _currentResultText.value = results.first()
                    addHistory("MIXER", results.first())
                }
            }
        }
    }

    fun generateNames(onComplete: ((List<NameResult>) -> Unit)? = null) {
        val curMode = _selectedMode.value
        val curGender = _selectedGender.value
        val curCountry = _selectedCountry.value
        val curStyle = _selectedStyle.value
        val curLastNameCat = _lastNameCategory.value
        val curIncludeMid = _includeMiddle.value
        val curCustFirst = _customFirst.value
        val curCustMid = _customMiddle.value
        val curCustLast = _customLast.value
        val curCount = _selectedCount.value
        val curLength = _selectedNameLength.value
        val curInitials = _initialsOnly.value
        val curExcludeDupes = _excludeDuplicates.value
        val curNoRepeat = _noRepeat.value
        val curFont = fontStyle.value

        _isGenerating.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val results = ModernGenerators.generateName(
                mode = curMode,
                gender = curGender,
                country = curCountry,
                style = curStyle,
                lastNameCategory = curLastNameCat,
                includeMiddle = curIncludeMid,
                customFirst = curCustFirst,
                customMiddle = curCustMid,
                customLast = curCustLast,
                count = curCount,
                lengthPref = curLength,
                initialsOnly = curInitials,
                excludeDuplicates = curExcludeDupes,
                noRepeatSession = curNoRepeat
            )
            withContext(Dispatchers.Main) {
                _generatedNames.value = results
                _isGenerating.value = false
                if (results.isNotEmpty()) {
                    val styled = FontStyles.apply(results.first().name, curFont)
                    _currentResultText.value = styled
                    addHistory("NAME", styled)
                }
                onComplete?.invoke(results)
            }
        }
    }

    fun generateCurrentTool(onComplete: ((String) -> Unit)? = null) {
        val tool = _activeTool.value
        val curGender = _selectedGender.value
        val curCountry = _selectedCountry.value
        val curStyle = _selectedStyle.value
        val curNoRepeat = _noRepeat.value
        val curCount = _selectedCount.value
        val curUStyle = _usernameStyle.value
        val curAddNum = _usernameAddNum.value
        val curAddUnder = _usernameAddUnder.value
        val curPwdLen = _passwordLength.value.toInt()
        val curUpper = _passwordUpper.value
        val curLower = _passwordLower.value
        val curNums = _passwordNums.value
        val curSyms = _passwordSyms.value
        val curInput = _stylishTextInput.value
        val curFont = _stylishFont.value
        val curNickCat = _nicknameCategory.value
        val curBioCat = _bioCategory.value
        val curBioLen = _bioLength.value
        val curBioEmoji = _bioEmoji.value
        val curMix1 = _mixerName1.value
        val curMix2 = _mixerName2.value

        _isGenerating.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val res: String = when (tool) {
                "Name", "Full Name", "First Name", "Middle Name", "Surname", "Last Name" -> {
                    val modeToUse = if (tool == "Name") _selectedMode.value else tool
                    val list = ModernGenerators.generateName(
                        mode = modeToUse,
                        gender = curGender,
                        country = curCountry,
                        style = curStyle,
                        lastNameCategory = "Random",
                        includeMiddle = true,
                        customFirst = _customFirst.value,
                        customMiddle = _customMiddle.value,
                        customLast = _customLast.value,
                        count = curCount.coerceAtLeast(1),
                        lengthPref = _selectedNameLength.value,
                        initialsOnly = false,
                        excludeDuplicates = true,
                        noRepeatSession = curNoRepeat
                    )
                    withContext(Dispatchers.Main) {
                        _generatedNames.value = list
                    }
                    list.firstOrNull()?.name ?: "Alex Morgan"
                }
                "First + Middle" -> {
                    val list = ModernGenerators.generateName(
                        mode = "First + Middle",
                        gender = curGender,
                        country = curCountry,
                        style = curStyle,
                        lastNameCategory = "Random",
                        includeMiddle = true,
                        customFirst = _customFirst.value,
                        customMiddle = _customMiddle.value,
                        customLast = "",
                        count = curCount.coerceAtLeast(1),
                        lengthPref = "Medium",
                        initialsOnly = false,
                        excludeDuplicates = true,
                        noRepeatSession = curNoRepeat
                    )
                    withContext(Dispatchers.Main) {
                        _generatedNames.value = list
                    }
                    list.firstOrNull()?.name ?: "Alex Lee"
                }
                "Username" -> {
                    val list = ModernGenerators.generateUsername(
                        style = curUStyle,
                        country = curCountry,
                        gender = curGender,
                        addNumbers = curAddNum,
                        addUnderscore = curAddUnder,
                        addDot = _usernameAddDot.value,
                        shortUsername = _usernameShort.value,
                        randomPrefix = _usernamePrefix.value,
                        randomSuffix = _usernameSuffix.value,
                        noRepeatSession = curNoRepeat,
                        count = 1
                    )
                    list.firstOrNull() ?: "alex_morgan"
                }
                "Password" -> PasswordGenerator.generatePassword(
                    curPwdLen, 
                    curUpper, 
                    curLower, 
                    curNums, 
                    curSyms, 
                    _passwordExcludeSimilar.value, 
                    _passwordExcludeAmbiguous.value
                )
                "Nickname" -> NicknameGenerator.generateNickname(curNickCat, curCountry, curGender)
                "Bio" -> BioGenerator.generateBio(curBioCat, curBioLen, curBioEmoji)
                "Stylish Text" -> FontStyles.apply(curInput.ifEmpty { com.example.utils.AppConstants.APP_NAME }, curFont)
                "Random Profile" -> {
                    val p = ProfileGenerator.generateProfile(curCountry, curGender)
                    "${p.name} (@${p.username})"
                }
                "Smart Assistant" -> SmartAssistant.findNames("popular cute $curGender name", 1).firstOrNull()?.name ?: "Aria"
                "Name Mixer" -> {
                    val mixed = NameMixer.mix(curMix1, curMix2)
                    withContext(Dispatchers.Main) {
                        _mixerResults.value = mixed
                    }
                    mixed.firstOrNull() ?: "$curMix1$curMix2"
                }
                else -> {
                    val list = ModernGenerators.generateName("Full Name", curGender, curCountry, curStyle, "Random", true, "", "", "", 1, "Medium", false, true, curNoRepeat)
                    list.firstOrNull()?.name ?: "Alex Morgan"
                }
            }

            withContext(Dispatchers.Main) {
                _currentResultText.value = res
                _isGenerating.value = false
                addHistory(tool.uppercase(), res)
                onComplete?.invoke(res)
            }
        }
    }

    // === Lazy Tab Loaders ===
    private var homeLoaded = false
    fun onHomeVisible() {
        if (homeLoaded) return
        homeLoaded = true
        viewModelScope.launch(Dispatchers.IO) {
            // Lazy load or warm up home-related data
        }
    }

    private var generateLoaded = false
    fun onGenerateVisible() {
        if (generateLoaded) return
        generateLoaded = true
        viewModelScope.launch(Dispatchers.IO) {
            // Lazy load or warm up generator caches
        }
    }

    private var historyLoaded = false
    fun onHistoryVisible() {
        if (historyLoaded) return
        historyLoaded = true
        viewModelScope.launch(Dispatchers.IO) {
            // Lazy load or warm up history data
        }
    }

    private var settingsLoaded = false
    fun onSettingsVisible() {
        if (settingsLoaded) return
        settingsLoaded = true
        viewModelScope.launch(Dispatchers.IO) {
            // Lazy load or warm up settings data
        }
    }
}

class MainViewModelFactory(
    private val historyRepository: HistoryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(historyRepository, favoriteRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
