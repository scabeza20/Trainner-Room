package com.example.trainer_room.model

import androidx.compose.runtime.Immutable

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Immutable
data class RoutineState(
    val title: String = "Piernas",
    val mainTargetSeconds: Int = 0,
    val mainElapsedSeconds: Int = 0,
    val isMainRunning: Boolean = false,
    val exercises: List<ExerciseItem> = defaultExercises(3),
    val activeExerciseId: String? = "1",
    val activeRunningExerciseId: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isDarkTheme: Boolean = true,
    val defaultCountersCount: Int = 3,
    val alarmSoundEnabled: Boolean = true,
    val alarmDurationSeconds: Int = 3,
    val isTestingAlarm: Boolean = false
) {
    val completedExercisesCount: Int
        get() = exercises.count { it.isCompleted }

    val activeExerciseIndex: Int
        get() {
            val idx = exercises.indexOfFirst { it.id == activeExerciseId }
            return if (idx >= 0) idx else 0
        }

    val isRunning: Boolean
        get() = activeRunningExerciseId != null
}

fun defaultExercises(count: Int = 3): List<ExerciseItem> {
    val catalog = listOf(
        ExerciseItem(
            id = "1",
            name = "Sentadillas",
            targetSeconds = 45,
            elapsedSeconds = 0,
            isCompleted = false,
            badgeColorIndex = 0,
            badgeShapeIndex = 0,
            totalReps = 3,
            currentRep = 1
        ),
        ExerciseItem(
            id = "2",
            name = "Zancadas",
            targetSeconds = 32,
            elapsedSeconds = 0,
            isCompleted = false,
            badgeColorIndex = 1,
            badgeShapeIndex = 1,
            totalReps = 3,
            currentRep = 1
        ),
        ExerciseItem(
            id = "3",
            name = "Puente de glúteos",
            targetSeconds = 60,
            elapsedSeconds = 0,
            isCompleted = false,
            badgeColorIndex = 2,
            badgeShapeIndex = 2,
            totalReps = 3,
            currentRep = 1
        ),
        ExerciseItem(
            id = "4",
            name = "Elevación de talones",
            targetSeconds = 30,
            elapsedSeconds = 0,
            isCompleted = false,
            badgeColorIndex = 3,
            badgeShapeIndex = 3,
            totalReps = 3,
            currentRep = 1
        ),
        ExerciseItem(
            id = "5",
            name = "Descanso",
            targetSeconds = 90,
            elapsedSeconds = 0,
            isCompleted = false,
            badgeColorIndex = 4,
            badgeShapeIndex = 4,
            totalReps = 1,
            currentRep = 1
        )
    )

    val safeCount = count.coerceIn(1, 10)
    return if (safeCount <= catalog.size) {
        catalog.take(safeCount)
    } else {
        catalog + (catalog.size until safeCount).map { i ->
            ExerciseItem(
                id = (i + 1).toString(),
                name = "Serie ${i + 1}",
                targetSeconds = 45,
                elapsedSeconds = 0,
                isCompleted = false,
                badgeColorIndex = i % 5,
                badgeShapeIndex = i % 5,
                totalReps = 3,
                currentRep = 1
            )
        }
    }
}
