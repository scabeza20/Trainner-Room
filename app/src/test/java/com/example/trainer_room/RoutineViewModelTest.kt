package com.example.trainer_room

import com.example.trainer_room.audio.IAlarmSoundPlayer
import com.example.trainer_room.data.ITrainerPreferences
import com.example.trainer_room.model.ExerciseItem
import com.example.trainer_room.model.ThemeMode
import com.example.trainer_room.ui.RoutineViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeTrainerPreferences : ITrainerPreferences {
    var savedExercisesList: List<ExerciseItem>? = null
    var savedThemeMode: ThemeMode = ThemeMode.SYSTEM
    var savedDefaultCount: Int = 3
    var savedAlarmEnabled: Boolean = true
    var savedAlarmDuration: Int = 3
    var savedRoutineTitle: String = "Piernas"
    var savedMainTargetSeconds: Int = 0

    override fun loadExercises(): List<ExerciseItem>? = savedExercisesList
    override fun saveExercises(exercises: List<ExerciseItem>) {
        savedExercisesList = exercises
    }

    override fun loadThemeMode(): ThemeMode = savedThemeMode
    override fun saveThemeMode(mode: ThemeMode) {
        savedThemeMode = mode
    }

    override fun loadDefaultCountersCount(): Int = savedDefaultCount
    override fun saveDefaultCountersCount(count: Int) {
        savedDefaultCount = count
    }

    override fun isAlarmEnabled(): Boolean = savedAlarmEnabled
    override fun saveAlarmEnabled(enabled: Boolean) {
        savedAlarmEnabled = enabled
    }

    override fun loadAlarmDuration(): Int = savedAlarmDuration
    override fun saveAlarmDuration(seconds: Int) {
        savedAlarmDuration = seconds
    }

    override fun loadRoutineTitle(): String = savedRoutineTitle
    override fun saveRoutineTitle(title: String) {
        savedRoutineTitle = title
    }

    override fun loadMainTargetSeconds(): Int = savedMainTargetSeconds
    override fun saveMainTargetSeconds(seconds: Int) {
        savedMainTargetSeconds = seconds
    }
}

class FakeAlarmSoundPlayer : IAlarmSoundPlayer {
    var playedAlarmDuration: Int? = null
    var isPlaying: Boolean = false

    override fun playAlarm(durationSeconds: Int, onComplete: (() -> Unit)?) {
        playedAlarmDuration = durationSeconds
        isPlaying = true
    }

    override fun stop() {
        isPlaying = false
    }

    override fun release() {
        isPlaying = false
    }
}

class RoutineViewModelTest {

    private lateinit var testScope: CoroutineScope
    private lateinit var fakePreferences: FakeTrainerPreferences
    private lateinit var fakeSoundPlayer: FakeAlarmSoundPlayer
    private lateinit var viewModel: RoutineViewModel

