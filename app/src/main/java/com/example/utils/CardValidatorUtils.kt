package com.example.utils

data class CardAnalysisResult(
    val rawNumber: String,
    val formattedNumber: String,
    val brand: CardBrand,
    val cardType: String,
    val cardTier: String,
    val issuerBank: String,
    val country: String,
    val isLuhnValid: Boolean,
    val lengthValid: Boolean,
    val summaryStatus: String
)

enum class CardBrand(val displayName: String, val patternDesc: String) {
    VISA("Visa", "Starts with 4 (13, 16, or 19 digits)"),
    MASTERCARD("MasterCard", "Starts with 51-55 or 2221-2720 (16 digits)"),
    AMERICAN_EXPRESS("American Express", "Starts with 34 or 37 (15 digits)"),
    DISCOVER("Discover", "Starts with 6011, 644-649, 65 (16 digits)"),
    JCB("JCB", "Starts with 3528-3589 (16 digits)"),
    DINERS_CLUB("Diners Club", "Starts with 300-305, 36, 38 (14-16 digits)"),
    UNION_PAY("UnionPay", "Starts with 62 (16-19 digits)"),
    MAESTRO("Maestro", "Starts with 50, 56-58, 6 (12-19 digits)"),
    RUPAY("RuPay", "Starts with 60, 6521, 6522 (16 digits)"),
    UNKNOWN("Unknown Network", "Unrecognized IIN prefix")
}

object CardValidatorUtils {

    fun cleanCardNumber(input: String): String {
        return input.filter { it.isDigit() }
    }

    fun formatCardNumber(cleanNumber: String): String {
        val brand = detectCardBrand(cleanNumber)
        return if (brand == CardBrand.AMERICAN_EXPRESS) {
            // 4-6-5 format for Amex
            buildString {
                for (i in cleanNumber.indices) {
                    if (i == 4 || i == 10) append(' ')
                    append(cleanNumber[i])
                }
            }
        } else {
            // 4-4-4-4 format
            cleanNumber.chunked(4).joinToString(" ")
        }
    }

    fun maskCardNumber(cleanNumber: String): String {
        if (cleanNumber.length < 10) return cleanNumber
        val start = cleanNumber.take(6)
        val end = cleanNumber.takeLast(4)
        val maskedMiddle = "*".repeat(cleanNumber.length - 10)
        return formatCardNumber("$start$maskedMiddle$end")
    }

