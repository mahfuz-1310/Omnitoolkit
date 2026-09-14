package com.example.utils

import java.security.SecureRandom
import java.util.regex.Pattern
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.random.Random

data class NameResult(val name: String, val meaning: String, val style: String, val length: String)

object SmartAssistant {
    fun findNames(query: String, count: Int): List<NameResult> {
        val q = query.lowercase()
        var gender = "Unisex"
        if (q.contains("boy") || q.contains("male") || q.contains("chele")) gender = "Male"
        if (q.contains("girl") || q.contains("female") || q.contains("woman") || q.contains("meye")) gender = "Female"

        var lengthPref = "Medium"
        if (q.contains("short")) lengthPref = "Short"
        if (q.contains("long")) lengthPref = "Long"

        val styleKeywords = listOf("cute", "modern", "stylish", "aesthetic", "royal", "gaming", "professional", "unique", "classic", "rare", "powerful", "soft", "elegant")
        val foundStyles = styleKeywords.filter { q.contains(it) }
        val primaryStyle = foundStyles.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Random"

        val baseNames = when (gender) {
            "Male" -> CountryData.getMaleNames("International")
            "Female" -> CountryData.getFemaleNames("International")
            else -> CountryData.getUnisexNames("International") + CountryData.getMaleNames("International") + CountryData.getFemaleNames("International")
        }

        val filtered = when (lengthPref) {
            "Short" -> baseNames.filter { it.length <= 4 }.ifEmpty { baseNames }
            "Long" -> baseNames.filter { it.length >= 7 }.ifEmpty { baseNames }
            else -> baseNames
        }

        val pool = filtered.shuffled()
        val stylesSample = listOf("Modern", "Classic", "Unique")
        val takeCount = minOf(count, pool.size)

        return pool.take(takeCount).map { name ->
            val lenDesc = if (name.length <= 4) "Short" else if (name.length >= 7) "Long" else "Medium"
            val style = if (primaryStyle != "Random") primaryStyle else stylesSample[Random.nextInt(stylesSample.size)]
            NameResult(name, Datasets.getMeaning(name), style, lenDesc)
        }
    }
}

object NameGenerator {
    fun generateNames(country: String, gender: String, count: Int): List<String> {
        val pool = when (gender) {
            "Male" -> CountryData.getMaleNames(country)
            "Female" -> CountryData.getFemaleNames(country)
            "Unisex" -> CountryData.getUnisexNames(country)
            else -> CountryData.getMaleNames(country) + CountryData.getFemaleNames(country)
        }
        if (pool.isEmpty()) return emptyList()
        val shuffled = pool.shuffled()
        return shuffled.take(count.coerceAtMost(shuffled.size))
    }
    
    fun generateName(country: String, gender: String): String {
        val pool = when (gender) {
            "Male" -> CountryData.getMaleNames(country)
            "Female" -> CountryData.getFemaleNames(country)
            "Unisex" -> CountryData.getUnisexNames(country)
            else -> CountryData.getMaleNames(country) + CountryData.getFemaleNames(country)
        }
        return if (pool.isNotEmpty()) pool[Random.nextInt(pool.size)] else "Name"
    }
}

object UsernameGenerator {
    fun generateUsername(style: String, addNumbers: Boolean, addUnderscore: Boolean, addDot: Boolean, short: Boolean, randomSuffix: Boolean, randomPrefix: Boolean, country: String = "International", gender: String = "Male"): String {
        return ModernGenerators.generateUsername(
            style = style,
            country = country,
            gender = gender,
            addNumbers = addNumbers,
            addUnderscore = addUnderscore,
            addDot = addDot,
            shortUsername = short,
            randomPrefix = randomPrefix,
            randomSuffix = randomSuffix,
            noRepeatSession = false,
            count = 1
        ).firstOrNull() ?: "user123"
    }
    
