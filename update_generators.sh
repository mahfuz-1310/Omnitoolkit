cat << 'INNER_EOF' >> app/src/main/java/com/example/utils/Generators.kt

object SessionCache {
    val generatedNames = mutableSetOf<String>()
    val generatedUsernames = mutableSetOf<String>()
    
    fun resetNames() { generatedNames.clear() }
    fun resetUsernames() { generatedUsernames.clear() }
}
INNER_EOF
bash update_generators.sh