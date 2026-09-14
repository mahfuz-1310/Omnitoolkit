package com.example.utils

object Datasets {
    fun cleanList(list: List<String>): List<String> {
        val cleaned = mutableSetOf<String>()
        val seenLower = mutableSetOf<String>()
        for (item in list) {
            val trimmed = item.trim().replace(Regex("\\s+"), " ")
            if (trimmed.isNotBlank()) {
                val lower = trimmed.lowercase()
                if (!seenLower.contains(lower)) {
                    seenLower.add(lower)
                    cleaned.add(trimmed)
                }
            }
        }
        return cleaned.toList()
    }

    val bioTemplates = mapOf(
        "Instagram" to listOf(
            "Just another dreamer ☁️",
            "Living my best life ✨",
            "Creating my own sunshine ☀️",
            "Wanderlust and city dust 🌆",
            "Making memories all over the world 🌍",
            "Less perfection, more authenticity 💫",
            "Doing what I love 🤍",
            "Life is short, make it sweet 🍭",
            "Chasing goals, catching flights ✈️",
            "Always hungry for success... and pizza 🍕"
        ),
        "TikTok" to listOf(
            "Welcome to my crazy life 🤪",
            "Just here for the vibes ✨",
            "I make videos sometimes 🎬",
            "Don't take me seriously 🤡",
            "Spreading positivity 🌈",
            "POV: You found my profile 👀",
            "Main character energy 💅",
            "I'm cooler on the internet 🧊",
            "Trying to go viral 📈",
            "Coffee addict ☕"
        ),
        "Gaming" to listOf(
            "Eat. Sleep. Game. Repeat. 🎮",
            "Leveling up in real life ⬆️",
            "Born to play 👾",
            "AFK in real life 💤",
            "My KD is higher than your GPA 📈",
            "Just one more game... 🕹️",
            "Console peasant / PC master race 💻",
            "Respawning... 🔄",
            "I don't need therapy, I need to game 🎧",
            "Always on that grind ⚔️"
        ),
        "Professional" to listOf(
            "Passionate about technology and design 💻",
            "Helping brands grow and succeed 📈",
            "Creative thinker, problem solver 💡",
            "Building the future, one line of code at a time 🚀",
            "Digital marketing enthusiast 📱",
            "Turning coffee into ideas ☕",
            "Innovating and inspiring 🌟",
            "Dedicated to excellence and continuous learning 📚",
            "Connecting the dots in a digital world 🌐",
            "Let's create something amazing together 🤝"
        ),
        "Aesthetic" to listOf(
            "Lost in a world of my own 🌙",
            "Finding beauty in the little things 🌸",
            "Romanticizing my life 🕯️",
            "Soft heart, strong mind ☁️",
            "Collecting moments, not things 📸",
            "Living in slow motion 🌿",
            "Stargazer ✨",
            "Art is how we decorate space, music is how we decorate time 🎨",
            "In love with the moon 🌕",
            "Just a soul with a body 🕊️"
        ),
        "Funny" to listOf(
            "I put the 'elusive' in influencer 🥷",
            "Professional overthinker 🧠",
            "I'm not lazy, I'm on energy-saving mode 🔋",
            "My life is basically a series of typos ⌨️",
            "I'm here for the snacks 🥨",
            "I whisper to my WiFi to be faster 🤫",
            "Error 404: Bio not found 🚫",
            "I need a 6-month vacation, twice a year 🌴",
            "I'm actually a dinosaur 🦖",
            "Sarcasm is my love language 💬"
        )
    )

    val nicknames = cleanList(listOf(
        "Ace", "Alpha", "Apollo", "Astro", "Atlas", "Bambi", "Bandit", "Bear", "Beast", "Bingo",
        "Blade", "Blaze", "Blue", "Boomer", "Boss", "Breeze", "Bubba", "Buddy", "Bug", "Bullet",
        "Buster", "Cash", "Champ", "Chaos", "Charm", "Chase", "Chief", "Chip", "Cobra", "Comet",
        "Cookie", "Crash", "Cupcake", "Dash", "Demon", "Diesel", "Dino", "Doc", "Dodge", "Duke",
        "Dusty", "Eagle", "Echo", "Falcon", "Fang", "Flame", "Flash", "Foxy", "Frost", "Ghost",
        "Glitch", "Goku", "Gonzo", "Goose", "Grizzly", "Gunner", "Guru", "Halo", "Hawk", "Hero",
        "Honey", "Hulk", "Hunter", "Ice", "Iron", "Jackal", "Jaws", "Jazz", "Jedi", "Joker",
        "Judge", "Juice", "Jumbo", "Kilo", "King", "Kong", "Laser", "Legend", "Lex", "Lion",
        "Lock", "Loki", "Mac", "Magic", "Major", "Mako", "Matrix", "Max", "Maya", "Mercy",
        "Midnight", "Mighty", "Ninja", "Nova", "Odin", "Omega", "Onyx", "Oreo", "Panda", "Panther",
        "Peanut", "Phantom", "Phoenix", "Pikachu", "Pilot", "Pixel", "Precious", "Prince", "Princess", "Puma"
    ))

    val adjectives = cleanList(listOf(
        "Awesome", "Brave", "Cool", "Daring", "Epic", "Fierce", "Great", "Happy", "Iron", "Jolly",
        "Keen", "Loyal", "Mighty", "Noble", "Omega", "Proud", "Quick", "Rapid", "Super", "True",
        "Ultra", "Vast", "Wild", "Xenon", "Young", "Zealous", "Alpha", "Badass", "Crazy", "Dark",
        "Elite", "Funky", "Godly", "Hyper", "Ill", "Juicy", "Killer", "Lethal", "Mega", "Ninja"
    ))

    val nouns = cleanList(listOf(
        "Ninja", "Sniper", "Gamer", "Player", "Master", "King", "Queen", "Lord", "Beast", "Dragon",
        "Tiger", "Wolf", "Bear", "Lion", "Eagle", "Hawk", "Shark", "Snake", "Viper", "Ghost",
        "Phantom", "Shadow", "Soul", "Spirit", "Demon", "Angel", "God", "Devil", "Titan", "Giant",
        "Warrior", "Fighter", "Knight", "Rider", "Hunter", "Killer", "Assassin", "Thief", "Rogue", "Mage"
    ))

    val aestheticWords = cleanList(listOf(
        "Aura", "Breeze", "Cloud", "Dawn", "Echo", "Fairy", "Glow", "Halo", "Iris", "Jade",
        "Kite", "Lace", "Moon", "Nova", "Opal", "Pearl", "Quartz", "Rain", "Star", "Teal",
        "Umbra", "Vibe", "Wave", "XRay", "Yarn", "Zen", "Angel", "Blush", "Cosmic", "Dream",
        "Ether", "Flora", "Glaze", "Haze", "Ink", "Jewel", "Karma", "Lush", "Mist", "Neon"
    ))

    val gamingWords = cleanList(listOf(
        "Frag", "Noob", "Pro", "Bot", "Ping", "Lag", "Rush", "Camp", "Loot", "Buff",
        "Nerf", "Aggro", "DPS", "Tank", "Heal", "Mana", "HP", "XP", "Level", "Rank",
        "Glitch", "Hack", "Mod", "Skin", "Drop", "Spawn", "Wipe", "Clutch", "Ace", "Carry",
        "Toxic", "Salty", "Sweat", "Tryhard", "Smurf", "Main", "Meta", "OP", "AFK", "GG"
    ))

    val royalWords = cleanList(listOf(
        "King", "Queen", "Prince", "Princess", "Duke", "Duchess", "Lord", "Lady", "Baron", "Knight",
        "Crown", "Throne", "Realm", "Empire", "Majesty", "Royal", "Noble", "Imperial", "Regal", "Sovereign"
    ))

    fun getMeaning(name: String): String {
        return when (kotlin.math.abs(name.hashCode()) % 5) {
            0 -> "Bringer of joy and happiness"
            1 -> "Strong, brave, and courageous"
            2 -> "Wise, intelligent, and thoughtful"
            3 -> "Beautiful, graceful, and elegant"
            4 -> "Kind, gentle, and compassionate"
            else -> "A unique and special soul"
        }
    }

    val personalities = listOf(
        "Creative & Artistic", "Analytical & Logical", "Adventurous & Bold", "Calm & Collected",
        "Enthusiastic & Energetic", "Thoughtful & Empathetic", "Ambitious & Driven", "Witty & Humorous"
    )