    fun buildUsername(baseName: String, prefix: String, suffix: String, addNumbers: Boolean, addUnderscore: Boolean, addDot: Boolean, randomLetters: Boolean): String {
        if(baseName.isEmpty()) return ""
        var user = baseName.lowercase().replace(" ", "")
        
        if(prefix.isNotEmpty()) user = "${prefix.lowercase()}${if(addUnderscore) "_" else ""}$user"
        if(suffix.isNotEmpty()) user = "$user${if(addUnderscore) "_" else ""}${suffix.lowercase()}"
        
        if(addNumbers) user = "$user${(10..999).random()}"
        if(randomLetters) {
            val letters = "abcdefghijklmnopqrstuvwxyz"
            user = "$user${letters.random()}${letters.random()}"
        }
        if (addDot && !user.contains(".")) {
             val idx = (1 until user.length).random()
             user = user.substring(0, idx) + "." + user.substring(idx)
        }
        if (addUnderscore && !user.contains("_")) {
             val idx = (1 until user.length).random()
             user = user.substring(0, idx) + "_" + user.substring(idx)
        }
        
        return user
    }
}

object BioGenerator {
    private val EMOJI_PATTERN: Pattern = Pattern.compile("[\\p{So}\\p{Cs}]+")

    fun generateBio(category: String, length: String, emoji: Boolean): String {
        val templates = Datasets.bioTemplates[category] ?: Datasets.bioTemplates.values.flatten()
        val numLines = when(length) {
            "Short" -> 1
            "Medium" -> 2
            "Long" -> 3
            else -> 2
        }
        
        val selected = templates.shuffled().take(numLines).joinToString("\n")
        return if (emoji) selected else EMOJI_PATTERN.matcher(selected).replaceAll("").trim()
    }
}

object NicknameGenerator {
    fun generateNickname(category: String, country: String = "International", gender: String = "Male"): String {
        val first = ModernGenerators.getRandomFirstName(country, gender)
        return when (category) {
            "Cute" -> "${first.take(3)}ie"
            "Gaming" -> "Pro_$first"
            "Funny" -> first + "osaurus"
            "Stylish" -> "xX${first}Xx"
            "Short" -> first.take(4)
            "Royal" -> "${Datasets.royalWords.random()} $first"
            "Couple" -> {
                val f = CountryData.getMaleNames(country).random().take(3)
                val m = CountryData.getFemaleNames(country).random().takeLast(3)
                "$f$m"
            }
            else -> first
        }
    }
}

data class RandomProfile(
    val name: String,
    val username: String,
    val nickname: String,
    val bio: String,
    val country: String,
    val ageRange: String,
    val personality: String,
    val interests: String
)

object ProfileGenerator {
    fun generateProfile(country: String = "International", gender: String = "Male"): RandomProfile {
        val name = "${NameGenerator.generateName(country, gender)} ${CountryData.getLastNames(country).random()}"
        val username = UsernameGenerator.generateUsername("Random", true, false, false, false, false, false, country, gender)
        val nickname = NicknameGenerator.generateNickname("Random", country, gender)
        val bio = BioGenerator.generateBio("Random", "Short", true)
        val ageRange = listOf("18-24", "25-34", "35-44", "45-54").random()
        val personality = Datasets.personalities.random()
        val interests = Datasets.interests.shuffled().take(3).joinToString(", ")
        
        return RandomProfile(name, username, nickname, bio, country, ageRange, personality, interests)
    }
}

object NameMixer {
    fun mix(first: String, middle: String, last: String): List<String> {
        val f = first.trim()
        val m = middle.trim()
        val l = last.trim()
        
        val combos = mutableListOf<String>()
        if (f.isNotEmpty() && l.isNotEmpty()) combos.add("$f $l")
        if (f.isNotEmpty() && m.isNotEmpty() && l.isNotEmpty()) combos.add("$f $m $l")
        if (f.isNotEmpty() && m.isNotEmpty() && l.isNotEmpty()) combos.add("$f ${m.firstOrNull()?.uppercase()}. $l")
        if (f.isNotEmpty() && m.isNotEmpty() && l.isNotEmpty()) combos.add("${f.firstOrNull()?.uppercase()}. $m $l")
        if (l.isNotEmpty() && f.isNotEmpty()) combos.add("$l, $f")
        if (f.isNotEmpty() && l.isNotEmpty()) combos.add("$f-${l.take(3)}")
        
        return combos.filter { it.isNotBlank() }
    }

