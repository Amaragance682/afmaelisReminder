package com.afmaelis.reminder.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.afmaelis.reminder.AfmaelisApp
import com.afmaelis.reminder.domain.BirthdayCalculator
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as AfmaelisApp
        val pendingResult = goAsync()
        app.appScope.launch {
            try {
                checkAndNotify(app, LocalDate.now())
            } finally {
                app.scheduler.scheduleNext()
                pendingResult.finish()
            }
        }
    }

    companion object {
        // The alarm and the app-start catch-up can run at the same moment.
        private val mutex = Mutex()

        /** Posts today's (and optionally tomorrow's) birthdays at most once per date. */
        suspend fun checkAndNotify(app: AfmaelisApp, today: LocalDate): Unit = mutex.withLock {
            val settings = app.settings
            if (settings.lastNotifiedDate == today) return
            // Leave the date unmarked so the catch-up can still notify once permission is granted.
            if (!app.notifications.canPostNotifications()) return

            val birthdays = app.repository.getAll()
            val todays = BirthdayCalculator.birthdaysOn(birthdays, today)
            if (todays.isNotEmpty()) app.notifications.showToday(todays, today)

            if (settings.settings.remindDayBefore) {
                val tomorrow = today.plusDays(1)
                val tomorrows = BirthdayCalculator.birthdaysOn(birthdays, tomorrow)
                if (tomorrows.isNotEmpty()) app.notifications.showTomorrow(tomorrows, tomorrow)
            }
            settings.lastNotifiedDate = today
        }
    }
}
