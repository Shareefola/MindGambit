#!/usr/bin/env bash
# ============================================================
# MindGambit — One-Command Setup
#
# Runs all setup steps in order:
#   1. Download fonts
#   2. Download Stockfish source
#   3. Verify Gradle wrapper
#   4. Print next steps
#
# Usage:  bash scripts/setup.sh
# ============================================================

set -e
cd "$(dirname "$0")/.."   # ensure we're in project root

echo ""
echo "╔══════════════════════════════════════╗"
echo "║      MindGambit — Project Setup      ║"
echo "╚══════════════════════════════════════╝"
echo ""

# Step 1: Fonts
echo "Step 1/3: Downloading fonts..."
bash scripts/download_fonts.sh
echo ""

# Step 2: Stockfish
echo "Step 2/3: Downloading Stockfish source..."
bash scripts/download_stockfish.sh
echo ""

# Step 3: Gradle wrapper
echo "Step 3/3: Verifying Gradle wrapper..."
if [ -f "gradlew" ]; then
    chmod +x gradlew
    echo "  ✓ gradlew is executable"
else
    echo "  ⚠ gradlew not found — you may need to run 'gradle wrapper' first"
fi

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║  Setup complete! Next steps:                     ║"
echo "║                                                  ║"
echo "║  Debug build (no signing needed):                ║"
echo "║    ./gradlew assembleDebug                       ║"
echo "║                                                  ║"
echo "║  Release build (requires keystore secrets):      ║"
echo "║    See MindGambit_Deployment_Guide.md            ║"
echo "║                                                  ║"
echo "║  Or just push to GitHub and let CI do it! 🚀    ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""