    fun mix(first: String, last: String): List<String> = mix(first, "", last)
}

object PasswordGenerator {
    private val secureRandom = SecureRandom()

    fun generatePassword(length: Int, uppercase: Boolean, lowercase: Boolean, numbers: Boolean, symbols: Boolean, excludeSimilar: Boolean, excludeAmbiguous: Boolean): String {
        var uppers = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var lowers = "abcdefghijklmnopqrstuvwxyz"
        var nums = "0123456789"
        var syms = "!@#$%^&*()_+~`|}{[]:;?><,./-="

        if (excludeSimilar) {
            val similar = "iIl10oO"
            uppers = uppers.filter { !similar.contains(it) }
            lowers = lowers.filter { !similar.contains(it) }
            nums = nums.filter { !similar.contains(it) }
        }

        if (excludeAmbiguous) {
            val ambiguous = "{}[]()/\\'\"`~,;:.<>"
            syms = syms.filter { !ambiguous.contains(it) }
        }

        val charsBuilder = StringBuilder()
        if (uppercase) charsBuilder.append(uppers)
        if (lowercase) charsBuilder.append(lowers)
        if (numbers) charsBuilder.append(nums)
        if (symbols) charsBuilder.append(syms)

        val chars = charsBuilder.toString()
        if (chars.isEmpty() || length <= 0) return ""

        val resultChars = CharArray(length)
        var pos = 0

        if (uppercase && uppers.isNotEmpty() && pos < length) resultChars[pos++] = uppers[secureRandom.nextInt(uppers.length)]
        if (lowercase && lowers.isNotEmpty() && pos < length) resultChars[pos++] = lowers[secureRandom.nextInt(lowers.length)]
        if (numbers && nums.isNotEmpty() && pos < length) resultChars[pos++] = nums[secureRandom.nextInt(nums.length)]
        if (symbols && syms.isNotEmpty() && pos < length) resultChars[pos++] = syms[secureRandom.nextInt(syms.length)]
        
        val charsLen = chars.length
        while (pos < length) {
            resultChars[pos++] = chars[secureRandom.nextInt(charsLen)]
        }
        
        // In-place Fisher-Yates shuffle
        for (i in length - 1 downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val tmp = resultChars[i]
            resultChars[i] = resultChars[j]
            resultChars[j] = tmp
        }
        
        return String(resultChars)
    }
    
    fun evaluateStrength(password: String): String {
        var score = 0
        val len = password.length
        if (len >= 8) score++
        if (len >= 12) score++
        if (len >= 16) score++
        
        var hasUpper = false
        var hasLower = false
        var hasDigit = false
        var hasSymbol = false
        
        for (i in 0 until len) {
            val c = password[i]
            when {
                c.isUpperCase() -> hasUpper = true
                c.isLowerCase() -> hasLower = true
                c.isDigit() -> hasDigit = true
                else -> hasSymbol = true
            }
        }
        
        if (hasUpper) score++
        if (hasLower) score++
        if (hasDigit) score++
        if (hasSymbol) score++
        
        return when {
            score < 3 -> "Weak"
            score < 5 -> "Medium"
            score < 7 -> "Strong"
            else -> "Very Strong"
        }
    }
    
    fun calculateEntropy(password: String): Int {
        if (password.isEmpty()) return 0
        var hasUpper = false
        var hasLower = false
        var hasDigit = false
        var hasSymbol = false
        
        val len = password.length
        for (i in 0 until len) {
            val c = password[i]
            when {
                c.isUpperCase() -> hasUpper = true
                c.isLowerCase() -> hasLower = true
                c.isDigit() -> hasDigit = true
                else -> hasSymbol = true
            }
        }
        
        var poolSize = 0
        if (hasUpper) poolSize += 26
        if (hasLower) poolSize += 26
        if (hasDigit) poolSize += 10
        if (hasSymbol) poolSize += 32
        
        if (poolSize == 0) return 0
        
        val entropy = len * log2(poolSize.toDouble())
        return entropy.roundToInt()
    }
}
