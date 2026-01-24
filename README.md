# Meditation Mixer

A privacy-first Android meditation and sleep audio app with tone generation, audio mixing, and gentle gamification.

## Features

- **Tone Generator**: Generate sine wave frequencies (1-40 Hz) for delta, theta, and alpha states
- **Audio Mixer**: Layer tones with your own audio files and ambient sounds
- **Timer with Fade-out**: Configurable session timers with smooth fade-out
- **Presets**: Save and load your favorite mix configurations
- **Streak Tracking**: Local-only gamification to encourage consistent practice
- **Privacy-First**: No analytics, no tracking, no accounts, fully offline

## Building

### Prerequisites

- JDK 17+
- Linux/macOS/Windows with Bash

### Build Debug APK

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK

```bash
./gradlew assembleRelease
```

### Run Tests

```bash
./gradlew testDebugUnitTest
```

### Run Lint

```bash
./gradlew lint
```

## Architecture

Multi-module Gradle project following Clean Architecture:

```
:app                    # Main application, DI, navigation, UI screens
:core:common           # Shared utilities, constants, dispatcher providers
:core:domain           # Business logic, entities, use cases, repository interfaces
:core:data             # Room database, DataStore, repository implementations
:core:audio            # Audio engine, tone generator, ExoPlayer integration
:core:ui               # Neumorphic theme, shared Compose components
```

### Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with custom neumorphic design
- **DI**: Hilt
- **Database**: Room
- **Preferences**: DataStore
- **Audio**: Media3/ExoPlayer + AudioTrack for tone generation
- **Build**: Gradle with Kotlin DSL

## CI/CD

GitHub Actions workflows:

- **PR Check**: Runs lint and tests on pull requests
- **Build**: Builds debug APK on push to main/develop
- **Release**: Creates GitHub release with APK on version tags

### Creating a Release

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

## F-Droid Compatibility

This app is designed for F-Droid distribution:

- No proprietary dependencies
- No analytics or tracking SDKs
- No Google Play Services required
- Builds from source with standard Gradle commands
- Minimal permissions (notifications, wake lock)

## Project Structure

```
app/
├── src/main/
│   ├── kotlin/com/mediationmixer/app/
│   │   ├── MainActivity.kt
│   │   ├── MeditationMixerApp.kt
│   │   ├── navigation/
│   │   ├── service/
│   │   └── ui/
│   │       ├── home/
│   │       ├── mixer/
│   │       ├── library/
│   │       ├── streaks/
│   │       └── settings/
│   └── res/
core/
├── common/
├── domain/
├── data/
├── audio/
└── ui/
```

## License

[Add your license here]

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Disclaimer

This app is for relaxation purposes only and is not intended to diagnose, treat, cure, or prevent any medical condition. Please listen at a comfortable volume. Consult a healthcare professional for sleep disorders or health concerns.
