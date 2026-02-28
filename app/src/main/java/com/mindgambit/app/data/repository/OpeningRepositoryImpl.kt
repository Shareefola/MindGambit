package com.mindgambit.app.data.repository

import com.mindgambit.app.data.database.dao.OpeningProgressDao
import com.mindgambit.app.data.database.entities.OpeningProgressEntity
import com.mindgambit.app.domain.model.*
import com.mindgambit.app.domain.repository.OpeningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ============================================================
// MindGambit — OpeningRepositoryImpl
// Contains full lesson content for all three opening paths.
// ============================================================

class OpeningRepositoryImpl @Inject constructor(
    private val progressDao: OpeningProgressDao
) : OpeningRepository {

    override fun getAllOpenings(): List<Opening> = ALL_OPENINGS

    override fun getProgress(openingId: OpeningId): Flow<OpeningProgress> =
        progressDao.getAllProgress().map { list ->
            val entity = list.find { it.openingId == openingId.name }
            val opening = getOpeningById(openingId) ?: return@map OpeningProgress(openingId, emptySet(), 0)
            entity?.toDomain(openingId)
                ?: OpeningProgress(openingId, emptySet(), opening.lessons.size)
        }

    override suspend fun markLessonComplete(openingId: OpeningId, lessonId: String) {
        val opening  = getOpeningById(openingId) ?: return
        val current  = progressDao.getProgress(openingId.name)
        val done     = current?.completedLessons
            ?.split(",")?.filter { it.isNotBlank() }?.toMutableSet()
            ?: mutableSetOf()
        done.add(lessonId)
        progressDao.upsertProgress(
            OpeningProgressEntity(
                openingId        = openingId.name,
                completedLessons = done.joinToString(","),
                totalLessons     = opening.lessons.size
            )
        )
    }

    override suspend fun getOpeningById(id: OpeningId): Opening? =
        ALL_OPENINGS.find { it.id == id }

    // ── Opening Content Database ──────────────────────────────

    companion object {

        val ALL_OPENINGS = listOf(LONDON, JOBAVA_LONDON, PIRC_DEFENSE)

        // ── LONDON SYSTEM ─────────────────────────────────────
        private val LONDON = Opening(
            id          = OpeningId.LONDON,
            name        = "London System",
            tag         = "Structure & Control",
            description = "A solid, low-theory opening for White that prioritises structure and development over sharp tactical play. Ideal for players who prefer positional chess.",
            eco         = "D02",
            moves       = "1. d4 d5 2. Bf4 Nf6 3. e3 e6 4. Nf3 Bd6 5. Bg3 O-O 6. Nbd2",
            lessons     = listOf(
                OpeningLesson(
                    id          = "london_1",
                    title       = "Core Setup",
                    description = "Learn the standard London pawn structure: d4, e3, c3 with the bishop on f4. This triangle is the heart of the system.",
                    fen         = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                    moves       = listOf("d2d4", "d7d5", "c1f4", "g8f6", "e2e3", "e7e6", "g1f3"),
                    isUnlocked  = true
                ),
                OpeningLesson(
                    id          = "london_2",
                    title       = "Bishop Development",
                    description = "Place the dark-squared bishop on f4 before locking in the pawn chain. The bishop must come out before e3 is played.",
                    fen         = "rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 2",
                    moves       = listOf("c1f4", "e7e6", "e2e3", "g8f6", "g1f3", "f8d6", "f4g3"),
                    isUnlocked  = true
                ),
                OpeningLesson(
                    id          = "london_3",
                    title       = "The Bf4–Bg3 Manoeuvre",
                    description = "When Black attacks the bishop with ...Bd6, retreat to g3 to maintain the bishop's influence on the long diagonal.",
                    fen         = "rnbqk2r/ppp2ppp/3pbn2/8/3P1B2/4PN2/PPP2PPP/RN1QKB1R w KQkq - 0 6",
                    moves       = listOf("f4g3", "f8g3", "h2g3"),
                    isUnlocked  = true
                ),
                OpeningLesson(
                    id          = "london_4",
                    title       = "Queenside Expansion",
                    description = "After completing development, break with c4 or b4 to challenge Black's centre and gain space on the queenside.",
                    fen         = "r1bq1rk1/ppp2ppp/2np1n2/3bp3/3P1B2/2PBPN2/PP3PPP/RN1QK2R w KQ - 0 8",
                    moves       = listOf("b1d2", "e8g8", "h2h3", "f8e8", "c3c4"),
                    isUnlocked  = false
                ),
                OpeningLesson(
                    id          = "london_5",
                    title       = "Kingside Attack Plan",
                    description = "Once castled, advance h4–h5 to open lines against the Black king. The London is not purely passive — it strikes when ready.",
                    fen         = "r1bq1rk1/ppp2ppp/2np1n2/4p3/3P1B2/2PBPN2/PP1N1PPP/R2QK2R w KQ e6 0 9",
                    moves       = listOf("e1g1", "c8e6", "h2h4", "f8e8", "h4h5"),
                    isUnlocked  = false
                ),
                OpeningLesson(
                    id          = "london_6",
                    title       = "Model Game: Barry Attack",
                    description = "Study how grandmasters use the London structure to convert positional advantages in the endgame.",
                    fen         = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                    moves       = listOf("d2d4", "g8f6", "g1f3", "d7d5", "c1f4", "c7c5", "e2e3", "b8c6", "c2c3"),
                    isUnlocked  = false
                ),
                OpeningLesson(
                    id          = "london_7",
                    title       = "Handling the ...c5 Break",
                    description = "Black's most challenging response. Learn to maintain tension and transition into favourable middlegames.",
                    fen         = "rnbqkb1r/pp2pppp/5n2/2pp4/3P1B2/4PN2/PPP2PPP/RN1QKB1R w KQkq - 0 5",
                    moves       = listOf("c2c3", "b8c6", "b1d2", "d8b6", "d1c2"),
                    isUnlocked  = false
                ),
                OpeningLesson(
                    id          = "london_8",
                    title       = "Endgame Conversion",
                    description = "The London's solid structure often leads to better endgames. Learn the key pawn breaks and king activation techniques.",
                    fen         = "8/ppp2ppp/3p4/3P4/3B4/4PN2/PPP2PPP/6K1 w - - 0 20",
                    moves       = listOf("g1f2", "g7g6", "f2e2", "f7f5", "e3e4"),
                    isUnlocked  = false
                )
            )
        )

        // ── JOBAVA LONDON ─────────────────────────────────────
        private val JOBAVA_LONDON = Opening(
            id          = OpeningId.JOBAVA_LONDON,
            name        = "Jobava London",
            tag         = "Aggressive Attack",
            description = "A dynamic and aggressive cousin of the London System, popularised by Baadur Jobava. Uses Nc3 instead of Nbd2, allowing immediate aggression.",
            eco         = "D00",
            moves       = "1. d4 d5 2. Bf4 Nf6 3. Nc3 e6 4. e3 Bd6 5. Nb5",
            lessons     = listOf(
                OpeningLesson(
                    id          = "jobava_1",
                    title       = "The Jobava Idea",
                    description = "Unlike the standard London, we develop the knight to c3 early, enabling the aggressive Nb5 jump to attack Black's bishop on d6.",
                    fen         = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                    moves       = listOf("d2d4", "d7d5", "c1f4", "g8f6", "b1c3"),
                    isUnlocked  = true
                ),
                OpeningLesson(
                    id          = "jobava_2",
                    title       = "The Nb5 Attack",
                    description = "After ...Bd6, the knight jumps to b5, forcing the bishop to move and disrupting Black's development. A powerful positional threat.",
                    fen         = "rnbqk2r/ppp2ppp/3pbn2/8/3P1B2/2N1PN2/PPP2PPP/R2QKB1R w KQkq - 2 7",
                    moves       = listOf("c3b5", "d6e7", "b5c7"),
                    isUnlocked  = true
                ),
                OpeningLesson(
                    id          = "jobava_3",
                    title       = "Attacking with g4",
                    description = "A key Jobava idea: push g4–g5 to attack the knight on f6 and open the g-file for rook pressure against the Black king.",
                    fen         = "r1bqk2r/ppp2ppp/3pbn2/1N6/3P1B2/4PN2/PPP2PPP/R2QKB1R w KQkq - 0 8",
                    moves       = listOf("h2h4", "e8g8", "g2g4", "f6e4", "g4g5"),
                    isUnlocked  = true
                ),
                OpeningLesson(
                    id          = "jobava_4",
                    title       = "Handling Black's Counterplay",
                    description = "Black often plays ...c5 to strike the centre. Learn how to maintain initiative while keeping structural solidity.",
                    fen         = "r1bq1rk1/pp3ppp/3pbn2/2p1N3/3P1B2/4PN2/PPP2PPP/R2QKB1R w KQ - 0 9",
                    moves       = listOf("d4c5", "d6c5", "d1d8", "f8d8", "b5c7"),
                    isUnlocked  = false
                ),
                OpeningLesson(
                    id          = "jobava_5",
                    title       = "The Rook Lift",
                    description = "A typical attacking plan: bring a rook to the third rank (R1e3 or Rf1–f3) and swing it to h3 or g3 for a kingside assault.",
                    fen         = "r2q1rk1/pp3ppp/3pb3/2p1N3/3P1B2/4PN2/PPP2PPP/R2QKB1R w KQ - 0 11",
                    moves       = listOf("f1e2", "a8c8", "e1g1", "c8c2", "f1f3", "f3h3"),
                    isUnlocked  = false
                ),
                OpeningLesson(
                    id          = "jobava_6",
                    title       = "Model Attack: Checkmate Patterns",
                    description = "Study the most common mating patterns arising from the Jobava London attack, including rook + bishop batteries and knight invasion.",
                    fen         = "r2q1rk1/pp3p1p/3pb1p1/2p5/3P1B2/4PNN1/PPP3PP/R2QK2R w KQ - 0 14",
                    moves       = listOf("g3h5", "g6h5", "d1h5", "f6h7", "f4h6"),
                    isUnlocked  = false
                )
            )
        )

        // ── PIRC DEFENSE ──────────────────────────────────────
        private val PIRC_DEFENSE = Opening(
            id          = OpeningId.PIRC,
            name        = "Pirc Defense",
            tag         = "Dynamic Counterplay",
            description = "A hypermodern defense where Black allows White to build a strong centre, then attacks it with pieces. Leads to rich, complex positions.",
            eco         = "B07",
            moves       = "1. e4 d6 2. d4 Nf6 3. Nc3 g6 4. Nf3 Bg7 5. Be2 O-O",
            lessons     = listOf(
                OpeningLesson(
                    id          = "pirc_1",
                    title       = "The Pirc Setup",
                    description = "Black's strategy: let White take the centre with e4+d4, then undermine it with ...c5 or ...e5 later. The fianchettoed bishop on g7 is the key piece.",
                    fen         = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
                    moves       = listOf("d7d6", "d2d4", "g8f6", "b1c3", "g7g6", "g1f3", "f8g7"),
                    isUnlocked  = true
                ),
                OpeningLesson(
                    id          = "pirc_2",
                    title       = "The Austrian Attack",
                    description = "White's most aggressive try: f4–f5 advance. Black must react precisely with ...c5 or ...e5 counterplay to avoid being crushed on the kingside.",
                    fen         = "rnbq1rk1/ppp1ppbp/3p1np1/8/3PPP2/2N2N2/PPP3PP/R1BQKB1R w KQ - 0 7",
                    moves       = listOf("f4f5", "c7c5", "d4d5", "e7e6", "f5e6", "f7e6"),
                    isUnlocked  = true
                ),
                OpeningLesson(
                    id          = "pirc_3",
                    title       = "The Classical Variation",
                    description = "White plays Be2–0–0 for a solid setup. Black counters with ...c5, hitting the d4 pawn and seeking queenside counterplay.",
                    fen         = "rnbq1rk1/ppp1ppbp/3p1np1/8/3PP3/2N2N2/PPP1BPPP/R1BQK2R b KQ - 3 7",
                    moves       = listOf("c7c5", "e1g1", "b8c6", "d4d5", "c6a5", "f3d2"),
                    isUnlocked  = true
                ),
                OpeningLesson(
                    id          = "pirc_4",
                    title       = "Counterattacking with ...e5",
                    description = "The ...e5 break is Black's most direct way to challenge White's centre. Learn when and how to play it effectively.",
                    fen         = "r1bq1rk1/ppp2pbp/3p1np1/4p3/3PP3/2N2N2/PPP1BPPP/R1BQ1RK1 w - e6 0 9",
                    moves       = listOf("d4d5", "a7a5", "g1h1", "b8d7", "f1g1", "d7c5"),
                    isUnlocked  = false
                ),
                OpeningLesson(
                    id          = "pirc_5",
                    title       = "The g7 Bishop in Action",
                    description = "The long diagonal bishop on g7 is Black's most powerful piece. Learn to activate it through pawn breaks and piece coordination.",
                    fen         = "r1bq1rk1/pp3pbp/3p1np1/2pP4/4P3/2N2N2/PP2BPPP/R1BQ1RK1 b - - 0 10",
                    moves       = listOf("f6h5", "c1e3", "h5f4", "e3f4", "g7d4"),
                    isUnlocked  = false
                ),
                OpeningLesson(
                    id          = "pirc_6",
                    title       = "Queenside Counterplay",
                    description = "When White attacks kingside, Black counter-attacks on the queenside. Learn the ...b5 and ...a5 expansion ideas.",
                    fen         = "r1bq1rk1/1p3pbp/3p1np1/p1pP4/4P3/2N1BN2/PP2BPPP/R2Q1RK1 b - - 0 12",
                    moves       = listOf("b7b5", "f3g5", "b5b4", "c3e2", "c5c4"),
                    isUnlocked  = false
                ),
                OpeningLesson(
                    id          = "pirc_7",
                    title       = "Model Game: Pirc in Practice",
                    description = "A complete annotated game showing how to handle the Pirc from move 1 to a successful endgame conversion.",
                    fen         = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
                    moves       = listOf("d7d6", "d2d4", "g8f6", "b1c3", "g7g6", "g1f3", "f8g7", "f1e2", "e8g8", "e1g1", "c7c6"),
                    isUnlocked  = false
                )
            )
        )
    }
}
