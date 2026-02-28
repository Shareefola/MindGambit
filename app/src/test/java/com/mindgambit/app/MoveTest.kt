package com.mindgambit.app.domain.model

import org.junit.Assert.*
import org.junit.Test

// ============================================================
// MindGambit — Move Unit Tests
// ============================================================

class MoveTest {

    @Test
    fun `toUci produces correct string for simple move`() {
        val move = Move(from = 12, to = 28)   // e2e4
        assertEquals("e2e4", move.toUci())
    }

    @Test
    fun `toUci includes promotion piece`() {
        val move = Move(from = 48, to = 56, promotion = PieceType.QUEEN)  // a7a8q
        assertEquals("a7a8q", move.toUci())
    }

    @Test
    fun `toUci promotion piece is lowercase`() {
        val move = Move(from = 48, to = 56, promotion = PieceType.KNIGHT)
        assertTrue(move.toUci().last().isLowerCase())
    }

    @Test
    fun `isCapture returns true when CAPTURE flag set`() {
        val move = Move(from = 0, to = 8, flags = setOf(MoveFlag.CAPTURE))
        assertTrue(move.isCapture)
    }

    @Test
    fun `isCapture returns true for en passant`() {
        val move = Move(from = 0, to = 8, flags = setOf(MoveFlag.EN_PASSANT))
        assertTrue(move.isCapture)
        assertTrue(move.isEnPassant)
    }

    @Test
    fun `isCapture returns false for quiet move`() {
        val move = Move(from = 0, to = 8)
        assertFalse(move.isCapture)
    }

    @Test
    fun `isCastle returns true for kingside castling`() {
        val move = Move(from = 4, to = 6, flags = setOf(MoveFlag.CASTLE_KINGSIDE))
        assertTrue(move.isCastle)
    }

    @Test
    fun `isCastle returns true for queenside castling`() {
        val move = Move(from = 4, to = 2, flags = setOf(MoveFlag.CASTLE_QUEENSIDE))
        assertTrue(move.isCastle)
    }

    @Test
    fun `isPromotion returns true when promotion piece set`() {
        val move = Move(from = 48, to = 56, promotion = PieceType.QUEEN)
        assertTrue(move.isPromotion)
    }

    @Test
    fun `isPromotion returns false when no promotion piece`() {
        val move = Move(from = 8, to = 16)
        assertFalse(move.isPromotion)
    }

    @Test
    fun `toString equals toUci`() {
        val move = Move(from = 12, to = 28)
        assertEquals(move.toUci(), move.toString())
    }

    // ── squareOf / squareFile / squareRank ────────────────────

    @Test
    fun `squareOf produces correct index`() {
        assertEquals(0,  squareOf(0, 0))   // a1
        assertEquals(7,  squareOf(7, 0))   // h1
        assertEquals(56, squareOf(0, 7))   // a8
        assertEquals(63, squareOf(7, 7))   // h8
        assertEquals(28, squareOf(4, 3))   // e4
    }

    @Test
    fun `squareFile and squareRank round-trip`() {
        for (sq in 0..63) {
            assertEquals(sq, squareOf(squareFile(sq), squareRank(sq)))
        }
    }

    @Test
    fun `a1 is square 0`() {
        assertEquals(0, algebraicToSquare("a1"))
    }

    @Test
    fun `h8 is square 63`() {
        assertEquals(63, algebraicToSquare("h8"))
    }

    @Test
    fun `e4 is square 28`() {
        assertEquals(28, algebraicToSquare("e4"))
    }
}
