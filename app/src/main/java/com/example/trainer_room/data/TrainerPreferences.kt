package com.example.trainer_room.data

import android.content.Context
import android.content.SharedPreferences
import com.example.trainer_room.model.ExerciseItem
import com.example.trainer_room.model.ThemeMode
import org.json.JSONArray

interface ITrainerPreferences {
    fun loadExercises(): List<ExerciseItem>?
    fun saveExercises(exercises: List<ExerciseItem>)
    fun loadThemeMode(): ThemeMode
    fun saveThemeMode(mode: ThemeMode)
    fun loadDefaultCountersCount(): Int
    fun saveDefaultCountersCount(count: Int)
    fun isAlarmEnabled(): Boolean
    fun saveAlarmEnabled(enabled: Boolean)
    fun loadAlarmDuration(): Int
    fun saveAlarmDuration(seconds: Int)
    fun loadRoutineTitle(): String
    fun saveRoutineTitle(title: String)
    fun loadMainTargetSeconds(): Int
    fun saveMainTargetSeconds(seconds: Int)
}

class TrainerPreferences(context: Context) : ITrainerPreferences {

    private val prefs: SharedPreferences = context.getSharedPreferences("trainer_room_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_EXERCISES = "saved_exercises_json"
        private const val KEY_THEME_MODE = "saved_theme_mode"
        private const val KEY_DEFAULT_COUNTERS_COUNT = "default_counters_count"
        private const val KEY_ALARM_ENABLED = "alarm_sound_enabled"
        private const val KEY_ALARM_DURATION = "alarm_sound_duration"
        private const val KEY_ROUTINE_TITLE = "routine_title"
        private const val KEY_MAIN_TARGET = "main_target_seconds"
    }

    override fun loadExercises(): List<ExerciseItem>? {
        val jsonString = prefs.getString(KEY_EXERCISES, null) ?: return null
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<ExerciseItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(ExerciseItem.fromJsonObject(obj))
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    override fun saveExercises(exercises: List<ExerciseItem>) {
        try {
            val jsonArray = JSONArray()
            exercises.forEach { jsonArray.put(it.toJsonObject()) }
            prefs.edit().putString(KEY_EXERCISES, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun loadThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    override fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    override fun loadDefaultCountersCount(): Int {
        return prefs.getInt(KEY_DEFAULT_COUNTERS_COUNT, 3)
    }

    override fun saveDefaultCountersCount(count: Int) {
        prefs.edit().putInt(KEY_DEFAULT_COUNTERS_COUNT, count.coerceIn(1, 10)).apply()
    }

    override fun isAlarmEnabled(): Boolean {
        return prefs.getBoolean(KEY_ALARM_ENABLED, true)
    }

    override fun saveAlarmEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALARM_ENABLED, enabled).apply()
    }

    override fun loadAlarmDuration(): Int {
        return prefs.getInt(KEY_ALARM_DURATION, 3)
    }

    override fun saveAlarmDuration(seconds: Int) {
        prefs.edit().putInt(KEY_ALARM_DURATION, seconds.coerceIn(1, 10)).apply()
    }

    override fun loadRoutineTitle(): String {
        return prefs.getString(KEY_ROUTINE_TITLE, "Piernas") ?: "Piernas"
    }

    override fun saveRoutineTitle(title: String) {
        prefs.edit().putString(KEY_ROUTINE_TITLE, title).apply()
    }

    override fun loadMainTargetSeconds(): Int {
        return prefs.getInt(KEY_MAIN_TARGET, 0)
    }

    override fun saveMainTargetSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_MAIN_TARGET, seconds.coerceAtLeast(0)).apply()
    }
}