    val interests = listOf(
        "Coding & Tech", "Music Production", "Digital Art", "Gaming", "Photography",
        "Traveling", "Reading", "Fitness & Gym", "Cooking", "Writing", "Cryptocurrency",
        "Philosophy", "Astronomy", "Design", "Film & Cinema", "Fashion"
    )
}

object CountryData {
    val countries = listOf(
        "International", "Bangladesh", "India", "Pakistan", "United States", 
        "United Kingdom", "Canada", "Australia", "France", "Germany", 
        "Italy", "Spain", "Portugal", "Brazil", "Mexico", "Turkey", "Saudi Arabia", 
        "UAE", "Indonesia", "Malaysia", "Japan", "South Korea", "China", 
        "Russia", "Nigeria", "South Africa"
    )

    private val masterMale = Datasets.cleanList(listOf(
        "Aarav", "Abram", "Adrian", "Aiden", "Alex", "Alexander", "Ali", "Amir", "Anders", "Andrew",
        "Arjun", "Arman", "Arthur", "Asher", "Ayan", "Aydin", "Ben", "Benjamin", "Blake", "Bodhi",
        "Caleb", "Callum", "Cameron", "Carter", "Charles", "Chase", "Christian", "Christopher", "Cole", "Connor",
        "Daniel", "David", "Declan", "Diego", "Dominic", "Dylan", "Easton", "Edward", "Eli", "Elias",
        "Elijah", "Elliot", "Emerson", "Emmanuel", "Enzo", "Ethan", "Evan", "Everett", "Ezra", "Felix",
        "Finn", "Gabriel", "Gael", "Gavin", "George", "Gideon", "Giovanni", "Graham", "Grayson", "Griffin",
        "Harrison", "Harry", "Hayes", "Henry", "Hudson", "Hugh", "Hugo", "Hunter", "Ian", "Ibrahim",
        "Isaac", "Isaiah", "Ivan", "Jack", "Jackson", "Jacob", "James", "Jameson", "Jared", "Jasper",
        "Jaxon", "Jayden", "John", "Jonah", "Jonathan", "Jordan", "Joseph", "Joshua", "Josiah", "Jude",
        "Julian", "Kai", "Kaleb", "Kamil", "Karter", "Kayden", "Kenneth", "Kevin", "Khalil", "Kian",
        "Kieran", "Kingston", "Knox", "Kyler", "Landon", "Leo", "Leon", "Leonardo", "Levi", "Liam",
        "Lincoln", "Logan", "Luca", "Lucas", "Lucca", "Luke", "Mac", "Maddox", "Malachi", "Malik",
        "Marcus", "Mason", "Mateo", "Matthew", "Maverick", "Max", "Maxwell", "Micah", "Michael", "Milan",
        "Miles", "Milo", "Muhammad", "Nabil", "Nash", "Nathan", "Nathaniel", "Nico", "Nicolas", "Noah",
        "Nolan", "Oliver", "Omar", "Orion", "Oscar", "Owen", "Parker", "Patrick", "Paul", "Peter",
        "Preston", "Prince", "Quinn", "Rafael", "Raiden", "Ray", "Rayan", "Reid", "Remington", "Remy",
        "Rex", "Rhett", "Richard", "Riley", "River", "Robert", "Roman", "Rowan", "Ryder", "Ryker",
        "Rylan", "Ryland", "Said", "Sam", "Samuel", "Santiago", "Sawyer", "Sebastian", "Seth", "Silas",
        "Simon", "Solomon", "Soren", "Spencer", "Stanley", "Stephen", "Sterling", "Sullivan", "Tariq", "Tate",
        "Theo", "Theodore", "Thomas", "Timothy", "Tobias", "Tristan", "Tucker", "Tyler", "Umar", "Usman",
        "Victor", "Vincent", "Walter", "Waylon", "Wesley", "Weston", "William", "Winston", "Wyatt", "Xander",
        "Xavier", "Yusuf", "Zachary", "Zaid", "Zain", "Zander", "Zane", "Zayden", "Zayn", "Zion"
    ))

    private val masterFemale = Datasets.cleanList(listOf(
        "Aaliyah", "Abigail", "Ada", "Adaline", "Adalyn", "Addison", "Adeline", "Aisha", "Alana", "Alani",
        "Alanna", "Alayna", "Aleena", "Alessia", "Alexa", "Alexandra", "Alia", "Alice", "Alicia", "Alina",
        "Alisha", "Alison", "Alivia", "Aliyah", "Aliza", "Allie", "Allison", "Alyson", "Alyssa", "Amalia",
        "Amanda", "Amani", "Amara", "Amari", "Amaya", "Amber", "Amelia", "Amelie", "Amina", "Amira",
        "Amiyah", "Amy", "Ana", "Anabia", "Anahi", "Anais", "Analia", "Anastasia", "Andi", "Andrea",
        "Angel", "Angela", "Angelica", "Angelina", "Angie", "Anika", "Aniyah", "Anna", "Annabel", "Annabella",
        "Annabelle", "Annalise", "Anne", "Annie", "Annika", "Anya", "April", "Arabella", "Aria", "Ariah",
        "Ariana", "Arianna", "Ariel", "Ariella", "Ariya", "Ariyah", "Arya", "Ashley", "Aspen", "Astrid",
        "Athena", "Aubree", "Aubrey", "Aubrie", "Audrey", "August", "Aura", "Aurelia", "Aurora", "Autumn",
        "Ava", "Avah", "Avalynn", "Avery", "Aviana", "Avianna", "Aya", "Ayla", "Belen", "Bella",
        "Bellamy", "Bexley", "Bianca", "Blair", "Blake", "Blakely", "Bonnie", "Braelynn", "Briana", "Brianna",
        "Briar", "Bridget", "Briella", "Brielle", "Bristol", "Brittany", "Brooke", "Brooklyn", "Brooklynn", "Brynn",
        "Cadence", "Callie", "Camden", "Cameron", "Camila", "Camilla", "Camille", "Carly", "Carmen", "Carolina",
        "Caroline", "Carter", "Cassandra", "Cassidy", "Cataleya", "Catalina", "Catherine", "Cecelia", "Cecilia", "Celeste",
        "Celia", "Celine", "Chana", "Chandler", "Chanel", "Charlee", "Charleigh", "Charley", "Charli", "Charlie",
        "Charlotte", "Chaya", "Chelsea", "Cheyenne", "Chloe", "Christina", "Claire", "Clara", "Clare", "Clarissa",
        "Clementine", "Cleo", "Clover", "Colette", "Collins", "Cora", "Coraline", "Cordelia", "Corinne", "Crystal",
        "Cynthia", "Dahlia", "Daisy", "Dakota", "Daleyza", "Dallas", "Dani", "Daniela", "Daniella", "Danna",
        "Daphne", "Davina", "Dayana", "Delaney", "Delilah", "Demi", "Destiny", "Diana", "Dior", "Dorothy",
        "Dream", "Drew", "Dulce", "Dylan", "Eden", "Edith", "Egypt", "Eileen", "Elaina", "Elaine",
        "Eleanor", "Elena", "Eliana", "Elianna", "Elina", "Elisa", "Elisabeth", "Elise", "Eliza", "Elizabeth",
        "Ella", "Elle", "Ellen", "Elliana", "Ellie", "Elliot", "Elliott", "Ellis", "Ellison", "Elodie",
        "Eloise", "Elora", "Elsa", "Elsie", "Ember", "Emberly", "Emelia", "Emely", "Emerald", "Emerie",
        "Emerson", "Emersyn", "Emery", "Emilee", "Emilia", "Emily", "Emma", "Emmaline", "Emmalynn", "Emme",
        "Emmie", "Emmy", "Emory", "Ensley", "Erin", "Esme", "Esmeralda", "Estella", "Estelle", "Esther",
        "Etta", "Eva", "Evangeline", "Eve", "Evelyn", "Evelynn", "Everlee", "Everleigh", "Everly", "Evie",
        "Faith", "Fallon", "Fatima", "Faye", "Felicity", "Fernanda", "Finley", "Fiona", "Flora", "Florence",
        "Frances", "Francesca", "Frankie", "Freya", "Frida", "Gabriela", "Gabriella", "Gabrielle", "Galia",
        "Galilea", "Gemma", "Genesis", "Genevieve", "Georgia", "Gia", "Giana", "Gianna", "Giavanna", "Gigi",
        "Giovanna", "Giselle", "Grace", "Gracelyn", "Gracie", "Greta", "Guinevere", "Gwen", "Gwendolyn", "Hadassah"
    ))

