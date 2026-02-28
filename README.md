# MindGambit ♟

> **Chess Intelligence Platform** — a premium Android chess training app built with Kotlin + Jetpack Compose + Stockfish 16.

---

## Screenshots

| Home | Board | Tactics | Ladder |
|------|-------|---------|--------|
| Elo hero card, quick actions, Decision Protocol | Full chess board with player cards & clocks | Puzzle with motif badge & CCT hints | Rating ring, tier progress, breakdowns |

---

## Features

| Feature | Details |
|---------|---------|
| **Opening Repertoire** | London System, Jobava London, Pirc Defense — 21 interactive lessons |
| **Tactics Engine** | 20 starter puzzles (all motifs), spaced repetition scheduling |
| **Decision Protocol** | 5-step thinking: Scan → CCT → BlunderGuard → Eval → Execute |
| **Elo Ladder** | 4 separate ratings (Rapid/Blitz/Tactical/Strategic), 6 tiers |
| **Stockfish Analysis** | Full UCI integration via Android NDK, post-game review |
| **Game Review** | Accuracy %, move classification (Brilliant/Good/Inaccuracy/Mistake/Blunder) |
| **Onboarding** | 4-page setup wizard, skill-level selection, rating calibration |

---

## Tech Stack

```
Kotlin 2.0.21        — language
Jetpack Compose BOM  — UI (Material 3)
Hilt 2.52            — dependency injection
Room 2.6.1           — local database (SQLite)
Kotlin Coroutines    — async / reactive
Stockfish 16 (NDK)   — chess engine via JNI
Lottie               — animations
Firebase             — analytics + crashlytics
GitHub Actions       — CI/CD (signed APK on tag push)
```

---

## Project Structure

```
app/src/main/java/com/mindgambit/app/
├── core/
│   ├── di/          — Hilt modules
│   └── utils/       — EloCalculator, extensions
├── data/
│   ├── database/    — Room DB, DAOs, entities
│   ├── engine/      — StockfishEngine.kt (JNI bridge)
│   └── repository/  — Repository implementations
├── domain/
│   ├── model/       — ChessPosition, Move, Game, Puzzle, Opening, EloRating
│   ├── repository/  — Repository interfaces
│   └── usecase/     — Business logic use cases
└── presentation/
    ├── board/        — BoardScreen + ViewModel + ChessBoard composable
    ├── home/         — HomeScreen + ViewModel
    ├── tactics/      — TacticsScreen + ViewModel
    ├── openings/     — OpeningsScreen + Detail + Lesson + ViewModel
    ├── ladder/       — LadderScreen + ViewModel
    ├── review/       — ReviewScreen + ViewModel
    ├── onboarding/   — OnboardingScreen + ViewModel
    ├── components/   — MindGambitBottomBar
    ├── navigation/   — NavGraph, Screen
    └── theme/        — Color, Type, Shape, Theme
```

---

## Getting Started

### Prerequisites
- **Android Studio Hedgehog** or newer
- **JDK 17**
- **Android NDK r27c** (for Stockfish compilation)
- Git

### 1. Clone
```bash
git clone https://github.com/YOUR_USERNAME/mindgambit.git
cd mindgambit
```

### 2. Setup (fonts + Stockfish source)
```bash
bash scripts/setup.sh
```

### 3. Debug build
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### 4. Install on device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Release Build (CI/CD)

Releases are built automatically by GitHub Actions when you push a version tag.

### One-time keystore setup
```bash
# 1. Generate keystore (run once, keep safe)
keytool -genkey -v \
  -keystore keystore/mindgambit.jks \
  -alias mindgambit \
  -keyalg RSA -keysize 2048 \
  -validity 10000

# 2. Encode for GitHub Secrets
base64 -w 0 keystore/mindgambit.jks
```

### GitHub Secrets required
| Secret | Value |
|--------|-------|
| `KEYSTORE_BASE64` | Output of the base64 command above |
| `KEYSTORE_PASSWORD` | Password you set when generating the keystore |
| `KEY_ALIAS` | `mindgambit` |
| `KEY_PASSWORD` | Key password (can be same as store password) |

### Trigger a release
```bash
git tag v1.0.0
git push origin v1.0.0
# GitHub Actions builds, signs, and publishes APK to Releases tab
```

---

## Development Workflow

```
main branch   → production releases
develop branch → integration (triggers debug APK build)
feature/xxx   → feature branches, PR to develop
```

Every push to `main` or `develop` triggers a debug APK build.
Every `v*` tag push triggers a signed release build.

---

## Architecture

```
UI (Compose) → ViewModel → UseCase → Repository interface
                                          ↓
                                   Room Database
                                   StockfishEngine (JNI)
```

Clean Architecture: UI knows nothing about Room or Stockfish directly.

---

## Adding More Puzzles

Edit `PuzzleRepositoryImpl.kt` — add entries to `buildStarterPuzzles()`.

Each puzzle needs:
- `fen` — position in FEN notation
- `solutionMoves` — list of UCI moves (e.g. `["e2e4", "d7d5"]`)
- `motif` — `PuzzleMotif` enum value
- `difficulty` — 1–5 stars
- `eloRating` — difficulty rating (400–2400)

---

## License

MIT License — see `LICENSE` file.

---

*Built with Kotlin + Jetpack Compose. Engine powered by [Stockfish](https://stockfishchess.org/).*
