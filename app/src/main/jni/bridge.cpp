// ============================================================
// MindGambit — JNI Bridge
// Connects Kotlin StockfishEngine.kt to Stockfish C++ library.
// Uses POSIX pipes for UCI command communication.
// ============================================================

#include <jni.h>
#include <android/log.h>
#include <string>
#include <thread>
#include <mutex>
#include <queue>
#include <condition_variable>
#include <sstream>

#define TAG "MindGambit/Stockfish"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Forward declarations from Stockfish's main entry
namespace Stockfish {
    void init();
    void loop(const std::string& args);
}

// ── Pipe infrastructure ──────────────────────────────────────

static std::mutex              inputMutex;
static std::queue<std::string> inputQueue;
static std::condition_variable inputCv;

static std::mutex              outputMutex;
static std::queue<std::string> outputQueue;
static std::condition_variable outputCv;

static bool engineRunning = false;
static std::thread engineThread;

// Override Stockfish's stdin/stdout via custom stream buffers
class InputBuffer : public std::streambuf {
protected:
    int underflow() override {
        std::unique_lock<std::mutex> lock(inputMutex);
        inputCv.wait(lock, [] { return !inputQueue.empty(); });
        currentLine = inputQueue.front() + "\n";
        inputQueue.pop();
        setg(&currentLine[0], &currentLine[0], &currentLine[currentLine.size()]);
        return traits_type::to_int_type(*gptr());
    }
private:
    std::string currentLine;
};

class OutputBuffer : public std::streambuf {
protected:
    int overflow(int c) override {
        if (c != EOF) {
            if (c == '\n') {
                std::unique_lock<std::mutex> lock(outputMutex);
                outputQueue.push(lineBuffer);
                lineBuffer.clear();
                outputCv.notify_one();
            } else {
                lineBuffer += static_cast<char>(c);
            }
        }
        return c;
    }
private:
    std::string lineBuffer;
};

static InputBuffer*  inputBuf  = nullptr;
static OutputBuffer* outputBuf = nullptr;

// ── JNI implementations ──────────────────────────────────────

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_mindgambit_app_data_engine_StockfishEngine_nativeInit(
    JNIEnv* env, jobject thiz)
{
    try {
        inputBuf  = new InputBuffer();
        outputBuf = new OutputBuffer();

        // Redirect cin/cout to our buffers
        std::cin.rdbuf(inputBuf);
        std::cout.rdbuf(outputBuf);

        engineRunning = true;

        // Run Stockfish's UCI loop in a background thread
        engineThread = std::thread([]() {
            Stockfish::init();
            Stockfish::loop("");
        });
        engineThread.detach();

        LOGI("Stockfish engine initialized");
        return JNI_TRUE;
    } catch (...) {
        LOGE("Failed to initialize Stockfish");
        return JNI_FALSE;
    }
}

JNIEXPORT void JNICALL
Java_com_mindgambit_app_data_engine_StockfishEngine_nativeSendCommand(
    JNIEnv* env, jobject thiz, jstring command)
{
    const char* cmd = env->GetStringUTFChars(command, nullptr);
    {
        std::unique_lock<std::mutex> lock(inputMutex);
        inputQueue.push(std::string(cmd));
        inputCv.notify_one();
    }
    env->ReleaseStringUTFChars(command, cmd);
}

JNIEXPORT jstring JNICALL
Java_com_mindgambit_app_data_engine_StockfishEngine_nativeReadLine(
    JNIEnv* env, jobject thiz)
{
    std::unique_lock<std::mutex> lock(outputMutex);
    bool received = outputCv.wait_for(
        lock,
        std::chrono::milliseconds(5000),
        [] { return !outputQueue.empty(); }
    );

    if (!received || outputQueue.empty()) {
        return nullptr;
    }

    std::string line = outputQueue.front();
    outputQueue.pop();
    return env->NewStringUTF(line.c_str());
}

JNIEXPORT void JNICALL
Java_com_mindgambit_app_data_engine_StockfishEngine_nativeDestroy(
    JNIEnv* env, jobject thiz)
{
    engineRunning = false;
    // Send quit command to Stockfish's loop
    std::unique_lock<std::mutex> lock(inputMutex);
    inputQueue.push("quit");
    inputCv.notify_one();
    LOGI("Stockfish engine destroyed");
}

} // extern "C"
