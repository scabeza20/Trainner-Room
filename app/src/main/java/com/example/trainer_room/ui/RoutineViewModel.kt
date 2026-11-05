package com.example.trainer_room.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainer_room.model.ExerciseItem
import com.example.trainer_room.model.RoutineState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class RoutineViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineState())
    val uiState: StateFlow<RoutineState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                tickSecond()
            }
        }
    }

    private fun tickSecond() {
        _uiState.update { state ->
            // 1. Contador Principal Independiente
            var newMainElapsed = state.mainElapsedSeconds
            var newIsMainRunning = state.isMainRunning

            if (state.isMainRunning) {
                newMainElapsed += 1
                if (state.mainTargetSeconds > 0 && newMainElapsed >= state.mainTargetSeconds) {
                    newIsMainRunning = false
                    newMainElapsed = state.mainTargetSeconds
                }
            }

            // 2. Contadores Inferiores de Ejercicios
            var newExercises = state.exercises
            var newActiveIdx = state.activeExerciseIndex

            if (state.isRunning && state.activeExerciseIndex in state.exercises.indices) {
                val exercisesList = state.exercises.toMutableList()
                val activeEx = exercisesList[state.activeExerciseIndex]

                if (!activeEx.isCompleted) {
                    val newElapsed = activeEx.elapsedSeconds + 1
                    val isCompleted = newElapsed >= activeEx.targetSeconds
                    exercisesList[state.activeExerciseIndex] = activeEx.copy(
                        elapsedSeconds = newElapsed,
                        isCompleted = isCompleted
                    )

                    if (isCompleted) {
                        val nextUncompleted = exercisesList.indexOfFirst { !it.isCompleted }
                        if (nextUncompleted != -1) {
                            newActiveIdx = nextUncompleted
                        }
                    }
                    newExercises = exercisesList
                }
            }

            state.copy(
                mainElapsedSeconds = newMainElapsed,
                isMainRunning = newIsMainRunning,
                exercises = newExercises,
                activeExerciseIndex = newActiveIdx
            )
        }
    }

    fun toggleMainPlayPause() {
        _uiState.update { it.copy(isMainRunning = !it.isMainRunning) }
    }

    fun updateMainRoutine(title: String, targetSeconds: Int) {
        _uiState.update { state ->
            state.copy(
                title = title.ifBlank { "Rutina" },
                mainTargetSeconds = targetSeconds.coerceAtLeast(0),
                mainElapsedSeconds = 0,
                isMainRunning = false
            )
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isRunning = !it.isRunning) }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun selectExercise(index: Int) {
        _uiState.update { state ->
            if (index in state.exercises.indices) {
                state.copy(
                    activeExerciseIndex = index,
                    isRunning = true
                )
            } else state
        }
    }

    fun resetRoutine() {
        _uiState.update { state ->
            val resetExercises = state.exercises.map {
                it.copy(elapsedSeconds = 0, isCompleted = false)
            }
            state.copy(
                mainElapsedSeconds = 0,
                isMainRunning = false,
                activeExerciseIndex = 0,
                isRunning = false,
                exercises = resetExercises
            )
        }
    }

    fun addExercise(name: String, targetSeconds: Int, badgeColorIndex: Int = 0, badgeShapeIndex: Int = 0) {
        _uiState.update { state ->
            val newExercise = ExerciseItem(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "Serie ${state.exercises.size + 1}" },
                targetSeconds = targetSeconds.coerceAtLeast(1),
                elapsedSeconds = 0,
                isCompleted = false,
                badgeColorIndex = badgeColorIndex % 5,
                badgeShapeIndex = badgeShapeIndex % 5
            )
            val updatedList = state.exercises + newExercise
            state.copy(exercises = updatedList)
        }
    }

    fun editExercise(id: String, name: String, targetSeconds: Int, badgeColorIndex: Int, badgeShapeIndex: Int) {
        _uiState.update { state ->
            val updatedList = state.exercises.map {
                if (it.id == id) {
                    val safeTarget = targetSeconds.coerceAtLeast(1)
                    val newCompleted = it.elapsedSeconds >= safeTarget
                    it.copy(
                        name = name.ifBlank { "Serie ${state.exercises.indexOf(it) + 1}" },
                        targetSeconds = safeTarget,
                        badgeColorIndex = badgeColorIndex % 5,
                        badgeShapeIndex = badgeShapeIndex % 5,
                        isCompleted = newCompleted
                    )
                } else it
            }
            state.copy(exercises = updatedList)
        }
    }

    fun deleteExercise(id: String) {
        _uiState.update { state ->
            val updatedList = state.exercises.filterNot { it.id == id }
            val newActiveIndex = state.activeExerciseIndex.coerceAtMost((updatedList.size - 1).coerceAtLeast(0))
            state.copy(exercises = updatedList, activeExerciseIndex = newActiveIndex)
        }
    }
}
