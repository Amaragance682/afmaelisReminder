package com.afmaelis.reminder.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BirthdayCalculatorTest {

    private data class B(
        override val name: String,
        override val month: Int,
        override val day: Int,
        override val year: Int? = null,
    ) : BirthdayInfo

    private fun date(y: Int, m: Int, d: Int) = LocalDate.of(y, m, d)

    // Leap-day handling

    @Test
    fun leapDayFallsOn28FebInNonLeapYear() {
        assertEquals(date(2025, 2, 28), BirthdayCalculator.occurrenceIn(2025, 2, 29))
    }

    @Test
    fun leapDayStaysOn29FebInLeapYear() {
        assertEquals(date(2024, 2, 29), BirthdayCalculator.occurrenceIn(2024, 2, 29))
    }

    @Test
    fun leapDayBirthdayIsTodayOn28FebInNonLeapYear() {
        assertTrue(BirthdayCalculator.isToday(2, 29, date(2025, 2, 28)))
    }

    @Test
    fun leapDayBirthdayIsNotTodayOn28FebInLeapYear() {
        val today = date(2024, 2, 28)
        assertFalse(BirthdayCalculator.isToday(2, 29, today))
        assertEquals(1, BirthdayCalculator.daysUntil(2, 29, today))
    }

    @Test
    fun leapDayNextOccurrenceAfterPassingInNonLeapYearMovesToLeapYear() {
        assertEquals(date(2028, 2, 29), BirthdayCalculator.nextOccurrence(2, 29, date(2027, 3, 1)))
    }

    @Test
    fun leapDayNextOccurrenceFromLeapYearMarchIsNext28Feb() {
        assertEquals(date(2025, 2, 28), BirthdayCalculator.nextOccurrence(2, 29, date(2024, 3, 1)))
    }

    @Test
    fun birthdaysOnFindsLeapDayBirthdayOn28FebInNonLeapYear() {
        val leap = B("Leap", 2, 29)
        val regular = B("Regular", 2, 28)
        assertEquals(listOf(leap, regular), BirthdayCalculator.birthdaysOn(listOf(regular, leap), date(2025, 2, 28)))
        assertEquals(listOf(regular), BirthdayCalculator.birthdaysOn(listOf(regular, leap), date(2024, 2, 28)))
        assertEquals(listOf(leap), BirthdayCalculator.birthdaysOn(listOf(regular, leap), date(2024, 2, 29)))
    }

    // Year rollover

    @Test
    fun nextOccurrenceRollsOverToNextYear() {
        assertEquals(date(2026, 1, 1), BirthdayCalculator.nextOccurrence(1, 1, date(2025, 12, 31)))
    }

    @Test
    fun daysUntilAcrossNewYearIsOne() {
        assertEquals(1, BirthdayCalculator.daysUntil(1, 1, date(2025, 12, 31)))
    }

    @Test
    fun passedBirthdayIsAlmostAYearAway() {
        assertEquals(364, BirthdayCalculator.daysUntil(12, 30, date(2025, 12, 31)))
    }

    @Test
    fun birthdaysOnTomorrowAcrossNewYear() {
        val newYear = B("Nýár", 1, 1)
        val tomorrow = date(2025, 12, 31).plusDays(1)
        assertEquals(listOf(newYear), BirthdayCalculator.birthdaysOn(listOf(newYear, B("Other", 12, 31)), tomorrow))
    }

    // Today / tomorrow detection

    @Test
    fun birthdayTodayHasZeroDaysUntil() {
        val today = date(2025, 3, 14)
        assertTrue(BirthdayCalculator.isToday(3, 14, today))
        assertEquals(0, BirthdayCalculator.daysUntil(3, 14, today))
        assertEquals(today, BirthdayCalculator.nextOccurrence(3, 14, today))
    }

    @Test
    fun birthdayTomorrowHasOneDayUntil() {
        val today = date(2025, 3, 13)
        assertFalse(BirthdayCalculator.isToday(3, 14, today))
        assertEquals(1, BirthdayCalculator.daysUntil(3, 14, today))
    }

    @Test
    fun birthdayYesterdayIsNotToday() {
        assertFalse(BirthdayCalculator.isToday(3, 14, date(2025, 3, 15)))
    }

    @Test
    fun birthdaysOnReturnsOnlyMatchingDate() {
        val anna = B("Anna", 3, 14)
        val jon = B("Jón", 3, 15)
        val today = date(2025, 3, 14)
        assertEquals(listOf(anna), BirthdayCalculator.birthdaysOn(listOf(anna, jon), today))
        assertEquals(listOf(jon), BirthdayCalculator.birthdaysOn(listOf(anna, jon), today.plusDays(1)))
        assertTrue(BirthdayCalculator.birthdaysOn(listOf(anna, jon), today.plusDays(2)).isEmpty())
    }

    // Age calculation

    @Test
    fun ageTurningWhenBirthdayIsLaterThisYear() {
        assertEquals(30, BirthdayCalculator.ageTurning(B("Anna", 6, 1, 1995), date(2025, 3, 1)))
    }

    @Test
    fun ageTurningToday() {
        assertEquals(30, BirthdayCalculator.ageTurning(B("Anna", 3, 1, 1995), date(2025, 3, 1)))
    }

    @Test
    fun ageTurningWhenBirthdayAlreadyPassedCountsNextYear() {
        assertEquals(31, BirthdayCalculator.ageTurning(B("Anna", 2, 1, 1995), date(2025, 3, 1)))
    }

    @Test
    fun ageTurningAcrossNewYear() {
        assertEquals(26, BirthdayCalculator.ageTurning(B("Jón", 1, 1, 2000), date(2025, 12, 31)))
    }

    @Test
    fun ageTurningForLeapDayBirthdayInNonLeapYear() {
        assertEquals(25, BirthdayCalculator.ageTurning(B("Leap", 2, 29, 2000), date(2025, 2, 1)))
    }

    @Test
    fun ageTurningIsNullWithoutYear() {
        assertNull(BirthdayCalculator.ageTurning(B("Anna", 6, 1), date(2025, 3, 1)))
    }

    // Sorting

    @Test
    fun upcomingSortsByDaysUntilThenNameIgnoringCase() {
        val today = date(2025, 12, 30)
        val list = listOf(
            B("zoe", 1, 1),
            B("Bob", 12, 30),
            B("anna", 12, 31),
            B("Carl", 12, 29),
            B("Ari", 1, 1),
            B("adam", 12, 30),
        )
        val result = BirthdayCalculator.upcoming(list, today)
        assertEquals(listOf("adam", "Bob", "anna", "Ari", "zoe", "Carl"), result.map { it.birthday.name })
        assertEquals(listOf(0, 0, 1, 2, 2, 364), result.map { it.daysUntil })
    }

    @Test
    fun upcomingFillsOccurrenceAgeAndTodayFlag() {
        val today = date(2025, 3, 14)
        val result = BirthdayCalculator.upcoming(listOf(B("Anna", 3, 14, 1995), B("Jón", 3, 1)), today)
        val anna = result[0]
        assertEquals(today, anna.nextOccurrence)
        assertEquals(30, anna.ageTurning)
        assertTrue(anna.isToday)
        val jon = result[1]
        assertEquals(date(2026, 3, 1), jon.nextOccurrence)
        assertNull(jon.ageTurning)
        assertFalse(jon.isToday)
    }

    @Test
    fun upcomingOfEmptyListIsEmpty() {
        assertTrue(BirthdayCalculator.upcoming(emptyList<B>(), date(2025, 1, 1)).isEmpty())
    }
}
