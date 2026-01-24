# Release Process

## Versioning

This project follows [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes or major feature overhauls
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

Version format: `v{MAJOR}.{MINOR}.{PATCH}` (e.g., `v1.0.0`)

## Creating a Release

### 1. Update Version

Edit `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2  // Increment for each release
    versionName = "1.1.0"  // Update version string
}
```

### 2. Update Changelog

Add release notes to `CHANGELOG.md`:

```markdown
## [1.1.0] - 2024-XX-XX

### Added
- New feature description

### Changed
- Changed behavior description

### Fixed
- Bug fix description
```

### 3. Commit Changes

```bash
git add -A
git commit -m "Prepare release v1.1.0"
git push origin main
```

### 4. Create Tag

```bash
git tag -a v1.1.0 -m "Release v1.1.0"
git push origin v1.1.0
```

### 5. CI Builds Release

The GitHub Actions `release.yml` workflow will:

1. Build the release APK
2. Create a GitHub Release
3. Attach the unsigned APK

## Artifacts

### Debug Builds

- Built on every push to `main` or `develop`
- Available as GitHub Actions artifacts for 14 days
- Location: `app/build/outputs/apk/debug/app-debug.apk`

### Release Builds

- Built on version tags (`v*`)
- Attached to GitHub Releases
- Location: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Signing

### For Development

Debug builds are signed with the debug keystore automatically.

### For Distribution

1. Generate a release keystore:
   ```bash
   keytool -genkey -v -keystore meditation-mixer-release.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias meditation-mixer
   ```

2. Create `keystore.properties` (do not commit):
   ```properties
   storeFile=../meditation-mixer-release.jks
   storePassword=your_store_password
   keyAlias=meditation-mixer
   keyPassword=your_key_password
   ```

3. Update `app/build.gradle.kts` to read signing config from properties.

### For F-Droid

F-Droid builds from source and signs with their own keys. No action needed.

## F-Droid Submission

### Requirements

- [ ] App builds from source with `./gradlew assembleRelease`
- [ ] No proprietary dependencies
- [ ] No tracking/analytics
- [ ] Metadata in `fastlane/metadata/android/`
- [ ] Screenshots in appropriate folders

### Metadata Structure

```
fastlane/metadata/android/
├── en-US/
│   ├── full_description.txt
│   ├── short_description.txt
│   ├── title.txt
│   ├── changelogs/
│   │   └── 1.txt
│   └── images/
│       ├── icon.png
│       ├── featureGraphic.png
│       └── phoneScreenshots/
│           ├── 1.png
│           ├── 2.png
│           └── 3.png
```

### Submit to F-Droid

1. Fork [fdroiddata](https://gitlab.com/fdroid/fdroiddata)
2. Add app metadata file
3. Submit merge request

## Hotfix Process

For urgent fixes:

1. Create branch from latest tag: `git checkout -b hotfix/v1.0.1 v1.0.0`
2. Apply fix
3. Update version to patch level: `1.0.1`
4. Merge to main
5. Tag and release as above
