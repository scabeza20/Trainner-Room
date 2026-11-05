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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.trainer_room.ui.theme.DarkBadgeGreen
import com.example.trainer_room.ui.theme.LightBadgeGreen
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
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showModal by remember { mutableStateOf(false) }
    var showEditMainModal by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<ExerciseItem?>(null) }

    TrainerRoomTheme(darkTheme = state.isDarkTheme) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                // Barra superior de la app
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, top = 40.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { showEditMainModal = true }
                    ) {
                        AndroidBotIcon(tint = TrainerRoomTheme.extendedColors.androidGreen)
                        Text(
                            text = state.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Selector de tema Claro / Oscuro
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (state.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            },
            bottomBar = {
                // Barra inferior fija para la lista de ejercicios
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
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
                            contentColor = MaterialTheme.colorScheme.primary
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

                    // Botón Relleno Pausar / Iniciar para la rutina de abajo
                    Button(
                        onClick = { viewModel.togglePlayPause() },
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
                            imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isRunning) "Pausar" else "Iniciar",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isRunning) "Pausar serie" else "Iniciar serie",
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
                // Tarjeta Contador Total Independiente
                TotalTimeCard(
                    mainTargetSeconds = state.mainTargetSeconds,
                    mainElapsedSeconds = state.mainElapsedSeconds,
                    isMainRunning = state.isMainRunning,
                    onToggleMainPlay = { viewModel.toggleMainPlayPause() },
                    onClick = { showEditMainModal = true }
                )

                // Lista de Ejercicios Inferior
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.exercises) { index, exercise ->
                        val isActive = index == state.activeExerciseIndex
                        ExerciseRow(
                            index = index + 1,
                            exercise = exercise,
                            isActive = isActive,
                            isGlobalRunning = state.isRunning,
                            isDark = state.isDarkTheme,
                            onEdit = { 
                                exerciseToEdit = exercise
                                showModal = true
                            },
                            onTogglePlay = {
                                if (isActive) {
                                    viewModel.togglePlayPause()
                                } else {
                                    viewModel.selectExercise(index)
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Modal para añadir o editar series
        if (showModal) {
            AddSeriesModal(
                initialExercise = exerciseToEdit,
                onDismissRequest = { 
                    showModal = false
                    exerciseToEdit = null
                },
                onSaveSeries = { id, name, duration, colorIdx, shapeIdx ->
                    if (id == null) {
                        viewModel.addExercise(name, duration, colorIdx, shapeIdx)
                    } else {
                        viewModel.editExercise(id, name, duration, colorIdx, shapeIdx)
                    }
                },
                onDeleteSeries = { id ->
                    viewModel.deleteExercise(id)
                }
            )
        }

        // Modal para editar la rutina principal
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
}

@Composable
fun TotalTimeCard(
    mainTargetSeconds: Int,
    mainElapsedSeconds: Int,
    isMainRunning: Boolean,
    onToggleMainPlay: () -> Unit,
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
        withStyle(SpanStyle(fontSize = 52.sp, fontWeight = FontWeight.Normal)) {
            append(String.format(Locale.US, "%02d", h))
        }
        withStyle(SpanStyle(fontSize = 15.sp, color = onMuted)) {
            append("h ")
        }
        withStyle(SpanStyle(fontSize = 52.sp, fontWeight = FontWeight.Normal)) {
            append(String.format(Locale.US, "%02d", m))
        }
        withStyle(SpanStyle(fontSize = 15.sp, color = onMuted)) {
            append("m ")
        }
        withStyle(SpanStyle(fontSize = 52.sp, fontWeight = FontWeight.Normal)) {
            append(String.format(Locale.US, "%02d", s))
        }
        withStyle(SpanStyle(fontSize = 15.sp, color = onMuted)) {
            append("s")
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tiempo total de la rutina",
                fontSize = 13.sp,
                color = onMuted
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(text = annotatedTimeString)

            Spacer(modifier = Modifier.height(10.dp))

            // Botón pequeño de inicio / pausa para el contador principal independiente
            Button(
                onClick = onToggleMainPlay,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrainerRoomTheme.extendedColors.primaryDim,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = if (isMainRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isMainRunning) "Pausar" else "Iniciar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMainRunning) "Pausar" else "Iniciar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ExerciseRow(
    index: Int,
    exercise: ExerciseItem,
    isActive: Boolean,
    isGlobalRunning: Boolean,
    isDark: Boolean,
    onEdit: () -> Unit,
    onTogglePlay: () -> Unit,
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
    val okColor = if (isDark) DarkBadgeGreen else LightBadgeGreen

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

    val isRowRunning = isActive && isGlobalRunning
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
            .clickable { onEdit() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Badge Numérica
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(badgeShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Info del ejercicio
        Column(
            modifier = Modifier.weight(1f)
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

        // Botón de Acción de Fila
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        exercise.isCompleted -> Color.Transparent
                        isRowRunning -> MaterialTheme.colorScheme.primary
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
                isRowRunning -> {
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
                        tint = MaterialTheme.colorScheme.primary,
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

@Preview(showBackground = true, name = "Claro")
@Composable
fun RoutineScreenLightPreview() {
    val viewModel = remember {
        RoutineViewModel().apply {
            toggleTheme()
        }
    }
    RoutineScreen(viewModel = viewModel)
}
