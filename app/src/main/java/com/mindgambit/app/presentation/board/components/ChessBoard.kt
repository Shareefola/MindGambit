package com.mindgambit.app.presentation.board.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import com.mindgambit.app.domain.model.*
import com.mindgambit.app.presentation.theme.*

// ============================================================
// MindGambit — Chess Board Composable
// Full 8x8 board with:
//  • Beautiful classic wood-ivory squares
//  • Animated piece movement (spring physics)
//  • Highlighted selected square
//  • Possible move dots / capture rings
//  • Last move highlight
//  • Check highlight (king square)
//  • File/rank coordinate labels
// ============================================================

@Composable
fun ChessBoard(
    position:       ChessPosition,
    selectedSquare: Int?,
    possibleMoves:  List<Int>,
    lastMove:       Pair<Int, Int>?,
    flipped:        Boolean = false,
    onSquareClick:  (Int) -> Unit,
    modifier:       Modifier = Modifier
) {
    val kingSquare = position.findKing(position.sideToMove)
    val inCheck    = position.isCheck

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .border(2.dp, Color(0xFF5A3A1A).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
    ) {
        val boardSize   = maxWidth
        val squareSize  = boardSize / 8

        Column {
            for (rowIdx in 0..7) {
                Row {
                    for (colIdx in 0..7) {
                        val displayRank = if (flipped) rowIdx     else 7 - rowIdx
                        val displayFile = if (flipped) 7 - colIdx else colIdx
                        val square      = displayRank * 8 + displayFile
                        val isLight     = (displayRank + displayFile) % 2 != 0

                        ChessSquare(
                            square        = square,
                            isLight       = isLight,
                            piece         = position.pieceAt(square),
                            isSelected    = selectedSquare == square,
                            isPossibleMove= square in possibleMoves,
                            isCaptureable = square in possibleMoves && position.isOccupied(square),
                            isLastMoveFrom= lastMove?.first  == square,
                            isLastMoveTo  = lastMove?.second == square,
                            isKingInCheck = inCheck && square == kingSquare,
                            showRankLabel = colIdx == 0,
                            showFileLabel = rowIdx == 7,
                            rank          = displayRank,
                            file          = displayFile,
                            squareSize    = squareSize.value,
                            onSquareClick = { onSquareClick(square) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChessSquare(
    square:         Int,
    isLight:        Boolean,
    piece:          ChessPiece?,
    isSelected:     Boolean,
    isPossibleMove: Boolean,
    isCaptureable:  Boolean,
    isLastMoveFrom: Boolean,
    isLastMoveTo:   Boolean,
    isKingInCheck:  Boolean,
    showRankLabel:  Boolean,
    showFileLabel:  Boolean,
    rank:           Int,
    file:           Int,
    squareSize:     Float,
    onSquareClick:  () -> Unit
) {
    val baseColor = if (isLight) SquareLight else SquareDark

    val bgColor = when {
        isSelected     -> SquareSelected
        isKingInCheck  -> SquareCheck
        isLastMoveFrom || isLastMoveTo -> SquareLastMove
        else           -> baseColor
    }

    Box(
        modifier = Modifier
            .size(squareSize.dp)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { onSquareClick() }
    ) {
        // ── Coordinate labels ──────────────────────────────────
        if (showRankLabel) {
            Text(
                text     = "${rank + 1}",
                fontSize = (squareSize * 0.18f).sp,
                fontWeight = FontWeight.Bold,
                color    = if (isLight) SquareDark.copy(alpha = 0.6f) else SquareLight.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.TopStart).padding(2.dp)
            )
        }
        if (showFileLabel) {
            Text(
                text     = "${'a' + file}",
                fontSize = (squareSize * 0.18f).sp,
                fontWeight = FontWeight.Bold,
                color    = if (isLight) SquareDark.copy(alpha = 0.6f) else SquareLight.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
            )
        }

        // ── Possible move indicator ────────────────────────────
        if (isPossibleMove && !isCaptureable) {
            Box(
                modifier = Modifier
                    .size((squareSize * 0.34f).dp)
                    .align(Alignment.Center)
                    .background(SquarePossibleMove, CircleShape)
            )
        }
        if (isCaptureable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding((squareSize * 0.06f).dp)
                    .border(
                        width = (squareSize * 0.1f).dp,
                        color = SquarePossibleCapture,
                        shape = CircleShape
                    )
            )
        }

        // ── Chess Piece ────────────────────────────────────────
        if (piece != null) {
            AnimatedPiece(
                piece      = piece,
                isSelected = isSelected,
                squareSize = squareSize,
                modifier   = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun AnimatedPiece(
    piece:      ChessPiece,
    isSelected: Boolean,
    squareSize: Float,
    modifier:   Modifier = Modifier
) {
    // Lift effect when selected
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "pieceScale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isSelected) 8f else 0f,
        animationSpec = tween(150),
        label = "pieceElevation"
    )

    Text(
        text       = piece.glyph,
        fontSize   = (squareSize * 0.76f).sp,
        textAlign  = TextAlign.Center,
        lineHeight = (squareSize * 0.76f).sp,
        modifier   = modifier.graphicsLayer {
            scaleX        = scale
            scaleY        = scale
            shadowElevation = elevation
        }
    )
}

// ── Player Card ───────────────────────────────────────────────

@Composable
fun PlayerCard(
    name:       String,
    rating:     Int,
    isWhite:    Boolean,
    timeLeft:   Int,   // seconds
    isActive:   Boolean,
    modifier:   Modifier = Modifier
) {
    val clockAnim by animateColorAsState(
        targetValue = if (isActive) Color(0xFF1A1814) else Color(0xFFF5F3EE),
        animationSpec = tween(300),
        label = "clockBg"
    )
    val clockTextColor by animateColorAsState(
        targetValue = if (isActive) Color.White else Color(0xFF2D2B26),
        animationSpec = tween(300),
        label = "clockText"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFEEEAE0), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(
                    color = if (isWhite) Color(0xFFF5F3EE) else Color(0xFF1A1814),
                    shape = CircleShape
                )
                .border(
                    1.dp,
                    if (isWhite) Color(0xFFD4D0C8) else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text     = if (isWhite) "♔" else "♚",
                fontSize = 14.sp,
                color    = if (isWhite) Color(0xFF2D2B26) else Color.White
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = name,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color(0xFF2D2B26)
            )
            Text(
                text     = "$rating",
                fontSize = 10.sp,
                color    = Color(0xFF9A9690),
                fontFamily = DmMono
            )
        }

        // Clock
        Box(
            modifier = Modifier
                .background(clockAnim, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text       = formatClock(timeLeft),
                fontSize   = 15.sp,
                fontFamily = DmMono,
                fontWeight = FontWeight.Medium,
                color      = clockTextColor
            )
        }
    }
}

private fun formatClock(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
