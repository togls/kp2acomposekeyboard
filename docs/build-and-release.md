# Build and Release

## Local Debug Build

Build a debug APK:

```bash
./gradlew :app:assembleDebug
```

Run unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Run lint:

```bash
./gradlew :app:lintDebug
```

Install debug build:

```bash
./gradlew :app:installDebug
```

The debug build works without release signing inputs. If release signing inputs are present, the debug build currently reuses the release signing config so locally installed debug and release APKs can share the same signing identity.

## Android and Gradle Configuration

The app module reads SDK versions from `gradle.properties`:

```properties
android.compileSdk=...
android.targetSdk=...
android.minSdk=...
```

The app uses Java 17 and Kotlin JVM toolchain 17.

`isMinifyEnabled` is currently `false` for release builds. Release APKs are signed, but not minified or obfuscated.

## Local Release Signing

Release signing can be provided through `keystore.properties` at the project root:

```properties
storeFile=release-keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

The same values can be provided through environment variables:

```text
ANDROID_KEYSTORE_PATH
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

Environment variables are useful for CI. `keystore.properties` is useful for local builds.

Build a release APK:

```bash
./gradlew :app:assembleRelease
```

Signing files are ignored by Git:

```text
keystore.properties
*.jks
*.keystore
```

Do not commit keystores, passwords, aliases that reveal private naming, or generated signing property files.

## GitHub Actions Secrets

Nightly and release workflows expect these repository secrets:

```text
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

`ANDROID_KEYSTORE_BASE64` is a Base64-encoded keystore file. The workflows decode it into `release-keystore.jks`, then pass these runtime environment variables to Gradle:

```text
ANDROID_KEYSTORE_PATH=${{ github.workspace }}/release-keystore.jks
ANDROID_KEYSTORE_PASSWORD=${{ secrets.ANDROID_KEYSTORE_PASSWORD }}
ANDROID_KEY_ALIAS=${{ secrets.ANDROID_KEY_ALIAS }}
ANDROID_KEY_PASSWORD=${{ secrets.ANDROID_KEY_PASSWORD }}
```

Example Base64 encoding command on Linux or macOS:

```bash
base64 -w 0 release-keystore.jks > release-keystore.jks.base64
```

On systems where `base64 -w` is unavailable, use the platform's no-wrap option or remove line breaks before saving the secret.

## CI Workflow

`.github/workflows/ci.yml` runs on pushes and pull requests targeting `main`, plus manual dispatch.

It performs:

```bash
./gradlew :app:testDebugUnitTest --stacktrace
./gradlew :app:assembleDebug --stacktrace
./gradlew :app:lintDebug --stacktrace
```

It uploads lint reports when available.

## Nightly Workflow

`.github/workflows/nightly.yml` runs on a daily schedule and manual dispatch.

It performs:

1. Unit tests.
2. Signing secret validation.
3. Keystore decode.
4. Signed `:app:assembleRelease`.
5. APK copy to `nightly/kp2acomposekeyboard-nightly-{shortSha}.apk`.
6. Artifact upload with 14-day retention.
7. Unit test report upload.

The nightly artifact is a signed release APK. It is not a debug APK.

## Release Workflow

`.github/workflows/release.yml` runs when a tag matching this pattern is pushed:

```text
v*.*.*
```

It performs:

1. Signing secret validation.
2. Keystore decode.
3. Signed `:app:assembleRelease`.
4. APK copy to `release/kp2acomposekeyboard-{tag}.apk`.
5. GitHub Release creation through `softprops/action-gh-release`.

`GITHUB_TOKEN` is used only for release creation.

## Validation Checklist

Before publishing a release tag:

- [ ] `./gradlew :app:testDebugUnitTest` passes.
- [ ] `./gradlew :app:assembleDebug` passes.
- [ ] `./gradlew :app:lintDebug` passes.
- [ ] Release signing secrets are configured.
- [ ] `versionName` and `versionCode` in `app/build.gradle.kts` are correct.
- [ ] The tag matches `v*.*.*`.
- [ ] No keystore files or signing property files are staged.
