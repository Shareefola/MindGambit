package com.mindgambit.app.presentation.tactics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mindgambit.app.domain.model.*
import com.mindgambit.app.domain.repository.PuzzleRepository
import com.mindgambit.app.domain.usecase.*
import com.mindgambit.app.presentation.board.components.ChessBoard
import com.mindgambit.app.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// MindGambit — Tactics Screen
// ============================================================

enum class PuzzleFeedback { BRILLIANT, GOOD, WRONG, FAILED }

data class TacticsUiState(
    val currentPuzzle:  Puzzle?         = null,
    val selectedSquare: Int?            = null,
    val legalMoves:     List<Int>       = emptyList(),
    val lastMove:       Pair<Int, Int>? = null,
    val moveIndex:      Int             = 0,
    val attemptNumber:  Int             = 0,
    val isSolved:       Boolean         = false,
    val isFailed:       Boolean         = false,
    val feedback:       PuzzleFeedback? = null,
    val solvedCount:    Int             = 0
)

// ── Screen ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticsScreen(
    onNavigateUp: () -> Unit,
    viewModel:    TacticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Tactics", fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        "${uiState.solvedCount} solved",
                        fontFamily = DmMono,
                        fontSize   = 10.sp,
                        color      = TextTertiary,
                        modifier   = Modifier.padding(end = 16.dp)
                    )
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val puzzle = uiState.currentPuzzle

            if (puzzle == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
                return@Scaffold
            }

            PuzzleInfoBar(puzzle = puzzle)

            val sideToMove = puzzle.position.sideToMove
            Text(
                if (sideToMove == PieceColor.WHITE) "White to move." else "Black to move.",
                fontSize = 12.sp,
                color    = TextSecondary
            )

            ChessBoard(
                position       = puzzle.position,
                selectedSquare = uiState.selectedSquare,
                possibleMoves  = uiState.legalMoves,
                lastMove       = uiState.lastMove,
                flipped        = sideToMove == PieceColor.BLACK,
                onSquareClick  = viewModel::onSquareClick,
                modifier       = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = uiState.feedback != null,
                enter   = slideInVertically { it } + fadeIn(tween(250)),
                exit    = fadeOut(tween(150))
            ) {
                uiState.feedback?.let { fb -> MoveFeedbackChip(feedback = fb) }
            }

            PuzzleControls(
                isSolved   = uiState.isSolved,
                isFailed   = uiState.isFailed,
                onSkip     = viewModel::skipPuzzle,
                onNext     = viewModel::nextPuzzle,
                onHint     = viewModel::getHint
            )
        }
    }
}

// ── Sub-composables ────────────────────────────────────────

@Composable
private fun PuzzleInfoBar(puzzle: Puzzle) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Motif badge
        Box(
            modifier = Modifier
                .background(Color(0xFFEEF6FF), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFCCE3FF), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                puzzle.motif.name.replace("_", " "),
                fontFamily    = DmMono,
                fontSize      = 9.sp,
                letterSpacing = 1.sp,
                color         = Color(0xFF2A6ECC)
            )
        }

        // Difficulty stars
        Row {
            repeat(5) { i ->
                Text(
                    if (i < puzzle.difficulty) "★" else "☆",
                    fontSize = 12.sp,
                    color    = if (i < puzzle.difficulty) Gold else TextTertiary
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            "#${puzzle.id}",
            fontFamily = DmMono,
            fontSize   = 10.sp,
            color      = TextTertiary
        )
    }
}

@Composable
private fun MoveFeedbackChip(feedback: PuzzleFeedback) {
    val (text, bgColor, textColor) = when (feedback) {
        PuzzleFeedback.BRILLIANT -> Triple("✦ Brilliant!", Brilliant.copy(0.1f), Brilliant)
        PuzzleFeedback.GOOD      -> Triple("✓ Correct!",   Good.copy(0.1f),      Good)
        PuzzleFeedback.WRONG     -> Triple("✗ Not quite. Try again.", Error.copy(0.08f), Error)
        PuzzleFeedback.FAILED    -> Triple("✗ Puzzle failed.",        Error.copy(0.08f), Error)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, fontSize = 13.sp, color = textColor, fontWeight = FontWeight.Medium)
        when (feedback) {
            PuzzleFeedback.BRILLIANT -> Text("+ELO", fontFamily = DmMono, fontSize = 10.sp, color = textColor)
            PuzzleFeedback.FAILED    -> Text("-ELO", fontFamily = DmMono, fontSize = 10.sp, color = textColor)
            else                     -> {}
        }
    }
}

