package com.example.trainer_room.model

import androidx.compose.runtime.Immutable
import org.json.JSONObject

@Immutable
data class ExerciseItem(
    val id: String,
    val name: String,
    val targetSeconds: Int,
    val elapsedSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val badgeColorIndex: Int = 0,
    val badgeShapeIndex: Int = 0,
    val totalReps: Int = 1,
    val currentRep: Int = 1
) {
    val remainingSeconds: Int
        get() = (targetSeconds - elapsedSeconds).coerceAtLeast(0)

    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("targetSeconds", targetSeconds)
            put("badgeColorIndex", badgeColorIndex)
            put("badgeShapeIndex", badgeShapeIndex)
            put("totalReps", totalReps)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): ExerciseItem {
            return ExerciseItem(
                id = json.optString("id", java.util.UUID.randomUUID().toString()),
                name = json.optString("name", "Serie"),
                targetSeconds = json.optInt("targetSeconds", 45).coerceAtLeast(1),
                elapsedSeconds = 0, // Siempre se inicializa en 00:00:00
                isCompleted = false,
                badgeColorIndex = json.optInt("badgeColorIndex", 0),
                badgeShapeIndex = json.optInt("badgeShapeIndex", 0),
                totalReps = json.optInt("totalReps", 1).coerceIn(1, 99),
                currentRep = 1
            )
        }
    }
}
