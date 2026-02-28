package com.mindgambit.app.domain.model

// ============================================================
// MindGambit — Move Model
// Squares are 0–63 (rank 0 = rank 1, file 0 = a-file).
// ============================================================

data class Move(
    val from:      Int,
    val to:        Int,
    val promotion: PieceType? = null,
    val flags:     Set<MoveFlag> = emptySet()
) {
    val isCastle:     Boolean get() = MoveFlag.CASTLE_KINGSIDE  in flags || MoveFlag.CASTLE_QUEENSIDE in flags
    val isEnPassant:  Boolean get() = MoveFlag.EN_PASSANT       in flags
    val isPromotion:  Boolean get() = promotion != null
    val isCapture:    Boolean get() = MoveFlag.CAPTURE          in flags || isEnPassant

    // UCI notation e.g. "e2e4", "e7e8q"
    fun toUci(): String {
        val fromAlg = squareToAlgebraic(from)
        val toAlg   = squareToAlgebraic(to)
        val promo   = promotion?.symbol?.lowercaseChar()?.toString() ?: ""
        return "$fromAlg$toAlg$promo"
    }

    override fun toString() = toUci()
}

enum class MoveFlag {
    CAPTURE,
    EN_PASSANT,
    CASTLE_KINGSIDE,
    CASTLE_QUEENSIDE,
    DOUBLE_PAWN_PUSH,
    CHECK,
    CHECKMATE,
}

// Square helpers
fun squareToAlgebraic(sq: Int): String {
    val file = sq % 8
    val rank = sq / 8
    return "${'a' + file}${rank + 1}"
}

fun algebraicToSquare(alg: String): Int {
    val file = alg[0] - 'a'
    val rank = alg[1] - '1'
    return rank * 8 + file
}

fun squareFile(sq: Int): Int = sq % 8
fun squareRank(sq: Int): Int = sq / 8
fun squareOf(file: Int, rank: Int): Int = rank * 8 + file
