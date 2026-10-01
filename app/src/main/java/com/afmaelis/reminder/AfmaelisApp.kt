package com.afmaelis.reminder

import android.app.Application
import com.afmaelis.reminder.data.AppDatabase
import com.afmaelis.reminder.data.BirthdayRepository
import com.afmaelis.reminder.data.SettingsStore
import com.afmaelis.reminder.domain.ReminderTiming
import com.afmaelis.reminder.notify.NotificationHelper
import com.afmaelis.reminder.notify.ReminderReceiver
import com.afmaelis.reminder.notify.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AfmaelisApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.create(this) }
    val repository by lazy { BirthdayRepository(database.birthdayDao()) }
    val settings by lazy { SettingsStore(this) }
    val notifications by lazy { NotificationHelper(this) }
    val scheduler by lazy { ReminderScheduler(this, settings) }

    override fun onCreate() {
        super.onCreate()
        notifications.createChannel()
        scheduler.scheduleNext()
        appScope.launch { catchUp() }
    }

    /** Covers a missed alarm, e.g. the phone was off at the configured time. */
    suspend fun catchUp() {
        val now = LocalDateTime.now()
        if (ReminderTiming.shouldCatchUp(now, settings.settings.notifyTime, settings.lastNotifiedDate)) {
            ReminderReceiver.checkAndNotify(this, now.toLocalDate())
        }
    }
}
