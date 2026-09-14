sed -i '370,376c\
    fun getMiddleNames(country: String, gender: String = "Unisex"): List<String> {\
         return when(country) {\
            "Bangladesh", "Pakistan" -> when (gender) {\
                "Male" -> listOf("Hasan", "Ahmed", "Ali", "Uddin", "Arlo", "Hossain")\
                "Female" -> listOf("Khatun", "Begum", "Akter", "Jahan", "Banu")\
                else -> listOf("Hasan", "Ahmed", "Khatun", "Begum", "Arlo")\
            }\
            "India" -> when (gender) {\
                "Male" -> listOf("Raj", "Kumar", "Singh", "Prasad")\
                "Female" -> listOf("Kumari", "Devi", "Kaur", "Bai")\
                else -> listOf("Raj", "Kumar", "Kumari", "Devi")\
            }\
            else -> when (gender) {\
                "Male" -> listOf("James", "John", "Robert", "William", "Arlo", "Lee", "Paul")\
                "Female" -> listOf("Marie", "Ann", "Lynn", "Rose", "Grace", "Jane", "May")\
                else -> Datasets.middleNames\
            }\
        }\
    }' app/src/main/java/com/example/utils/Datasets.kt
