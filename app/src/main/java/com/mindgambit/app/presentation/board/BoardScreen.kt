package com.mindgambit.app.presentation.board

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindgambit.app.domain.model.*
import com.mindgambit.app.presentation.board.components.ChessBoard
import com.mindgambit.app.presentation.board.components.PlayerCard
import com.mindgambit.app.presentation.theme.*

// ============================================================
// MindGambit — Board Screen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    gameMode:       String,
    onNavigateUp:   () -> Unit,
    onGameComplete: (Long) -> Unit,
    viewModel:      BoardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gameMode) { viewModel.startGame(gameMode) }

    LaunchedEffect(uiState.gameId) {
        if (uiState.isGameOver && uiState.gameId != null) {
            onGameComplete(uiState.gameId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(GoldBg, RoundedCornerShape(20.dp))
                                .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text       = "${uiState.timeControlDisplay} · ${uiState.modeDisplayName}",
                                fontFamily = DmMono,
                                fontSize   = 10.sp,
                                letterSpacing = 1.sp,
                                color      = Gold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Black player (opponent)
            PlayerCard(
                name      = "Opponent",
                rating    = uiState.opponentRating,
                isWhite   = false,
                timeLeft  = uiState.blackTimeLeft,
                isActive  = uiState.sideToMove == PieceColor.BLACK,
                modifier  = Modifier.fillMaxWidth()
            )

            // Chess Board
            ChessBoard(
                position       = uiState.position,
                selectedSquare = uiState.selectedSquare,
                possibleMoves  = uiState.legalMoves,
                lastMove       = uiState.lastMove,
                flipped        = uiState.playerColor == PieceColor.BLACK,
                onSquareClick  = { square -> viewModel.onSquareClick(square) },
                modifier       = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Move history strip
            if (uiState.moveHistory.isNotEmpty()) {
                MoveHistoryStrip(moves = uiState.moveHistory)
            }

            // White player
            PlayerCard(
                name     = "You",
                rating   = uiState.playerRating,
                isWhite  = true,
                timeLeft = uiState.whiteTimeLeft,
                isActive = uiState.sideToMove == PieceColor.WHITE,
                modifier = Modifier.fillMaxWidth()
            )

            // Game controls
            GameControls(
                canUndo       = uiState.canUndo,
                onUndo        = viewModel::undoMove,
                onHint        = viewModel::requestHint,
                onResign      = viewModel::resignGame,
                onOfferDraw   = viewModel::offerDraw,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
    }

    // Promotion dialog
    if (uiState.showPromotionDialog) {
        PromotionDialog(
            isWhite = uiState.playerColor == PieceColor.WHITE,
            onPieceSelected = { piece -> viewModel.confirmPromotion(piece) }
        )
    }
}

@Composable
private fun MoveHistoryStrip(moves: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        moves.forEachIndexed { i, move ->
            val isLast = i == moves.lastIndex
            Box(
                modifier = Modifier
                    .background(
                        if (isLast) Obsidian else Color.White,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (isLast) Color.Transparent else Color(0xFFEEEAE0),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text       = move,
                    fontFamily = DmMono,
                    fontSize   = 11.sp,
                    color      = if (isLast) Color.White else TextPrimary
                )
            }
        }
    }
}

@Composable
private fun GameControls(
    canUndo:     Boolean,
    onUndo:      () -> Unit,
    onHint:      () -> Unit,
    onResign:    () -> Unit,
    onOfferDraw: () -> Unit,
    modifier:    Modifier = Modifier
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        ControlButton("⟵", enabled = canUndo, onClick = onUndo, modifier = Modifier.weight(1f))
        ControlButton("💡", enabled = true,    onClick = onHint,    modifier = Modifier.weight(1f))
        ControlButton("=",  enabled = true,    onClick = onOfferDraw, modifier = Modifier.weight(1f))
        ControlButton("⚑",  enabled = true,   onClick = onResign,  modifier = Modifier.weight(1f), isPrimary = false, isDanger = true)
    }
}

@Composable
private fun ControlButton(
    label:     String,
    enabled:   Boolean,
    onClick:   () -> Unit,
    modifier:  Modifier = Modifier,
    isPrimary: Boolean = false,
    isDanger:  Boolean = false
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    !enabled  -> Color(0xFFEEEAE0)
                    isDanger  -> Error.copy(0.1f)
                    isPrimary -> Gold
                    else      -> Color.White
                }
            )
            .border(
                1.dp,
                when {
                    !enabled  -> Color.Transparent
                    isDanger  -> Error.copy(0.2f)
                    else      -> Color(0xFFEEEAE0)
                },
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text     = label,
            fontSize = 18.sp,
            color    = when {
                !enabled -> TextTertiary
                isDanger -> Error
                isPrimary -> Color.White
                else      -> TextPrimary
            }
        )
    }
}

@Composable
private fun PromotionDialog(
    isWhite:         Boolean,
    onPieceSelected: (PieceType) -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title  = { Text("Promote Pawn", fontFamily = CormorantGaramond, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text   = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
                    .forEach { type ->
                        val piece = ChessPiece(type, if (isWhite) PieceColor.WHITE else PieceColor.BLACK)
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .clickable { onPieceSelected(type) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(piece.glyph, fontSize = 30.sp)
                        }
                    }
            }
        },
        confirmButton = {},
        shape = RoundedCornerShape(20.dp)
    )
}
