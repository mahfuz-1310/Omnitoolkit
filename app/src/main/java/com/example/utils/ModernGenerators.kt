package com.example.utils

import kotlin.random.Random

object ModernGenerators {

    data class FirstMiddleItem(
        val id: String = java.util.UUID.randomUUID().toString(),
        val firstName: String,
        val middleName: String,
        val country: String,
        val gender: String,
        val style: String
    ) {
        val fullName: String get() = "$firstName $middleName"
    }

    fun getRandomFirstName(country: String, gender: String): String {
        val pool = when(gender) {
            "Male" -> CountryData.getMaleNames(country)
            "Female" -> CountryData.getFemaleNames(country)
            else -> CountryData.getUnisexNames(country)
        }
        return pool.random()
    }

    fun getRandomMiddleName(country: String, gender: String): String {
        val pool = CountryData.getMiddleNames(country, gender)
        return pool.random()
    }

    private fun applyStyleToName(name: String, s: String): String {
        return when(s) {
            "Cute" -> if (!name.endsWith("y", true) && !name.endsWith("a", true)) "${name}ie" else name
            "Royal" -> "Sir $name".trim()
            else -> name
        }
    }

    fun generateFirstMiddle(
        country: String,
        gender: String,
        style: String,
        count: Int,
        noRepeatSession: Boolean
    ): List<FirstMiddleItem> {
        val rawFirstPool = when(gender) {
            "Male" -> CountryData.getMaleNames(country)
            "Female" -> CountryData.getFemaleNames(country)
            else -> CountryData.getUnisexNames(country)
        }
        val rawMiddlePool = CountryData.getMiddleNames(country, gender)
        
        if (rawFirstPool.isEmpty() || rawMiddlePool.isEmpty()) return emptyList()

        val firstPool = if (style != "Normal") rawFirstPool.map { applyStyleToName(it, style) } else rawFirstPool
        val middlePool = if (style != "Normal") rawMiddlePool.map { applyStyleToName(it, style) } else rawMiddlePool

        val results = ArrayList<FirstMiddleItem>(count)
        val seenCombos = HashSet<String>(count * 2)
        
        val maxPossible = firstPool.size * middlePool.size
        val targetCount = minOf(count, maxPossible)
        val maxAttempts = targetCount * 25
        var attempts = 0

        val fSize = firstPool.size
        val mSize = middlePool.size

        while (results.size < targetCount && attempts < maxAttempts) {
            attempts++
            val fName = firstPool[Random.nextInt(fSize)]
            val mName = middlePool[Random.nextInt(mSize)]
            
            if (fName.equals(mName, ignoreCase = true)) continue
            
            val comboKey = "$fName|$mName|$country|$gender|$style".lowercase()
            
            if (noRepeatSession) {
                if (SessionCache.generatedFirstMiddleNames.contains(comboKey) || seenCombos.contains(comboKey)) {
                    continue
                }
                SessionCache.generatedFirstMiddleNames.add(comboKey)
            }
            
            if (!seenCombos.add(comboKey)) continue
            
            results.add(
                FirstMiddleItem(
                    firstName = fName,
                    middleName = mName,
                    country = country,
                    gender = gender,
                    style = style
                )
            )
        }
        
        return results
    }
    
    fun generateName(
        mode: String,
        gender: String,
        country: String,
        style: String,
        lastNameCategory: String,
        includeMiddle: Boolean,
        customFirst: String,
        customMiddle: String,
        customLast: String,
        count: Int,
        lengthPref: String,
        initialsOnly: Boolean,
        excludeDuplicates: Boolean,
        noRepeatSession: Boolean
    ): List<NameResult> {
        // Pre-fetch candidate pools once
        var firstPool: List<String> = if (customFirst.isNotBlank()) {
            listOf(customFirst.trim())
        } else {
            val pool = when(gender) {
                "Male" -> CountryData.getMaleNames(country)
                "Female" -> CountryData.getFemaleNames(country)
                else -> CountryData.getUnisexNames(country)
            }
            when (lengthPref) {
                "Short" -> pool.filter { it.length <= 4 }.ifEmpty { pool }
                "Long" -> pool.filter { it.length >= 7 }.ifEmpty { pool }
                else -> pool
            }
        }

        var middlePool: List<String> = if ((mode == "Full Name" && includeMiddle) || mode == "Middle Name") {
            if (customMiddle.isNotBlank() && mode == "Full Name") {
                listOf(customMiddle.trim())
            } else {
                val pool = CountryData.getMiddleNames(country, gender)
                when (lengthPref) {
                    "Short" -> pool.filter { it.length <= 4 }.ifEmpty { pool }
                    "Long" -> pool.filter { it.length >= 7 }.ifEmpty { pool }
                    else -> pool
                }
            }
        } else emptyList()

        var lastPool: List<String> = if (mode == "Full Name" || mode == "Last Name") {
            if (customLast.isNotBlank() && mode == "Full Name") {
                listOf(customLast.trim())
            } else {
                CountryData.getLastNames(country)
            }
        } else emptyList()

        if (initialsOnly) {
            firstPool = firstPool.map { if (it.isNotEmpty()) "${it.first().uppercase()}." else it }
            if (middlePool.isNotEmpty()) {
                middlePool = middlePool.map { if (it.isNotEmpty()) "${it.first().uppercase()}." else it }
            }
            if (lastPool.isNotEmpty()) {
                lastPool = lastPool.map { if (it.isNotEmpty()) "${it.first().uppercase()}." else it }
            }
        }

        val results = LinkedHashSet<String>(count * 2)
        val maxAttempts = count * 30
        var attempts = 0

        val fSize = firstPool.size
        val mSize = middlePool.size
        val lSize = lastPool.size

        while (results.size < count && attempts < maxAttempts) {
            attempts++
            
            val first = if (fSize > 0) firstPool[Random.nextInt(fSize)] else ""
            val middle = if (mSize > 0) middlePool[Random.nextInt(mSize)] else ""
            val last = if (lSize > 0) lastPool[Random.nextInt(lSize)] else ""
            
            val finalName = when(mode) {
                "Full Name" -> {
                    when {
                        middle.isNotEmpty() && last.isNotEmpty() -> "$first $middle $last"
                        last.isNotEmpty() -> "$first $last"
                        middle.isNotEmpty() -> "$first $middle"
                        else -> first
                    }
                }
                "First Name" -> first
                "Middle Name" -> middle.ifEmpty { first }
                "Last Name" -> last.ifEmpty { first }
                "Username" -> UsernameGenerator.generateUsername(style, true, false, false, false, false, false, country, gender)
                else -> first
            }

            if (finalName.isBlank()) continue
            
            if (noRepeatSession) {
                if (mode == "Middle Name") {
                    if (!SessionCache.generatedMiddleNames.contains(finalName)) {
                        SessionCache.generatedMiddleNames.add(finalName)
                        results.add(finalName)
                    }
                } else {
                    if (!SessionCache.generatedNames.contains(finalName)) {
                        SessionCache.generatedNames.add(finalName)
                        results.add(finalName)
                    }
                }
            } else if (excludeDuplicates) {
                results.add(finalName)
            } else {
                if (results.contains(finalName)) {
                    results.add("$finalName $attempts")
                } else {
                    results.add(finalName)
                }
            }
        }
        
        return results.take(count).map { name ->
            val firstToken = name.split(" ").firstOrNull() ?: name
            NameResult(name, Datasets.getMeaning(firstToken), style, lengthPref)
        }
    }
    
