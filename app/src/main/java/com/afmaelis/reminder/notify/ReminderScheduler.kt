package com.afmaelis.reminder.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.afmaelis.reminder.data.SettingsStore
import com.afmaelis.reminder.domain.ReminderTiming
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderScheduler(private val context: Context, private val settings: SettingsStore) {

    /** Replaces any pending alarm with one at the next occurrence of the configured time. */
    fun scheduleNext() {
        val trigger = ReminderTiming.nextTrigger(LocalDateTime.now(), settings.settings.notifyTime)
        val triggerMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        context.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
    }
}