    private val masterUnisex = Datasets.cleanList(listOf(
        "Aiden", "Alex", "Ali", "Amari", "Angel", "Ariel", "Armani", "Arya", "Ashton", "Aspen",
        "August", "Avery", "Azariah", "Bailey", "Baker", "Baylor", "Bellamy", "Blake", "Blakely", "Bowie",
        "Briar", "Brooke", "Brooklyn", "Cameron", "Campbell", "Carmelo", "Carter", "Casey", "Cassidy", "Chandler",
        "Charlie", "Chase", "Corey", "Dakota", "Dallas", "Dani", "Darian", "Dawson", "Denver", "Devon",
        "Drew", "Dylan", "Eden", "Elliott", "Ellis", "Emerson", "Emery", "Emory", "Erin", "Ezra",
        "Finley", "Frankie", "Gael", "Genesis", "Gianni", "Grayson", "Grey", "Harley", "Harlow", "Harper",
        "Haven", "Hayden", "Hunter", "Indigo", "Ira", "Ivory", "Jaden", "Jamie", "Jay", "Jayden",
        "Jaylen", "Jesse", "Jody", "Jordan", "Journey", "Jude", "Justice", "Kaden", "Kai", "Kamryn",
        "Keegan", "Kelly", "Kendall", "Kennedy", "Kerry", "Kieran", "Kinley", "Kye", "Kylan", "Lake",
        "Landry", "Lane", "Lee", "Leighton", "Lennon", "Lennox", "Leslie", "Lex", "Lincoln", "Linden",
        "Logan", "London", "Lou", "Luca", "Lucian", "Lyric", "Mackenzie", "Maddox", "Marley", "Mason",
        "Max", "Maxwell", "Micah", "Milan", "Miles", "Miller", "Monroe", "Montana", "Morgan", "Murphy",
        "Nash", "Nevaeh", "Nico", "Noah", "Noel", "Nolan", "Nova", "Oakley", "Ocean", "Onyx",
        "Owen", "Paris", "Parker", "Pat", "Payton", "Peyton", "Phoenix", "Piper", "Presley", "Quinn",
        "Ray", "Rayne", "Reagan", "Reese", "Remy", "Ren", "Rey", "Riley", "River", "Robin",
        "Rory", "Rowan", "Royal", "Rylan", "Sage", "Sailor", "Salem", "Sam", "Sasha", "Sawyer",
        "Scout", "Shawn", "Shay", "Shiloh", "Sidney", "Skylar", "Skyler", "Sloan", "Sloane", "Spencer",
        "Sterling", "Stevie", "Sutton", "Sydney", "Tanner", "Tate", "Tatum", "Taylor", "Teagan", "Terry",
        "Tobias", "Tory", "Trace", "Trinity", "Tristan", "Tyler", "Umber", "Val", "Waverly", "West",
        "Winter", "Wren", "Wyatt", "Yael", "Zuri"
    ))

    private val masterSurnames = Datasets.cleanList(listOf(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Hernandez", "Lopez", "Gonzales", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
        "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
        "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
        "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts",
        "Gomez", "Phillips", "Evans", "Turner", "Diaz", "Parker", "Cruz", "Edwards", "Collins", "Reyes",
        "Stewart", "Morris", "Morales", "Murphy", "Cook", "Rogers", "Gutierrez", "Ortiz", "Morgan", "Cooper",
        "Peterson", "Bailey", "Reed", "Kelly", "Howard", "Ramos", "Kim", "Cox", "Ward", "Richardson",
        "Watson", "Brooks", "Chavez", "Wood", "James", "Bennett", "Gray", "Mendoza", "Ruiz", "Hughes",
        "Price", "Alvarez", "Castillo", "Sanders", "Patel", "Myers", "Long", "Ross", "Foster", "Jimenez"
    ))

    private val masterMiddle = Datasets.cleanList(listOf(
        "James", "Lee", "Marie", "Grace", "Lynn", "Rose", "Ann", "Elizabeth", "Thomas", "William", 
        "Alexander", "Joseph", "Michael", "David", "John", "Charles", "Edward", "George", "Henry", "Arthur", 
        "Louis", "Paul", "Peter", "Richard", "Robert", "Stephen", "Jane", "Mary", "Alice", "Catherine", 
        "Margaret", "Anne", "Claire", "Louise", "Renee", "Nicole", "Michelle", "Dawn", "Ray", "Alan"
    ))