    fun generateUsername(
        style: String,
        country: String,
        gender: String,
        addNumbers: Boolean,
        addUnderscore: Boolean,
        addDot: Boolean,
        shortUsername: Boolean,
        randomPrefix: Boolean,
        randomSuffix: Boolean,
        noRepeatSession: Boolean,
        count: Int
    ): List<String> {
        val results = LinkedHashSet<String>(count * 2)
        val firstPool = when(gender) {
            "Male" -> CountryData.getMaleNames(country)
            "Female" -> CountryData.getFemaleNames(country)
            else -> CountryData.getUnisexNames(country) + CountryData.getMaleNames(country) + CountryData.getFemaleNames(country)
        }
        val lastPool = CountryData.getLastNames(country)
        
        if (firstPool.isEmpty()) return emptyList()
        val nouns = Datasets.nouns
        val adjectives = Datasets.adjectives

        val maxAttempts = count * 40
        var attempts = 0
        
        while (results.size < count && attempts < maxAttempts) {
            attempts++
            
            val first = firstPool.random().replace(" ", "").lowercase()
            val last = if (lastPool.isNotEmpty()) lastPool.random().replace(" ", "").lowercase() else nouns.random().lowercase()
            
            var user = when(style) {
                "Cute" -> {
                    val adj = adjectives.random().lowercase()
                    if (Random.nextBoolean()) "${first}${adj}" else "${adj}${first}"
                }
                "Aesthetic" -> {
                    val words = Datasets.aestheticWords
                    val w = words.random().lowercase()
                    if (Random.nextBoolean()) "${first}_${w}" else "${w}_${first}"
                }
                "Gaming" -> {
                    val gPrefixes = listOf("Pro", "Elite", "Legend", "Epic", "Slayer", "Shadow")
                    if (Random.nextBoolean()) "${gPrefixes.random()}${first}" else "${first}${Random.nextInt(10, 999)}"
                }
                "Professional" -> {
                    if (lastPool.isNotEmpty()) "${first}.${last}" else "${first}.${nouns.random().lowercase()}"
                }
                else -> { // Random/Default/Aesthetic/Cute improvements: Real name combinations
                    val patterns = mutableListOf<() -> String>(
                        { "${first}${last}" },
                        { "${first}_${last}" },
                        { "${first}.${last}" }
                    )
                    if (first.length > 3) patterns.add({ "${first.take(3)}${last}" })
                    if (last.length > 3) patterns.add({ "${first}${last.take(3)}" })
                    
                    patterns.random().invoke()
                }
            }
            
            if (shortUsername) {
                user = first.take(6)
            }
            
            if (randomPrefix) {
                val p = listOf("the", "real", "its", "im", "official").random()
                user = "${p}${if(addUnderscore) "_" else if(addDot) "." else ""}${user}"
            }
            if (randomSuffix) {
                val s = listOf("ok", "world", "me", "here", "live").random()
                user = "${user}${if(addUnderscore) "_" else if(addDot) "." else ""}${s}"
            }
            
            if (addUnderscore && !user.contains("_")) {
                val idx = (1 until user.length).random()
                user = user.substring(0, idx) + "_" + user.substring(idx)
            }
            if (addDot && !user.contains(".")) {
                val idx = (1 until user.length).random()
                user = user.substring(0, idx) + "." + user.substring(idx)
            }
            if (addNumbers) {
                user += Random.nextInt(10, 999).toString()
            }
            
            // Clean up multiple separators
            user = user.replace("__", "_").replace("..", ".").replace("_.", "_").replace("._", ".")
            
            if (noRepeatSession) {
                if (!SessionCache.generatedUsernames.contains(user)) {
                    SessionCache.generatedUsernames.add(user)
                    results.add(user)
                }
            } else {
                results.add(user)
            }
        }
        return results.toList()
    }
}
