package com.mindgambit.app.domain.model

// ============================================================
// MindGambit — FEN Parser
// Converts between FEN strings and ChessPosition objects.
// FEN ranks go 8→1 (top to bottom), files a→h (left to right).
// Our board: index 0 = a1 (bottom-left), 63 = h8 (top-right).
// ============================================================

object FenParser {

    fun fromFen(fen: String): ChessPosition {
        val parts = fen.trim().split(" ")
        require(parts.size >= 4) { "Invalid FEN: $fen" }

        val pieces = Array<ChessPiece?>(64) { null }

        // Parse piece placement (rank 8 → rank 1)
        var rank = 7
        var file = 0
        for (ch in parts[0]) {
            when {
                ch == '/' -> { rank--; file = 0 }
                ch.isDigit() -> file += ch.digitToInt()
                else -> {
                    val color = if (ch.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
                    val type  = charToPieceType(ch.uppercaseChar())
                    pieces[rank * 8 + file] = ChessPiece(type, color)
                    file++
                }
            }
        }

        // Side to move
        val sideToMove = if (parts[1] == "w") PieceColor.WHITE else PieceColor.BLACK

        // Castling rights
        val castling = parts[2]
        val castlingRights = CastlingRights(
            whiteKingside  = 'K' in castling,
            whiteQueenside = 'Q' in castling,
            blackKingside  = 'k' in castling,
            blackQueenside = 'q' in castling,
        )

        // En passant
        val epSquare = if (parts[3] == "-") null else algebraicToSquare(parts[3])

        // Clocks
        val halfMove = parts.getOrNull(4)?.toIntOrNull() ?: 0
        val fullMove = parts.getOrNull(5)?.toIntOrNull() ?: 1

        return ChessPosition(
            pieces          = pieces,
            sideToMove      = sideToMove,
            castlingRights  = castlingRights,
            enPassantSquare = epSquare,
            halfMoveClock   = halfMove,
            fullMoveNumber  = fullMove
        )
    }

    fun toFen(pos: ChessPosition): String {
        val sb = StringBuilder()

        // Piece placement
        for (rank in 7 downTo 0) {
            var empty = 0
            for (file in 0..7) {
                val piece = pos.pieces[rank * 8 + file]
                if (piece == null) {
                    empty++
                } else {
                    if (empty > 0) { sb.append(empty); empty = 0 }
                    val ch = pieceTypeToChar(piece.type)
                    sb.append(if (piece.color == PieceColor.WHITE) ch.uppercaseChar() else ch.lowercaseChar())
                }
            }
            if (empty > 0) sb.append(empty)
            if (rank > 0) sb.append('/')
        }

        sb.append(' ')
        sb.append(if (pos.sideToMove == PieceColor.WHITE) 'w' else 'b')
        sb.append(' ')
        sb.append(pos.castlingRights.toFenString())
        sb.append(' ')
        sb.append(pos.enPassantSquare?.let { squareToAlgebraic(it) } ?: "-")
        sb.append(' ')
        sb.append(pos.halfMoveClock)
        sb.append(' ')
        sb.append(pos.fullMoveNumber)

        return sb.toString()
    }

    private fun charToPieceType(ch: Char): PieceType = when (ch) {
        'K' -> PieceType.KING
        'Q' -> PieceType.QUEEN
        'R' -> PieceType.ROOK
        'B' -> PieceType.BISHOP
        'N' -> PieceType.KNIGHT
        'P' -> PieceType.PAWN
        else -> throw IllegalArgumentException("Unknown piece: $ch")
    }

    private fun pieceTypeToChar(type: PieceType): Char = when (type) {
        PieceType.KING   -> 'k'
        PieceType.QUEEN  -> 'q'
        PieceType.ROOK   -> 'r'
        PieceType.BISHOP -> 'b'
        PieceType.KNIGHT -> 'n'
        PieceType.PAWN   -> 'p'
    }
}
