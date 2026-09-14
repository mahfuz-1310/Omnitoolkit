package com.example.feature.system.shell

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class ShellHistoryStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shell_history_prefs", Context.MODE_PRIVATE)
    private val _history = MutableStateFlow<List<ShellHistoryItem>>(emptyList())
    val history: StateFlow<List<ShellHistoryItem>> = _history.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val rawJson = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val list = mutableListOf<ShellHistoryItem>()
        try {
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ShellHistoryItem(
                        id = obj.optString("id"),
                        command = obj.optString("command"),
                        timestamp = obj.optLong("timestamp"),
                        exitCode = obj.optInt("exitCode"),
                        executionTimeMs = obj.optLong("executionTimeMs")
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        _history.value = list
    }

    fun addEntry(item: ShellHistoryItem) {
        val current = _history.value.toMutableList()
        // Remove duplicate if run recently to keep list concise
        current.removeAll { it.command == item.command }
        current.add(0, item)
        // Keep up to 50 items
        val trimmed = current.take(50)
        saveHistory(trimmed)
    }

    fun deleteEntry(id: String) {
        val current = _history.value.filter { it.id != id }
        saveHistory(current)
    }

    fun clearHistory() {
        saveHistory(emptyList())
    }

    private fun saveHistory(list: List<ShellHistoryItem>) {
        _history.value = list
        try {
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("command", item.command)
                    put("timestamp", item.timestamp)
                    put("exitCode", item.exitCode)
                    put("executionTimeMs", item.executionTimeMs)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    companion object {
        private const val KEY_HISTORY = "history_entries"
    }
}
