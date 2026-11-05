package com.example.trainer_room.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainer_room.model.ExerciseItem
import com.example.trainer_room.ui.RoutineViewModel
import com.example.trainer_room.ui.components.AddSeriesModal
import com.example.trainer_room.ui.components.EditMainRoutineModal
import com.example.trainer_room.ui.theme.BadgeShapesList
import com.example.trainer_room.ui.theme.TrainerRoomTheme
import com.example.trainer_room.ui.theme.getBadgeColors
import java.util.Locale

@Composable
fun AndroidBotIcon(
    modifier: Modifier = Modifier,
    tint: Color = TrainerRoomTheme.extendedColors.androidGreen
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Cabeza
        val path = Path().apply {
            moveTo(w * 0.1f, h * 0.75f)
            lineTo(w * 0.9f, h * 0.75f)
            arcTo(
                rect = Rect(w * 0.1f, h * 0.25f, w * 0.9f, h * 0.85f),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(path = path, color = tint)

        // Antenas
        drawLine(
            color = tint,
            start = Offset(w * 0.32f, h * 0.38f),
            end = Offset(w * 0.18f, h * 0.12f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.68f, h * 0.38f),
            end = Offset(w * 0.82f, h * 0.12f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Ojos
        drawCircle(
            color = Color.White,
            radius = 1.8.dp.toPx(),
            center = Offset(w * 0.35f, h * 0.5f)
        )
        drawCircle(
            color = Color.White,
            radius = 1.8.dp.toPx(),
            center = Offset(w * 0.65f, h * 0.5f)
        )
    }
}

@Composable
fun RoutineScreen(
    viewModel: RoutineViewModel,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showModal by remember { mutableStateOf(false) }
    var showEditMainModal by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<ExerciseItem?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Barra superior con Menú a la izquierda y Título Centrado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menú a la izquierda en vez del logo de Android
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menú de ajustes",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Título de la rutina centrado
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showEditMainModal = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AndroidBotIcon(
                        tint = TrainerRoomTheme.extendedColors.androidGreen,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = state.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }

                // Espaciador balanceador a la derecha (48dp para centrado perfecto)
                Spacer(modifier = Modifier.size(48.dp))
            }
        },
        bottomBar = {
            // Barra inferior fija con botones "+ Serie" e "Iniciar / Pausar"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Tonal + Serie
                Button(
                    onClick = {
                        exerciseToEdit = null
                        showModal = true
                    },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrainerRoomTheme.extendedColors.primaryDim,
                        contentColor = TrainerRoomTheme.extendedColors.tonalContent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir serie",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Serie",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Botón Relleno Iniciar / Pausar para la serie activa
                val isAnyRunning = state.activeRunningExerciseId != null
                val targetExerciseId = state.activeRunningExerciseId
                    ?: state.activeExerciseId
                    ?: state.exercises.firstOrNull()?.id

                Button(
                    onClick = {
                        if (targetExerciseId != null) {
                            viewModel.toggleExercisePlay(targetExerciseId)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isAnyRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isAnyRunning) "Pausar" else "Iniciar",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAnyRunning) "Pausar serie" else "Iniciar serie",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tarjeta Contador Total (Centrada)
            TotalTimeCard(
                mainTargetSeconds = state.mainTargetSeconds,
                mainElapsedSeconds = state.mainElapsedSeconds,
                isMainRunning = state.isMainRunning,
                activeExerciseIndex = state.activeExerciseIndex,
                totalExercises = state.exercises.size,
                onToggleMainPlay = { viewModel.toggleMainPlayPause() },
                onResetMain = { viewModel.resetMainCounter() },
                onClick = { showEditMainModal = true }
            )

            // Lista de Ejercicios Inferior con claves estables
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.exercises, key = { _, exercise -> exercise.id }) { index, exercise ->
                    val isRunning = state.activeRunningExerciseId == exercise.id
                    val isActive = state.activeExerciseId == exercise.id || isRunning

                    ExerciseRow(
                        index = index + 1,
                        exercise = exercise,
                        isActive = isActive,
                        isRunning = isRunning,
                        isDark = state.isDarkTheme,
                        onEdit = {
                            exerciseToEdit = exercise
                            showModal = true
                        },
                        onTogglePlay = {
                            viewModel.toggleExercisePlay(exercise.id)
                        },
                        onReset = {
                            viewModel.resetExercise(exercise.id)
                        },
                        onIncrementReps = {
                            viewModel.incrementReps(exercise.id)
                        },
                        onDecrementReps = {
                            viewModel.decrementReps(exercise.id)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Modal para añadir o editar series (prototipo-modal.png)
    if (showModal) {
        AddSeriesModal(
            initialExercise = exerciseToEdit,
            onDismissRequest = {
                showModal = false
                exerciseToEdit = null
            },
            onSaveSeries = { id, name, duration, colorIdx, shapeIdx, totalReps ->
                if (id == null) {
                    viewModel.addExercise(name, duration, colorIdx, shapeIdx, totalReps)
                } else {
                    viewModel.editExercise(id, name, duration, colorIdx, shapeIdx, totalReps)
                }
            },
            onDeleteSeries = { id ->
                viewModel.deleteExercise(id)
            }
        )
    }

    // Modal para editar la rutina principal (sin opción de eliminar)
    if (showEditMainModal) {
        EditMainRoutineModal(
            currentTitle = state.title,
            currentTargetSeconds = state.mainTargetSeconds,
            onDismissRequest = { showEditMainModal = false },
            onSaveMainRoutine = { title, targetSecs ->
                viewModel.updateMainRoutine(title, targetSecs)
            }
        )
    }
}

@Composable
fun RoundsIndicator(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val safeTotal = totalCount.coerceAtLeast(1)
        val safeCurrent = currentIndex.coerceIn(0, safeTotal - 1)

        for (i in 0 until safeTotal) {
            val isActive = i == safeCurrent
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(width = 28.dp, height = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TrainerRoomTheme.extendedColors.primaryDim)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Serie ${safeCurrent + 1} de $safeTotal",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TotalTimeCard(
    mainTargetSeconds: Int,
    mainElapsedSeconds: Int,
    isMainRunning: Boolean,
    activeExerciseIndex: Int,
    totalExercises: Int,
    onToggleMainPlay: () -> Unit,
    onResetMain: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displaySeconds = if (mainTargetSeconds > 0) {
        (mainTargetSeconds - mainElapsedSeconds).coerceAtLeast(0)
    } else {
        mainElapsedSeconds
    }

    val h = displaySeconds / 3600
    val m = (displaySeconds % 3600) / 60
    val s = displaySeconds % 60

    val onMuted = TrainerRoomTheme.extendedColors.onMuted

    val annotatedTimeString = buildAnnotatedString {
        withStyle(SpanStyle(fontSize = 56.sp, fontWeight = FontWeight.Normal)) {
            append(String.format(Locale.US, "%02d", h))
        }
        withStyle(SpanStyle(fontSize = 15.sp, color = onMuted)) {
            append("h ")
        }
        withStyle(SpanStyle(fontSize = 56.sp, fontWeight = FontWeight.Normal)) {
            append(String.format(Locale.US, "%02d", m))
        }
        withStyle(SpanStyle(fontSize = 15.sp, color = onMuted)) {
            append("m ")
        }
        withStyle(SpanStyle(fontSize = 56.sp, fontWeight = FontWeight.Normal)) {
            append(String.format(Locale.US, "%02d", s))
        }
        withStyle(SpanStyle(fontSize = 15.sp, color = onMuted)) {
            append("s")
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Tiempo total de la rutina",
                fontSize = 13.sp,
                color = onMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = annotatedTimeString,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Indicador de rondas / series centrado
            RoundsIndicator(
                currentIndex = activeExerciseIndex,
                totalCount = totalExercises
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Controles del contador de sesión centrados
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onToggleMainPlay,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrainerRoomTheme.extendedColors.primaryDim,
                        contentColor = TrainerRoomTheme.extendedColors.tonalContent
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = if (isMainRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isMainRunning) "Pausar sesión" else "Iniciar sesión",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMainRunning) "Pausar sesión" else "Iniciar sesión",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (mainElapsedSeconds > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onResetMain,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reiniciar sesión",
                            tint = onMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseRow(
    index: Int,
    exercise: ExerciseItem,
    isActive: Boolean,
    isRunning: Boolean,
    isDark: Boolean,
    onEdit: () -> Unit,
    onTogglePlay: () -> Unit,
    onReset: () -> Unit,
    onIncrementReps: () -> Unit,
    onDecrementReps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColors = getBadgeColors(isDark)
    val badgeColor = badgeColors[exercise.badgeColorIndex % badgeColors.size]
    val badgeShape = BadgeShapesList[exercise.badgeShapeIndex % BadgeShapesList.size]

    val secs = if (exercise.isCompleted) exercise.targetSeconds else (exercise.targetSeconds - exercise.elapsedSeconds).coerceAtLeast(0)
    val h = secs / 3600
    val m = (secs % 3600) / 60
    val s = secs % 60

    val onMuted = TrainerRoomTheme.extendedColors.onMuted
    val okColor = TrainerRoomTheme.extendedColors.ok
    val onShapeColor = TrainerRoomTheme.extendedColors.onShape

    val formattedRowTime = buildAnnotatedString {
        withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Normal)) {
            append(String.format(Locale.US, "%02d", h))
        }
        withStyle(SpanStyle(fontSize = 12.sp, color = onMuted)) {
            append("h ")
        }
        withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Normal)) {
            append(String.format(Locale.US, "%02d", m))
        }
        withStyle(SpanStyle(fontSize = 12.sp, color = onMuted)) {
            append("m ")
        }
        withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Normal)) {
            append(String.format(Locale.US, "%02d", s))
        }
        withStyle(SpanStyle(fontSize = 12.sp, color = onMuted)) {
            append("s")
        }
    }

    val rowBackground = if (isActive) TrainerRoomTheme.extendedColors.surfaceHi else MaterialTheme.colorScheme.surface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(rowBackground)
            .then(
                if (isActive) Modifier.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                ) else Modifier
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Insignia Numérica b1-b5 con color de texto --on-shape (index.html)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(badgeShape)
                .background(badgeColor)
                .clickable { onEdit() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = onShapeColor
            )
        }

        // Info del ejercicio (Nombre y Tiempo)
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onEdit() }
        ) {
            Text(
                text = exercise.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = if (exercise.isCompleted) onMuted else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formattedRowTime,
                color = if (exercise.isCompleted) onMuted else MaterialTheme.colorScheme.onSurface
            )
        }

        // Stepper de Repeticiones [-] 1/3 [+]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = onDecrementReps,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Disminuir repeticiones",
                    modifier = Modifier.size(14.dp),
                    tint = onMuted
                )
            }

            Text(
                text = "${exercise.currentRep}/${exercise.totalReps}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (exercise.isCompleted) onMuted else MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = onIncrementReps,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aumentar repeticiones",
                    modifier = Modifier.size(14.dp),
                    tint = onMuted
                )
            }
        }

        // Botón de Reinicio Individual (↺)
        IconButton(
            onClick = onReset,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = "Reiniciar serie",
                tint = onMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        // Botón de Acción de Fila (Play / Pause / Check)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        exercise.isCompleted -> Color.Transparent
                        isRunning -> MaterialTheme.colorScheme.primary
                        else -> TrainerRoomTheme.extendedColors.primaryDim
                    }
                )
                .clickable { onTogglePlay() },
            contentAlignment = Alignment.Center
        ) {
            when {
                exercise.isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completado",
                        tint = okColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                isRunning -> {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pausar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Iniciar",
                        tint = TrainerRoomTheme.extendedColors.tonalContent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Oscuro")
@Composable
fun RoutineScreenDarkPreview() {
    val viewModel = remember { RoutineViewModel() }
    RoutineScreen(viewModel = viewModel)
}
