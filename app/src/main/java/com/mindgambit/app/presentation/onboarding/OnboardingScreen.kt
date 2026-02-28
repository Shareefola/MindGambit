package com.mindgambit.app.presentation.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mindgambit.app.domain.model.RatingMode
import com.mindgambit.app.domain.repository.EloRepository
import com.mindgambit.app.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// MindGambit — Onboarding Screen
// 4 pages: Welcome → Chess Level → Features → Ready
// ============================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel:  OnboardingViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope      = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1A1814), Color(0xFF2D2518), Color(0xFF1A1814))
                )
            )
    ) {
        // Background chess piece watermark
        Text(
            "♛",
            fontSize = 320.sp,
            color    = Color(0x06B8862E),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-60).dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // Skip button
            AnimatedVisibility(
                visible = pagerState.currentPage < 3,
                modifier = Modifier.align(Alignment.End)
            ) {
                TextButton(
                    onClick  = {
                        scope.launch { pagerState.animateScrollToPage(3) }
                    },
                    modifier = Modifier.padding(top = 16.dp, end = 16.dp)
                ) {
                    Text("Skip", fontFamily = DmMono, fontSize = 11.sp, color = Color.White.copy(0.4f))
                }
            }
            if (pagerState.currentPage == 3) Spacer(Modifier.height(56.dp))

            // Pager
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> ChessLevelPage(
                        selectedLevel = uiState.selectedLevel,
                        onLevelSelect = viewModel::setLevel
                    )
                    2 -> FeaturesPage()
                    3 -> ReadyPage()
                }
            }

            // Page indicators
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(4) { i ->
                    val isActive = pagerState.currentPage == i
                    val width by animateDpAsState(
                        if (isActive) 24.dp else 6.dp,
                        spring(Spring.DampingRatioMediumBouncy),
                        label = "dotWidth"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(width)
                            .background(
                                if (isActive) Gold else Color.White.copy(0.2f),
                                CircleShape
                            )
                    )
                }
            }

            // CTA button
            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Button(
                    onClick  = {
                        if (pagerState.currentPage < 3) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            viewModel.completeOnboarding()
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Gold
                    )
                ) {
                    Text(
                        if (pagerState.currentPage < 3) "Continue" else "Start Playing ♟",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 16.sp,
                        color      = Color.White
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Page 1: Welcome ───────────────────────────────────────────

@Composable
private fun WelcomePage() {
    val infiniteAnim = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteAnim.animateFloat(
        initialValue   = -6f,
        targetValue    = 6f,
        animationSpec  = infiniteRepeatable(
            tween(2400, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Column(
        modifier              = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        // Logo piece
        Text(
            "♞",
            fontSize = 88.sp,
            color    = Gold,
            modifier = Modifier.graphicsLayer { translationY = offsetY }
        )

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "Mind",
                fontFamily = CormorantGaramond,
                fontWeight = FontWeight.Bold,
                fontSize   = 48.sp,
                color      = Color.White
            )
            Text(
                "Gambit",
                fontFamily = CormorantGaramond,
                fontWeight = FontWeight.Bold,
                fontStyle  = FontStyle.Italic,
                fontSize   = 48.sp,
                color      = Gold
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Chess Intelligence Platform",
            fontFamily  = DmMono,
            fontSize    = 11.sp,
            letterSpacing = 2.sp,
            color       = Color.White.copy(0.4f)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            "Master chess through intelligent training,\nreal-time analysis, and structured learning.",
            textAlign   = TextAlign.Center,
            fontSize    = 16.sp,
            color       = Color.White.copy(0.7f),
            lineHeight  = 24.sp,
            fontFamily  = DmSans
        )
    }
}

// ── Page 2: Chess Level ───────────────────────────────────────

private data class SkillLevel(
    val label:       String,
    val description: String,
    val startingElo: Int,
    val icon:        String
)

private val SKILL_LEVELS = listOf(
    SkillLevel("Just Starting",  "I'm new to chess or still learning the rules",  400,  "🌱"),
    SkillLevel("Casual Player",  "I play occasionally and know the basics",        800,  "♟"),
    SkillLevel("Club Player",    "I play regularly and know some openings",        1200, "⭐"),
    SkillLevel("Tournament",     "I compete and have studied chess seriously",     1600, "🏆"),
)

@Composable
private fun ChessLevelPage(
    selectedLevel: Int,
    onLevelSelect: (Int) -> Unit
) {
    Column(
        modifier              = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            "What's your level?",
            fontFamily  = CormorantGaramond,
            fontWeight  = FontWeight.Bold,
            fontSize    = 32.sp,
            color       = Color.White,
            textAlign   = TextAlign.Center
        )
        Text(
            "We'll set your starting rating and\ncalibrate the training difficulty.",
            fontFamily  = DmSans,
            fontSize    = 14.sp,
            color       = Color.White.copy(0.5f),
            textAlign   = TextAlign.Center,
            lineHeight  = 22.sp,
            modifier    = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(28.dp))

        SKILL_LEVELS.forEachIndexed { index, level ->
            val isSelected = selectedLevel == index
            val borderColor by animateColorAsState(
                if (isSelected) Gold else Color.White.copy(0.1f),
                label = "levelBorder"
            )
            val bgColor by animateColorAsState(
                if (isSelected) GoldBg else Color.White.copy(0.05f),
                label = "levelBg"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable { onLevelSelect(index) }
                    .padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(level.icon, fontSize = 24.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        level.label,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp,
                        color      = if (isSelected) Gold else Color.White
                    )
                    Text(
                        level.description,
                        fontSize   = 11.sp,
                        color      = Color.White.copy(0.45f),
                        modifier   = Modifier.padding(top = 2.dp)
                    )
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Page 3: Features ──────────────────────────────────────────

private data class Feature(val icon: String, val title: String, val body: String)

private val FEATURES = listOf(
    Feature("♟", "Opening Repertoire",    "London System, Jobava London & Pirc Defense with interactive lessons."),
    Feature("🧩", "Tactical Puzzles",      "200+ puzzles across 12 motifs with spaced repetition scheduling."),
    Feature("🧠", "Decision Protocol",     "5-step thinking system to eliminate blunders before you play them."),
    Feature("▲",  "Elo Ladder",            "Separate ratings for Rapid, Blitz, Tactical and Strategic play."),
    Feature("◎",  "Game Analysis",         "Post-game review powered by Stockfish with move classification."),
)

@Composable
private fun FeaturesPage() {
    Column(
        modifier              = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            "Everything you need",
            fontFamily = CormorantGaramond,
            fontWeight = FontWeight.Bold,
            fontSize   = 32.sp,
            color      = Color.White,
            textAlign  = TextAlign.Center
        )
        Text(
            "A complete chess training ecosystem,\nbuilt around how you actually improve.",
            fontFamily = DmSans,
            fontSize   = 14.sp,
            color      = Color.White.copy(0.5f),
            textAlign  = TextAlign.Center,
            lineHeight = 22.sp,
            modifier   = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        FEATURES.forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp),
                verticalAlignment     = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(GoldBg, RoundedCornerShape(10.dp))
                        .border(1.dp, GoldBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(feature.icon, fontSize = 18.sp)
                }
                Column {
                    Text(feature.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                    Text(feature.body,  fontSize = 12.sp, color = Color.White.copy(0.5f), lineHeight = 18.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

// ── Page 4: Ready ─────────────────────────────────────────────

@Composable
private fun ReadyPage() {
    val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue  = 0.95f,
        targetValue   = 1.05f,
        animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "readyPulse"
    )

    Column(
        modifier              = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Text(
            "♔",
            fontSize = 80.sp,
            color    = Gold,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
        )

        Spacer(Modifier.height(28.dp))

        Text(
            "You're ready.",
            fontFamily = CormorantGaramond,
            fontWeight = FontWeight.Bold,
            fontSize   = 40.sp,
            color      = Color.White,
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Your journey to chess mastery starts\nwith a single move. Make it count.",
            fontFamily = DmSans,
            fontSize   = 16.sp,
            color      = Color.White.copy(0.6f),
            textAlign  = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(Modifier.height(40.dp))

        // Stats preview
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ReadyStatCell("0", "Games")
            ReadyStatCell("0", "Puzzles")
            ReadyStatCell("0", "Streak")
        }
    }
}

@Composable
private fun ReadyStatCell(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontFamily = CormorantGaramond,
            fontWeight = FontWeight.Bold,
            fontSize   = 28.sp,
            color      = Gold
        )
        Text(
            label,
            fontFamily    = DmMono,
            fontSize      = 9.sp,
            letterSpacing = 1.5.sp,
            color         = Color.White.copy(0.4f)
        )
    }
}

// ── ViewModel ─────────────────────────────────────────────────

data class OnboardingUiState(
    val selectedLevel: Int = 0,
    val isDone:        Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val eloRepository: EloRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setLevel(levelIndex: Int) {
        _uiState.update { it.copy(selectedLevel = levelIndex) }
    }

    fun completeOnboarding() {
        val startingElo = SKILL_LEVELS[_uiState.value.selectedLevel].startingElo
        viewModelScope.launch {
            // Set initial ratings for all modes based on chosen level
            RatingMode.entries.forEach { mode ->
                eloRepository.updateRating(mode, startingElo, 0)
            }
            _uiState.update { it.copy(isDone = true) }
        }
    }
}

// Note: SKILL_LEVELS is defined once above in this file as a top-level private val.
