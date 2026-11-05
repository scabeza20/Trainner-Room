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
    var savedAlarmEachRound: Boolean = true
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

    override fun isAlarmOnEachRound(): Boolean = savedAlarmEachRound
    override fun saveAlarmOnEachRound(enabled: Boolean) {
        savedAlarmEachRound = enabled
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
        val state = viewModel.uiState.value
        assertEquals("Piernas", state.title)
        assertEquals(3, state.exercises.size)
        assertEquals("Sentadillas", state.exercises[0].name)
        assertEquals("Zancadas", state.exercises[1].name)
        assertEquals("Puente de glúteos", state.exercises[2].name)

        assertEquals(0, state.mainElapsedSeconds)
        state.exercises.forEach {
            assertEquals(0, it.elapsedSeconds)
            assertEquals(1, it.currentRep)
            assertFalse(it.isCompleted)
        }
    }

    @Test
    fun persistence_deletedCountersRemainDeletedWhenReopeningApp() {
        val initialList = viewModel.uiState.value.exercises
        assertEquals(3, initialList.size)
        val secondId = initialList[1].id

        viewModel.deleteExercise(secondId)
        assertEquals(2, viewModel.uiState.value.exercises.size)
        assertFalse(viewModel.uiState.value.exercises.any { it.id == secondId })

        assertNotNull(fakePreferences.savedExercisesList)
        assertEquals(2, fakePreferences.savedExercisesList!!.size)
        assertFalse(fakePreferences.savedExercisesList!!.any { it.id == secondId })

        val newViewModel = RoutineViewModel(
            preferences = fakePreferences,
            soundPlayer = fakeSoundPlayer,
            coroutineScope = testScope
        )

        val restoredState = newViewModel.uiState.value
        assertEquals(2, restoredState.exercises.size)
        assertFalse(restoredState.exercises.any { it.id == secondId })

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
    fun alarmTrigger_eachRoundVsOnlyFinalRound_persistence() {
        val exercise = viewModel.uiState.value.exercises.first()
        val id = exercise.id

        viewModel.setAlarmSoundEnabled(true)
        viewModel.setAlarmDurationSeconds(3)
        viewModel.editExercise(
            id = id,
            name = exercise.name,
            targetSeconds = 2,
            badgeColorIndex = 0,
            badgeShapeIndex = 0,
            totalReps = 2
        )

        // 1. Caso alarmOnEachRound = true (debe sonar en cada ronda)
        viewModel.setAlarmOnEachRound(true)
        assertTrue(viewModel.uiState.value.alarmOnEachRound)
        assertTrue(fakePreferences.savedAlarmEachRound)

        viewModel.toggleExercisePlay(id)
        viewModel.tickSecond() // 1s
        assertFalse(fakeSoundPlayer.isPlaying)

        viewModel.tickSecond() // 2s -> completa ronda 1, pasa a ronda 2
        assertTrue(fakeSoundPlayer.isPlaying)
        fakeSoundPlayer.isPlaying = false

        viewModel.tickSecond() // 1s de ronda 2
        assertFalse(fakeSoundPlayer.isPlaying)

        viewModel.tickSecond() // 2s de ronda 2 -> completa serie final
        assertTrue(fakeSoundPlayer.isPlaying)
        fakeSoundPlayer.isPlaying = false

        // 2. Caso alarmOnEachRound = false (NO debe sonar en ronda 1, SOLO en ronda final)
        viewModel.setAlarmOnEachRound(false)
        assertFalse(viewModel.uiState.value.alarmOnEachRound)
        assertFalse(fakePreferences.savedAlarmEachRound)

        viewModel.resetExercise(id)
        viewModel.toggleExercisePlay(id)

        viewModel.tickSecond() // 1s
        viewModel.tickSecond() // 2s -> completa ronda 1
        assertFalse(fakeSoundPlayer.isPlaying) // NO debe sonar

        viewModel.tickSecond() // 1s de ronda 2
        viewModel.tickSecond() // 2s de ronda 2 -> completa serie final
        assertTrue(fakeSoundPlayer.isPlaying) // SÍ debe sonar

        // 3. Verificar que persiste al recrear el ViewModel
        val restoredVm = RoutineViewModel(
            preferences = fakePreferences,
            soundPlayer = fakeSoundPlayer,
            coroutineScope = testScope
        )
        assertFalse(restoredVm.uiState.value.alarmOnEachRound)
    }

    @Test
    fun silenceCurrentAlarm_stopsAudioWithoutDisablingFutureAlarms() {
        val exercise = viewModel.uiState.value.exercises.first()
        val id = exercise.id

        viewModel.setAlarmSoundEnabled(true)
        viewModel.editExercise(
            id = id,
            name = exercise.name,
            targetSeconds = 1,
            badgeColorIndex = 0,
            badgeShapeIndex = 0,
            totalReps = 1
        )
        viewModel.toggleExercisePlay(id)
        viewModel.tickSecond() // Detona la alarma

        assertTrue(viewModel.uiState.value.isAlarmPlaying)
        assertTrue(fakeSoundPlayer.isPlaying)

        // Silenciar alerta actual
        viewModel.silenceCurrentAlarm()

        assertFalse(viewModel.uiState.value.isAlarmPlaying)
        assertFalse(fakeSoundPlayer.isPlaying)
        assertTrue(viewModel.uiState.value.alarmSoundEnabled) // Sigue habilitado para posteriores
    }

    @Test
    fun silenceAndResetAllToZero_stopsAudio_disablesFutureAlarms_andResetsCounters() {
        val exercise = viewModel.uiState.value.exercises.first()
        val id = exercise.id

        viewModel.setAlarmSoundEnabled(true)
        viewModel.toggleMainPlayPause()
        viewModel.toggleExercisePlay(id)

        viewModel.tickSecond()
        viewModel.tickSecond()

        // Poner en marcha alarma
        viewModel.testAlarmSound()
        assertTrue(viewModel.uiState.value.isAlarmPlaying)
        assertTrue(fakeSoundPlayer.isPlaying)

        // Parada de emergencia: silenciar todo y poner a 0
        viewModel.silenceAndResetAllToZero(disableFutureAlarms = true)

        assertFalse(viewModel.uiState.value.isAlarmPlaying)
        assertFalse(fakeSoundPlayer.isPlaying)
        assertFalse(viewModel.uiState.value.alarmSoundEnabled)
        assertFalse(fakePreferences.savedAlarmEnabled)
        assertEquals(0, viewModel.uiState.value.mainElapsedSeconds)
        assertFalse(viewModel.uiState.value.isMainRunning)
        assertNull(viewModel.uiState.value.activeRunningExerciseId)
        viewModel.uiState.value.exercises.forEach {
            assertEquals(0, it.elapsedSeconds)
            assertEquals(1, it.currentRep)
            assertFalse(it.isCompleted)
        }
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
