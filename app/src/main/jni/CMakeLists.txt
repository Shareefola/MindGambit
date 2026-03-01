# ============================================================
# MindGambit — CMakeLists.txt
# Builds Stockfish 16 as a shared library via Android NDK
# ============================================================

cmake_minimum_required(VERSION 3.22.1)
project("stockfish")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Base flags applied to all sources (stockfish + bridge)
set(COMMON_FLAGS
    -DNDEBUG
    -O3
    -funroll-loops
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

# Apply common flags to the whole target
target_compile_options(stockfish PRIVATE ${COMMON_FLAGS})

# Apply -fno-exceptions/-fno-rtti only to Stockfish sources for performance;
# bridge.cpp legitimately uses exceptions and <iostream>/<chrono>.
if(STOCKFISH_SOURCES)
    set_source_files_properties(${STOCKFISH_SOURCES} PROPERTIES
        COMPILE_FLAGS "-fno-exceptions -fno-rtti"
    )
endif()

# Link Android log library for debugging
find_library(log-lib log)
target_link_libraries(stockfish ${log-lib})
