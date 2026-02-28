#!/usr/bin/env bash
# ============================================================
# MindGambit — Stockfish Source Downloader
#
# Downloads Stockfish 16 source into app/src/main/jni/stockfish/
# so the NDK CMake build can compile it.
#
# Run once before first build:  bash scripts/download_stockfish.sh
# ============================================================

set -e

JNI_DIR="app/src/main/jni"
SF_DIR="$JNI_DIR/stockfish"
SF_VERSION="sf_16.1"
SF_URL="https://github.com/official-stockfish/Stockfish/archive/refs/tags/${SF_VERSION}.tar.gz"

echo "📥 Downloading Stockfish ${SF_VERSION}..."

if [ -d "$SF_DIR" ] && [ "$(ls -A $SF_DIR)" ]; then
    echo "  ✓ Stockfish source already present at $SF_DIR"
    echo "  Delete the directory and re-run to force re-download."
    exit 0
fi

mkdir -p "$SF_DIR"

# Download & extract
TMP=$(mktemp -d)
curl -fsSL "$SF_URL" -o "$TMP/stockfish.tar.gz"
echo "  📦 Extracting..."
tar -xzf "$TMP/stockfish.tar.gz" -C "$TMP"

# Copy only the src directory (C++ source files we need)
SF_SRC=$(find "$TMP" -name "*.cpp" -path "*/src/*" -exec dirname {} \; | head -1)
if [ -z "$SF_SRC" ]; then
    echo "❌ Could not find Stockfish source directory in archive"
    exit 1
fi

cp -r "$SF_SRC"/. "$SF_DIR/"
rm -rf "$TMP"

echo ""
echo "✅ Stockfish source downloaded to $SF_DIR"
echo ""
echo "Source files:"
ls "$SF_DIR"/*.cpp 2>/dev/null | head -20

echo ""
echo "The CMakeLists.txt at $JNI_DIR/CMakeLists.txt will compile these"
echo "automatically when you run ./gradlew assembleDebug"
