package com.mindgambit.app.presentation.ladder

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mindgambit.app.domain.model.*
import com.mindgambit.app.domain.repository.EloRepository
import com.mindgambit.app.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// MindGambit — Ladder Screen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LadderScreen(
    onNavigateUp: () -> Unit,
    viewModel:    LadderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Elo Ladder", fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .background(GoldBg, RoundedCornerShape(20.dp))
                            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Season 3", fontFamily = DmMono, fontSize = 9.sp, letterSpacing = 1.5.sp, color = Gold)
                    }
                    Spacer(Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Rating ring card
            RatingRingCard(uiState = uiState)

            // Rating breakdown
            RatingBreakdownCard(uiState = uiState)

            // Tier list
            SectionLabel("TIER PROGRESS")
            TierListCard(currentRating = uiState.rapidRating)

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RatingRingCard(uiState: LadderUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Ring
        val animatedProgress by animateFloatAsState(
            targetValue   = Tier.fromRating(uiState.rapidRating).progressFraction(uiState.rapidRating),
            animationSpec = tween(1400, easing = EaseOut),
            label         = "ringAnim"
        )

        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 9.dp.toPx()
                val radius      = (size.minDimension - strokeWidth) / 2
                val sweep       = 360f * animatedProgress

                // Background ring
                drawArc(
                    color      = Color(0xFFEEEAE0),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter  = false,
                    style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
                // Progress ring
                drawArc(
                    brush      = Brush.sweepGradient(listOf(Gold, GoldLight, Gold)),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter  = false,
                    style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${uiState.rapidRating}",
                    fontFamily = CormorantGaramond,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 22.sp,
                    color      = TextPrimary
                )
                Text("RAPID", fontFamily = DmMono, fontSize = 8.sp, letterSpacing = 1.sp, color = TextTertiary)
            }
        }

        // Info
        Column {
            val tier = Tier.fromRating(uiState.rapidRating)
            Text(tier.displayName, fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
            Text("${tier.minRating}–${tier.maxRating}", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(Success.copy(0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("↑ +${uiState.monthlyDelta} this month", fontFamily = DmMono, fontSize = 10.sp, color = Success, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun RatingBreakdownCard(uiState: LadderUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(20.dp))
            .padding(4.dp)
    ) {
        SectionLabel("ALL RATINGS", modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
        listOf(
            Triple("⏱", "Rapid",    uiState.rapidRating),
            Triple("⚡","Blitz",    uiState.blitzRating),
            Triple("🧩","Tactical", uiState.tacticalRating),
            Triple("♟","Strategic",uiState.strategicRating)
        ).forEachIndexed { i, (icon, name, rating) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (i < 3) Modifier.border(bottom = 0.5.dp, color = Color(0xFFEEEAE0)) else Modifier)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 16.sp, modifier = Modifier.width(28.dp))
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1f))
                Text("$rating", fontFamily = DmMono, fontSize = 13.sp, color = Gold, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// Extension for bottom border only
fun Modifier.border(bottom: Dp, color: Color): Modifier = this.drawBehind {
    drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height), strokeWidth = bottom.toPx())
}

@Composable
private fun TierListCard(currentRating: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(20.dp))
            .padding(4.dp)
    ) {
        Tier.entries.forEachIndexed { i, tier ->
            val isCurrent = Tier.fromRating(currentRating) == tier
            val isCleared = currentRating >= tier.maxRating
            val isLocked  = currentRating < tier.minRating

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isCurrent) GoldBg else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .then(
                        if (isCurrent) Modifier.border(1.dp, GoldBorder, RoundedCornerShape(12.dp)) else Modifier
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isLocked) "🔒" else tier.badge, fontSize = 20.sp, modifier = Modifier.width(32.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(tier.displayName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isLocked) TextTertiary else TextPrimary)
                    Text("${tier.minRating}–${tier.maxRating}", fontFamily = DmMono, fontSize = 9.sp, color = TextTertiary, modifier = Modifier.padding(top = 1.dp))
                }
                when {
                    isCleared -> Text("✓ Cleared", fontSize = 11.sp, color = Success)
                    isCurrent -> Text("● Active",  fontSize = 11.sp, color = Gold, fontWeight = FontWeight.SemiBold)
                    isLocked  -> Text("Locked",    fontSize = 11.sp, color = TextTertiary)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text          = text,
        fontFamily    = DmMono,
        fontSize      = 9.sp,
        letterSpacing = 2.sp,
        color         = TextTertiary,
        modifier      = modifier
    )
}

// ── ViewModel ──────────────────────────────────────────────────

data class LadderUiState(
    val rapidRating:     Int = 800,
    val blitzRating:     Int = 800,
    val tacticalRating:  Int = 800,
    val strategicRating: Int = 800,
    val monthlyDelta:    Int = 0,
    val isLoading:       Boolean = true
)

@HiltViewModel
class LadderViewModel @Inject constructor(
    private val eloRepository: EloRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LadderUiState())
    val uiState: StateFlow<LadderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val ratings = eloRepository.getAllRatings()
            _uiState.update {
                it.copy(
                    rapidRating     = ratings[RatingMode.RAPID]?.rating     ?: 800,
                    blitzRating     = ratings[RatingMode.BLITZ]?.rating     ?: 800,
                    tacticalRating  = ratings[RatingMode.TACTICAL]?.rating  ?: 800,
                    strategicRating = ratings[RatingMode.STRATEGIC]?.rating ?: 800,
                    monthlyDelta    = 98,  // from history
                    isLoading       = false
                )
            }
        }
    }
}