    @Before
    fun setUp() {
        testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        fakePreferences = FakeTrainerPreferences()
        fakeSoundPlayer = FakeAlarmSoundPlayer()
        viewModel = RoutineViewModel(
            preferences = fakePreferences,
            soundPlayer = fakeSoundPlayer,
            coroutineScope = testScope
        )
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun initialState_hasDefaultThreeExercises_andAllZeroTime() {
        // Requisito: Por defecto deben ser 3 contadores y todos inicializados en 00:00:00
        val state = viewModel.uiState.value
        assertEquals("Piernas", state.title)
        assertEquals(3, state.exercises.size)
        assertEquals("Sentadillas", state.exercises[0].name)
        assertEquals("Zancadas", state.exercises[1].name)
        assertEquals("Puente de glúteos", state.exercises[2].name)

        // Todos en 00:00:00
        assertEquals(0, state.mainElapsedSeconds)
        state.exercises.forEach {
            assertEquals(0, it.elapsedSeconds)
            assertEquals(1, it.currentRep)
            assertFalse(it.isCompleted)
        }
    }

    @Test
    fun persistence_deletedCountersRemainDeletedWhenReopeningApp() {
        // Requisito: Conservar el estado de los contadores eliminados al volver a entrar
        val initialList = viewModel.uiState.value.exercises
        assertEquals(3, initialList.size)
        val secondId = initialList[1].id

        // Eliminar el segundo ejercicio ("Zancadas")
        viewModel.deleteExercise(secondId)
        assertEquals(2, viewModel.uiState.value.exercises.size)
        assertFalse(viewModel.uiState.value.exercises.any { it.id == secondId })

        // Verificar que se guardó en preferencias
        assertNotNull(fakePreferences.savedExercisesList)
        assertEquals(2, fakePreferences.savedExercisesList!!.size)
        assertFalse(fakePreferences.savedExercisesList!!.any { it.id == secondId })

        // Simular volver a entrar a la app creando un nuevo ViewModel con las mismas preferencias
        val newViewModel = RoutineViewModel(
            preferences = fakePreferences,
            soundPlayer = fakeSoundPlayer,
            coroutineScope = testScope
        )

        val restoredState = newViewModel.uiState.value
        assertEquals(2, restoredState.exercises.size)
        assertFalse(restoredState.exercises.any { it.id == secondId })

        // Verificar que los contadores están en 00:00:00 al volver a ingresar
        restoredState.exercises.forEach {
            assertEquals(0, it.elapsedSeconds)
            assertEquals(1, it.currentRep)
            assertFalse(it.isCompleted)
        }
    }

    @Test
    fun canCreateAtLeastFiveAdditionalCounters() {
        val initialCount = viewModel.uiState.value.exercises.size
        assertEquals(3, initialCount)

        for (i in 1..5) {
            viewModel.addExercise(
                name = "Extra $i",
                targetSeconds = 30 + i * 5,
                badgeColorIndex = i % 5,
                badgeShapeIndex = i % 5,
                totalReps = 2
            )
        }

        val updatedExercises = viewModel.uiState.value.exercises
        assertEquals(8, updatedExercises.size)
        assertTrue(updatedExercises.any { it.name == "Extra 1" })
        assertTrue(updatedExercises.any { it.name == "Extra 5" })
    }

    @Test
    fun canEditMainRoutine_butCannotDeleteIt() {
        viewModel.updateMainRoutine(title = "Rutina Pecho", targetSeconds = 2400)
        val state = viewModel.uiState.value
        assertEquals("Rutina Pecho", state.title)
        assertEquals(2400, state.mainTargetSeconds)
        assertEquals(0, state.mainElapsedSeconds)
        assertFalse(state.isMainRunning)
        // No existe función deleteMainRoutine
    }

    @Test
    fun individualCounterControls_playPauseAndReset() {
        val exercise = viewModel.uiState.value.exercises.first()
        val id = exercise.id

        viewModel.toggleExercisePlay(id)
        assertEquals(id, viewModel.uiState.value.activeRunningExerciseId)

        viewModel.toggleExercisePlay(id)
        assertNull(viewModel.uiState.value.activeRunningExerciseId)

        viewModel.incrementReps(id)
        viewModel.resetExercise(id)
        val resetEx = viewModel.uiState.value.exercises.first { it.id == id }
        assertEquals(0, resetEx.elapsedSeconds)
        assertEquals(1, resetEx.currentRep)
        assertFalse(resetEx.isCompleted)
    }

    @Test
    fun nuclearAlarm_triggersWhenExerciseCompleted_ifEnabled() {
        // Requisito: Sonido de alerta de alarma nuclear
        val exercise = viewModel.uiState.value.exercises.first()
        val id = exercise.id

        viewModel.setAlarmSoundEnabled(true)
        viewModel.setAlarmDurationSeconds(4)
        viewModel.editExercise(
            id = id,
            name = exercise.name,
            targetSeconds = 1,
            badgeColorIndex = 0,
            badgeShapeIndex = 0,
            totalReps = 1
        )
        viewModel.toggleExercisePlay(id)

        // Al completarse la serie:
        viewModel.tickSecond()

        assertTrue(fakeSoundPlayer.isPlaying)
        assertEquals(4, fakeSoundPlayer.playedAlarmDuration)
    }

    @Test
    fun nuclearAlarm_doesNotTrigger_ifDisabled() {
        val exercise = viewModel.uiState.value.exercises.first()
        val id = exercise.id

        viewModel.setAlarmSoundEnabled(false)
        viewModel.editExercise(
            id = id,
            name = exercise.name,
            targetSeconds = 1,
            badgeColorIndex = 0,
            badgeShapeIndex = 0,
            totalReps = 1
        )
        viewModel.toggleExercisePlay(id)

        viewModel.tickSecond()

        assertFalse(fakeSoundPlayer.isPlaying)
        assertNull(fakeSoundPlayer.playedAlarmDuration)
    }

    @Test
    fun settingsScreen_themeModeAndDefaultCountersPersistence() {
        viewModel.setThemeMode(ThemeMode.DARK, isSystemDark = false)
        assertTrue(viewModel.uiState.value.isDarkTheme)
        assertEquals(ThemeMode.DARK, fakePreferences.savedThemeMode)

        viewModel.setThemeMode(ThemeMode.LIGHT, isSystemDark = true)
        assertFalse(viewModel.uiState.value.isDarkTheme)
        assertEquals(ThemeMode.LIGHT, fakePreferences.savedThemeMode)

        viewModel.setDefaultCountersCount(4)
        assertEquals(4, viewModel.uiState.value.defaultCountersCount)
        assertEquals(4, fakePreferences.savedDefaultCount)

        viewModel.resetToDefaultExercises()
        assertEquals(4, viewModel.uiState.value.exercises.size)
    }
}
