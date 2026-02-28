package com.mindgambit.app.data.engine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

// ============================================================
// MindGambit — Stockfish Engine
// Communicates with Stockfish 16 via JNI bridge.
// All engine operations run on a dedicated IO thread.
// ============================================================

data class EngineResult(
    val bestMove:    String,          // UCI format e.g. "e2e4"
    val ponderMove:  String?  = null, // suggested ponder move
    val evaluation:  Int      = 0,    // centipawns (positive = white advantage)
    val mateIn:      Int?     = null, // null if no forced mate detected
    val depth:       Int      = 0,
    val principalVar:List<String> = emptyList()
)

enum class EngineState { UNINITIALIZED, READY, THINKING, ERROR }

@Singleton
class StockfishEngine @Inject constructor() {

    private val engineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _state = MutableStateFlow(EngineState.UNINITIALIZED)
    val state: StateFlow<EngineState> = _state

    // ── JNI declarations ──────────────────────────────────────
    private external fun nativeInit(): Boolean
    private external fun nativeSendCommand(command: String)
    private external fun nativeReadLine(): String?
    private external fun nativeDestroy()

    init {
        System.loadLibrary("stockfish")
        engineScope.launch { initialize() }
    }

    private suspend fun initialize() {
        val ok = withContext(Dispatchers.IO) { nativeInit() }
        if (ok) {
            sendCommand("uci")
            waitForResponse("uciok")
            sendCommand("setoption name Threads value 2")
            sendCommand("setoption name Hash value 16")
            sendCommand("isready")
            waitForResponse("readyok")
            _state.value = EngineState.READY
        } else {
            _state.value = EngineState.ERROR
        }
    }

    // ── Public API ────────────────────────────────────────────

    suspend fun getBestMove(
        fen:       String,
        moveTime:  Int  = 1000,   // milliseconds
        depth:     Int  = 15
    ): EngineResult = withContext(Dispatchers.IO) {
        _state.value = EngineState.THINKING

        sendCommand("position fen $fen")
        sendCommand("go movetime $moveTime depth $depth")

        val result = collectSearchOutput()
        _state.value = EngineState.READY
        result
    }

    suspend fun evaluatePosition(fen: String, depth: Int = 12): EngineResult =
        withContext(Dispatchers.IO) {
            _state.value = EngineState.THINKING
            sendCommand("position fen $fen")
            sendCommand("go depth $depth")
            val result = collectSearchOutput()
            _state.value = EngineState.READY
            result
        }

    suspend fun getLegalMoves(fen: String): List<String> =
        withContext(Dispatchers.IO) {
            sendCommand("position fen $fen")
            sendCommand("d")     // Stockfish debug prints legal moves
            val lines = mutableListOf<String>()
            var line: String?
            do {
                line = nativeReadLine()
                if (line != null) lines.add(line)
            } while (line != null && !line.startsWith("Checkers"))
            // Parse "Legal moves:" line
            lines.find { it.startsWith("Legal moves:") }
                ?.removePrefix("Legal moves:")
                ?.trim()
                ?.split(" ")
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        }

    fun stopThinking() {
        sendCommand("stop")
    }

    fun newGame() {
        sendCommand("ucinewgame")
    }

    // ── Internal helpers ──────────────────────────────────────

    private fun sendCommand(command: String) {
        nativeSendCommand(command)
    }

    private fun waitForResponse(expected: String): String {
        var line: String?
        do { line = nativeReadLine() } while (line != null && !line.startsWith(expected))
        return line ?: ""
    }

    private fun collectSearchOutput(): EngineResult {
        var bestMove    = ""
        var evaluation  = 0
        var mateIn: Int? = null
        var depth       = 0
        val pv          = mutableListOf<String>()

        var line: String?
        do {
            line = nativeReadLine() ?: break

            when {
                line.startsWith("info") -> {
                    parseInfoLine(line).let { info ->
                        info.eval?.let { evaluation = it }
                        info.mate?.let { mateIn = it }
                        info.depth?.let { depth = it }
                        info.pv.let { if (it.isNotEmpty()) { pv.clear(); pv.addAll(it) } }
                    }
                }
                line.startsWith("bestmove") -> {
                    val parts = line.split(" ")
                    bestMove = parts.getOrNull(1) ?: ""
                }
            }
        } while (!line.startsWith("bestmove"))

        return EngineResult(
            bestMove     = bestMove,
            ponderMove   = null,
            evaluation   = evaluation,
            mateIn       = mateIn,
            depth        = depth,
            principalVar = pv.toList()
        )
    }

    private data class InfoParsed(
        val depth: Int?         = null,
        val eval:  Int?         = null,
        val mate:  Int?         = null,
        val pv:    List<String> = emptyList()
    )

    private fun parseInfoLine(line: String): InfoParsed {
        val tokens = line.split(" ")
        var depth: Int?         = null
        var eval:  Int?         = null
        var mate:  Int?         = null
        val pv = mutableListOf<String>()
        var inPv = false

        var i = 0
        while (i < tokens.size) {
            when (tokens[i]) {
                "depth"  -> depth = tokens.getOrNull(++i)?.toIntOrNull()
                "cp"     -> { eval = tokens.getOrNull(++i)?.toIntOrNull(); inPv = false }
                "mate"   -> { mate = tokens.getOrNull(++i)?.toIntOrNull(); inPv = false }
                "pv"     -> inPv = true
                else     -> if (inPv) pv.add(tokens[i])
            }
            i++
        }
        return InfoParsed(depth, eval, mate, pv)
    }

    fun destroy() {
        engineScope.cancel()
        nativeDestroy()
    }
}
