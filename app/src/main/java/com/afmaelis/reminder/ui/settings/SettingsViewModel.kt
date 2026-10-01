package com.afmaelis.reminder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afmaelis.reminder.data.SettingsStore
import com.afmaelis.reminder.notify.NotificationHelper
import com.afmaelis.reminder.notify.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalTime

data class SettingsUiState(
    val notifyTime: LocalTime,
    val remindDayBefore: Boolean,
    val notificationsEnabled: Boolean,
)

class SettingsViewModel(
    private val settings: SettingsStore,
    private val scheduler: ReminderScheduler,
    private val notifications: NotificationHelper,
) : ViewModel() {

    private val notificationsEnabled = MutableStateFlow(notifications.canPostNotifications())

    val uiState: StateFlow<SettingsUiState> =
        combine(settings.settingsFlow, notificationsEnabled) { s, enabled ->
            SettingsUiState(s.notifyTime, s.remindDayBefore, enabled)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            settings.settings.let { SettingsUiState(it.notifyTime, it.remindDayBefore, notificationsEnabled.value) },
        )

    fun setNotifyTime(time: LocalTime) {
        settings.setNotifyTime(time)
        scheduler.scheduleNext()
    }

    fun setRemindDayBefore(enabled: Boolean) {
        settings.setRemindDayBefore(enabled)
        scheduler.scheduleNext()
    }

    fun sendTestNotification() = notifications.showTest()

    /** Called on resume, since permission can change in system settings while we are paused. */
    fun refreshPermission() {
        notificationsEnabled.value = notifications.canPostNotifications()
    }
}
