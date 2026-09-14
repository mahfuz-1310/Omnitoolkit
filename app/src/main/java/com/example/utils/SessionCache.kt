package com.example.utils

object SessionCache {
    val generatedNames = mutableSetOf<String>()
    val generatedUsernames = mutableSetOf<String>()
    val generatedMiddleNames = mutableSetOf<String>()
    val generatedFirstMiddleNames = mutableSetOf<String>()

    fun resetNames() { generatedNames.clear() }
    fun resetUsernames() { generatedUsernames.clear() }
    fun resetMiddleNames() { generatedMiddleNames.clear() }
    fun resetFirstMiddleNames() { generatedFirstMiddleNames.clear() }
}
