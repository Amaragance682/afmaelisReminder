package com.afmaelis.reminder.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate
import java.time.LocalTime

data class Settings(
    val notifyTime: LocalTime,
    val remindDayBefore: Boolean,
)

class SettingsStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val settings: Settings
        get() = Settings(
            notifyTime = LocalTime.of(
                prefs.getInt(KEY_HOUR, DEFAULT_HOUR),
                prefs.getInt(KEY_MINUTE, 0),
            ),
            remindDayBefore = prefs.getBoolean(KEY_DAY_BEFORE, false),
        )

    val settingsFlow: Flow<Settings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(settings) }
        trySend(settings)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun setNotifyTime(time: LocalTime) = prefs.edit {
        putInt(KEY_HOUR, time.hour)
        putInt(KEY_MINUTE, time.minute)
    }

    fun setRemindDayBefore(enabled: Boolean) = prefs.edit { putBoolean(KEY_DAY_BEFORE, enabled) }

    var lastNotifiedDate: LocalDate?
        get() = prefs.getString(KEY_LAST_NOTIFIED, null)?.let(LocalDate::parse)
        set(value) = prefs.edit { putString(KEY_LAST_NOTIFIED, value?.toString()) }

    private companion object {
        const val KEY_HOUR = "notify_hour"
        const val KEY_MINUTE = "notify_minute"
        const val KEY_DAY_BEFORE = "remind_day_before"
        const val KEY_LAST_NOTIFIED = "last_notified_date"
        const val DEFAULT_HOUR = 9
    }
}
