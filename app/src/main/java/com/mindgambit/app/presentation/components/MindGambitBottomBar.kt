package com.mindgambit.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.mindgambit.app.presentation.theme.*

// ============================================================
// MindGambit — Bottom Navigation Bar
// Central FAB train button. Animated active states.
// ============================================================

@Composable
fun MindGambitBottomBar(
    currentRoute:    String,
    onHomeClick:     () -> Unit,
    onOpeningsClick: () -> Unit,
    onTrainClick:    () -> Unit,
    onTacticsClick:  () -> Unit,
    onLadderClick:   () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Color.White,
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .border(
                1.dp,
                Color(0xFFEEEAE0),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            NavItem(
                icon      = "⌂",
                label     = "Home",
                isActive  = currentRoute == "home",
                onClick   = onHomeClick
            )
            NavItem(
                icon     = "♟",
                label    = "Open",
                isActive = currentRoute == "openings",
                onClick  = onOpeningsClick
            )

            // Central FAB
            TrainFab(onClick = onTrainClick)

            NavItem(
                icon     = "🧩",
                label    = "Tactics",
                isActive = currentRoute == "tactics",
                onClick  = onTacticsClick
            )
            NavItem(
                icon     = "▲",
                label    = "Ladder",
                isActive = currentRoute == "ladder",
                onClick  = onLadderClick
            )
        }
    }
}

@Composable
private fun NavItem(
    icon:     String,
    label:    String,
    isActive: Boolean,
    onClick:  () -> Unit
) {
    val iconColor by animateColorAsState(
        if (isActive) Gold else TextTertiary,
        label = "navColor"
    )
    val scale by animateFloatAsState(
        if (isActive) 1.08f else 1f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "navScale"
    )

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(icon, fontSize = 20.sp, color = iconColor)
        Text(
            label,
            fontFamily = DmMono,
            fontSize   = 8.sp,
            letterSpacing = 0.5.sp,
            color      = iconColor,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun TrainFab(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.92f else 1f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "fabScale"
    )

    Box(
        modifier = Modifier
            .size(54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Gold, Color(0xFF8B5E3C))
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) {
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text("▶", fontSize = 20.sp, color = Color.White)
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(150)
            pressed = false
        }
    }
}
