package com.mindgambit.app.presentation.review

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mindgambit.app.domain.repository.GameRepository
import com.mindgambit.app.presentation.board.components.ChessBoard
import com.mindgambit.app.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// MindGambit — Review Screen
// Post-game analysis: accuracy, move classification, eval bar
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    gameId:      Long,
    onNavigateUp: () -> Unit,
    viewModel:   ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gameId) { viewModel.loadGame(gameId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Game Review", fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        uiState.game?.let {
                            Text(it.timeControl.displayName, fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Accuracy gauge + result
            AccuracyCard(
                accuracy  = uiState.accuracy,
                eloDelta  = uiState.game?.eloDelta ?: 0,
                result    = uiState.game?.result ?: GameResult.DRAW
            )

            // Move classification breakdown
            MoveClassificationCard(moves = uiState.classifiedMoves)

            // Board with move navigation
            ReviewBoardCard(
                uiState  = uiState,
                onPrevMove = viewModel::previousMove,
                onNextMove = viewModel::nextMove,
                onFirstMove= viewModel::firstMove,
                onLastMove = viewModel::lastMove
            )

            // ELO sparkline
            EloSparklineCard(deltas = uiState.recentDeltas)

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AccuracyCard(accuracy: Float, eloDelta: Int, result: GameResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Accuracy ring
        val animAcc by animateFloatAsState(
            targetValue   = accuracy / 100f,
            animationSpec = tween(1200, easing = EaseOut),
            label         = "accAnim"
        )
        Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val sw = 8.dp.toPx()
                drawArc(Color(0xFFEEEAE0), -90f, 360f, false, style = Stroke(sw, cap = StrokeCap.Round))
                val color = when {
                    accuracy >= 80 -> android.graphics.Color.parseColor("#3A9E6A")
                    accuracy >= 60 -> android.graphics.Color.parseColor("#F4A623")
                    else           -> android.graphics.Color.parseColor("#D44040")
                }
                drawArc(Color(color), -90f, 360f * animAcc, false, style = Stroke(sw, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${accuracy.toInt()}%", fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("ACCURACY", fontFamily = DmMono, fontSize = 7.sp, letterSpacing = 1.sp, color = TextTertiary)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            val (resultText, resultColor) = when (result) {
                GameResult.WHITE_WIN -> Pair("Victory", Success)
                GameResult.BLACK_WIN -> Pair("Defeat",  Error)
                GameResult.DRAW      -> Pair("Draw",    Warning)
                else                 -> Pair("Analysed",TextSecondary)
            }
            Text(resultText, fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = resultColor)
            Spacer(Modifier.height(4.dp))
            val deltaColor = if (eloDelta >= 0) Success else Error
            Box(
                modifier = Modifier
                    .background(deltaColor.copy(0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    "${if (eloDelta >= 0) "+" else ""}$eloDelta ELO",
                    fontFamily = DmMono,
                    fontSize   = 11.sp,
                    color      = deltaColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MoveClassificationCard(moves: List<ClassifiedMove>) {
    val brilliant  = moves.count { it.quality == MoveQuality.BRILLIANT }
    val good       = moves.count { it.quality == MoveQuality.GOOD }
    val inaccuracy = moves.count { it.quality == MoveQuality.INACCURACY }
    val mistake    = moves.count { it.quality == MoveQuality.MISTAKE }
    val blunder    = moves.count { it.quality == MoveQuality.BLUNDER }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text("Move Analysis", fontFamily = CormorantGaramond, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MoveQualityCell("$brilliant", "Brilliant", Brilliant)
            MoveQualityCell("$good",       "Good",       Good)
            MoveQualityCell("$inaccuracy", "Inaccuracy", Inaccuracy)
            MoveQualityCell("$mistake",    "Mistake",    Mistake)
            MoveQualityCell("$blunder",    "Blunder",    Blunder)
        }
    }
}

@Composable
private fun MoveQualityCell(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = color)
        Text(label, fontFamily = DmMono, fontSize = 8.sp, letterSpacing = 0.5.sp, color = TextTertiary)
    }
}

@Composable
private fun ReviewBoardCard(
    uiState:    ReviewUiState,
    onPrevMove: () -> Unit,
    onNextMove: () -> Unit,
    onFirstMove:() -> Unit,
    onLastMove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Position", fontFamily = CormorantGaramond, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        ChessBoard(
            position       = uiState.currentPosition,
            selectedSquare = null,
            possibleMoves  = emptyList(),
            lastMove       = uiState.lastMoveHighlight,
            onSquareClick  = {},
            modifier       = Modifier.fillMaxWidth()
        )

        // Move annotation for current move
        uiState.currentMoveAnnotation?.let { annotation ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(annotation.quality.color.copy(0.08f), RoundedCornerShape(10.dp))
                    .border(1.dp, annotation.quality.color.copy(0.2f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(annotation.quality.symbol, fontSize = 16.sp)
                    Text(annotation.moveAlgebraic, fontFamily = DmMono, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
                Text(annotation.quality.displayName, fontSize = 12.sp, color = annotation.quality.color, fontWeight = FontWeight.SemiBold)
            }
        }

        // Navigation controls
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val navButtons = listOf(
                Triple("|←", onFirstMove, uiState.currentMoveIndex > 0),
                Triple("←",  onPrevMove,  uiState.currentMoveIndex > 0),
                Triple("→",  onNextMove,  uiState.currentMoveIndex < uiState.totalMoves - 1),
                Triple("→|", onLastMove,  uiState.currentMoveIndex < uiState.totalMoves - 1)
            )
            navButtons.forEach { (label, action, enabled) ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (enabled) Color.White else Color(0xFFF5F3EE), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(10.dp))
                        .clickable(enabled = enabled) { action() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontFamily = DmMono, fontSize = 12.sp, color = if (enabled) TextPrimary else TextTertiary)
                }
            }
        }
    }
}

@Composable
private fun EloSparklineCard(deltas: List<Int>) {
    if (deltas.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Rating History", fontFamily = CormorantGaramond, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            val total = deltas.sum()
            Text(
                "${if (total >= 0) "+" else ""}$total",
                fontFamily = DmMono, fontSize = 12.sp,
                color = if (total >= 0) Success else Error,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(12.dp))

        // Simple sparkline
        val maxVal = deltas.runningFold(0) { acc, d -> acc + d }.max().coerceAtLeast(1)
        val minVal = deltas.runningFold(0) { acc, d -> acc + d }.min().coerceAtMost(0)
        val range = (maxVal - minVal).toFloat().coerceAtLeast(1f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            val w = size.width; val h = size.height
            val cumulative = deltas.runningFold(0) { acc, d -> acc + d }
            val points     = cumulative.mapIndexed { i, v ->
                androidx.compose.ui.geometry.Offset(
                    x = i * w / (cumulative.size - 1).coerceAtLeast(1),
                    y = h - ((v - minVal) / range) * h
                )
            }
            // Gradient fill
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(0f, h)
            points.forEach { path.lineTo(it.x, it.y) }
            path.lineTo(w, h); path.close()
            drawPath(path, Brush.verticalGradient(listOf(Gold.copy(0.2f), Color.Transparent)))
            // Line
            for (i in 1 until points.size) {
                drawLine(Gold, points[i - 1], points[i], strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            }
            // End dot
            points.lastOrNull()?.let {
                drawCircle(Gold, radius = 5.dp.toPx(), center = it)
                drawCircle(Color.White, radius = 3.dp.toPx(), center = it)
            }
        }
    }
}

// ── Review Models ─────────────────────────────────────────────

enum class MoveQuality(
    val displayName: String,
    val symbol:      String,
    val color:       Color
) {
    BRILLIANT  ("Brilliant",  "✦", Brilliant),
    GOOD       ("Good",       "✓", Good),
    INACCURACY ("Inaccuracy", "?!", Inaccuracy),
    MISTAKE    ("Mistake",    "?",  Mistake),
    BLUNDER    ("Blunder",   "??", Blunder),
    FORCED     ("Forced",    "□",  Color(0xFF9B59B6)),
    BEST       ("Best",      "★",  Brilliant),
}

data class ClassifiedMove(
    val moveAlgebraic: String,
    val quality:       MoveQuality,
    val evalBefore:    Int = 0,
    val evalAfter:     Int = 0
)

data class MoveAnnotation(
    val moveAlgebraic: String,
    val quality:       MoveQuality
)

// ── ViewModel ──────────────────────────────────────────────────

data class ReviewUiState(
    val game:                 Game?              = null,
    val accuracy:             Float              = 0f,
    val classifiedMoves:      List<ClassifiedMove> = emptyList(),
    val currentPosition:      ChessPosition      = ChessPosition.STARTING,
    val currentMoveIndex:     Int                = 0,
    val totalMoves:           Int                = 0,
    val lastMoveHighlight:    Pair<Int,Int>?     = null,
    val currentMoveAnnotation:MoveAnnotation?    = null,
    val recentDeltas:         List<Int>          = emptyList(),
    val isLoading:            Boolean            = true
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private var positionHistory: List<ChessPosition> = emptyList()

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            val game = gameRepository.getGameById(gameId)
            if (game != null) {
                val classified = generateMockClassification(game)
                positionHistory = listOf(ChessPosition.STARTING)

                _uiState.update {
                    it.copy(
                        game             = game,
                        accuracy         = game.accuracy.takeIf { a -> a > 0f } ?: 74f,
                        classifiedMoves  = classified,
                        currentPosition  = ChessPosition.STARTING,
                        currentMoveIndex = 0,
                        totalMoves       = classified.size,
                        recentDeltas     = listOf(-5, 8, -3, 12, 6, 15, -2, 18, 9, 22, 7, 14),
                        isLoading        = false
                    )
                }
            } else {
                // No game found — show demo data
                _uiState.update {
                    it.copy(
                        accuracy         = 74f,
                        classifiedMoves  = getDemoMoves(),
                        totalMoves       = getDemoMoves().size,
                        recentDeltas     = listOf(-5, 8, -3, 12, 6, 15, -2, 18, 9, 22, 7, 14),
                        isLoading        = false
                    )
                }
            }
        }
    }

    fun nextMove() {
        val state = _uiState.value
        if (state.currentMoveIndex < state.totalMoves - 1) {
            val nextIdx = state.currentMoveIndex + 1
            val annotation = state.classifiedMoves.getOrNull(nextIdx)?.let {
                MoveAnnotation(it.moveAlgebraic, it.quality)
            }
            _uiState.update {
                it.copy(
                    currentMoveIndex      = nextIdx,
                    currentMoveAnnotation = annotation
                )
            }
        }
    }

    fun previousMove() {
        val state = _uiState.value
        if (state.currentMoveIndex > 0) {
            val prevIdx = state.currentMoveIndex - 1
            _uiState.update { it.copy(currentMoveIndex = prevIdx, currentMoveAnnotation = null) }
        }
    }

    fun firstMove() { _uiState.update { it.copy(currentMoveIndex = 0, currentMoveAnnotation = null) } }
    fun lastMove()  { _uiState.update { it.copy(currentMoveIndex = it.totalMoves - 1) } }

    private fun generateMockClassification(game: Game): List<ClassifiedMove> {
        // In production: run each position through Stockfish, compare to played move
        return getDemoMoves()
    }

    private fun getDemoMoves() = listOf(
        ClassifiedMove("d4",  MoveQuality.BEST),
        ClassifiedMove("d5",  MoveQuality.GOOD),
        ClassifiedMove("Bf4", MoveQuality.BEST),
        ClassifiedMove("Nf6", MoveQuality.GOOD),
        ClassifiedMove("e3",  MoveQuality.GOOD),
        ClassifiedMove("e6",  MoveQuality.GOOD),
        ClassifiedMove("Nf3", MoveQuality.BEST),
        ClassifiedMove("Bd6", MoveQuality.INACCURACY),
        ClassifiedMove("Bg3", MoveQuality.BEST),
        ClassifiedMove("O-O", MoveQuality.GOOD),
        ClassifiedMove("Nbd2",MoveQuality.GOOD),
        ClassifiedMove("c5",  MoveQuality.GOOD),
        ClassifiedMove("c3",  MoveQuality.BEST),
        ClassifiedMove("Nc6", MoveQuality.MISTAKE),
        ClassifiedMove("Bb5", MoveQuality.BRILLIANT),
        ClassifiedMove("Qc7", MoveQuality.GOOD),
        ClassifiedMove("Bxc6",MoveQuality.GOOD),
        ClassifiedMove("Qxc6",MoveQuality.BLUNDER),
        ClassifiedMove("Ne5", MoveQuality.BEST)
    )
}
