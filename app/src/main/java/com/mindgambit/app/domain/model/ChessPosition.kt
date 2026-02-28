package com.mindgambit.app.domain.model

// ============================================================
// MindGambit — Chess Position
// Full game state: pieces, turn, castling rights, en passant,
// half-move clock, full-move number.
// ============================================================

data class ChessPosition(
    val pieces:          Array<ChessPiece?> = Array(64) { null },
    val sideToMove:      PieceColor          = PieceColor.WHITE,
    val castlingRights:  CastlingRights      = CastlingRights(),
    val enPassantSquare: Int?                 = null,    // square behind double-pushed pawn
    val halfMoveClock:   Int                  = 0,       // for 50-move rule
    val fullMoveNumber:  Int                  = 1,
    val moveHistory:     List<Move>           = emptyList(),
    val isCheck:         Boolean              = false,
    val isCheckmate:     Boolean              = false,
    val isStalemate:     Boolean              = false,
    val isDraw:          Boolean              = false
) {
    fun pieceAt(square: Int): ChessPiece? = pieces.getOrNull(square)

    fun isOccupied(square: Int): Boolean = pieces.getOrNull(square) != null

    fun isOccupiedByColor(square: Int, color: PieceColor): Boolean =
        pieces.getOrNull(square)?.color == color

    fun findKing(color: PieceColor): Int? =
        pieces.indexOfFirst { it?.type == PieceType.KING && it.color == color }
            .takeIf { it >= 0 }

    fun toFen(): String = FenParser.toFen(this)

    // For data class equality ignoring Array (needs manual equals)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChessPosition) return false
        return pieces.contentEquals(other.pieces) &&
                sideToMove == other.sideToMove &&
                castlingRights == other.castlingRights &&
                enPassantSquare == other.enPassantSquare
    }

    override fun hashCode(): Int {
        var result = pieces.contentHashCode()
        result = 31 * result + sideToMove.hashCode()
        result = 31 * result + castlingRights.hashCode()
        result = 31 * result + (enPassantSquare ?: 0)
        return result
    }

    companion object {
        val STARTING: ChessPosition get() = FenParser.fromFen(FEN_START)
        const val FEN_START = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }
}

data class CastlingRights(
    val whiteKingside:  Boolean = true,
    val whiteQueenside: Boolean = true,
    val blackKingside:  Boolean = true,
    val blackQueenside: Boolean = true
) {
    fun toFenString(): String {
        val sb = StringBuilder()
        if (whiteKingside)  sb.append('K')
        if (whiteQueenside) sb.append('Q')
        if (blackKingside)  sb.append('k')
        if (blackQueenside) sb.append('q')
        return if (sb.isEmpty()) "-" else sb.toString()
    }
}
