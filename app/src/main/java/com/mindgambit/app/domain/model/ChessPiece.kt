package com.mindgambit.app.domain.model

// ============================================================
// MindGambit — Chess Piece Model
// ============================================================

enum class PieceColor { WHITE, BLACK;
    fun opposite() = if (this == WHITE) BLACK else WHITE
}

enum class PieceType(val symbol: Char) {
    KING   ('K'),
    QUEEN  ('Q'),
    ROOK   ('R'),
    BISHOP ('B'),
    KNIGHT ('N'),
    PAWN   ('P');
}

data class ChessPiece(
    val type:  PieceType,
    val color: PieceColor
) {
    // Unicode glyph for rendering
    val glyph: String get() = when (color) {
        PieceColor.WHITE -> when (type) {
            PieceType.KING   -> "♔"
            PieceType.QUEEN  -> "♕"
            PieceType.ROOK   -> "♖"
            PieceType.BISHOP -> "♗"
            PieceType.KNIGHT -> "♘"
            PieceType.PAWN   -> "♙"
        }
        PieceColor.BLACK -> when (type) {
            PieceType.KING   -> "♚"
            PieceType.QUEEN  -> "♛"
            PieceType.ROOK   -> "♜"
            PieceType.BISHOP -> "♝"
            PieceType.KNIGHT -> "♞"
            PieceType.PAWN   -> "♟"
        }
    }

    // Resource name for SVG piece images (e.g. "wp", "bk")
    val resourceName: String get() =
        "${color.name.first().lowercaseChar()}${type.name.first().lowercaseChar()}"

    // Algebraic notation char
    val algebraicChar: Char get() = if (type == PieceType.PAWN) ' ' else type.symbol

    override fun toString() = "${color.name[0]}${type.name[0]}"
}

// Convenience constructors
fun whitePiece(type: PieceType) = ChessPiece(type, PieceColor.WHITE)
fun blackPiece(type: PieceType) = ChessPiece(type, PieceColor.BLACK)