    // Country-specific datasets
    private val countryMap = mapOf(
        "Bangladesh" to CountryDetails(
            male = Datasets.cleanList(listOf("Aarav", "Abeer", "Ahsan", "Akash", "Aman", "Anik", "Arafat", "Arif", "Arnab", "Aryan", "Ashiq", "Asif", "Ayan", "Azmain", "Fahad", "Faisal", "Farhan", "Hasan", "Hasib", "Ibrahim", "Imran", "Imtiaz", "Jahid", "Jamil", "Jihad", "Kabir", "Mahbub", "Mahdi", "Mahfuz", "Mahmud", "Mamun", "Maruf", "Masud", "Mehedi", "Minhaz", "Mizan", "Monir", "Nabil", "Nahid", "Naim", "Najmul", "Nasim", "Nazim", "Nazmul", "Niaz", "Nihal", "Niloy", "Noman", "Noor", "Parvez", "Pavel", "Rafi", "Rafiq", "Rahat", "Rahim", "Rahman", "Raihan", "Rajib", "Rakib", "Ratul", "Rayan", "Rayhan", "Reza", "Rifat", "Riyad", "Rohan", "Rubel", "Sabbir", "Sadat", "Sadi", "Sadik", "Safwan", "Sajib", "Sajid", "Salman", "Sami", "Samir", "Saqib", "Shafi", "Shahriar", "Shakil", "Shamim", "Shanto", "Shawon", "Sohel", "Sumon", "Tanvir", "Tariq", "Taufiq", "Touhid", "Zayed", "Zayen", "Zishan")),
            female = Datasets.cleanList(listOf("Aafia", "Aaliyah", "Aamina", "Adiba", "Adila", "Afsana", "Afshin", "Aisha", "Alifa", "Alina", "Aliya", "Amira", "Anan", "Ananya", "Anika", "Anila", "Anjum", "Anusha", "Aqila", "Ariba", "Arifa", "Arisha", "Arpita", "Asha", "Asma", "Atia", "Ayesha", "Aziza", "Barsha", "Bela", "Bithi", "Brishti", "Bushra", "Chaity", "Dalia", "Dania", "Deepa", "Dina", "Diya", "Ema", "Esha", "Eva", "Fabiha", "Fadia", "Fahmida", "Faiza", "Fariha", "Farjana", "Farzana", "Fatima", "Fawzia", "Fayza", "Ferdous", "Firoza", "Fiza", "Habiba", "Hafsa", "Halima", "Hana", "Hania", "Hasina", "Hena", "Hera", "Hiba", "Hima", "Hira", "Hoor", "Humaira", "Iffat", "Inaya", "Iqra", "Irin", "Ishita", "Ishrat", "Ismat", "Ivy", "Jahan", "Jahanara", "Jamila", "Jannat", "Jarin", "Jasmin", "Jeba", "Jesmin", "Jinia", "Joya", "Juhi", "Jui", "Julekha", "Kajal", "Kakoli", "Kalpana", "Kaniz", "Karima", "Kashfia", "Khadija", "Khatun", "Keya", "Kiran", "Koly", "Lamia", "Lamisa", "Latifa", "Lia", "Liza", "Lopa", "Lubna", "Luna", "Madiha", "Mahia", "Maliha", "Marium", "Mehnaz", "Mihika", "Mitu", "Momena", "Moumita", "Moushumi", "Mst", "Munira", "Muskan", "Nabila", "Nadia", "Nafisa", "Naila", "Naima", "Najia", "Namira", "Nanziba", "Nasrin", "Nastaran", "Natasha", "Nazifa", "Naznin", "Nabila", "Nigar", "Nila", "Nilufa", "Nisha", "Nishat", "Noshin", "Novera", "Nusrat", "Orpi", "Popy", "Priya", "Puja", "Rabeya", "Radiya", "Rafia", "Rahima", "Raila", "Raisa", "Raka", "Rakhi", "Rania", "Rashida", "Rawnak", "Razia", "Rebeka", "Reema", "Reshma", "Rifa", "Rimi", "Ripa", "Ritu", "Riya", "Roksana", "Ruma", "Rumana", "Ruquaiya", "Sabah", "Sabiha", "Sabina", "Sabrina", "Sadia", "Safa", "Safia", "Saima", "Salsabil", "Salsi", "Salma", "Samia", "Samira", "Sanjida", "Sanya", "Sara", "Sarika", "Sayma", "Sejuti", "Selina", "Shaila", "Shahnaz", "Shaila", "Shakira", "Shamima", "Sharmin", "Shefa", "Sheuly", "Shifa", "Shirin", "Shohag", "Shormi", "Shrabonti", "Shreya", "Shuchi", "Sifat", "Simin", "Sinthia", "Sirajum", "Smriti", "Sohana", "Sonia", "Subha", "Subrina", "Suchi", "Sultana", "Sumaiya", "Sumita", "Suraiya", "Susmita", "Swarnali", "Tahia", "Tahmina", "Tahsin", "Tania", "Tanjila", "Tanmya", "Tanzila", "Tasfia", "Tasmia", "Tasnia", "Tasnim", "Tasnova", "Tazrin", "Tehreem", "Tina", "Tithi", "Trisha", "Umme", "Urmila", "Warda", "Zahra", "Zainab", "Zakia", "Zarin", "Zeba", "Zenia", "Zerin", "Zohra")),
            unisex = Datasets.cleanList(listOf("Noor", "Kiran", "Nasim", "Anwar", "Amin", "Rayan", "Sami", "Sakin")),
            middle = Datasets.cleanList(listOf("Hasan", "Ahmed", "Ali", "Uddin", "Hossain", "Rahman", "Islam", "Chowdhury", "Akter", "Khatun", "Begum", "Jahan", "Banu", "Ara", "Sultana")),
            last = Datasets.cleanList(listOf("Rahman", "Islam", "Hossain", "Ahmed", "Chowdhury", "Talukder", "Sarker", "Uddin", "Khan", "Ali", "Mia", "Talukdar", "Bhuiyan", "Miah", "Siddique", "Akther", "Begum", "Khatun", "Haque", "Karim", "Mollah", "Biswas", "Roy", "Das", "Sen", "Paul", "Barua"))
        ),
        "India" to CountryDetails(
            male = Datasets.cleanList(listOf("Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun", "Sai", "Reyansh", "Ayaan", "Krishna", "Ishaan", "Dhruv", "Kian", "Kabir", "Rudra", "Advik", "Darsh", "Ahaan", "Ryaan", "Veer", "Pranav", "Karthik", "Rohan", "Siddharth", "Shivam", "Aayush", "Ayush", "Dev", "Yash", "Raghav", "Ansh", "Abhinav", "Aryan", "Atharv", "Harsh", "Kunal", "Manav", "Nakul", "Om", "Parth", "Rahul", "Sahil", "Samarth", "Tanmay", "Vedant", "Abhi", "Akash", "Amit", "Aniket", "Ankit", "Anurag", "Ashish", "Avinash", "Bharat", "Chetan", "Deepak", "Dinesh", "Gaurav", "Girish", "Hemant", "Jatin", "Kamal", "Karan", "Lokesh", "Manoj", "Mayur", "Mohit", "Mukesh", "Naveen", "Nikhil", "Nitin", "Pankaj", "Piyush", "Prakash", "Pradeep", "Pramod", "Pratik", "Praveen", "Rajesh", "Rakesh", "Ramesh", "Ranjit", "Ravi", "Sachin", "Sandeep", "Sanjay", "Saurabh", "Shailesh", "Shashank", "Shubham", "Sunil", "Suraj", "Suresh", "Tarun", "Tushar", "Varun", "Vikas", "Vinay", "Vineet", "Vipan", "Vishal", "Vivek")),
            female = Datasets.cleanList(listOf("Saanvi", "Aanya", "Aadhya", "Kiara", "Diya", "Pari", "Ananya", "Rhea", "Pihu", "Avni", "Ahana", "Navya", "Meera", "Myra", "Ridhima", "Shanaya", "Trisha", "Anika", "Kavya", "Ishita", "Anvi", "Prisha", "Riya", "Sneha", "Tanvi", "Aditi", "Akshara", "Amaya", "Amrita", "Ananya", "Anushka", "Archana", "Bhavna", "Deepika", "Disha", "Divya", "Geeta", "Harshita", "Isha", "Jaya", "Jyoti", "Kajal", "Kavita", "Komal", "Kriti", "Lakshmi", "Madhuri", "Mahi", "Mamta", "Manju", "Meenakshi", "Monika", "Namrata", "Neha", "Nidhi", "Nikita", "Nisha", "Pallavi", "Pooja", " Poonam", "Pragati", "Priyanka", "Radha", "Rajni", "Rashmi", "Rekha", "Ritu", "Ruchi", "Rupal", "Sakshi", "Sangeeta", "Sarita", "Seema", "Shalini", "Shikha", "Shivani", "Shruti", "Simran", "Smita", "Sonal", "Sonam", "Suman", "Sunita", "Swati", "Tanvi", "Trupti", "Usha", "Vaishali", "Vandana", "Varsha", "Vidya", "Yamini")),
            unisex = Datasets.cleanList(listOf("Krishna", "Arya", "Kiran", "Samir", "Amrit", "Jasmin", "Devi", "Prasad")),
            middle = Datasets.cleanList(listOf("Raj", "Kumar", "Singh", "Prasad", "Devi", "Kumari", "Kaur", "Bai", "Lal", "Nath", "Chandra")),
            last = Datasets.cleanList(listOf("Patel", "Singh", "Kumar", "Sharma", "Gupta", "Verma", "Reddy", "Mehta", "Joshi", "Choudhury", "Das", "Bose", "Sen", "Iyer", "Nair", "Rao", "Pillai", "Menon", "Deshmukh", "Patil", "Shinde", "Kulkarni", "Desai", "Chavan", "Jadhav", "Bansal", "Aggarwal", "Mittal", "Goyal", "Singhal", "Saxena", "Tiwari", "Mishra", "Pandey", "Dubey", "Shukla", "Tripathi", "Upadhyay", "Thakur", "Chauhan", "Rana", "Yadav", "Verma", "Sinha", "Jha", "Choudhary"))
        ),
        "Pakistan" to CountryDetails(
            male = Datasets.cleanList(listOf("Ali", "Hamza", "Bilal", "Usman", "Omar", "Hassan", "Hussain", "Abdullah", "Ahmed", "Zayn", "Zain", "Fahad", "Faisal", "Farhan", "Haris", "Ibrahim", "Imran", "Junaid", "Kamran", "Kashif", "Khurram", "Nabeel", "Nadir", "Nasir", "Noman", "Qasim", "Rashid", "Rehan", "Saad", "Salman", "Samir", "Shahbaz", "Shahid", "Shakeel", "Shoaib", "Sohail", "Sufyan", "Sultan", "Talha", "Tariq", "Touseef", "Umar", "Wajahat", "Waqas", "Waseem", "Yasir", "Zafar", "Zahid", "Zeeshan")),
            female = Datasets.cleanList(listOf("Zara", "Hira", "Sana", "Mariam", "Rabia", "Ayesha", "Fatima", "Zainab", "Khadija", "Amna", "Anum", "Aqsa", "Areeba", "Asma", "Bushra", "Dania", "Erum", "Fauzia", "Fiza", "Ghazal", "Gul", "Hafsa", "Haleema", "Hania", "Hina", "Humaira", "Iffat", "Iqra", "Iram", "Kanwal", "Kiran", "Mahnoor", "Maliha", "Mehwish", "Momena", "Munazza", "Nabila", "Nadia", "Naila", "Neelam", "Nida", "Nosheen", "Palwasha", "Quratulain", "Rida", "Sadia", "Safia", "Saima", "Saira", "Salma")),
            unisex = Datasets.cleanList(listOf("Noor", "Kiran", "Nasim", "Anwar", "Zeeshan", "Daniyal")),
            middle = Datasets.cleanList(listOf("Hasan", "Ahmed", "Ali", "Uddin", "Arlo", "Hossain", "Khan", "Shah", "Malik")),
            last = Datasets.cleanList(listOf("Khan", "Syed", "Shah", "Malik", "Ali", "Ahmed", "Butt", "Chaudhry", "Sheikh", "Qureshi", "Ansari", "Mirza", "Awan", "Rajput", "Mughal", "Baloch", "Pathan", "Siddiqui", "Farooqi", "Hashmi"))
        ),
        "United States" to CountryDetails(
            male = masterMale,
            female = masterFemale,
            unisex = masterUnisex,
            middle = masterMiddle,
            last = masterSurnames
        ),
        "United Kingdom" to CountryDetails(
            male = Datasets.cleanList(listOf("Oliver", "George", "Arthur", "Noah", "Muhammad", "Leo", "Oscar", "Harry", "Archie", "Jack", "Henry", "Charlie", "Freddie", "Alfie", "Lucas", "Thomas", "Mason", "Logan", "Teddy", "Theodore", "Alexander", "Arlo", "Elijah", "William", "James", "Benjamin", "Lucas", "Daniel", "Logan", "Sebastian", "Jackson", "Aiden", "Matthew", "Samuel", "David", "Joseph", "Carter", "Owen", "Wyatt", "John", "Luke", "Gabriel", "Anthony", "Isaac", "Grayson", "Julian", "Levi", "Aria", "Lincoln", "Jaxon", "Mateo", "Maverick", "Josiah", "Isaiah", "Charles", "Caleb", "Enzo", "Miles", "Theodore", "Nathan", "Adrian", "Cameron", "Santiago", "Eli", "Aaron", "Christian", "Landon", "Connor", "Hunter", "Ezra", "Easton", "Colton", "Jordan", "Angel", "Brayden", "Asher", "Dominic", "Austin", "Ian", "Adam", "Elias", "Jaxson", "Greyson", "Jose", "Ezekiel", "Carson", "Evan", "Maverick", "Bryson", "Jace", "Cooper", "Xavier", "Carson", "Leonardo", "Easton", "Ezra", "Sawyer")),
            female = Datasets.cleanList(listOf("Olivia", "Amelia", "Isla", "Ava", "Ivy", "Freya", "Lily", "Florence", "Mia", "Willow", "Harper", "Evelyn", "Phoebe", "Violet", "Elsie", "Emily", "Sofia", "Grace", "Isabella", "Ella", "Charlotte", "Rose", "Daisy", "Sienna", "Alice", "Luna", "Maya", "Penelope", "Layla", "Clara", "Evie", "Matilda", "Mila", "Aria", "Aurora", "Hazel", "Eleanor", "Hannah", "Lucy", "Ellie", "Bella", "Zoe", "Stella", "Scarlett", "Savannah", "Audrey", "Skylar", "Paisley", "Everly", "Nora", "Caroline", "Nova", "Genesis", "Emilia", "Kennedy", "Samantha", "Maya", "Willow", "Kinsley", "Naomi", "Aaliyah", "Elena", "Sarah", "Ariana", "Allison", "Gabriella", "Alice", "Madelyn", "Cora", "Ruby", "Eva", "Serenity", "Autumn", "Adeline", "Hailey", "Gianna", "Valentina", "Isla", "Eliana", "Quinn", "Nevaeh", "Ivy", "Sadie", "Piper", "Lydia", "Alexa", "Josephine", "Emery", "Julia", "Delilah", "Arianna", "Vivian", "Kaylee", "Sophie", "Brielle", "Madeline", "Phoebe", "Clara", "Hadley", "Vivian", "Emersyn", "Mackenzie", "Reagan")),
            unisex = Datasets.cleanList(listOf("Charlie", "Riley", "Rowan", "Finley", "Logan", "Harper", "Rory", "Frankie", "Eden", "Reese")),
            middle = Datasets.cleanList(listOf("James", "John", "William", "George", "Thomas", "Charles", "Henry", "Edward", "Rose", "Grace", "May", "Jane", "Anne", "Elizabeth")),
            last = Datasets.cleanList(listOf("Smith", "Jones", "Taylor", "Brown", "Williams", "Wilson", "Johnson", "Davies", "Patel", "Wright", "Robinson", "Thompson", "Evans", "Hughes", "Roberts", "Green", "Hall", "Wood", "Harris", "Martin", "Jackson", "Clarke", "Clark", "Lewis", "Turner", "Carter", "Hill", "Baker", "Allsop", "Walker", "Scott", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Hill", "Flores", "Green"))
        ),
        "Canada" to CountryDetails(
            male = Datasets.cleanList(listOf("Liam", "Noah", "Jackson", "Lucas", "Oliver", "William", "Benjamin", "Elijah", "Levi", "Theodore", "Jack", "Mason", "Ethan", "Logan", "Owen", "Caleb", "Ryan", "Leo", "Thomas", "Nathan")),
            female = Datasets.cleanList(listOf("Olivia", "Emma", "Charlotte", "Amelia", "Sophia", "Chloe", "Mia", "Harper", "Mila", "Hannah", "Ella", "Aria", "Abigail", "Lily", "Scarlett", "Victoria", "Zoey", "Penelope", "Riley", "Leah")),
            unisex = Datasets.cleanList(listOf("Logan", "Riley", "Charlie", "Avery", "Quinn", "Parker", "Peyton", "Rowan")),
            middle = Datasets.cleanList(listOf("James", "John", "Robert", "Michael", "William", "David", "Mary", "Elizabeth", "Anne", "Marie", "Grace")),
            last = Datasets.cleanList(listOf("Smith", "Brown", "Tremblay", "Martin", "Roy", "Gagnon", "Lee", "Wilson", "MacDonald", "Taylor", "Anderson", "Thomas", "Johnson", "White", "Harris", "Campbell", "Gauthier", "Morin", "Lavoie", "Fortin"))
        ),
        "Australia" to CountryDetails(
            male = Datasets.cleanList(listOf("Oliver", "Noah", "Jack", "William", "Leo", "Lucas", "Henry", "Thomas", "Charlie", "James", "Ethan", "Alexander", "Mason", "Archie", "Logan", "Benjamin", "Elijah", "Liam", "Theodore", "Hudson")),
            female = Datasets.cleanList(listOf("Isla", "Charlotte", "Olivia", "Amelia", "Mia", "Harper", "Evie", "Grace", "Willow", "Chloe", "Matilda", "Zoe", "Lucy", "Lily", "Sophie", "Sienna", "Hazel", "Ruby", "Ella", "Freya")),
            unisex = Datasets.cleanList(listOf("Riley", "Harper", "Charlie", "Rowan", "Finley", "River", "Indigo", "Sage")),
            middle = Datasets.cleanList(listOf("John", "James", "William", "Thomas", "George", "Rose", "May", "Grace", "Jane", "Louise")),
            last = Datasets.cleanList(listOf("Smith", "Jones", "Williams", "Brown", "Wilson", "Taylor", "Morton", "Davis", "White", "Anderson", "Martin", "Thompson", "Thomas", "Jackson", "White", "Harris", "Clark", "Lewis", "Robinson", "Walker"))
        ),
        "France" to CountryDetails(
            male = Datasets.cleanList(listOf("Gabriel", "Raphaël", "Léo", "Louis", "Lucas", "Adam", "Arthur", "Jules", "Maël", "Noah", "Hugo", "Louis", "Nathan", "Ethan", "Sacha", "Théo", "Gabin", "Liam", "Paul", "Timothée")),
            female = Datasets.cleanList(listOf("Jade", "Louise", "Emma", "Ambre", "Alice", "Rose", "alba", "Chloé", "Mia", "Lina", "Julia", "Lou", "Iris", "Agathe", "Romane", "Inès", "Juliette", "Anna", "Nina", "Charlie")),
            unisex = Datasets.cleanList(listOf("Charlie", "Camille", "Eden", "Sacha", "Lou", "Noam", "Morgan", "Alex")),
            middle = Datasets.cleanList(listOf("Marie", "Jean", "Pierre", "Paul", "Louis", "Henri", "Claire", "Anne", "Lucie", "Sophie")),
            last = Datasets.cleanList(listOf("Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard", "Petit", "Durand", "Leroy", "Moreau", "Simon", "Laurent", "Lefebvre", "Michel", "Garcia", "David", "Bertrand", "Roux", "Vincent", "Fournier"))
        ),
        "Germany" to CountryDetails(
            male = Datasets.cleanList(listOf("Noah", "Matteo", "Elias", "Leon", "Paul", "Finn", "Lukas", "Henry", "Felix", "Emil", "Luca", "Louis", "Liam", "Ben", "Friedrich", "Maximilian", "Alexander", "Karl", "Johann", "Anton")),
            female = Datasets.cleanList(listOf("Emilia", "Hannah", "Sophia", "Emma", "Mia", "Mila", "Lina", "Ella", "Clara", "Klara", "Marie", "Lea", "Luisa", "Johanna", "Greta", "Lena", "Charlotte", "Amelie", "Frieda", "Ida")),
            unisex = Datasets.cleanList(listOf("Mika", "Toni", "Noemi", "Kim", "Robin", "Alex", "Luca", "Elias")),
            middle = Datasets.cleanList(listOf("Maria", "Sophie", "Marie", "Alexander", "Karl", "Johann", "Christian", "Wolfgang", "Heinrich", "Elisabeth")),
            last = Datasets.cleanList(listOf("Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker", "Schulz", "Hoffmann", "Schäfer", "Koch", "Bauer", "Richter", "Klein", "Wolf", "Schröder", "Neumann", "Schwarz", "Zimmermann"))
        ),
        "Italy" to CountryDetails(
            male = Datasets.cleanList(listOf("Leonardo", "Francesco", "Alessandro", "Lorenzo", "Mattia", "Tommaso", "Gabriele", "Andrea", "Riccardo", "Edoardo", "Leonardo", "Federico", "Giuseppe", "Antonio", "Giovanni", "Pietro", "Filippo", "Davide", "Diego", "Christian")),
            female = Datasets.cleanList(listOf("Sofia", "Aurora", "Giulia", "Ginevra", "Beatrice", "Alice", "Vittoria", "Emma", "Ludovica", "Matilde", "Camilla", "Giorgia", "Bianca", "Nicole", "Caterina", "Greta", "Sara", "Chiara", "Martina", "Francesca")),
            unisex = Datasets.cleanList(listOf("Andrea", "Alex", "Noa", "Elio", "Nicola")),
            middle = Datasets.cleanList(listOf("Maria", "Giovanni", "Giuseppe", "Antonio", "Francesco", "Rosa", "Anna", "Paolo", "Luigi", "Elena")),
            last = Datasets.cleanList(listOf("Rossi", "Russo", "Ferrari", "Esposito", "Bianchi", "Romano", "Colombo", "Ricci", "Marino", "Greta", "Bruno", "Gallo", "Conti", "De Luca", "Mancini", "Costa", "Giordano", "Rizzo", "Lombardi", "Moretti"))
        ),
        "Spain" to CountryDetails(
            male = Datasets.cleanList(listOf("Martín", "Mateo", "Lucas", "Leo", "Daniel", "Alejandro", "Pablo", "Manuel", "Álvaro", "Adrián", "Enzo", "Mario", "Stefan", "Thiago", "Oliver", "Marco", "Izan", "Bruno", "David", "Alex")),
            female = Datasets.cleanList(listOf("Lucía", "Sofía", "Martina", "María", "Paula", "Valeria", "Julia", "Daniella", "Emma", "Carla", "Noa", "Alma", "Aitana", "Carmen", "Vera", "Chloe", "Lola", "Elena", "Ana", "Gala")),
            unisex = Datasets.cleanList(listOf("Alex", "Cruz", "Dani", "Mar", "Trinidad", "Guadalupe")),
            middle = Datasets.cleanList(listOf("María", "José", "Manuel", "Antonio", "Francisco", "Jesús", "Carmen", "Dolores", "Ana", "Mercedes")),
            last = Datasets.cleanList(listOf("García", "Rodríguez", "González", "Fernández", "López", "Martínez", "Sánchez", "Pérez", "Gómez", "Martín", "Jiménez", "Ruiz", "Hernández", "Díaz", "Moreno", "Muñoz", "Álvarez", "Romero", "Alonso", "Gutiérrez"))
        ),
        "Portugal" to CountryDetails(
            male = Datasets.cleanList(listOf("Francisco", "Afonso", "Duarte", "Tomás", "João", "Lourenço", "Gabriel", "Martim", "Santiago", "Rodrigo", "Lucas", "Benjamín", "Matheus", "Guilherme", "Diego", "Vasco", "Miguel", "Gonçalo", "Bernardo", "Enzo")),
            female = Datasets.cleanList(listOf("Maria", "Leonor", "Alice", "Matilde", "Beatriz", "Benedita", "Carolina", "Camila", "Margarida", "Francisca", "Inês", "Ana", "Clara", "Diana", "Aurora", "Vicência", "Sofia", "Luísa", "Lara", "Rita")),
            unisex = Datasets.cleanList(listOf("Rui", "Alex", "Chris", "Gil", "Noa")),
            middle = Datasets.cleanList(listOf("Maria", "João", "Manuel", "José", "António", "Miguel", "Ana", "Sofia", "Inês", "Beatriz")),
            last = Datasets.cleanList(listOf("Silva", "Santos", "Ferreira", "Pereira", "Oliveira", "Costa", "Rodrigues", "Martins", "Jesus", "Sousa", "Fernandes", "Gonçalves", "Gomes", "Lopes", "Marques", "Alves", "Almeida", "Ribeiro", "Pinto", "Carvalho"))
        ),
        "Brazil" to CountryDetails(
            male = Datasets.cleanList(listOf("Miguel", "Arthur", "Gael", "Théo", "Heitor", "Davi", "Bernardo", "Gabriel", "Ravi", "Noah", "Samuel", "Pedro", "Benício", "Benjamin", "Matheus", "Lucas", "Nicolas", "Joaquim", "Vicente", "Rafael")),
            female = Datasets.cleanList(listOf("Helena", "Alice", "Laura", "Sophia", "Manuela", "Maitê", "Liz", "Cicero", "Heloísa", "Julia", "Cecília", "Pietra", "Aurora", "Antonella", "Isis", "Maya", "Luna", "Agatha", "Esther", "Melina")),
            unisex = Datasets.cleanList(listOf("Alex", "Taylor", "Jordan", "Cris", "Kai", "Eden")),
            middle = Datasets.cleanList(listOf("Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima", "Gomes")),
            last = Datasets.cleanList(listOf("Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima", "Gomes", "Costa", "Ribeiro", "Martins", "Carvalho", "Rocha", "Almeida", "Araújo", "Barbosa", "Cardoso", "Cavalcanti"))
        ),
        "Mexico" to CountryDetails(
            male = Datasets.cleanList(listOf("Mateo", "Santiago", "Leonardo", "Sofía", "Sebastián", "Matías", "Alejandro", "Diego", "Emiliano", "Daniel", "Miguel", "Alexander", "Javier", "Gabriel", "Carlos", "Rodrigo", "Fernando", "Ángel", "Eduardo", "Ricardo")),
            female = Datasets.cleanList(listOf("Sofía", "Valentina", "Regina", "María", "Ximena", "Camila", "Valeria", "Victoria", "Renata", "Daniela", "Paula", "Mariana", "Gabriela", "Andrea", "Lucía", "Fernanda", "Sara", "Alejandra", "Natalia", "Jimena")),
            unisex = Datasets.cleanList(listOf("Alex", "Guadalupe", "Cruz", "Trinidad", "Paz")),
            middle = Datasets.cleanList(listOf("Guadalupe", "María", "José", "De", "Jesús", "Francisco", "Manuel", "Fernando", "Alejandro", "Sofía")),
            last = Datasets.cleanList(listOf("Hernández", "García", "Martínez", "López", "González", "Pérez", "Rodríguez", "Sánchez", "Ramírez", "Cruz", "Flores", "Gómez", "Morales", "Vázquez", "Jiménez", "Reyes", "Díaz", "Torres", "Gutiérrez", "Ruiz"))
        ),
        "Turkey" to CountryDetails(
            male = Datasets.cleanList(listOf("Alparslan", "Yusuf", "Kuzey", "Miraç", "Eymen", "Göktuğ", "Ömer", "Mustafa", "Aras", "Ali", "Çınar", "Kerem", "Mehmet", "Ahmet", "Efe", "Hamza", "Ayaz", "Yiğit", "Musa", "Bera")),
            female = Datasets.cleanList(listOf("Zeynep", "Defne", "Elif", "Asel", "Asya", "Lina", "Nehir", "Ebrar", "Azra", "Eylül", "Elisa", "Duru", "Eslem", "Miray", "Güneş", "İkra", "Hiranur", "Öykü", "Meryem", "Büşra")),
            unisex = Datasets.cleanList(listOf("Deniz", "Özgür", "Yağmur", "Ayaz", "Güneş", "Umut", "Evren")),
            middle = Datasets.cleanList(listOf("Can", "Han", "Kaan", "Cem", "Nur", "Su", "Naz", "Gül", "Efe")),
            last = Datasets.cleanList(listOf("Yılmaz", "Kaya", "Demir", "Çelik", "Şahin", "Yıldız", "Yıldırım", "Öztürk", "Aydın", "Özdemir", "Arslan", "Doğan", "Kılıç", "Aslan", "Çetin", "Kara", "Koç", "Kurt", "Özkan", "Şimşek"))
        ),
        "Saudi Arabia" to CountryDetails(
            male = Datasets.cleanList(listOf("Mohammed", "Ahmed", "Ali", "Fahad", "Abdullah", "Ibrahim", "Abdulrahman", "Saud", "Khalid", "Sultan", "Omar", "Bandar", "Nasser", "Faisal", "Rayan", "Turki", "Hamza", "Ziyad", "Tariq", "Sami")),
            female = Datasets.cleanList(listOf("Fatima", "Aisha", "Mariam", "Noor", "Sara", "Reem", "Layan", "Dana", "Joud", "Ghadah", "Munirah", "Haya", "Latifah", "Afnan", "Manal", "Nouf", "Raneem", "Tala", "Shahad", "Razan")),
            unisex = Datasets.cleanList(listOf("Noor", "Dana", "Rayan", "Zayan", "Fajer")),
            middle = Datasets.cleanList(listOf("Bin", "Ibn", "Abdul", "Al", "Din", "Hassan", "Hussein")),
            last = Datasets.cleanList(listOf("Al-Qahtani", "Al-Ghamdi", "Al-Otaibi", "Al-Dosari", "Al-Harbi", "Al-Shahrani", "Al-Mutairi", "Al-Dossary", "Al-Zahrani", "Al-Shehri", "Al-Subaie", "Al-Tamimi", "Al-Malki", "Al-Balawi", "Al-Rashidi"))
        ),
        "UAE" to CountryDetails(
            male = Datasets.cleanList(listOf("Rashid", "Mohammed", "Hamdan", "Zayed", "Khalifa", "Saeed", "Butt", "Sultan", "Mansoor", "Ahmed", "Ali", "Tariq", "Marwan", "Faisal", "Abdullah", "Majid", "Obaid", "Matar", "Salim", "Tahnoun")),
            female = Datasets.cleanList(listOf("Latifah", "Shamma", "Meera", "Fatima", "Maitha", "Salama", "Alya", "Al Reem", "Mariam", "Mera", "Hessa", "Wadha", "Shaikha", "Amna", "Hoor", "Noor", "Sara", "Dana", "Reem", "Aisha")),
            unisex = Datasets.cleanList(listOf("Noor", "Dana", "Rayan", "Bakhit")),
            middle = Datasets.cleanList(listOf("Bin", "Mohammed", "Ahmed", "Ali", "Saeed", "Rashid")),
            last = Datasets.cleanList(listOf("Al Mazrouei", "Al Nuaimi", "Al Shamsi", "Al Ketbi", "Al Suwaidi", "Al Ali", "Al Marzooqi", "Al Mheiri", "Al Kaabi", "Al Baloushi", "Al Hammadi", "Al Dhaheri", "Al Qemzi", "Al Shehhi", "Al Sharqi"))
        ),
        "Indonesia" to CountryDetails(
            male = Datasets.cleanList(listOf("Althaf", "Arka", "Bagas", "Bima", "Dimas", "Eko", "Fajar", "Galang", "Gilang", "Hadi", "Irfan", "Joko", "Krisna", "Lukman", "Muhammad", "Naufal", "Pratama", "Putra", "Rama", "Reza", "Rian", "Rizky", "Satria", "Tio", "Wahyu", "Yuda", "Zaki")),
            female = Datasets.cleanList(listOf("Ayu", "Bunga", "Citra", "Dewi", "Diah", "Fitri", "Intan", "Kartika", "Lestari", "Melati", "Nabila", "Nisa", "Putri", "Rahma", "Ratna", "Sari", "Siti", "Sri", "Utami", "Wulandari", "Zahra")),
            unisex = Datasets.cleanList(listOf("Dara", "Rian", "Tegar", "Bayu", "Krisna")),
            middle = Datasets.cleanList(listOf("Pratama", "Putra", "Putri", "Kusuma", "Wijaya", "Wijoyo", "Santoso", "Sari")),
            last = Datasets.cleanList(listOf("Pratama", "Putra", "Wibowo", "Saputra", "Santoso", "Hidayat", "Nugroho", "Setiawan", "Kusuma", "Wijaya", "Susanto", "Utomo", "Ramadhan", "Prasetyo", "Saputri", "Lestari", "Pertiwi", "Anggraini", "Wulandari", "Kusuma"))
        ),
        "Malaysia" to CountryDetails(
            male = Datasets.cleanList(listOf("Amirul", "Aiman", "Farhan", "Haikal", "Irfan", "Luqman", "Ziqri", "Daniel", "Adam", "Rayyan", "Aqil", "Haris", "Zarif", "Imran", "Syahiran", "Azim", "Faris", "Hazim", "Zul", "Khairul")),
            female = Datasets.cleanList(listOf("Nur", "Aina", "Amira", "Dania", "Fatin", "Hana", "Izzah", "Maisarah", "Nabilah", "Qistina", "Salsabila", "Sofea", "Syazwani", "Warda", "Zahra", "Aliah", "Balqis", "Batrisyia", "Farhah", "Insyirah")),
            unisex = Datasets.cleanList(listOf("Nur", "Danial", "Aiman", "Azim", "Irfan")),
            middle = Datasets.cleanList(listOf("Bin", "Binti", "Mohd", "Abdul", "Ahmad", "Bin", "Muhammad")),
            last = Datasets.cleanList(listOf("Abdullah", "Ahmad", "Ali", "Azman", "Fadzil", "Hassan", "Hussin", "Ismail", "Ibrahim", "Johari", "Kamal", "Karim", "Latif", "Mahmud", "Mansor", "Musa", "Noor", "Omar", "Osman", "Rahman", "Ramli", "Razak", "Rosli", "Saad", "Salleh", "Shamsudin", "Sulaiman", "Tahir", "Yusof", "Zainal"))
        ),
        "Japan" to CountryDetails(
            male = Datasets.cleanList(listOf("Haruto", "Yuto", "Sota", "Yuki", "Ren", "Hinata", "Sora", "Asahi", "Minato", "Itsuki", "Haruki", "Ryusei", "Kaito", "Sho", "Taiga", "Riku", "Daiki", "Keita", "Takeru", "Kenji")),
            female = Datasets.cleanList(listOf("Hina", "Yui", "Sakura", "Rin", "Aoi", "Mei", "Akari", "Himari", "Kokona", "Mio", "Riko", "Sara", "Yna", "Nana", "Kaho", "Hanan", "Noa", "Miku", "Chiaki", "Erika")),
            unisex = Datasets.cleanList(listOf("Akira", "Hinata", "Makoto", "Ren", "Kaoru", "Sora", "Yuuki")),
            middle = Datasets.cleanList(listOf("Takeshi", "Hiroshi", "Kenji", "Ichiro", "Jiro", "Saburo", "Kazuko", "Yoko", "Emiko", "Keiko")),
            last = Datasets.cleanList(listOf("Sato", "Suzuki", "Takahashi", "Tanaka", "Watanabe", "Ito", "Hakamada", "Yamamoto", "Nakamura", "Kobayashi", "Kato", "Yoshida", "Yamada", "Sasaki", "Yamaguchi", "Matsumoto", "Inoue", "Kimura", "Hayashi", "Shimizu"))
        ),
        "South Korea" to CountryDetails(
            male = Datasets.cleanList(listOf("Min-ho", "Seojun", "Do-hyun", "Ji-hu", "Ha-jun", "Eun-woo", "Si-woo", "Ye-jun", "Do-yun", "Ju-won", "Geon-woo", "Woo-jin", "Ji-ho", "Jun-seo", "Hyun-woo", "Dohyun", "Min-jae", "Seung-woo", "Tae-hyun", "Sun-woo")),
            female = Datasets.cleanList(listOf("Seo-ah", "Ji-u", "Ha-yoon", "Seo-yun", "Ji-ah", "Ha-eun", "Eun-ji", "Ah-rin", "Ji-min", "Su-ah", "Si-un", "Chae-won", "Yoo-jin", "Ye-ji", "Da-un", "Na-yeon", "Si-ah", "Chae-eun", "Yoon-seo", "Min-seo")),
            unisex = Datasets.cleanList(listOf("Ji-hu", "Min-seo", "Si-woo", "Jin", "Young")),
            middle = Datasets.cleanList(listOf("Min", "Jun", "Woo", "Seo", "Ji", "Hyun", "Young", "Jin", "Su", "Eun")),
            last = Datasets.cleanList(listOf("Kim", "Lee", "Park", "Choi", "Jung", "Kang", "Cho", "Yoon", "Jang", "Lim", "Han", "Oh", "Seo", "Shin", "Kwon", "Hwang", "Ahn", "Song", "Ryu", "Hong"))
        ),
        "China" to CountryDetails(
            male = Datasets.cleanList(listOf("Yichen", "Haoyu", "Yifei", "Zihan", "Yixuan", "Ruixiang", "Ming", "Jing", "Wei", "Hao", "Long", "Chen", "Jun", "Feng", "Lei", "Peng", "Tao", "Qiang", "Bo", "Gang")),
            female = Datasets.cleanList(listOf("Yixuan", "Xinyi", "Zixin", "Shiqi", "Yutong", "Yixin", "Mei", "Fang", "Jing", "Li", "Na", "Yan", "Xia", "Qing", "Hui", "Lan", "Ju", "Hua", "Chun", "Meilian")),
            unisex = Datasets.cleanList(listOf("Yang", "Wen", "Jing", "Chen", "Lin", "Qing")),
            middle = Datasets.cleanList(listOf("Guo", "Wang", "Li", "Zhang", "Liu", "Chen", "Yang", "Huang", "Zhao", "Wu")),
            last = Datasets.cleanList(listOf("Wang", "Li", "Zhang", "Liu", "Chen", "Yang", "Huang", "Zhao", "Wu", "Zhou", "Xu", "Sun", "Ma", "Zhu", "Hu", "Guo", "He", "Gao", "Lin", "Luo"))
        ),
        "Russia" to CountryDetails(
            male = Datasets.cleanList(listOf("Aleksandr", "Dmitriy", "Maxim", "Sergey", "Andrey", "Aleksey", "Artem", "Ilya", "Kirill", "Mikhail", "Nikita", "Matvey", "Roman", "Egor", "Arseniy", "Ivan", "Denis", "Evgeny", "Timur", "Vladislav")),
            female = Datasets.cleanList(listOf("Sofia", "Anna", "Maria", "Alisa", "Eva", "Victoria", "Polina", "Varvara", "Ksenia", "Alina", "Daria", "Arina", "Yelyzaveta", "Anastasia", "Milana", "Kira", "Veronika", "Ulyana", "Tatiana", "Ekaterina")),
            unisex = Datasets.cleanList(listOf("Sasha", "Zhenya", "Valeri", "Misha", "Nikita")),
            middle = Datasets.cleanList(listOf("Aleksandrovich", "Dmitriyevich", "Sergeyevich", "Andreyevich", "Alexandrovna", "Dmitriyevna", "Sergeyevna", "Ivanovich", "Ivanovna")),
            last = Datasets.cleanList(listOf("Ivanov", "Smirnov", "Kuznetsov", "Popov", "Vasilyev", "Petrov", "Sokolov", "Mikhailov", "Fedorov", "Morozov", "Volkov", "Alekseev", "Lebedev", "Semenov", "Egorov", "Pavlov", "Kozlov", "Stepanov", "Nikolaev", "Orlov"))
        ),
        "Nigeria" to CountryDetails(
            male = Datasets.cleanList(listOf("Ade", "Chinedu", "Emeka", "Oluwaseun", "Oluwadamilare", "Ayodele", "Babatunde", "Chibueze", "Chidi", "Chike", "Damilola", "Ejiro", "Femi", "Folorunso", "Ifeanyi", "Kelechi", "Modupe", "Musa", "Nnamdi", "Obinna", "Oladipo", "Olumide", "Omotayo", "Opeyemi", "Segun", "Taiwo", "Uche", "Wale", "Yemi", "Zack")),
            female = Datasets.cleanList(listOf("Adedayo", "Adeola", "Adunni", "Aisha", "Amaka", "Anike", "Ayomide", "Bolanle", "Chioma", "Chinyere", "Ejiro", "Eniola", "Evelyn", "Fatima", "Folake", "Funmilayo", "Ifeoma", "Kikelomo", "Modupe", "Nkechi", "Nneka", "Olamide", "Oluwafunmilayo", "Omolara", "Omowunmi", "Temitope", "Titilayo", "Yetunde", "Zainab", "Zuwaira")),
            unisex = Datasets.cleanList(listOf("Ayomide", "Oluwatosin", "Damilola", "Opeyemi", "Taiwo", "Kehinde")),
            middle = Datasets.cleanList(listOf("Oluwaseun", "Chinedu", "Ade", "Olumide", "Adewale", "Ifeanyi", "Folake", "Chioma")),
            last = Datasets.cleanList(listOf("Okafor", "Okonkwo", "Adebayo", "Adeleke", "Oluwaseun", "Nwosu", "Eze", "Balogun", "Ojo", "Bakare", "Ogundipe", "Onwuachi", "Adichie", "Obi", "Igwe", "Okoro", "Abubakar", "Danjuma", "Garba", "Bello"))
        ),
        "South Africa" to CountryDetails(
            male = Datasets.cleanList(listOf("Sipho", "Lethabo", "Banele", "Kabelo", "Thabo", "Jabulani", "Tshepo", "Mandla", "Lungile", "Bongani", "Willem", "Johan", "Pieter", "Hendrik", "Jacobus", "Andries", "Jan", "Charl", "Francois", "Liam")),
            female = Datasets.cleanList(listOf("Amahle", "Melokuhle", "Lethabo", "Mbalenhle", "Thandeka", "Zinhle", "Nandi", "Lerato", "Precious", "Nomusa", "Anri", "Chante", "Marike", "Zoe", "Chloe", "Mia", "Hannah", "Emma", "Jessica", "Samantha")),
            unisex = Datasets.cleanList(listOf("Lethabo", "Kabelo", "Tshepo", "Banele", "Reatlegile")),
            middle = Datasets.cleanList(listOf("Sipho", "Thabo", "John", "David", "Maria", "Elizabeth", "Grace")),
            last = Datasets.cleanList(listOf("Dlamini", "Nkosi", "Khumalo", "Ndlovu", "Mokoena", "Molefe", "Zulu", "Van Wyk", "Van Der Merwe", "Botha", "Pretorius", "Coetzee", "Kruger", "Venter", "Meyer", "Nel", "De Villiers", "Du Plessis", "Fourie", "Smith"))
        )
    )

    private val masterMaleMiddle = Datasets.cleanList(masterMiddle + listOf("James", "John", "Robert", "William", "Arlo", "Lee", "Paul"))
    private val masterFemaleMiddle = Datasets.cleanList(masterMiddle + listOf("Marie", "Ann", "Lynn", "Rose", "Grace", "Jane", "May"))

    fun getMaleNames(country: String): List<String> {
        return countryMap[country]?.male ?: masterMale
    }

    fun getFemaleNames(country: String): List<String> {
        return countryMap[country]?.female ?: masterFemale
    }

    fun getUnisexNames(country: String): List<String> {
        return countryMap[country]?.unisex ?: masterUnisex
    }

    fun getLastNames(country: String): List<String> {
        return countryMap[country]?.last ?: masterSurnames
    }

    fun getMiddleNames(country: String, gender: String = "Unisex"): List<String> {
        val details = countryMap[country]
        if (details != null) {
            return when (gender) {
                "Male" -> details.maleMiddle
                "Female" -> details.femaleMiddle
                else -> details.middle
            }
        }
        return when (gender) {
            "Male" -> masterMaleMiddle
            "Female" -> masterFemaleMiddle
            else -> masterMiddle
        }
    }
}

data class CountryDetails(
    val male: List<String>,
    val female: List<String>,
    val unisex: List<String>,
    val middle: List<String>,
    val last: List<String>,
    val maleMiddle: List<String> = Datasets.cleanList(middle + male.take(15)),
    val femaleMiddle: List<String> = Datasets.cleanList(middle + female.take(15))
)
