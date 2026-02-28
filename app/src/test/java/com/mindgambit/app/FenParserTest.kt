package com.mindgambit.app.domain.model

import org.junit.Assert.*
import org.junit.Test

// ============================================================
// MindGambit — FenParser Unit Tests
// ============================================================

class FenParserTest {

    // ── Starting position ─────────────────────────────────────

    @Test
    fun `starting position parses correctly`() {
        val pos = FenParser.fromFen(ChessPosition.FEN_START)

        // Side to move
        assertEquals(PieceColor.WHITE, pos.sideToMove)

        // Castling rights
        assertTrue(pos.castlingRights.whiteKingside)
        assertTrue(pos.castlingRights.whiteQueenside)
        assertTrue(pos.castlingRights.blackKingside)
        assertTrue(pos.castlingRights.blackQueenside)

        // No en passant
        assertNull(pos.enPassantSquare)

        // Clocks
        assertEquals(0, pos.halfMoveClock)
        assertEquals(1, pos.fullMoveNumber)
    }

    @Test
    fun `starting position has correct pieces on rank 1`() {
        val pos = FenParser.fromFen(ChessPosition.FEN_START)

        // White back rank (rank 0 in our indexing = rank 1)
        assertEquals(PieceType.ROOK,   pos.pieces[0]?.type)
        assertEquals(PieceType.KNIGHT, pos.pieces[1]?.type)
        assertEquals(PieceType.BISHOP, pos.pieces[2]?.type)
        assertEquals(PieceType.QUEEN,  pos.pieces[3]?.type)
        assertEquals(PieceType.KING,   pos.pieces[4]?.type)
        assertEquals(PieceType.BISHOP, pos.pieces[5]?.type)
        assertEquals(PieceType.KNIGHT, pos.pieces[6]?.type)
        assertEquals(PieceType.ROOK,   pos.pieces[7]?.type)

        // All white
        (0..7).forEach { i ->
            assertEquals(PieceColor.WHITE, pos.pieces[i]?.color)
        }
    }

    @Test
    fun `starting position has correct pieces on rank 8`() {
        val pos = FenParser.fromFen(ChessPosition.FEN_START)

        // Black back rank (rank 7 in our indexing = rank 8)
        val rank8Start = 7 * 8
        assertEquals(PieceType.ROOK,   pos.pieces[rank8Start]?.type)
        assertEquals(PieceType.KNIGHT, pos.pieces[rank8Start + 1]?.type)
        assertEquals(PieceType.BISHOP, pos.pieces[rank8Start + 2]?.type)
        assertEquals(PieceType.QUEEN,  pos.pieces[rank8Start + 3]?.type)
        assertEquals(PieceType.KING,   pos.pieces[rank8Start + 4]?.type)

        // All black
        (rank8Start..rank8Start + 7).forEach { i ->
            assertEquals(PieceColor.BLACK, pos.pieces[i]?.color)
        }
    }

    @Test
    fun `ranks 3 to 6 are empty in starting position`() {
        val pos = FenParser.fromFen(ChessPosition.FEN_START)

        for (rank in 2..5) {
            for (file in 0..7) {
                assertNull("Square ${rank*8+file} should be empty", pos.pieces[rank * 8 + file])
            }
        }
    }

    @Test
    fun `white pawns are on rank 2`() {
        val pos = FenParser.fromFen(ChessPosition.FEN_START)

        for (file in 0..7) {
            val piece = pos.pieces[8 + file]  // rank 1 (0-indexed)
            assertEquals(PieceType.PAWN, piece?.type)
            assertEquals(PieceColor.WHITE, piece?.color)
        }
    }

    @Test
    fun `black pawns are on rank 7`() {
        val pos = FenParser.fromFen(ChessPosition.FEN_START)

        for (file in 0..7) {
            val piece = pos.pieces[6 * 8 + file]  // rank 6 (0-indexed)
            assertEquals(PieceType.PAWN, piece?.type)
            assertEquals(PieceColor.BLACK, piece?.color)
        }
    }

    // ── Round-trip ────────────────────────────────────────────

    @Test
    fun `FEN round-trip for starting position`() {
        val original = ChessPosition.FEN_START
        val pos      = FenParser.fromFen(original)
        val roundTrip = FenParser.toFen(pos)
        assertEquals(original, roundTrip)
    }

    @Test
    fun `FEN round-trip for mid-game position`() {
        val fen = "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/5N2/PPPP1PPP/RNBQ1RK1 b kq - 3 5"
        val pos = FenParser.fromFen(fen)
        val roundTrip = FenParser.toFen(pos)
        assertEquals(fen, roundTrip)
    }

    @Test
    fun `FEN round-trip for position with en passant`() {
        val fen = "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3"
        val pos = FenParser.fromFen(fen)
        val roundTrip = FenParser.toFen(pos)
        assertEquals(fen, roundTrip)
    }

    @Test
    fun `FEN with partial castling rights parses correctly`() {
        val fen = "r1bqk2r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 0 1"
        val pos = FenParser.fromFen(fen)
        assertTrue(pos.castlingRights.whiteKingside)
        assertTrue(pos.castlingRights.whiteQueenside)
        assertTrue(pos.castlingRights.blackKingside)
        assertTrue(pos.castlingRights.blackQueenside)
    }

    @Test
    fun `FEN with no castling rights parses correctly`() {
        val fen = "4k3/8/8/8/8/8/8/4K3 w - - 0 1"
        val pos = FenParser.fromFen(fen)
        assertFalse(pos.castlingRights.whiteKingside)
        assertFalse(pos.castlingRights.whiteQueenside)
        assertFalse(pos.castlingRights.blackKingside)
        assertFalse(pos.castlingRights.blackQueenside)
    }

    // ── Algebraic helpers ─────────────────────────────────────

    @Test
    fun `squareToAlgebraic converts correctly`() {
        assertEquals("a1", squareToAlgebraic(0))
        assertEquals("h1", squareToAlgebraic(7))
        assertEquals("a8", squareToAlgebraic(56))
        assertEquals("h8", squareToAlgebraic(63))
        assertEquals("e4", squareToAlgebraic(28))
        assertEquals("d5", squareToAlgebraic(35))
    }

    @Test
    fun `algebraicToSquare converts correctly`() {
        assertEquals(0,  algebraicToSquare("a1"))
        assertEquals(7,  algebraicToSquare("h1"))
        assertEquals(56, algebraicToSquare("a8"))
        assertEquals(63, algebraicToSquare("h8"))
        assertEquals(28, algebraicToSquare("e4"))
        assertEquals(35, algebraicToSquare("d5"))
    }

    @Test
    fun `algebraic round-trip`() {
        for (sq in 0..63) {
            assertEquals(sq, algebraicToSquare(squareToAlgebraic(sq)))
        }
    }

    // ── Edge cases ────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun `invalid FEN throws exception`() {
        FenParser.fromFen("not_a_fen")
    }

    @Test
    fun `findKing returns correct square`() {
        val pos = FenParser.fromFen(ChessPosition.FEN_START)
        assertEquals(4,  pos.findKing(PieceColor.WHITE))   // e1
        assertEquals(60, pos.findKing(PieceColor.BLACK))   // e8
    }
}
