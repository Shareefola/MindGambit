package com.mindgambit.app.presentation.openings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mindgambit.app.domain.model.*
import com.mindgambit.app.domain.repository.OpeningRepository
import com.mindgambit.app.presentation.board.components.ChessBoard
import com.mindgambit.app.presentation.components.MindGambitBottomBar
import com.mindgambit.app.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// MindGambit — Openings Screen (list + detail + lesson)
// ============================================================

// ── OPENINGS LIST SCREEN ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningsScreen(
    onNavigateUp:      () -> Unit,
    onOpeningSelected: (String) -> Unit,
    viewModel:         OpeningsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Repertoire", fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("Your opening paths", fontSize = 11.sp, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            MindGambitBottomBar(
                currentRoute    = "openings",
                onHomeClick     = onNavigateUp,
                onOpeningsClick = {},
                onTrainClick    = {},
                onTacticsClick  = {},
                onLadderClick   = {}
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            uiState.openingsWithProgress.forEach { (opening, progress) ->
                OpeningPathCard(
                    opening   = opening,
                    progress  = progress,
                    onClick   = { onOpeningSelected(opening.id.name) }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OpeningPathCard(
    opening:  Opening,
    progress: OpeningProgress?,
    onClick:  () -> Unit
) {
    val progressFraction = progress?.progressFraction ?: 0f
    val animatedProgress by animateFloatAsState(
        targetValue   = progressFraction,
        animationSpec = tween(1000, easing = EaseOut),
        label         = "openingProgress"
    )

    val (bannerColors, accentColor) = when (opening.id) {
        OpeningId.LONDON       -> Pair(listOf(Color(0xFF2D2B26), Color(0xFF5C4B2A)), Gold)
        OpeningId.JOBAVA_LONDON-> Pair(listOf(Color(0xFF1A2A3A), Color(0xFF2A4A6A)), Color(0xFF4A9EFF))
        OpeningId.PIRC         -> Pair(listOf(Color(0xFF2A1A2A), Color(0xFF5A2A5A)), Color(0xFF9B59B6))
    }

    val bannerPiece = when (opening.id) {
        OpeningId.LONDON        -> "♗"
        OpeningId.JOBAVA_LONDON -> "♞"
        OpeningId.PIRC          -> "♜"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onClick() }
    ) {
        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Brush.linearGradient(bannerColors))
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    opening.name,
                    fontFamily = CormorantGaramond,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 20.sp,
                    color      = Color.White
                )
                Text(
                    opening.tag,
                    fontFamily = DmMono,
                    fontSize   = 9.sp,
                    letterSpacing = 1.5.sp,
                    color      = Color.White.copy(0.5f)
                )
            }
            Text(
                bannerPiece,
                fontSize = 70.sp,
                color    = Color.White.copy(0.15f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
            )
        }

        // Progress section
        Column(modifier = Modifier.padding(14.dp, 12.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "${progress?.progressPercent ?: 0}%",
                    fontFamily = DmMono,
                    fontSize   = 12.sp,
                    color      = accentColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${progress?.completedLessons?.size ?: 0} / ${opening.lessons.size} lessons",
                    fontSize = 11.sp,
                    color    = TextTertiary
                )
            }

            Spacer(Modifier.height(6.dp))

            // Animated progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFFEEEAE0), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(accentColor, RoundedCornerShape(4.dp))
                )
            }

            Spacer(Modifier.height(10.dp))

            // Lesson pills
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val completedIds = progress?.completedLessons ?: emptySet()
                opening.lessons.take(4).forEach { lesson ->
                    val done = lesson.id in completedIds
                    Box(
                        modifier = Modifier
                            .background(
                                if (done) accentColor.copy(0.1f) else Color(0xFFF5F3EE),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (done) accentColor.copy(0.25f) else Color(0xFFEEEAE0),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text      = if (done) "✓ ${lesson.title.take(6)}" else lesson.title.take(6),
                            fontSize  = 10.sp,
                            color     = if (done) accentColor else TextTertiary,
                            fontWeight = if (done) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
                if (opening.lessons.size > 4) {
                    Text("+${opening.lessons.size - 4}", fontSize = 10.sp, color = TextTertiary,
                        modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
    }
}

// ── OPENING DETAIL SCREEN ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningDetailScreen(
    openingId:       String,
    onNavigateUp:    () -> Unit,
    onLessonClick:   (String) -> Unit,
    viewModel:       OpeningsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val opening  = uiState.openingsWithProgress.find { it.first.id.name == openingId }?.first

    if (opening == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = Gold)
        }
        return
    }

    val progress = uiState.openingsWithProgress.find { it.first.id.name == openingId }?.second
    val completedIds = progress?.completedLessons ?: emptySet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(opening.name, fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Description
            Text(
                opening.description,
                fontSize = 13.sp,
                color    = TextSecondary,
                lineHeight = 20.sp
            )

            // ECO code + moves
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(GoldBg, RoundedCornerShape(8.dp))
                        .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(opening.eco, fontFamily = DmMono, fontSize = 10.sp, color = Gold)
                }
                Text(opening.moves, fontSize = 11.sp, color = TextTertiary, fontStyle = FontStyle.Italic,
                    modifier = Modifier.align(Alignment.CenterVertically))
            }

            Divider(color = Color(0xFFEEEAE0), modifier = Modifier.padding(vertical = 4.dp))

            Text("Lessons", fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 18.sp)

            // Lesson cards
            opening.lessons.forEachIndexed { index, lesson ->
                val isDone     = lesson.id in completedIds
                val isUnlocked = lesson.isUnlocked || index == 0 ||
                        opening.lessons.getOrNull(index - 1)?.id in completedIds

                LessonCard(
                    lesson     = lesson,
                    index      = index + 1,
                    isDone     = isDone,
                    isUnlocked = isUnlocked,
                    onClick    = { if (isUnlocked) onLessonClick(lesson.id) }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LessonCard(
    lesson:     OpeningLesson,
    index:      Int,
    isDone:     Boolean,
    isUnlocked: Boolean,
    onClick:    () -> Unit
) {
    val bgColor  = if (isDone) Color(0xFFF5FFF9) else Color.White
    val border   = if (isDone) Color(0xFF3A9E6A).copy(0.2f) else Color(0xFFEEEAE0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(enabled = isUnlocked) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Index circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    when {
                        isDone     -> Success
                        isUnlocked -> GoldBg
                        else       -> Color(0xFFF5F3EE)
                    },
                    RoundedCornerShape(50)
                )
                .border(1.dp, if (isDone) Success.copy(0.3f) else GoldBorder, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else if (!isUnlocked) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
            } else {
                Text("$index", fontFamily = DmMono, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Gold)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                lesson.title,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                color      = if (isUnlocked) TextPrimary else TextTertiary
            )
            Text(
                lesson.description.take(60) + if (lesson.description.length > 60) "…" else "",
                fontSize = 11.sp,
                color    = TextTertiary,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Text(
            if (isUnlocked) "→" else "",
            fontSize = 16.sp,
            color    = Gold
        )
    }
}

// ── OPENING LESSON SCREEN ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningLessonScreen(
    openingId: String,
    lessonId:  String,
    onComplete: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: OpeningsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val opening = uiState.openingsWithProgress.find { it.first.id.name == openingId }?.first
    val lesson  = opening?.lessons?.find { it.id == lessonId }

    if (lesson == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Gold) }
        return
    }

    var currentMoveIndex by remember { mutableStateOf(0) }
    var position by remember { mutableStateOf(FenParser.fromFen(lesson.fen)) }
    var showComplete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson.title, fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onNavigateUp) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Description
            Card(
                colors  = CardDefaults.cardColors(containerColor = GoldBg),
                shape   = RoundedCornerShape(14.dp),
                border  = BorderStroke(1.dp, GoldBorder)
            ) {
                Text(
                    lesson.description,
                    modifier  = Modifier.padding(14.dp),
                    fontSize  = 13.sp,
                    color     = TextPrimary,
                    lineHeight = 20.sp
                )
            }

            // Chess board showing current lesson position
            ChessBoard(
                position       = position,
                selectedSquare = null,
                possibleMoves  = emptyList(),
                lastMove       = null,
                onSquareClick  = {},
                modifier       = Modifier.fillMaxWidth()
            )

            // Move progress
            Text(
                "Move ${currentMoveIndex} / ${lesson.moves.size}",
                fontFamily = DmMono,
                fontSize   = 10.sp,
                color      = TextTertiary
            )

            // Navigation buttons
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick  = {
                        if (currentMoveIndex > 0) {
                            currentMoveIndex--
                            position = FenParser.fromFen(lesson.fen) // replay from start
                        }
                    },
                    enabled  = currentMoveIndex > 0,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) { Text("← Previous") }

                if (currentMoveIndex < lesson.moves.size) {
                    Button(
                        onClick = {
                            currentMoveIndex++
                            if (currentMoveIndex == lesson.moves.size) showComplete = true
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = if (showComplete) Success else Obsidian)
                    ) {
                        Text(if (currentMoveIndex == lesson.moves.size - 1) "Finish →" else "Next →")
                    }
                } else {
                    Button(
                        onClick  = {
                            viewModel.completeLesson(OpeningId.valueOf(openingId), lessonId)
                            onComplete()
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Text("✓ Lesson Complete")
                    }
                }
            }
        }
    }
}

// ── ViewModel ──────────────────────────────────────────────────

data class OpeningsUiState(
    val openingsWithProgress: List<Pair<Opening, OpeningProgress?>> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class OpeningsViewModel @Inject constructor(
    private val openingRepository: OpeningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OpeningsUiState())
    val uiState: StateFlow<OpeningsUiState> = _uiState.asStateFlow()

    init { loadOpenings() }

    private fun loadOpenings() {
        viewModelScope.launch {
            val openings = openingRepository.getAllOpenings()
            // Combine all progress flows
            val progressFlows = openings.map { opening ->
                openingRepository.getProgress(opening.id).map { progress -> Pair(opening, progress) }
            }
            combine(progressFlows) { pairs -> pairs.toList() }.collect { pairs ->
                _uiState.update { it.copy(openingsWithProgress = pairs, isLoading = false) }
            }
        }
    }

    fun completeLesson(openingId: OpeningId, lessonId: String) {
        viewModelScope.launch {
            openingRepository.markLessonComplete(openingId, lessonId)
        }
    }
}
