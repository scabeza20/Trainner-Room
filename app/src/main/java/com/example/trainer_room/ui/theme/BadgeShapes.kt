package com.example.trainer_room.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// Formas de insignia especificadas en el prototipo b1 - b5
// CSS: top-left top-right bottom-right bottom-left
val BadgeShape1 = RoundedCornerShape(topStart = 14.dp, topEnd = 22.dp, bottomEnd = 14.dp, bottomStart = 22.dp)
val BadgeShape2 = CircleShape
val BadgeShape3 = RoundedCornerShape(topStart = 12.dp, topEnd = 20.dp, bottomEnd = 12.dp, bottomStart = 20.dp)
val BadgeShape4 = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 8.dp)
val BadgeShape5 = RoundedCornerShape(14.dp)

val BadgeShapesList: List<Shape> = listOf(
    BadgeShape1,
    BadgeShape2,
    BadgeShape3,
    BadgeShape4,
    BadgeShape5
)

@Composable
@ReadOnlyComposable
fun getBadgeColors(isDark: Boolean): List<Color> {
    return if (isDark) {
        listOf(
            DarkBadgeCoral,
            DarkBadgeYellow,
            DarkBadgeGreen,
            DarkBadgeBlue,
            DarkBadgeSky
        )
    } else {
        listOf(
            LightBadgeCoral,
            LightBadgeYellow,
            LightBadgeGreen,
            LightBadgeBlue,
            LightBadgeSky
        )
    }
}
