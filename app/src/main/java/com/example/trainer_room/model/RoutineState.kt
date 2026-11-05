package com.example.trainer_room.model

data class RoutineState(
    val title: String = "Piernas",
    val mainTargetSeconds: Int = 0,
    val mainElapsedSeconds: Int = 0,
    val isMainRunning: Boolean = false,
    val exercises: List<ExerciseItem> = defaultExercises(),
    val activeExerciseIndex: Int = 0,
    val isRunning: Boolean = false,
    val isDarkTheme: Boolean = true
)

fun defaultExercises(): List<ExerciseItem> = listOf(
    ExerciseItem(
        id = "1",
        name = "Sentadillas",
        targetSeconds = 45,
        elapsedSeconds = 0,
        isCompleted = false,
        badgeColorIndex = 0,
        badgeShapeIndex = 0
    ),
    ExerciseItem(
        id = "2",
        name = "Zancadas",
        targetSeconds = 32,
        elapsedSeconds = 0,
        isCompleted = false,
        badgeColorIndex = 1,
        badgeShapeIndex = 1
    ),
    ExerciseItem(
        id = "3",
        name = "Puente de glúteos",
        targetSeconds = 60,
        elapsedSeconds = 0,
        isCompleted = false,
        badgeColorIndex = 2,
        badgeShapeIndex = 2
    ),
    ExerciseItem(
        id = "4",
        name = "Elevación de talones",
        targetSeconds = 30,
        elapsedSeconds = 0,
        isCompleted = false,
        badgeColorIndex = 3,
        badgeShapeIndex = 3
    ),
    ExerciseItem(
        id = "5",
        name = "Descanso",
        targetSeconds = 90,
        elapsedSeconds = 0,
        isCompleted = false,
        badgeColorIndex = 4,
        badgeShapeIndex = 4
    )
)
