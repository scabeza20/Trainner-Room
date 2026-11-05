package com.example.trainer_room.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainer_room.model.ExerciseItem
import com.example.trainer_room.ui.theme.BadgeShapesList
import com.example.trainer_room.ui.theme.TrainerRoomTheme
import com.example.trainer_room.ui.theme.getBadgeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSeriesModal(
    initialExercise: ExerciseItem? = null,
    onDismissRequest: () -> Unit,
    onSaveSeries: (id: String?, name: String, durationSeconds: Int, colorIndex: Int, shapeIndex: Int, totalReps: Int) -> Unit,
    onDeleteSeries: (id: String) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditMode = initialExercise != null

    val initH = (initialExercise?.targetSeconds ?: 0) / 3600
    val initM = if (initialExercise != null) ((initialExercise.targetSeconds % 3600) / 60) else 0
    val initS = if (initialExercise != null) (initialExercise.targetSeconds % 60) else 45

    var exerciseName by remember(initialExercise) { mutableStateOf(initialExercise?.name ?: "") }
    var hours by remember(initialExercise) { mutableIntStateOf(initH) }
    var minutes by remember(initialExercise) { mutableIntStateOf(initM) }
    var seconds by remember(initialExercise) { mutableIntStateOf(initS) }
    var reps by remember(initialExercise) { mutableIntStateOf(initialExercise?.totalReps ?: 3) }

    var selectedColorIndex by remember(initialExercise) { mutableIntStateOf(initialExercise?.badgeColorIndex ?: 0) }
    var selectedShapeIndex by remember(initialExercise) { mutableIntStateOf(initialExercise?.badgeShapeIndex ?: 0) }

    val presetNames = listOf("Sentadillas", "Zancadas", "Puente de glúteos", "Elevación de talones", "Descanso")
    val isDark = TrainerRoomTheme.extendedColors.isDark
    val badgeColors = getBadgeColors(isDark)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Cabecera: Título + Papelera roja a la derecha (como en prototipo-modal.png)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Editar serie" else "Nueva serie",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isEditMode) {
                    IconButton(
                        onClick = {
                            onDeleteSeries(initialExercise!!.id)
                            onDismissRequest()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar serie",
                            tint = TrainerRoomTheme.extendedColors.deleteRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Campo de Nombre
            OutlinedTextField(
                value = exerciseName,
                onValueChange = { exerciseName = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = TrainerRoomTheme.extendedColors.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            // Chips de Sugerencias rápidas
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presetNames) { name ->
                    FilterChip(
                        selected = exerciseName == name,
                        onClick = { exerciseName = name },
                        label = { Text(name, fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TrainerRoomTheme.extendedColors.primaryDim,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Selector Horario con tambor destacado
            TimeWheelPicker(
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                onTimeChange = { h, m, s ->
                    hours = h
                    minutes = m
                    seconds = s
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Selector de Repeticiones y Estilo de Insignia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector de repeticiones de la serie
                Column {
                    Text(
                        text = "Repeticiones",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrainerRoomTheme.extendedColors.onMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(TrainerRoomTheme.extendedColors.surfaceHi)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { if (reps > 1) reps -= 1 },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Menos",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${reps}x",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = { if (reps < 99) reps += 1 },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Más",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Selector de Insignias b1-b5
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Insignia",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrainerRoomTheme.extendedColors.onMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        badgeColors.forEachIndexed { idx, color ->
                            val isSelected = idx == selectedColorIndex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(BadgeShapesList[idx % BadgeShapesList.size])
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = BadgeShapesList[idx % BadgeShapesList.size]
                                    )
                                    .clickable {
                                        selectedColorIndex = idx
                                        selectedShapeIndex = idx % BadgeShapesList.size
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción inferiores: Cancelar y Guardar (prototipo-modal.png)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Cancelar (Tonal)
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrainerRoomTheme.extendedColors.primaryDim,
                        contentColor = TrainerRoomTheme.extendedColors.tonalContent
                    )
                ) {
                    Text(
                        text = "Cancelar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Botón Guardar (Primario)
                Button(
                    onClick = {
                        val totalSecs = (hours * 3600) + (minutes * 60) + seconds
                        onSaveSeries(
                            initialExercise?.id,
                            exerciseName,
                            totalSecs.coerceAtLeast(1),
                            selectedColorIndex,
                            selectedShapeIndex,
                            reps
                        )
                        onDismissRequest()
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
                    Text(
                        text = "Guardar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
