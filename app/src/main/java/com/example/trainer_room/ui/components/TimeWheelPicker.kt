package com.example.trainer_room.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainer_room.ui.theme.TrainerRoomTheme
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelDrumColumn(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 52.dp
) {
    val count = range.count()
    val multiplier = 200
    val middleOffset = (multiplier / 2) * count
    val initialFirstVisible = remember(range) {
        val center = middleOffset + (selectedValue - range.first).coerceIn(0, count - 1)
        (center - 1).coerceAtLeast(0)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialFirstVisible)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val centerIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex + 1
        }
    }

    LaunchedEffect(centerIndex) {
        val calculated = range.first + (centerIndex % count)
        if (calculated != selectedValue) {
            onValueChange(calculated)
        }
    }

    LaunchedEffect(selectedValue) {
        if (!listState.isScrollInProgress) {
            val currentCalculated = range.first + (centerIndex % count)
            if (currentCalculated != selectedValue) {
                val currentBase = (centerIndex / count) * count
                val targetCenter = currentBase + (selectedValue - range.first)
                listState.scrollToItem((targetCenter - 1).coerceAtLeast(0))
            }
        }
    }

    Box(
        modifier = modifier.height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.height(itemHeight * 3)
        ) {
            items(count * multiplier) { index ->
                val value = range.first + (index % count)
                val isSelected = index == centerIndex

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "%02d", value),
                        fontSize = if (isSelected) 32.sp else 26.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            TrainerRoomTheme.extendedColors.onMuted.copy(alpha = 0.40f)
                        },
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
    val itemHeight = 52.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Títulos de columnas: Horas, Minutos, Segundos (como en prototipo-modal.png)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = "Horas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TrainerRoomTheme.extendedColors.onMuted,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Minutos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TrainerRoomTheme.extendedColors.onMuted,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Segundos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TrainerRoomTheme.extendedColors.onMuted,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }

        // Tambor de 3 columnas con banda destacada redondeada al centro
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * 3),
            contentAlignment = Alignment.Center
        ) {
            // Banda destacada redondeada (surfaceHi)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TrainerRoomTheme.extendedColors.surfaceHi)
            )

            // Columnas y separadores ":"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                WheelDrumColumn(
                    range = 0..99,
                    selectedValue = hours,
                    onValueChange = { h -> onTimeChange(h, minutes, seconds) },
                    itemHeight = itemHeight,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = ":",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                WheelDrumColumn(
                    range = 0..59,
                    selectedValue = minutes,
                    onValueChange = { m -> onTimeChange(hours, m, seconds) },
                    itemHeight = itemHeight,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = ":",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                WheelDrumColumn(
                    range = 0..59,
                    selectedValue = seconds,
                    onValueChange = { s -> onTimeChange(hours, minutes, s) },
                    itemHeight = itemHeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
