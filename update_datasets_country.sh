cat << 'INNER_EOF' >> app/src/main/java/com/example/utils/Datasets.kt

object CountryData {
    val countries = listOf(
        "International", "Bangladesh", "India", "Pakistan", "United States", 
        "United Kingdom", "Canada", "Australia", "France", "Germany", 
        "Italy", "Spain", "Brazil", "Mexico", "Turkey", "Saudi Arabia", 
        "UAE", "Indonesia", "Malaysia", "Japan", "South Korea", "China", 
        "Russia", "Nigeria", "South Africa"
    )
    
    // Fake localized mapping for simplicity, in a real app this would be extensive
    fun getMaleNames(country: String): List<String> {
        return when(country) {
            "Bangladesh" -> listOf("Zayen", "Ayaan", "Rahim", "Tariq", "Hasan")
            "India" -> listOf("Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun")
            "Pakistan" -> listOf("Ali", "Hamza", "Bilal", "Usman", "Omar")
            "Japan" -> listOf("Haruto", "Yuto", "Sota", "Yuki", "Ren")
            "Germany" -> listOf("Leon", "Paul", "Jonas", "Elias", "Ben")
            else -> Datasets.maleNames
        }
    }
    
    fun getFemaleNames(country: String): List<String> {
        return when(country) {
            "Bangladesh" -> listOf("Aisha", "Fatima", "Sara", "Nusrat", "Sadia")
            "India" -> listOf("Saanvi", "Aanya", "Aadhya", "Kiara", "Diya")
            "Pakistan" -> listOf("Zara", "Hira", "Sana", "Mariam", "Rabia")
            "Japan" -> listOf("Hina", "Yui", "Sakura", "Rin", "Aoi")
            "Germany" -> listOf("Mia", "Emma", "Hannah", "Sofia", "Anna")
            else -> Datasets.femaleNames
        }
    }
    
    fun getUnisexNames(country: String): List<String> {
        return when(country) {
            "Bangladesh", "Pakistan" -> listOf("Noor", "Kiran", "Nasim", "Anwar")
            "India" -> listOf("Krishna", "Arya", "Kiran")
            "Japan" -> listOf("Akira", "Hinata", "Makoto", "Ren")
            else -> Datasets.unisexNames
        }
    }
    
    fun getLastNames(country: String): List<String> {
        return when(country) {
            "Bangladesh" -> listOf("Rahman", "Islam", "Hossain", "Ahmed", "Chowdhury")
            "India" -> listOf("Patel", "Singh", "Kumar", "Sharma", "Gupta")
            "Pakistan" -> listOf("Khan", "Syed", "Shah", "Malik", "Ali")
            "Japan" -> listOf("Sato", "Suzuki", "Takahashi", "Tanaka", "Watanabe")
            "Germany" -> listOf("Müller", "Schmidt", "Schneider", "Fischer", "Weber")
            else -> Datasets.surnames
        }
    }
    
    fun getMiddleNames(country: String): List<String> {
         return when(country) {
            "Bangladesh", "Pakistan" -> listOf("Hasan", "Ahmed", "Ali", "Uddin")
            "India" -> listOf("Raj", "Kumar", "Singh")
            else -> Datasets.middleNames
        }
    }
}
INNER_EOF
bash update_datasets_country.sh