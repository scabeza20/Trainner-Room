package com.example.trainer_room.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainer_room.audio.IAlarmSoundPlayer
import com.example.trainer_room.data.ITrainerPreferences
import com.example.trainer_room.model.ExerciseItem
import com.example.trainer_room.model.RoutineState
import com.example.trainer_room.model.ThemeMode
import com.example.trainer_room.model.defaultExercises
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class RoutineViewModel(
    private val preferences: ITrainerPreferences? = null,
    private val soundPlayer: IAlarmSoundPlayer? = null,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope by lazy { coroutineScope ?: viewModelScope }

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<RoutineState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        checkTickerStatus()
    }

    private fun loadInitialState(): RoutineState {
        val defaultCount = preferences?.loadDefaultCountersCount() ?: 3
        val savedExercises = preferences?.loadExercises()

        // Los contadores siempre inician limpios en 00:00:00 al entrar a la app
        val initialExercises = if (savedExercises != null && savedExercises.isNotEmpty()) {
            savedExercises.map { it.copy(elapsedSeconds = 0, currentRep = 1, isCompleted = false) }
        } else {
            defaultExercises(defaultCount)
        }

        val themeMode = preferences?.loadThemeMode() ?: ThemeMode.SYSTEM
        val routineTitle = preferences?.loadRoutineTitle() ?: "Piernas"
        val mainTarget = preferences?.loadMainTargetSeconds() ?: 0
        val alarmEnabled = preferences?.isAlarmEnabled() ?: true
        val alarmDuration = preferences?.loadAlarmDuration() ?: 3

        return RoutineState(
            title = routineTitle,
            mainTargetSeconds = mainTarget,
            mainElapsedSeconds = 0,
            isMainRunning = false,
            exercises = initialExercises,
            activeExerciseId = initialExercises.firstOrNull()?.id,
            activeRunningExerciseId = null,
            themeMode = themeMode,
            isDarkTheme = themeMode != ThemeMode.LIGHT,
            defaultCountersCount = defaultCount,
            alarmSoundEnabled = alarmEnabled,
            alarmDurationSeconds = alarmDuration,
            isTestingAlarm = false
        )
    }

    fun updateSystemDarkTheme(isSystemDark: Boolean) {
        _uiState.update { state ->
            val resolvedDark = when (state.themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            state.copy(isDarkTheme = resolvedDark)
        }
    }

    fun setThemeMode(mode: ThemeMode, isSystemDark: Boolean = false) {
        preferences?.saveThemeMode(mode)
        _uiState.update { state ->
            val resolvedDark = when (mode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            state.copy(themeMode = mode, isDarkTheme = resolvedDark)
        }
    }

    fun setDefaultCountersCount(count: Int) {
        val safeCount = count.coerceIn(1, 10)
        preferences?.saveDefaultCountersCount(safeCount)
        _uiState.update { it.copy(defaultCountersCount = safeCount) }
    }

    fun setAlarmSoundEnabled(enabled: Boolean) {
        preferences?.saveAlarmEnabled(enabled)
        _uiState.update { it.copy(alarmSoundEnabled = enabled) }
    }

    fun setAlarmDurationSeconds(seconds: Int) {
        val safeSeconds = seconds.coerceIn(1, 10)
        preferences?.saveAlarmDuration(safeSeconds)
        _uiState.update { it.copy(alarmDurationSeconds = safeSeconds) }
    }

    fun testAlarmSound() {
        val duration = _uiState.value.alarmDurationSeconds
        _uiState.update { it.copy(isTestingAlarm = true) }
        soundPlayer?.playAlarm(duration) {
            _uiState.update { it.copy(isTestingAlarm = false) }
        }
    }

    fun stopAlarmSound() {
        soundPlayer?.stop()
        _uiState.update { it.copy(isTestingAlarm = false) }
    }

    fun resetToDefaultExercises() {
        val count = _uiState.value.defaultCountersCount
        val defaults = defaultExercises(count)
        preferences?.saveExercises(defaults)
        _uiState.update { state ->
            state.copy(
                exercises = defaults,
                activeExerciseId = defaults.firstOrNull()?.id,
                activeRunningExerciseId = null
            )
        }
        checkTickerStatus()
    }

    private fun checkTickerStatus() {
        val state = _uiState.value
        val shouldRun = state.isMainRunning || state.activeRunningExerciseId != null

        if (shouldRun && (tickerJob == null || tickerJob?.isActive == false)) {
            tickerJob = scope.launch(Dispatchers.Default) {
                while (isActive) {
                    delay(1000L)
                    tickSecond()
                }
            }
        } else if (!shouldRun && tickerJob?.isActive == true) {
            tickerJob?.cancel()
            tickerJob = null
        }
    }

    fun tickSecond() {
        var shouldTriggerAlarm = false
        var alarmDuration = 3

        _uiState.update { state ->
            // 1. Contador Principal
            var newMainElapsed = state.mainElapsedSeconds
            var newIsMainRunning = state.isMainRunning

            if (state.isMainRunning) {
                newMainElapsed += 1
                if (state.mainTargetSeconds > 0 && newMainElapsed >= state.mainTargetSeconds) {
                    newIsMainRunning = false
                    newMainElapsed = state.mainTargetSeconds
                }
            }

            // 2. Contador Secundario Activo
            var newRunningId = state.activeRunningExerciseId
            val updatedExercises = state.exercises.map { exercise ->
                if (exercise.id == newRunningId && !exercise.isCompleted) {
                    val nextElapsed = exercise.elapsedSeconds + 1
                    if (nextElapsed >= exercise.targetSeconds) {
                        // Comprobación de repeticiones
                        if (exercise.currentRep < exercise.totalReps) {
                            exercise.copy(
                                currentRep = exercise.currentRep + 1,
                                elapsedSeconds = 0,
                                isCompleted = false
                            )
                        } else {
                            newRunningId = null // Detener conteo de esta serie
                            if (state.alarmSoundEnabled) {
                                shouldTriggerAlarm = true
                                alarmDuration = state.alarmDurationSeconds
                            }
                            exercise.copy(
                                elapsedSeconds = exercise.targetSeconds,
                                isCompleted = true
                            )
                        }
                    } else {
                        exercise.copy(elapsedSeconds = nextElapsed)
                    }
                } else {
                    exercise
                }
            }

            state.copy(
                mainElapsedSeconds = newMainElapsed,
                isMainRunning = newIsMainRunning,
                exercises = updatedExercises,
                activeRunningExerciseId = newRunningId
            )
        }

        if (shouldTriggerAlarm) {
            soundPlayer?.playAlarm(alarmDuration)
        }

        checkTickerStatus()
    }

    // --- Control del Contador Principal (Sesión) ---

    fun toggleMainPlayPause() {
        _uiState.update { it.copy(isMainRunning = !it.isMainRunning) }
        checkTickerStatus()
    }

    fun resetMainCounter() {
        _uiState.update { it.copy(mainElapsedSeconds = 0, isMainRunning = false) }
        checkTickerStatus()
    }

    fun updateMainRoutine(title: String, targetSeconds: Int) {
        val safeTitle = title.ifBlank { "Rutina" }
        val safeTarget = targetSeconds.coerceAtLeast(0)
        preferences?.saveRoutineTitle(safeTitle)
        preferences?.saveMainTargetSeconds(safeTarget)
        _uiState.update { state ->
            state.copy(
                title = safeTitle,
                mainTargetSeconds = safeTarget,
                mainElapsedSeconds = 0,
                isMainRunning = false
            )
        }
        checkTickerStatus()
    }

    // --- Control de Contadores Secundarios Individuales ---

    fun toggleExercisePlay(id: String) {
        _uiState.update { state ->
            val isCurrentRunning = state.activeRunningExerciseId == id
            if (isCurrentRunning) {
                state.copy(activeRunningExerciseId = null)
            } else {
                val updatedExercises = state.exercises.map { ex ->
                    if (ex.id == id && ex.isCompleted) {
                        ex.copy(isCompleted = false, elapsedSeconds = 0, currentRep = 1)
                    } else ex
                }
                state.copy(
                    exercises = updatedExercises,
                    activeExerciseId = id,
                    activeRunningExerciseId = id
                )
            }
        }
        checkTickerStatus()
    }

    fun resetExercise(id: String) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.map { ex ->
                if (ex.id == id) {
                    ex.copy(elapsedSeconds = 0, currentRep = 1, isCompleted = false)
                } else ex
            }
            val newRunningId = if (state.activeRunningExerciseId == id) null else state.activeRunningExerciseId
            state.copy(exercises = updatedExercises, activeRunningExerciseId = newRunningId)
        }
        checkTickerStatus()
    }

    fun incrementReps(id: String) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.map { ex ->
                if (ex.id == id) {
                    val nextReps = (ex.totalReps + 1).coerceAtMost(99)
                    val stillCompleted = ex.isCompleted && ex.currentRep >= nextReps
                    ex.copy(totalReps = nextReps, isCompleted = stillCompleted)
                } else ex
            }
            preferences?.saveExercises(updatedExercises)
            state.copy(exercises = updatedExercises)
        }
    }

    fun decrementReps(id: String) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.map { ex ->
                if (ex.id == id) {
                    val prevReps = (ex.totalReps - 1).coerceAtLeast(1)
                    val nextCurrent = ex.currentRep.coerceAtMost(prevReps)
                    ex.copy(totalReps = prevReps, currentRep = nextCurrent)
                } else ex
            }
            preferences?.saveExercises(updatedExercises)
            state.copy(exercises = updatedExercises)
        }
    }

    fun addExercise(
        name: String,
        targetSeconds: Int,
        badgeColorIndex: Int = 0,
        badgeShapeIndex: Int = 0,
        totalReps: Int = 1
    ) {
        _uiState.update { state ->
            val newExercise = ExerciseItem(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "Serie ${state.exercises.size + 1}" },
                targetSeconds = targetSeconds.coerceAtLeast(1),
                elapsedSeconds = 0,
                isCompleted = false,
                badgeColorIndex = badgeColorIndex % 5,
                badgeShapeIndex = badgeShapeIndex % 5,
                totalReps = totalReps.coerceIn(1, 99),
                currentRep = 1
            )
            val updated = state.exercises + newExercise
            preferences?.saveExercises(updated)
            state.copy(exercises = updated)
        }
    }

    fun editExercise(
        id: String,
        name: String,
        targetSeconds: Int,
        badgeColorIndex: Int,
        badgeShapeIndex: Int,
        totalReps: Int = 1
    ) {
        _uiState.update { state ->
            val updated = state.exercises.map { ex ->
                if (ex.id == id) {
                    val safeTarget = targetSeconds.coerceAtLeast(1)
                    val safeReps = totalReps.coerceIn(1, 99)
                    val newCompleted = ex.elapsedSeconds >= safeTarget && ex.currentRep >= safeReps
                    ex.copy(
                        name = name.ifBlank { "Serie" },
                        targetSeconds = safeTarget,
                        badgeColorIndex = badgeColorIndex % 5,
                        badgeShapeIndex = badgeShapeIndex % 5,
                        totalReps = safeReps,
                        isCompleted = newCompleted
                    )
                } else ex
            }
            preferences?.saveExercises(updated)
            state.copy(exercises = updated)
        }
    }

    fun deleteExercise(id: String) {
        _uiState.update { state ->
            val filtered = state.exercises.filterNot { it.id == id }
            preferences?.saveExercises(filtered)
            val newRunningId = if (state.activeRunningExerciseId == id) null else state.activeRunningExerciseId
            val newActiveId = if (state.activeExerciseId == id) filtered.firstOrNull()?.id else state.activeExerciseId
            state.copy(
                exercises = filtered,
                activeRunningExerciseId = newRunningId,
                activeExerciseId = newActiveId
            )
        }
        checkTickerStatus()
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer?.release()
    }
}
