package com.afmaelis.reminder.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.afmaelis.reminder.MainActivity
import com.afmaelis.reminder.R
import com.afmaelis.reminder.data.Birthday
import java.time.LocalDate

class NotificationHelper(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = context.getString(R.string.channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canPostNotifications(): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permissionGranted && manager.areNotificationsEnabled()
    }

    fun showToday(birthdays: List<Birthday>, date: LocalDate) {
        val title = context.resources.getQuantityString(R.plurals.notification_today_title, birthdays.size, birthdays.size)
        post(ID_TODAY, title, formatNames(birthdays, date))
    }

    fun showTomorrow(birthdays: List<Birthday>, date: LocalDate) {
        val title = context.resources.getQuantityString(R.plurals.notification_tomorrow_title, birthdays.size, birthdays.size)
        post(ID_TOMORROW, title, context.getString(R.string.notification_tomorrow_text, formatNames(birthdays, date)))
    }

    fun showTest() {
        post(ID_TEST, context.getString(R.string.notification_test_title), context.getString(R.string.notification_test_text))
    }

    private fun formatNames(birthdays: List<Birthday>, date: LocalDate): String =
        birthdays.joinToString(context.getString(R.string.name_separator)) { b ->
            b.year?.let { context.getString(R.string.name_with_age, b.name, date.year - it) } ?: b.name
        }

    private fun post(id: Int, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

    private companion object {
        const val CHANNEL_ID = "birthdays_today"
        const val ID_TODAY = 1
        const val ID_TOMORROW = 2
        const val ID_TEST = 3
    }
}
