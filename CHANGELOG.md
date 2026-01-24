# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial release of Meditation Mixer
- Tone generator with frequency slider (1-40 Hz)
- Frequency presets: Delta (2Hz), Theta (4Hz, 6Hz), Alpha (10Hz), Beta (20Hz)
- Audio layer mixing with volume controls
- User audio import via Storage Access Framework
- Built-in ambience sounds (rain, ocean, forest, wind, river)
- Session timer with configurable duration (15/30/60/90 min + custom)
- Fade-out at end of session
- Preset save/load functionality
- Home screen with quick-start session button
- Mixer screen with layer controls
- Library screen for browsing ambience sounds
- Streak tracking for consecutive night sessions
- Achievement system (First Night, Weekender, Builder, Importer, Consistent)
- Settings screen with:
  - Daily reminder toggle
  - Fade duration configuration
  - Privacy policy
  - Disclaimer
- Neumorphic dark UI design
- Background playback with notification controls
- Audio focus handling
- Foreground service for reliable playback

### Technical
- Multi-module Gradle architecture
- Jetpack Compose UI
- Room database for presets and history
- DataStore for preferences
- Hilt dependency injection
- Media3/ExoPlayer for audio playback
- Custom AudioTrack tone generator
- GitHub Actions CI/CD
- F-Droid compatible build

## [1.0.0] - TBD

### Added
- First public release

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0   | TBD  | Initial release |
