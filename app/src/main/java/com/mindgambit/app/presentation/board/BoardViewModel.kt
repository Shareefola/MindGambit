package com.mindgambit.app.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindgambit.app.data.engine.StockfishEngine
import com.mindgambit.app.domain.model.*
import com.mindgambit.app.domain.repository.*
import com.mindgambit.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

// ============================================================
// MindGambit — BoardViewModel
// ============================================================

data class BoardUiState(
    val position:            ChessPosition  = ChessPosition.STARTING,
    val selectedSquare:      Int?           = null,
    val legalMoves:          List<Int>      = emptyList(),
    val lastMove:            Pair<Int,Int>? = null,
    val moveHistory:         List<String>   = emptyList(),
    val sideToMove:          PieceColor     = PieceColor.WHITE,
    val playerColor:         PieceColor     = PieceColor.WHITE,
    val playerRating:        Int            = 1200,
    val opponentRating:      Int            = 1200,
    val whiteTimeLeft:       Int            = 600,
    val blackTimeLeft:       Int            = 600,
    val isGameOver:          Boolean        = false,
    val gameId:              Long?          = null,
    val canUndo:             Boolean        = false,
    val showPromotionDialog: Boolean        = false,
    val pendingPromoMove:    Move?          = null,
    val modeDisplayName:     String         = "Rapid",
    val timeControlDisplay:  String         = "10+0",
    val engineThinking:      Boolean        = false
)

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val engine:           StockfishEngine,
    private val getLegalMoves:    GetLegalMovesUseCase,
    private val getBestMove:      GetBestMoveUseCase,
    private val gameRepository:   GameRepository,
    private val eloRepository:    EloRepository,
    private val updateElo:        UpdateEloUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    private var clockJob: Job? = null

    fun startGame(modeStr: String) {
        val mode = try { GameMode.valueOf(modeStr) } catch (e: Exception) { GameMode.RAPID }
        val timeControl = when (modeStr) {
            "BLITZ"    -> TimeControl.BLITZ_5
            "BLITZ_3_2"-> TimeControl.BLITZ_3_2
            else       -> TimeControl.RAPID_10
        }

        engine.newGame()

        _uiState.update {
            it.copy(
                position           = ChessPosition.STARTING,
                sideToMove         = PieceColor.WHITE,
                playerColor        = PieceColor.WHITE,
                whiteTimeLeft      = timeControl.initialSeconds,
                blackTimeLeft      = timeControl.initialSeconds,
                modeDisplayName    = timeControl.displayName,
                timeControlDisplay = timeControl.toString(),
                moveHistory        = emptyList(),
                isGameOver         = false,
                gameId             = null
            )
        }

        startClock()
    }

    fun onSquareClick(square: Int) {
        val state = _uiState.value
        if (state.isGameOver || state.engineThinking) return

        val selected = state.selectedSquare

        when {
            // Deselect
            selected == square -> {
                _uiState.update { it.copy(selectedSquare = null, legalMoves = emptyList()) }
            }
            // Make move
            selected != null && square in state.legalMoves -> {
                val piece = state.position.pieceAt(selected)
                // Check if pawn promotion
                if (piece?.type == PieceType.PAWN) {
                    val rank = square / 8
                    if ((piece.color == PieceColor.WHITE && rank == 7) ||
                        (piece.color == PieceColor.BLACK && rank == 0)) {
                        _uiState.update {
                            it.copy(
                                showPromotionDialog = true,
                                pendingPromoMove    = Move(selected, square)
                            )
                        }
                        return
                    }
                }
                makeMove(Move(selected, square))
            }
            // Select a piece
            state.position.isOccupiedByColor(square, state.sideToMove) -> {
                viewModelScope.launch {
                    val moves = getLegalMoves(state.position, square)
                    _uiState.update {
                        it.copy(
                            selectedSquare = square,
                            legalMoves     = moves.map { m -> m.to }
                        )
                    }
                }
            }
        }
    }

    fun confirmPromotion(pieceType: PieceType) {
        val pending = _uiState.value.pendingPromoMove ?: return
        _uiState.update { it.copy(showPromotionDialog = false, pendingPromoMove = null) }
        makeMove(pending.copy(promotion = pieceType))
    }

    private fun makeMove(move: Move) {
        val state = _uiState.value
        // Apply move to position (simplified — full engine handles legality)
        val newPos = applyMove(state.position, move)
        val moveAlg = moveToAlgebraic(state.position, move)

        _uiState.update {
            it.copy(
                position       = newPos,
                selectedSquare = null,
                legalMoves     = emptyList(),
                lastMove       = Pair(move.from, move.to),
                sideToMove     = newPos.sideToMove,
                moveHistory    = it.moveHistory + moveAlg,
                canUndo        = true
            )
        }

        // If opponent's turn, ask engine for response
        if (newPos.sideToMove != state.playerColor) {
            requestEngineMove(newPos)
        }
    }

    private fun requestEngineMove(position: ChessPosition) {
        viewModelScope.launch {
            _uiState.update { it.copy(engineThinking = true) }
            val result = getBestMove(position, moveTime = 1500)
            _uiState.update { it.copy(engineThinking = false) }

            val uci = result.bestMove
            if (uci.length >= 4) {
                val from = algebraicToSquare(uci.substring(0, 2))
                val to   = algebraicToSquare(uci.substring(2, 4))
                val promo = uci.getOrNull(4)?.let { ch -> promotionChar(ch) }
                makeMove(Move(from, to, promo))
            }
        }
    }

    fun undoMove() {
        // Undo last two moves (player + engine response)
        val history = _uiState.value.moveHistory
        if (history.size < 2) return
        // Rebuild position from PGN minus last 2 moves
        // Simplified here — full implementation replays from start
        _uiState.update { it.copy(canUndo = false) }
    }

    fun requestHint() {
        viewModelScope.launch {
            val pos    = _uiState.value.position
            val result = getBestMove(pos, moveTime = 500)
            val uci    = result.bestMove
            if (uci.length >= 4) {
                val from = algebraicToSquare(uci.substring(0, 2))
                _uiState.update { it.copy(selectedSquare = from) }
            }
        }
    }

    fun resignGame() {
        clockJob?.cancel()
        _uiState.update { it.copy(isGameOver = true) }
        viewModelScope.launch {
            updateElo(RatingMode.RAPID, _uiState.value.opponentRating, 0.0)
        }
    }

    fun offerDraw() {
        // In PvE vs engine: engine accepts draw offer based on eval
        clockJob?.cancel()
        _uiState.update { it.copy(isGameOver = true) }
        viewModelScope.launch {
            updateElo(RatingMode.RAPID, _uiState.value.opponentRating, 0.5)
        }
    }

    private fun startClock() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (_uiState.value.let { !it.isGameOver }) {
                delay(1000)
                _uiState.update { state ->
                    if (state.sideToMove == PieceColor.WHITE) {
                        val newTime = (state.whiteTimeLeft - 1).coerceAtLeast(0)
                        if (newTime == 0) state.copy(whiteTimeLeft = 0, isGameOver = true)
                        else state.copy(whiteTimeLeft = newTime)
                    } else {
                        val newTime = (state.blackTimeLeft - 1).coerceAtLeast(0)
                        if (newTime == 0) state.copy(blackTimeLeft = 0, isGameOver = true)
                        else state.copy(blackTimeLeft = newTime)
                    }
                }
            }
        }
    }

    // Placeholder move application — replace with full MoveGenerator
    private fun applyMove(pos: ChessPosition, move: Move): ChessPosition {
        val newPieces = pos.pieces.copyOf()
        val piece     = newPieces[move.from] ?: return pos
        newPieces[move.to]   = if (move.promotion != null) ChessPiece(move.promotion, piece.color) else piece
        newPieces[move.from] = null
        return pos.copy(
            pieces     = newPieces,
            sideToMove = pos.sideToMove.opposite()
        )
    }

    private fun moveToAlgebraic(pos: ChessPosition, move: Move): String {
        return move.toUci()  // simplified — full SAN conversion needed
    }

    private fun promotionChar(ch: Char) = when (ch) {
        'q' -> PieceType.QUEEN; 'r' -> PieceType.ROOK
        'b' -> PieceType.BISHOP; else -> PieceType.KNIGHT
    }

    override fun onCleared() {
        super.onCleared()
        clockJob?.cancel()
    }
}
