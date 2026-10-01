# Afmælis Reminder

A small, offline Android app that keeps track of your friends' birthdays and
reminds you on the day (and optionally the day before) with a notification.
No account, no backend, no network access — everything stays on the phone.

The app was built from [`SPEC.md`](SPEC.md) using the prompt in
[`PROMPT.md`](PROMPT.md).

## Features

- Add, edit and delete birthdays: name, day and month, optional birth year and
  an optional note (gift ideas and so on).
- Start screen shows who has a birthday **today** (highlighted, with
  "Turns N today!" when the year is known) and an **upcoming** list sorted by
  the next occurrence, with "tomorrow" / "in N days" and the age they turn.
- Daily notification at a time you choose (default 09:00) listing everyone
  whose birthday is today, e.g. "Anna (30), Jón".
- Optional "day before" reminder as a separate notification.
- 29 February birthdays are celebrated on 28 February in non-leap years.
- Reliable scheduling without special permissions: the alarm is re-armed after
  reboot, app updates and clock or time zone changes, never notifies twice for
  the same day, and catches up when the phone was off at the reminder time.
- Settings: notification time, day-before toggle, test notification and the
  current notification permission state.
- Material 3 with dynamic colour on Android 12+.

## Building

Requires Android Studio Ladybug (2024.2) or newer, or JDK 17 with the Android SDK.

```sh
./gradlew testDebugUnitTest   # unit tests for the date and validation rules
./gradlew assembleDebug       # app/build/outputs/apk/debug/app-debug.apk
```

GitHub Actions runs both on every push and uploads the debug APK as an
artifact.

## Install on phone

1. Open the project root in Android Studio (Ladybug or newer); let Gradle sync.
2. On the phone: Settings → About phone → tap *Build number* 7× → Developer
   options → enable *USB debugging*.
3. Connect via USB (or Wi-Fi pairing), choose the device, press **Run**.
4. Allow notifications when prompted. Optionally exclude the app from battery
   optimisation on aggressive OEM ROMs.
