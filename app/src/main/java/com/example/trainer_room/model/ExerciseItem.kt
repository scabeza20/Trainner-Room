package com.example.trainer_room.model

data class ExerciseItem(
    val id: String,
    val name: String,
    val targetSeconds: Int,
    val elapsedSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val badgeColorIndex: Int = 0,
    val badgeShapeIndex: Int = 0
) {
    val remainingSeconds: Int
        get() = (targetSeconds - elapsedSeconds).coerceAtLeast(0)
}