@Composable
private fun PuzzleControls(
    isSolved: Boolean,
    isFailed: Boolean,
    onSkip:   () -> Unit,
    onNext:   () -> Unit,
    onHint:   () -> Unit
) {
    val showNext = isSolved || isFailed
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!showNext) {
            OutlinedButton(
                onClick  = onSkip,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(12.dp)
            ) { Text("Skip") }

            OutlinedButton(
                onClick  = onHint,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(12.dp)
            ) { Text("💡 Hint") }
        } else {
            Button(
                onClick  = onNext,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Obsidian)
            ) { Text("Next Puzzle →") }
        }
    }
}

// ── ViewModel ─────────────────────────────────────────────

@HiltViewModel
class TacticsViewModel @Inject constructor(
    private val getNextPuzzle:    GetNextPuzzleUseCase,
    private val validateSolution: ValidateSolutionUseCase,
    private val markSolved:       MarkPuzzleSolvedUseCase,
    private val puzzleRepository: PuzzleRepository,
    private val getLegalMoves:    GetLegalMovesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TacticsUiState())
    val uiState: StateFlow<TacticsUiState> = _uiState.asStateFlow()

    init {
        loadNextPuzzle()
        viewModelScope.launch {
            puzzleRepository.getSolvedCount().collect { count ->
                _uiState.update { it.copy(solvedCount = count) }
            }
        }
    }

    private fun loadNextPuzzle() {
        viewModelScope.launch {
            val puzzle = getNextPuzzle()
            _uiState.update {
                it.copy(
                    currentPuzzle  = puzzle,
                    selectedSquare = null,
                    legalMoves     = emptyList(),
                    lastMove       = null,
                    moveIndex      = 0,
                    isSolved       = false,
                    isFailed       = false,
                    feedback       = null,
                    attemptNumber  = 0
                )
            }
        }
    }

    fun onSquareClick(square: Int) {
        val state  = _uiState.value
        val puzzle = state.currentPuzzle ?: return
        if (state.isSolved || state.isFailed) return

        val selected = state.selectedSquare

        when {
            selected == square -> {
                _uiState.update { it.copy(selectedSquare = null, legalMoves = emptyList()) }
            }
            selected != null && square in state.legalMoves -> {
                val uci     = "${squareToAlgebraic(selected)}${squareToAlgebraic(square)}"
                val correct = validateSolution(puzzle, state.moveIndex, uci)

                val feedback = when {
                    correct && state.attemptNumber == 0 -> PuzzleFeedback.BRILLIANT
                    correct                             -> PuzzleFeedback.GOOD
                    else                                -> PuzzleFeedback.WRONG
                }

                val nowSolved = correct && state.moveIndex + 1 >= puzzle.solutionMoves.size
                val nowFailed = !correct && state.attemptNumber >= 2

                _uiState.update {
                    it.copy(
                        selectedSquare = null,
                        legalMoves     = emptyList(),
                        lastMove       = Pair(selected, square),
                        feedback       = feedback,
                        isSolved       = nowSolved,
                        isFailed       = nowFailed,
                        moveIndex      = if (correct) it.moveIndex + 1 else it.moveIndex,
                        attemptNumber  = if (!correct) it.attemptNumber + 1 else it.attemptNumber
                    )
                }

                if (nowSolved) viewModelScope.launch { markSolved(puzzle, true) }
                if (nowFailed) viewModelScope.launch { markSolved(puzzle, false) }
            }
            puzzle.position.isOccupiedByColor(square, puzzle.position.sideToMove) -> {
                viewModelScope.launch {
                    val moves = getLegalMoves(puzzle.position, square)
                    _uiState.update {
                        it.copy(selectedSquare = square, legalMoves = moves.map { m -> m.to })
                    }
                }
            }
        }
    }

    fun skipPuzzle() = loadNextPuzzle()
    fun nextPuzzle() = loadNextPuzzle()

    fun getHint() {
        val puzzle    = _uiState.value.currentPuzzle ?: return
        val moveIndex = _uiState.value.moveIndex
        val hintMove  = puzzle.solutionMoves.getOrNull(moveIndex) ?: return
        if (hintMove.length >= 2) {
            val from = algebraicToSquare(hintMove.substring(0, 2))
            _uiState.update { it.copy(selectedSquare = from, attemptNumber = it.attemptNumber + 1) }
        }
    }
}
