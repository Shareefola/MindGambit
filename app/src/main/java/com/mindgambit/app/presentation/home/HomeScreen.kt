package com.mindgambit.app.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindgambit.app.domain.model.*
import com.mindgambit.app.presentation.components.*
import com.mindgambit.app.presentation.theme.*

// ============================================================
// MindGambit — Home Screen
// ============================================================

@Composable
fun HomeScreen(
    onNavigateToBoard:    (String) -> Unit,
    onNavigateToTactics:  () -> Unit,
    onNavigateToOpenings: () -> Unit,
    onNavigateToLadder:   () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            MindGambitBottomBar(
                currentRoute         = "home",
                onHomeClick          = {},
                onOpeningsClick      = onNavigateToOpenings,
                onTrainClick         = { onNavigateToBoard("RAPID") },
                onTacticsClick       = onNavigateToTactics,
                onLadderClick        = onNavigateToLadder
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ───────────────────────────────────────
            HomeTopBar()

            // ── Content with staggered entry animations ───────
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(8.dp))

                    // Elo Hero Card
                    EloHeroCard(
                        uiState       = uiState,
                        onLadderClick = onNavigateToLadder
                    )

                    Spacer(Modifier.height(16.dp))

                    // Stats row
                    StatsRow(uiState = uiState)

                    Spacer(Modifier.height(20.dp))

                    // Section: Train Today
                    SectionTitle("Train Today")
                    Spacer(Modifier.height(10.dp))

                    QuickActionsGrid(
                        onPlayRapid   = { onNavigateToBoard("RAPID") },
                        onPlayBlitz   = { onNavigateToBoard("BLITZ") },
                        onTactics     = onNavigateToTactics,
                        onOpenings    = onNavigateToOpenings
                    )

                    Spacer(Modifier.height(20.dp))

                    // Decision Protocol status
                    SectionTitle("Decision Protocol")
                    Spacer(Modifier.height(10.dp))
                    DecisionProtocolCard()

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text       = "Mind",
                fontFamily = CormorantGaramond,
                fontWeight = FontWeight.Bold,
                fontSize   = 26.sp,
                color      = Gold,
                modifier   = Modifier.offset(y = 2.dp)
            )
        }
        // Inline to get italic on second word
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text       = "Mind",
                fontFamily = CormorantGaramond,
                fontWeight = FontWeight.Bold,
                fontSize   = 26.sp,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text       = "Gambit",
                fontFamily = CormorantGaramond,
                fontWeight = FontWeight.Bold,
                fontStyle  = FontStyle.Italic,
                fontSize   = 26.sp,
                color      = Gold
            )
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    Brush.linearGradient(listOf(Gold, Color(0xFF8B5E3C))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun EloHeroCard(
    uiState:      HomeUiState,
    onLadderClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1A1814), Color(0xFF3A2D1A))
                )
            )
            .clickable { onLadderClick() }
            .padding(24.dp)
    ) {
        // Background queen watermark
        Text(
            text     = "♛",
            fontSize = 110.sp,
            color    = Color(0x10B8862E),
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 12.dp, y = 12.dp)
        )

        Column {
            Text(
                text       = "RAPID RATING",
                fontFamily = DmMono,
                fontSize   = 9.sp,
                letterSpacing = 2.5.sp,
                color      = Gold.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(4.dp))

            // Animated Elo number
            val animatedRating by animateIntAsState(
                targetValue   = uiState.rapidRating,
                animationSpec = tween(durationMillis = 1200, easing = EaseOut),
                label         = "ratingAnim"
            )
            Text(
                text       = "$animatedRating",
                fontFamily = CormorantGaramond,
                fontWeight = FontWeight.Bold,
                fontSize   = 58.sp,
                color      = Color.White,
                lineHeight = 58.sp
            )

            // Tier badge
            val tier = Tier.fromRating(uiState.rapidRating)
            Row(
                modifier = Modifier
                    .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(tier.badge, fontSize = 11.sp)
                Text(
                    text       = tier.displayName.uppercase(),
                    fontFamily = DmMono,
                    fontSize   = 9.sp,
                    letterSpacing = 1.5.sp,
                    color      = GoldLight
                )
            }

            Spacer(Modifier.height(14.dp))

            // Progress bar to next tier
            val nextTier = tier.let {
                val next = it.ordinal + 1
                if (next < Tier.entries.size) Tier.entries[next] else null
            }
            if (nextTier != null) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("${tier.minRating}", fontFamily = DmMono, fontSize = 9.sp, color = Color.White.copy(0.4f))

                    val progress = tier.progressFraction(uiState.rapidRating)
                    val animatedProgress by animateFloatAsState(
                        targetValue   = progress,
                        animationSpec = tween(1400, easing = EaseOut),
                        label         = "progressAnim"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                            .height(3.dp)
                            .background(Color.White.copy(0.1f), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .background(
                                    Brush.horizontalGradient(listOf(Gold, GoldLight)),
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                    Text("${nextTier.minRating}", fontFamily = DmMono, fontSize = 9.sp, color = GoldLight)
                }
            }
        }
    }
}

@Composable
private fun StatsRow(uiState: HomeUiState) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatPill("${uiState.dayStreak}", "Day Streak", Gold,  Modifier.weight(1f))
        StatPill("${uiState.accuracy}%","Accuracy",   MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
        StatPill("${uiState.puzzlesSolved}","Puzzles", MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(value: String, label: String, valueColor: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = valueColor)
        Text(label, fontFamily = DmMono, fontSize = 8.sp, letterSpacing = 1.sp, color = TextTertiary)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text       = title,
        fontFamily = CormorantGaramond,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        color      = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun QuickActionsGrid(
    onPlayRapid:  () -> Unit,
    onPlayBlitz:  () -> Unit,
    onTactics:    () -> Unit,
    onOpenings:   () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard("⚔",  "Play Rapid",  "10+0 rated",       isPrimary = true,  onClick = onPlayRapid,  modifier = Modifier.weight(1f))
            QuickActionCard("⚡", "Blitz",        "3+2 rated",        isPrimary = false, onClick = onPlayBlitz,  modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard("🧩", "Daily Puzzle", "Fork • ★★★☆☆",    isPrimary = false, onClick = onTactics,    modifier = Modifier.weight(1f))
            QuickActionCard("♟", "Openings",     "London — Lesson 7", isPrimary = false, onClick = onOpenings,  modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickActionCard(
    icon:      String,
    name:      String,
    subtitle:  String,
    isPrimary: Boolean,
    onClick:   () -> Unit,
    modifier:  Modifier = Modifier
) {
    val bgBrush = if (isPrimary)
        Brush.linearGradient(listOf(Gold, Color(0xFF8B5E3C)))
    else
        Brush.linearGradient(listOf(Color.White, Color.White))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .then(
                if (!isPrimary) Modifier.border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                name,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                color      = if (isPrimary) Color.White else TextPrimary
            )
            Text(
                subtitle,
                fontSize = 10.sp,
                color    = if (isPrimary) Color.White.copy(0.7f) else TextTertiary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun DecisionProtocolCard() {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue   = 0.4f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label          = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🧠", fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "5-Step Thinking Active",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                color      = TextPrimary
            )
            Text(
                "CCT Scanner · BlunderGuard · Eval Mode",
                fontSize = 10.sp,
                color    = TextTertiary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        // Live dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer { alpha = pulse }
                .background(Success, CircleShape)
        )
    }
}
