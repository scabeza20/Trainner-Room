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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedButton
import com.example.trainer_room.model.ExerciseItem
import com.example.trainer_room.ui.theme.BadgeShapesList
import com.example.trainer_room.ui.theme.TrainerRoomTheme
import com.example.trainer_room.ui.theme.getBadgeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSeriesModal(
    initialExercise: ExerciseItem? = null,
    onDismissRequest: () -> Unit,
    onSaveSeries: (id: String?, name: String, durationSeconds: Int, colorIndex: Int, shapeIndex: Int) -> Unit,
    onDeleteSeries: (id: String) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val initH = (initialExercise?.targetSeconds ?: 0) / 3600
    val initM = if (initialExercise != null) ((initialExercise.targetSeconds % 3600) / 60) else 1
    val initS = (initialExercise?.targetSeconds ?: 0) % 60

    var exerciseName by remember(initialExercise) { mutableStateOf(initialExercise?.name ?: "") }
    var hours by remember(initialExercise) { mutableIntStateOf(initH) }
    var minutes by remember(initialExercise) { mutableIntStateOf(initM) }
    var seconds by remember(initialExercise) { mutableIntStateOf(initS) }

    var selectedColorIndex by remember(initialExercise) { mutableIntStateOf(initialExercise?.badgeColorIndex ?: 0) }
    var selectedShapeIndex by remember(initialExercise) { mutableIntStateOf(initialExercise?.badgeShapeIndex ?: 0) }
    
    val isEditMode = initialExercise != null

    val presetNames = listOf("Sentadillas", "Zancadas", "Puente de glúteos", "Elevación de talones", "Descanso")
    val isDark = TrainerRoomTheme.extendedColors.isDark
    val badgeColors = getBadgeColors(isDark)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = if (isEditMode) "Editar Serie" else "Nueva Serie",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Campo de Nombre
            OutlinedTextField(
                value = exerciseName,
                onValueChange = { exerciseName = it },
                label = { Text("Nombre del ejercicio o descanso") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = TrainerRoomTheme.extendedColors.outline
                )
            )

            // Chips de Sugerencias
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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

            // Selector de tiempo Rueda (TimeWheelPicker)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF151617))
                    .padding(8.dp)
            ) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Color e Insignia
            Text(
                text = "Estilo de Insignia",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TrainerRoomTheme.extendedColors.onMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    badgeColors.forEachIndexed { idx, color ->
                        val isSelected = idx == selectedColorIndex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Añadir / Guardar
            Button(
                onClick = {
                    val totalSecs = (hours * 3600) + (minutes * 60) + seconds
                    onSaveSeries(initialExercise?.id, exerciseName, totalSecs, selectedColorIndex, selectedShapeIndex)
                    onDismissRequest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (isEditMode) "Guardar cambios" else "Añadir a la rutina",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isEditMode) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        onDeleteSeries(initialExercise!!.id)
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar serie", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