    /**
     * Algorithmic Validation using Luhn Formula (Mod 10)
     */
    fun validateLuhn(cleanNumber: String): Boolean {
        if (cleanNumber.length < 13 || cleanNumber.length > 19) return false
        var sum = 0
        var alternate = false
        for (i in cleanNumber.length - 1 downTo 0) {
            var n = cleanNumber[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = (n % 10) + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }

    /**
     * Auto-detect network from IIN / BIN prefix
     */
    fun detectCardBrand(cleanNumber: String): CardBrand {
        if (cleanNumber.isEmpty()) return CardBrand.UNKNOWN

        // Visa: starts with 4
        if (cleanNumber.startsWith("4")) {
            return CardBrand.VISA
        }

        // American Express: starts with 34 or 37
        if (cleanNumber.startsWith("34") || cleanNumber.startsWith("37")) {
            return CardBrand.AMERICAN_EXPRESS
        }

        // Diners Club: 300-305, 36, 38
        if (cleanNumber.startsWith("36") || cleanNumber.startsWith("38") ||
            (cleanNumber.length >= 3 && cleanNumber.take(3).toIntOrNull() in 300..305)
        ) {
            return CardBrand.DINERS_CLUB
        }

        // JCB: 3528-3589
        if (cleanNumber.length >= 4) {
            val prefix4 = cleanNumber.take(4).toIntOrNull() ?: 0
            if (prefix4 in 3528..3589) return CardBrand.JCB
        }

        // MasterCard: 51-55 or 2221-2720
        if (cleanNumber.length >= 2) {
            val prefix2 = cleanNumber.take(2).toIntOrNull() ?: 0
            if (prefix2 in 51..55) return CardBrand.MASTERCARD
        }
        if (cleanNumber.length >= 4) {
            val prefix4 = cleanNumber.take(4).toIntOrNull() ?: 0
            if (prefix4 in 2221..2720) return CardBrand.MASTERCARD
        }

        // Discover: 6011, 622126-622925, 644-649, 65
        if (cleanNumber.startsWith("6011") || cleanNumber.startsWith("65")) {
            return CardBrand.DISCOVER
        }
        if (cleanNumber.length >= 3) {
            val prefix3 = cleanNumber.take(3).toIntOrNull() ?: 0
            if (prefix3 in 644..649) return CardBrand.DISCOVER
        }

        // RuPay: 60, 6521, 6522, 508
        if (cleanNumber.startsWith("6521") || cleanNumber.startsWith("6522") || cleanNumber.startsWith("508")) {
            return CardBrand.RUPAY
        }

        // UnionPay: 62
        if (cleanNumber.startsWith("62")) {
            return CardBrand.UNION_PAY
        }

        // Maestro: 5018, 5020, 5038, 5893, 6304, 6759, 6761, 6762, 6763
        if (cleanNumber.startsWith("50") || cleanNumber.startsWith("56") ||
            cleanNumber.startsWith("57") || cleanNumber.startsWith("58") ||
            cleanNumber.startsWith("67")
        ) {
            return CardBrand.MAESTRO
        }

        return CardBrand.UNKNOWN
    }

    fun isLengthValidForBrand(cleanNumber: String, brand: CardBrand): Boolean {
        val len = cleanNumber.length
        return when (brand) {
            CardBrand.VISA -> len == 13 || len == 16 || len == 19
            CardBrand.MASTERCARD -> len == 16
            CardBrand.AMERICAN_EXPRESS -> len == 15
            CardBrand.DISCOVER -> len == 16 || len == 19
            CardBrand.JCB -> len in 15..19
            CardBrand.DINERS_CLUB -> len in 14..16
            CardBrand.UNION_PAY -> len in 16..19
            CardBrand.MAESTRO -> len in 12..19
            CardBrand.RUPAY -> len == 16
            CardBrand.UNKNOWN -> len in 13..19
        }
    }

    /**
     * Standard BIN Lookup Heuristic / Bank Issuer identification based on first 6 digits (IIN/BIN).
     */
    fun lookupBinDetails(cleanNumber: String, brand: CardBrand): Triple<String, String, String> {
        val bin6 = cleanNumber.take(6)
        val bin4 = cleanNumber.take(4)
        val bin2 = cleanNumber.take(2)

        var issuer = "Standard Financial Institution"
        var country = "International"
        var tier = "Classic / Standard"

        when {
            // Chase
            bin6.startsWith("4147") || bin6.startsWith("4246") || bin6.startsWith("4388") || bin6.startsWith("4400") || bin6.startsWith("4737") -> {
                issuer = "JPMorgan Chase Bank, N.A."
                country = "United States (US)"
                tier = "Signature / Platinum"
            }
            // Bank of America
            bin6.startsWith("4800") || bin6.startsWith("4810") || bin6.startsWith("5524") || bin6.startsWith("5546") || bin6.startsWith("4744") -> {
                issuer = "Bank of America, N.A."
                country = "United States (US)"
                tier = "Preferred Rewards / World"
            }
            // Citibank
            bin6.startsWith("5466") || bin6.startsWith("5424") || bin6.startsWith("4128") || bin6.startsWith("4000") -> {
                issuer = "Citibank, N.A."
                country = "United States (US)"
                tier = "Double Cash / Premier"
            }
            // Wells Fargo
            bin6.startsWith("4003") || bin6.startsWith("5181") || bin6.startsWith("5203") -> {
                issuer = "Wells Fargo Bank, N.A."
                country = "United States (US)"
                tier = "Active Cash / Autograph"
            }
            // Capital One
            bin6.startsWith("5178") || bin6.startsWith("5291") || bin6.startsWith("5536") || bin6.startsWith("4021") -> {
                issuer = "Capital One Bank (USA), N.A."
                country = "United States (US)"
                tier = "Venture / Quicksilver"
            }
            // American Express Centurion Bank
            brand == CardBrand.AMERICAN_EXPRESS -> {
                issuer = "American Express National Bank"
                country = "United States (US)"
                tier = if (bin6.startsWith("3714") || bin6.startsWith("3782")) "Gold / Platinum Charge" else "Membership Rewards"
            }
            // Barclays
            bin6.startsWith("4929") || bin6.startsWith("5433") || bin6.startsWith("4508") -> {
                issuer = "Barclays Bank UK PLC"
                country = "United Kingdom (GB)"
                tier = "Barclaycard Platinum"
            }
            // HSBC
            bin6.startsWith("4012") || bin6.startsWith("5400") || bin6.startsWith("5239") -> {
                issuer = "HSBC Bank plc"
                country = "United Kingdom / Global"
                tier = "Premier / Advance"
            }
            // Royal Bank of Canada (RBC)
            bin6.startsWith("4514") || bin6.startsWith("5191") -> {
                issuer = "Royal Bank of Canada (RBC)"
                country = "Canada (CA)"
                tier = "Avion Visa Infinite"
            }
            // Commonwealth Bank of Australia
            bin6.startsWith("4564") || bin6.startsWith("5163") -> {
                issuer = "Commonwealth Bank of Australia"
                country = "Australia (AU)"
                tier = "CommBank Awards"
            }
            // Discover Financial
            brand == CardBrand.DISCOVER -> {
                issuer = "Discover Bank"
                country = "United States (US)"
                tier = "Discover it Cashback"
            }
            // JCB International
            brand == CardBrand.JCB -> {
                issuer = "JCB Co., Ltd."
                country = "Japan (JP)"
                tier = "JCB Gold / Ultimate"
            }
            // UnionPay International
            brand == CardBrand.UNION_PAY -> {
                issuer = "China UnionPay Co., Ltd."
                country = "China (CN)"
                tier = "Diamond / Platinum"
            }
            // RuPay (NPCI)
            brand == CardBrand.RUPAY -> {
                issuer = "National Payments Corporation of India (NPCI)"
                country = "India (IN)"
                tier = "RuPay Select / Platinum"
            }
            else -> {
                issuer = when (brand) {
                    CardBrand.VISA -> "Visa Issuer Network"
                    CardBrand.MASTERCARD -> "MasterCard Worldwide Network"
                    CardBrand.MAESTRO -> "Maestro Debit Network"
                    CardBrand.DINERS_CLUB -> "Diners Club International"
                    else -> "Global Card Issuer"
                }
                country = "Global / Multiregional"
                tier = "Standard Consumer"
            }
        }

        return Triple(issuer, country, tier)
    }

    fun analyzeCard(input: String): CardAnalysisResult {
        val clean = cleanCardNumber(input)
        val formatted = formatCardNumber(clean)
        val brand = detectCardBrand(clean)
        val isLuhn = validateLuhn(clean)
        val isLength = isLengthValidForBrand(clean, brand)
        val (issuer, country, tier) = lookupBinDetails(clean, brand)

        val cardType = when {
            brand == CardBrand.MAESTRO -> "Debit Card"
            brand == CardBrand.AMERICAN_EXPRESS -> "Credit / Charge Card"
            clean.startsWith("4") && (clean.startsWith("4147") || clean.startsWith("4003")) -> "Debit Card"
            clean.startsWith("5") && clean.startsWith("5181") -> "Debit Card"
            brand != CardBrand.UNKNOWN -> "Credit / Debit Card"
            else -> "Unknown Card Type"
        }

        val summaryStatus = when {
            clean.isEmpty() -> "No Card Entered"
            !isLength -> "Invalid Length (${clean.length} digits)"
            !isLuhn -> "Luhn Check Failed (Checksum Invalid)"
            else -> "Mathematically Valid Number (${brand.displayName})"
        }

        return CardAnalysisResult(
            rawNumber = clean,
            formattedNumber = formatted,
            brand = brand,
            cardType = cardType,
            cardTier = tier,
            issuerBank = issuer,
            country = country,
            isLuhnValid = isLuhn,
            lengthValid = isLength,
            summaryStatus = summaryStatus
        )
    }

    /**
     * Extracts potential 13-19 digit sequences from OCR text
     */
    fun extractCardNumbersFromOcrText(text: String): List<String> {
        val results = mutableListOf<String>()
        // Match numbers grouped with spaces, dashes or contiguous
        val regex = Regex("""(?:\d[ -]*?){13,19}""")
        val matches = regex.findAll(text)
        for (match in matches) {
            val cleaned = cleanCardNumber(match.value)
            if (cleaned.length in 13..19) {
                results.add(cleaned)
            }
        }
        return results.distinct()
    }
}
