package com.afmaelis.reminder.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.afmaelis.reminder.AfmaelisApp

/**
 * Re-arms the daily alarm after reboot, app update or clock/time zone changes.
 * Starting the process also runs the catch-up check in [AfmaelisApp.onCreate].
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in HANDLED_ACTIONS) {
            (context.applicationContext as AfmaelisApp).scheduler.scheduleNext()
        }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
