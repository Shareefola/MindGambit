package com.mindgambit.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================
// MindGambit — Shape System
// Soft, rounded corners — premium feel without bubble excess.
// ============================================================
val MindGambitShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(14.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// Convenience
val ShapeCard      = RoundedCornerShape(20.dp)
val ShapeChip      = RoundedCornerShape(50.dp)
val ShapeButton    = RoundedCornerShape(14.dp)
val ShapeBottomBar = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
val ShapeDialog    = RoundedCornerShape(24.dp)
