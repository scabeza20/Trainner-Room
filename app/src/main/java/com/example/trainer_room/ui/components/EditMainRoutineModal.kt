package com.example.trainer_room.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainer_room.ui.theme.TrainerRoomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMainRoutineModal(
    currentTitle: String,
    currentTargetSeconds: Int,
    onDismissRequest: () -> Unit,
    onSaveMainRoutine: (title: String, targetSeconds: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var routineTitle by remember { mutableStateOf(currentTitle) }

    val initH = currentTargetSeconds / 3600
    val initM = (currentTargetSeconds % 3600) / 60
    val initS = currentTargetSeconds % 60

    var hours by remember { mutableIntStateOf(initH) }
    var minutes by remember { mutableIntStateOf(initM) }
    var seconds by remember { mutableIntStateOf(initS) }

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
                text = "Editar Rutina Principal",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Nombre de la Rutina
            OutlinedTextField(
                value = routineTitle,
                onValueChange = { routineTitle = it },
                label = { Text("Nombre de la rutina") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = TrainerRoomTheme.extendedColors.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fijar Tiempo Objetivo (00:00:00 para conteo libre)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TrainerRoomTheme.extendedColors.onMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Selector Rueda + Teclado para tiempo principal
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

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar Cambios
            Button(
                onClick = {
                    val totalSecs = (hours * 3600) + (minutes * 60) + seconds
                    onSaveMainRoutine(routineTitle, totalSecs)
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
                    text = "Guardar Cambios",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
