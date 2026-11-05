package com.example.trainer_room.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainer_room.ui.theme.TrainerRoomTheme
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPickerColumn(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 50.dp
) {
    val count = range.count()
    val initialIndex = (selectedValue - range.first).coerceIn(0, count - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(selectedValue) {
        val targetIndex = (selectedValue - range.first).coerceIn(0, count - 1)
        if (listState.firstVisibleItemIndex != targetIndex && !listState.isScrollInProgress) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) initialIndex
            else {
                val centerOffset = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - centerOffset) }?.index ?: initialIndex
            }
        }
    }

    LaunchedEffect(selectedIndex) {
        val calculatedValue = range.first + selectedIndex
        if (calculatedValue != selectedValue) {
            onValueChange(calculatedValue)
        }
    }

    Box(
        modifier = modifier.height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            modifier = Modifier.height(itemHeight * 3)
        ) {
            items(count) { index ->
                val value = range.first + index
                val isSelected = index == selectedIndex
                val alpha by animateFloatAsState(if (isSelected) 1.0f else 0.35f, label = "alpha")
                val scale by animateFloatAsState(if (isSelected) 1.15f else 0.85f, label = "scale")

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "%02d", value),
                        fontSize = if (isSelected) 36.sp else 28.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier
                            .alpha(alpha)
                            .scale(scale),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TimeWheelPicker(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onTimeChange: (h: Int, m: Int, s: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var hoursText by remember(hours) { mutableStateOf(String.format(Locale.US, "%02d", hours)) }
    var minutesText by remember(minutes) { mutableStateOf(String.format(Locale.US, "%02d", minutes)) }
    var secondsText by remember(seconds) { mutableStateOf(String.format(Locale.US, "%02d", seconds)) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezados superiores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = "Hours", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text(text = "Minutes", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text(text = "Seconds", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }

        // Ruedas selectoras (Deslizado)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            WheelPickerColumn(
                range = 0..23,
                selectedValue = hours,
                onValueChange = { h -> onTimeChange(h, minutes, seconds) },
                modifier = Modifier.weight(1f)
            )

            Text(
                text = ":",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            WheelPickerColumn(
                range = 0..59,
                selectedValue = minutes,
                onValueChange = { m -> onTimeChange(hours, m, seconds) },
                modifier = Modifier.weight(1f)
            )

            Text(
                text = ":",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            WheelPickerColumn(
                range = 0..59,
                selectedValue = seconds,
                onValueChange = { s -> onTimeChange(hours, minutes, s) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Entradas manuales de texto (Escribir el tiempo directamente)
        Text(
            text = "O escribe el tiempo deseado:",
            fontSize = 12.sp,
            color = TrainerRoomTheme.extendedColors.onMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campo Horas
            OutlinedTextField(
                value = hoursText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(2)
                    hoursText = filtered
                    val hVal = filtered.toIntOrNull()?.coerceIn(0, 23) ?: 0
                    onTimeChange(hVal, minutes, seconds)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(68.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = TrainerRoomTheme.extendedColors.outline,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Text(text = ":", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp))

            // Campo Minutos
            OutlinedTextField(
                value = minutesText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(2)
                    minutesText = filtered
                    val mVal = filtered.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    onTimeChange(hours, mVal, seconds)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(68.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = TrainerRoomTheme.extendedColors.outline,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Text(text = ":", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp))

            // Campo Segundos
            OutlinedTextField(
                value = secondsText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(2)
                    secondsText = filtered
                    val sVal = filtered.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    onTimeChange(hours, minutes, sVal)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(68.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = TrainerRoomTheme.extendedColors.outline,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
    }
}
