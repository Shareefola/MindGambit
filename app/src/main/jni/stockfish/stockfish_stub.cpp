// ============================================================
// MindGambit — Stockfish Stub
// Provides minimal symbol definitions so the project links when
// the full Stockfish source tree is not present.
// Replace this file (and add the real Stockfish sources to the
// stockfish/ directory) when integrating Stockfish properly.
// ============================================================

#include <string>
#include <android/log.h>

#define TAG "MindGambit/Stockfish"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

namespace Stockfish {

void init() {
    LOGI("Stockfish stub: init() called — Stockfish sources not present");
}

void loop(const std::string& /* commandArgs */) {
    LOGI("Stockfish stub: loop() called — Stockfish sources not present");
    // The JNI bridge's nativeInit() returns false if init fails gracefully,
    // but this stub just silently does nothing so the app can still launch.
    // Real engine output is never produced; StockfishEngine will stay in
    // EngineState.ERROR once it detects no "uciok" response.
}

} // namespace Stockfish
