package com.afmaelis.reminder.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object ReminderTiming {

    /** Next time the daily check should run: today at [time] if still ahead, otherwise tomorrow. */
    fun nextTrigger(now: LocalDateTime, time: LocalTime): LocalDateTime {
        val todayAt = now.toLocalDate().atTime(time)
        return if (todayAt.isAfter(now)) todayAt else todayAt.plusDays(1)
    }

    /** True when today's notification time has passed but today has not been notified yet. */
    fun shouldCatchUp(now: LocalDateTime, time: LocalTime, lastNotified: LocalDate?): Boolean =
        !now.toLocalTime().isBefore(time) && lastNotified != now.toLocalDate()
}
