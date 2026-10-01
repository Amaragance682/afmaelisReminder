# Afmælis Reminder — Specification

A small, offline Android app that stores friends' birthdays and posts a
notification on the day (and optionally the day before) so the user never
misses one.

## 1. Goals

- Add, edit and delete birthdays quickly.
- See at a glance who has a birthday today and what is coming up.
- Get a reliable daily notification listing everyone whose birthday is today.
- Build and install from Android Studio onto a personal phone with no backend,
  no account and no network access.

### Non-goals (v1)

- Cloud sync, accounts, sharing.
- Contacts import / Google Calendar integration (possible later).
- Sending messages/greetings from the app.
- Home screen widgets.

## 2. Platform & tech stack

| Concern        | Choice                                                         |
|----------------|----------------------------------------------------------------|
| Language       | Kotlin                                                         |
| UI             | Jetpack Compose + Material 3 (dynamic color on Android 12+)    |
| Persistence    | Room (SQLite), schema exported                                 |
| Settings       | SharedPreferences (tiny key/value set)                         |
| Scheduling     | `AlarmManager.setAndAllowWhileIdle` (inexact, no special perm) |
| Build          | Gradle Kotlin DSL + version catalog, Gradle wrapper checked in |
| minSdk/target  | minSdk 26 (native `java.time`), target/compile SDK 35          |
| DI             | None — a tiny manual service locator in the `Application`      |
| Package        | `is.afmaelis.reminder` → use `com.afmaelis.reminder` (`is` is a Kotlin keyword) |

## 3. Data model

`Birthday` (Room entity, table `birthdays`):

| Field   | Type      | Notes                                          |
|---------|-----------|------------------------------------------------|
| `id`    | `Long`    | Auto-generated primary key                     |
| `name`  | `String`  | Required, trimmed, non-blank                   |
| `month` | `Int`     | 1–12                                           |
| `day`   | `Int`     | 1–31, must be valid for the month (29 Feb ok)  |
| `year`  | `Int?`    | Optional birth year; enables "turns N"         |
| `note`  | `String?` | Optional free text (gift ideas, etc.)          |

Validation: `day` must be ≤ the length of `month` in a leap year; if `year` is
given it must be between 1900 and the current year and the full date must not
be in the future and must exist (29 Feb only in leap years).

## 4. Date rules (pure Kotlin, unit tested)

All in `BirthdayCalculator`, taking a `today: LocalDate` parameter (never
calling `now()` internally) so it is deterministic and testable.

- **Occurrence in a year:** the birthday's month/day in that year. A
  **29 February** birthday falls on **28 February** in non-leap years.
- **Next occurrence:** this year's occurrence if `>= today`, else next year's.
- **Days until:** days from `today` to next occurrence (0 = today).
- **Is today:** next occurrence equals `today`.
- **Age turning:** if `year` is known, `nextOccurrence.year - year`; else null.
- **Upcoming sort:** ascending by days-until, then by name (case-insensitive).
- **Birthdays on a date:** all birthdays whose occurrence in that date's year
  equals that date (used by the notifier for "today" and "tomorrow").

## 5. Screens

### 5.1 Birthday list (start screen)
- Top app bar: app name, settings action.
- **Today** section (only if non-empty): highlighted cards, cake icon,
  "Turns N today!" when the year is known.
- **Upcoming** section: everyone else sorted by next occurrence. Each row:
  name, date (e.g. "14 March"), "in N days" / "tomorrow", and "turns N" if known.
- Empty state with a hint to add the first birthday.
- FAB "+" opens the add screen. Tapping a row opens edit.

### 5.2 Add / edit birthday
- Fields: Name (text), Day + Month (dropdowns or a picker), optional Year,
  optional Note.
- Inline validation errors; Save disabled until valid.
- Edit mode shows a Delete action with a confirmation dialog.

### 5.3 Settings
- Daily notification time (time picker, default 09:00).
- Toggle: "Also remind me the day before" (default off).
- "Send test notification" button.
- Shows notification permission state with a button to grant/open settings
  when denied.

## 6. Notifications

- Channel `birthdays_today` ("Birthdays", high importance).
- Once per day at the configured time the app checks birthdays:
  - Today: one notification. Title "🎂 Birthday today" (or "🎂 N birthdays
    today"); text lists names, with ages if known, e.g. "Anna (30), Jón".
  - Tomorrow (if enabled): separate notification "Tomorrow: Anna, Jón".
  - Nothing posted when there are no matches.
- Tapping a notification opens the app list screen.
- Android 13+: request `POST_NOTIFICATIONS` at first launch.

### Scheduling & reliability
- `ReminderScheduler.scheduleNext()` sets one inexact alarm for the next
  occurrence of the configured time (today if still ahead, otherwise tomorrow)
  via `setAndAllowWhileIdle(RTC_WAKEUP, …)`. No exact-alarm permission needed;
  a few minutes of drift is acceptable.
- The alarm receiver posts notifications, then schedules the next alarm.
- Rescheduled on: app start, settings change, `BOOT_COMPLETED`,
  `MY_PACKAGE_REPLACED`, `TIME_SET`, `TIMEZONE_CHANGED`.
- De-duplication: store the last date notifications were posted
  (`last_notified_date`); never post twice for the same date.
- Catch-up: on app start, if the configured time has already passed today and
  today was not yet notified, run the check immediately (covers a phone that
  was off at alarm time).
- DB access in the receiver uses `goAsync()` + a coroutine.

## 7. Project layout

```
app/src/main/java/com/afmaelis/reminder/
  AfmaelisApp.kt              Application; creates DB, channel, schedules
  MainActivity.kt             Compose host + navigation + permission request
  data/Birthday.kt            Entity
  data/BirthdayDao.kt         DAO (Flow for list, suspend for CRUD)
  data/AppDatabase.kt         Room database
  data/BirthdayRepository.kt
  data/SettingsStore.kt       SharedPreferences wrapper
  domain/BirthdayCalculator.kt  Pure date logic (no Android imports)
  domain/BirthdayValidator.kt   Pure validation (no Android imports)
  notify/NotificationHelper.kt
  notify/ReminderScheduler.kt
  notify/ReminderReceiver.kt    Alarm → check & notify → reschedule
  notify/BootReceiver.kt
  ui/list/…  ui/edit/…  ui/settings/…  ui/theme/…
app/src/test/…                 JUnit tests for domain/
```

## 8. Quality bar

- `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` succeed.
- Unit tests cover: leap-day handling, year rollover (31 Dec → 1 Jan),
  today/tomorrow detection, age calculation, sorting, validation.
- No network permission. No analytics. All strings in `strings.xml`.
- A GitHub Actions workflow builds the debug APK and runs unit tests on push.

## 9. Install on phone

1. Open the project root in Android Studio (Ladybug or newer); let Gradle sync.
2. On the phone: Settings → About phone → tap *Build number* 7× → Developer
   options → enable *USB debugging*.
3. Connect via USB (or Wi-Fi pairing), choose the device, press **Run**.
4. Allow notifications when prompted. Optionally exclude the app from battery
   optimisation on aggressive OEM ROMs.
