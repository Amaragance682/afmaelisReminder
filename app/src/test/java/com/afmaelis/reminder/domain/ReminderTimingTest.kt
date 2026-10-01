package com.afmaelis.reminder.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderTimingTest {

    private val nine = LocalTime.of(9, 0)

    @Test
    fun nextTriggerIsTodayWhenTimeIsAhead() {
        val now = LocalDateTime.of(2025, 3, 14, 8, 59)
        assertEquals(LocalDateTime.of(2025, 3, 14, 9, 0), ReminderTiming.nextTrigger(now, nine))
    }

    @Test
    fun nextTriggerIsTomorrowWhenTimeHasPassed() {
        val now = LocalDateTime.of(2025, 3, 14, 9, 0)
        assertEquals(LocalDateTime.of(2025, 3, 15, 9, 0), ReminderTiming.nextTrigger(now, nine))
    }

    @Test
    fun nextTriggerRollsOverYear() {
        val now = LocalDateTime.of(2025, 12, 31, 22, 0)
        assertEquals(LocalDateTime.of(2026, 1, 1, 9, 0), ReminderTiming.nextTrigger(now, nine))
    }

    @Test
    fun catchUpWhenTimePassedAndNotNotified() {
        val now = LocalDateTime.of(2025, 3, 14, 10, 0)
        assertTrue(ReminderTiming.shouldCatchUp(now, nine, LocalDate.of(2025, 3, 13)))
        assertTrue(ReminderTiming.shouldCatchUp(now, nine, null))
    }

    @Test
    fun noCatchUpWhenAlreadyNotifiedToday() {
        val now = LocalDateTime.of(2025, 3, 14, 10, 0)
        assertFalse(ReminderTiming.shouldCatchUp(now, nine, LocalDate.of(2025, 3, 14)))
    }

    @Test
    fun noCatchUpBeforeConfiguredTime() {
        val now = LocalDateTime.of(2025, 3, 14, 8, 0)
        assertFalse(ReminderTiming.shouldCatchUp(now, nine, null))
    }
}
