#!/usr/bin/env bash
# ============================================================
# MindGambit — Font Downloader
#
# Downloads all required font files from Google Fonts into
# app/src/main/res/font/
#
# Run once before building:  bash scripts/download_fonts.sh
# ============================================================

set -e

FONT_DIR="app/src/main/res/font"
mkdir -p "$FONT_DIR"

echo "📥 Downloading MindGambit fonts..."

# ── Cormorant Garamond ────────────────────────────────────────
# Source: https://fonts.google.com/specimen/Cormorant+Garamond

BASE="https://github.com/CatharsisFonts/Cormorant/raw/master/fonts/ttf"

download_font() {
    local url="$1"
    local dest="$2"
    if [ -f "$FONT_DIR/$dest" ]; then
        echo "  ✓ $dest already exists, skipping"
    else
        echo "  ↓ Downloading $dest..."
        curl -fsSL "$url" -o "$FONT_DIR/$dest"
    fi
}

# Cormorant Garamond weights
download_font \
  "https://fonts.gstatic.com/s/cormorantgaramond/v16/co3YmX5slCNuHLi8bLeY9MK7whWMhyjYrEtA.ttf" \
  "cormorant_garamond_regular.ttf"

download_font \
  "https://fonts.gstatic.com/s/cormorantgaramond/v16/co3WmX5slCNuHLi8bLeY9MK7whWMhyjzSmlhTkM.ttf" \
  "cormorant_garamond_italic.ttf"

download_font \
  "https://fonts.gstatic.com/s/cormorantgaramond/v16/co3XmX5slCNuHLi8bLeY9MK7whWMhyjYrGNjlk.ttf" \
  "cormorant_garamond_semibold.ttf"

download_font \
  "https://fonts.gstatic.com/s/cormorantgaramond/v16/co3VmX5slCNuHLi8bLeY9MK7whWMhyjzSmlhTnBn.ttf" \
  "cormorant_garamond_semibold_italic.ttf"

download_font \
  "https://fonts.gstatic.com/s/cormorantgaramond/v16/co3XmX5slCNuHLi8bLeY9MK7whWMhyjYqmNjlk.ttf" \
  "cormorant_garamond_bold.ttf"

download_font \
  "https://fonts.gstatic.com/s/cormorantgaramond/v16/co3VmX5slCNuHLi8bLeY9MK7whWMhyjzSmlhTkBo.ttf" \
  "cormorant_garamond_bold_italic.ttf"

# ── DM Sans ───────────────────────────────────────────────────
# Source: https://fonts.google.com/specimen/DM+Sans

download_font \
  "https://fonts.gstatic.com/s/dmsans/v14/rP2Hp2ywxg089UriCZOIHQ.ttf" \
  "dm_sans_light.ttf"

download_font \
  "https://fonts.gstatic.com/s/dmsans/v14/rP2tp2ywxg089UriAqc5CQ.ttf" \
  "dm_sans_regular.ttf"

download_font \
  "https://fonts.gstatic.com/s/dmsans/v14/rP2Hp2ywxg089UriCZaJHQ.ttf" \
  "dm_sans_medium.ttf"

download_font \
  "https://fonts.gstatic.com/s/dmsans/v14/rP2Hp2ywxg089UriCZKIHQ.ttf" \
  "dm_sans_semibold.ttf"

download_font \
  "https://fonts.gstatic.com/s/dmsans/v14/rP2Hp2ywxg089UriCZaOHQ.ttf" \
  "dm_sans_bold.ttf"

# ── DM Mono ───────────────────────────────────────────────────
# Source: https://fonts.google.com/specimen/DM+Mono

download_font \
  "https://fonts.gstatic.com/s/dmmono/v14/aFTR7PB1QTsUX8KYvrumAIkx.ttf" \
  "dm_mono_regular.ttf"

download_font \
  "https://fonts.gstatic.com/s/dmmono/v14/aFTU7PB1QTsUX8KYth-orYataIk.ttf" \
  "dm_mono_medium.ttf"

echo ""
echo "✅ All fonts downloaded to $FONT_DIR"
echo ""
echo "Font files created:"
ls -la "$FONT_DIR"/*.ttf 2>/dev/null || echo "  (no .ttf files found — check download errors above)"
