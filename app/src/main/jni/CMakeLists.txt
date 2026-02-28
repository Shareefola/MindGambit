# ============================================================
# MindGambit — CMakeLists.txt
# Builds Stockfish 16 as a shared library via Android NDK
# ============================================================

cmake_minimum_required(VERSION 3.22.1)
project("stockfish")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Stockfish compile flags — optimised for mobile
set(STOCKFISH_FLAGS
    -DNDEBUG
    -O3
    -funroll-loops
    -fno-exceptions
    -fno-rtti
    -DIS_64BIT
    -DUSE_POPCNT
)

# Collect all Stockfish source files
file(GLOB STOCKFISH_SOURCES
    "stockfish/*.cpp"
    "stockfish/syzygy/*.cpp"
)

# Our JNI bridge
set(BRIDGE_SOURCES bridge.cpp)

# Build as shared library loaded by Java/Kotlin at runtime
add_library(
    stockfish
    SHARED
    ${STOCKFISH_SOURCES}
    ${BRIDGE_SOURCES}
)

target_compile_options(stockfish PRIVATE ${STOCKFISH_FLAGS})

# Link Android log library for debugging
find_library(log-lib log)
target_link_libraries(stockfish ${log-lib})
