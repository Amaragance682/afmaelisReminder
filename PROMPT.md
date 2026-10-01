# Build prompt

The prompt below was used to generate this app from `SPEC.md`. It can be
re-used with any coding agent to regenerate or extend the project.

---

You are a senior Android engineer. Build the Android app described in
`SPEC.md` at the repository root. Read the whole spec first; it is the source
of truth. Where the spec is silent, choose the simplest idiomatic option.

## Deliverable

A complete, buildable Android Studio project at the repository root:

- `settings.gradle.kts`, root `build.gradle.kts`, `gradle.properties`,
  `gradle/libs.versions.toml`, the Gradle wrapper (`gradlew`, `gradlew.bat`,
  `gradle/wrapper/gradle-wrapper.properties` + `gradle-wrapper.jar`).
- A single `:app` module with the package layout from SPEC §7.
- Unit tests for everything in `domain/`.
- `.github/workflows/android.yml` running `./gradlew testDebugUnitTest
  assembleDebug` on JDK 17 and uploading the debug APK as an artifact.
- `.gitignore` suitable for Android Studio.
- Rewrite `README.md`: what the app does, screenshots-free feature list, and
  the "Install on phone" steps from SPEC §9.

## Versions (use these exact, mutually-compatible versions)

- Android Gradle Plugin 8.7.3, Gradle wrapper 8.11.1
- Kotlin 2.0.21 with the `org.jetbrains.kotlin.plugin.compose` plugin
- KSP 2.0.21-1.0.28, Room 2.6.1 (`room-runtime`, `room-ktx`, `room-compiler`)
- Compose BOM 2024.12.01, `activity-compose` 1.9.3,
  `navigation-compose` 2.8.5, `lifecycle-runtime-compose` /
  `lifecycle-viewmodel-compose` 2.8.7, `core-ktx` 1.15.0
- `material-icons-extended` from the BOM
- JUnit 4.13.2
- compileSdk/targetSdk 35, minSdk 26, Java/Kotlin JVM target 17

## Engineering rules

1. `domain/` must be pure Kotlin (only `java.time` / stdlib). Every function
   that depends on the current date takes `today: LocalDate` as a parameter.
2. Implement exactly the date rules in SPEC §4, including 29 Feb → 28 Feb in
   non-leap years and year rollover.
3. Write the domain tests first, then the code; cover every case listed in
   SPEC §8.
4. ViewModels expose `StateFlow` UI state; screens are stateless composables
   taking state + callbacks. Use `viewModelScope` and Room `Flow`s.
5. No DI framework: `AfmaelisApp` holds `database`, `repository`, `settings`
   as lazy properties; ViewModels get them through a
   `ViewModelProvider.Factory` built with `viewModelFactory { initializer { … } }`.
6. Notifications & scheduling exactly as SPEC §6: inexact
   `setAndAllowWhileIdle`, reschedule on boot/time changes/app start/settings
   change, `last_notified_date` de-duplication, catch-up on app start,
   `goAsync()` in receivers. `PendingIntent`s use `FLAG_IMMUTABLE`.
7. Manifest: `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`; receivers
   `exported="false"` except the boot/time receiver which must be
   `exported="true"` to receive system broadcasts. No `INTERNET` permission.
8. All user-visible text in `res/values/strings.xml` (English). Use plurals
   where counts appear.
9. Provide an adaptive launcher icon built from vector drawables (a cake or
   gift glyph) — no binary PNGs.
10. Keep it small and readable: no speculative abstractions, no TODOs, no
    commented-out code. Comments only where the reason is non-obvious.

## Verification

- Run the domain unit tests and make them pass. If the Android SDK is not
  available in your environment, verify the `domain/` code and tests in a
  throwaway pure-Kotlin/JVM Gradle project, then copy them back unchanged.
- Re-read every Android source file against the versions above for API
  mismatches (imports, Material 3 API names, Room annotations, manifest
  entries) since it may not be compilable locally.
- Finish with a short report: files created, how tests were verified, and any
  assumptions made.
