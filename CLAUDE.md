# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Meditation Mixer is a privacy-first Android meditation/sleep audio app with tone generation, audio mixing, and gamification. Designed for F-Droid distribution (no proprietary dependencies, no analytics, fully offline).

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK (outputs to app/build/outputs/apk/debug/)
./gradlew assembleRelease        # Build release APK (minified + shrunk)
./gradlew testDebugUnitTest      # Run all unit tests
./gradlew :core:audio:testDebugUnitTest  # Run tests for a single module
./gradlew lint                   # Run Android lint
```

Requires JDK 17+. Uses Gradle configuration cache (`org.gradle.configuration-cache=true`).

## Architecture

Multi-module Clean Architecture with 6 Gradle modules:

```
:app              → DI wiring, navigation, UI screens, foreground service
:core:common      → DispatcherProvider, Constants, Result wrapper, TimeUtils
:core:domain      → Models, repository interfaces, use cases (pure Kotlin, no Android deps except Hilt)
:core:data        → Room DB, DataStore prefs, repository implementations
:core:audio       → AudioEngine, ToneGenerator, NoiseGenerator, ExoPlayer integration
:core:ui          → Neumorphic theme (MeditationColors, MeditationTypography), shared Compose components
```

**Dependency flow**: `app` → all core modules; `core:audio` → `common`, `domain`, `data`; `core:data` → `common`, `domain`; `core:domain` → `common`; `core:ui` → standalone (Compose only).

### Package name quirk

The app module uses namespace `com.mediationmixer.app` (typo: "mediation" not "meditation"). All core modules use `com.meditationmixer.core.*`. The applicationId is `com.meditationmixer.app`. Be aware of this inconsistency when adding imports across module boundaries.

### Key patterns

- **DI**: Hilt with `@InstallIn(SingletonComponent::class)`. Three Hilt modules: `AppModule` (app), `DataModule` (core:data, split into `DatabaseModule` + `RepositoryModule`), `AudioModule` (core:audio). Repository interfaces in domain, implementations bound via `@Binds`.
- **ViewModels**: `@HiltViewModel` with constructor injection. Each screen has its own ViewModel.
- **Navigation**: Jetpack Navigation Compose with `sealed class Screen` routes in `MeditationMixerNavHost.kt`. Bottom bar hidden on Mixer and Presets screens.
- **Audio engine**: `AudioEngine` interface with `AudioEngineImpl`. `ToneGenerator` uses `AudioTrack` with raw PCM sine wave generation (binaural beats via amplitude modulation at 1-40 Hz). `NoiseGenerator` for ambient noise. ExoPlayer handles user audio files.
- **Database**: Room v1 with entities `PresetEntity`, `SessionHistoryEntity`, `StreakEntity`, `AchievementEntity`. Type converters for `LayerConfig` lists. `DatabaseCallback` seeds default presets.
- **Settings**: DataStore Preferences via `SettingsDataStore`/`SettingsRepositoryImpl`.
- **Coroutines**: `DispatcherProvider` interface for testable dispatchers (injected via AppModule).
- **Theme**: Dark-only neumorphic design. Custom `MeditationColors` object with gradient brushes. Shared components: `NeumorphicButton`, `NeumorphicCard`, `NeumorphicSlider`.

### Domain model

Core types in `core:domain:model`:
- `Preset` (id, name, layers: `List<LayerConfig>`, timer/fade config, favorite flag)
- `LayerConfig` (type: `TONE`/`USER_AUDIO`/`AMBIENCE`, volume, frequency, sourceUri, loop)
- `Session` (state: `IDLE`/`PLAYING`/`PAUSED`/`FADING_OUT`/`COMPLETED`, progress, timing)
- `StreakData`, `Settings`

### Screens

Home → Presets → Mixer (main flow), Library, Streaks, Settings (bottom bar tabs).

## CI

GitHub Actions workflows on PRs to `main`/`develop`: lint → test → build debug APK.

## Version Catalog

Dependencies managed in `gradle/libs.versions.toml`. Use `libs.*` references in build files. Key versions: Kotlin 1.9.22, Compose BOM 2024.02.00, Hilt 2.50, Room 2.6.1, Media3 1.2.1.

## CRITICAL: No Android Studio / SDK Installation

**NEVER install Android Studio or Android SDK on this device.** We have Gradle. Use GitHub CI to build.

## Workflow Orchestration

### 1. Plan Mode Default
- Enter plan mode for ANY non-trivial task (3+ steps or architectural decisions)
- If something goes sideways, STOP and re-plan immediately — don't keep pushing
- Use plan mode for verification steps, not just building
- Write detailed specs upfront to reduce ambiguity

### 2. Subagent Strategy
- Use subagents liberally to keep main context window clean
- Offload research, exploration, and parallel analysis to subagents
- For complex problems, throw more compute at it via subagents
- One task per subagent for focused execution

### 3. Self-Improvement Loop
- After ANY correction from the user, update `tasks/lessons.md` with the pattern
- Write rules for yourself that prevent the same mistake
- Ruthlessly iterate on these lessons until mistake rate drops
- Review lessons at session start for relevant project

### 4. Verification Before Done
- Never mark a task complete without proving it works
- Diff your behavior between main and your changes when relevant
- Ask yourself: "Would a staff engineer approve this?"
- Run tests, check logs, demonstrate correctness

### 5. Demand Elegance (Balanced)
- For non-trivial changes: pause and ask "is there a more elegant way?"
- If a fix feels hacky: "Knowing everything I know now, implement the elegant solution"
- Skip this for simple, obvious fixes — don't over-engineer
- Challenge your own work before presenting it

### 6. Autonomous Bug Fixing
- When given a bug report: just fix it. Don't ask for hand-holding
- Point at logs, errors, failing tests — then resolve them
- Zero context switching required from the user
- Go fix failing CI tests without being told how

## Task Management

1. **Plan First**: Write plan to `tasks/todo.md` with checkable items
2. **Verify Plan**: Check in before starting implementation
3. **Track Progress**: Mark items complete as you go
4. **Explain Changes**: High-level summary at each step
5. **Document Results**: Add review section to `tasks/todo.md`
6. **Capture Lessons**: Update `tasks/lessons.md` after corrections

## Core Principles

- **Simplicity First**: Make every change as simple as possible. Impact minimal code.
- **No Laziness**: Find root causes. No temporary fixes. Senior developer standards.
- **Minimal Impact**: Changes should only touch what's necessary. Avoid introducing bugs.

## IMPORTANT

Before completing any task, run these checks:
- Scan for hardcoded secrets, API keys, passwords
- Check for SQL injection, shell injection, path traversal
- Verify all user inputs are validated
- Run the test suite
- Check for type errors
