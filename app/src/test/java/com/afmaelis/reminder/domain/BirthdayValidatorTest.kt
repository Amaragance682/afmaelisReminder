package com.afmaelis.reminder.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BirthdayValidatorTest {

    private val today = LocalDate.of(2025, 6, 15)

    private fun validate(name: String = "Anna", month: Int = 3, day: Int = 14, year: String = "") =
        BirthdayValidator.validate(name, month, day, year, today)

    @Test
    fun validWithoutYear() {
        assertTrue(validate().isValid)
    }

    @Test
    fun validWithYear() {
        assertTrue(validate(year = "1995").isValid)
    }

    @Test
    fun yearIsTrimmed() {
        assertTrue(validate(year = " 1995 ").isValid)
    }

    @Test
    fun blankNameIsInvalid() {
        assertEquals(NameError.BLANK, validate(name = "   ").nameError)
        assertEquals(NameError.BLANK, validate(name = "").nameError)
    }

    @Test
    fun monthOutOfRangeIsInvalid() {
        assertEquals(DateError.INVALID_MONTH, validate(month = 0).dateError)
        assertEquals(DateError.INVALID_MONTH, validate(month = 13).dateError)
    }

    @Test
    fun dayOutOfRangeIsInvalid() {
        assertEquals(DateError.INVALID_DAY, validate(day = 0).dateError)
        assertEquals(DateError.INVALID_DAY, validate(day = 32).dateError)
        assertEquals(DateError.INVALID_DAY, validate(month = 4, day = 31).dateError)
        assertEquals(DateError.INVALID_DAY, validate(month = 2, day = 30).dateError)
    }

    @Test
    fun lastDayOfEachMonthIsValid() {
        for (month in 1..12) {
            val result = validate(month = month, day = BirthdayValidator.maxDayOfMonth(month))
            assertTrue("month $month", result.isValid)
        }
    }

    @Test
    fun leapDayWithoutYearIsValid() {
        assertTrue(validate(month = 2, day = 29).isValid)
    }

    @Test
    fun leapDayInLeapYearIsValid() {
        assertTrue(validate(month = 2, day = 29, year = "2000").isValid)
    }

    @Test
    fun leapDayInNonLeapYearIsInvalid() {
        assertEquals(YearError.NOT_A_LEAP_YEAR, validate(month = 2, day = 29, year = "2001").yearError)
        assertEquals(YearError.NOT_A_LEAP_YEAR, validate(month = 2, day = 29, year = "1900").yearError)
    }

    @Test
    fun nonNumericYearIsInvalid() {
        assertEquals(YearError.NOT_A_NUMBER, validate(year = "19x5").yearError)
    }

    @Test
    fun yearBefore1900IsInvalid() {
        assertEquals(YearError.OUT_OF_RANGE, validate(year = "1899").yearError)
        assertNull(validate(year = "1900").yearError)
    }

    @Test
    fun yearAfterCurrentYearIsInvalid() {
        assertEquals(YearError.OUT_OF_RANGE, validate(year = "2026").yearError)
    }

    @Test
    fun dateLaterThisYearIsInFuture() {
        assertEquals(YearError.IN_FUTURE, validate(month = 6, day = 16, year = "2025").yearError)
    }

    @Test
    fun todayThisYearIsValid() {
        assertTrue(validate(month = 6, day = 15, year = "2025").isValid)
    }

    @Test
    fun multipleErrorsAreReportedTogether() {
        val result = validate(name = "", month = 4, day = 31, year = "abc")
        assertFalse(result.isValid)
        assertEquals(NameError.BLANK, result.nameError)
        assertEquals(DateError.INVALID_DAY, result.dateError)
        assertEquals(YearError.NOT_A_NUMBER, result.yearError)
    }

    @Test
    fun maxDayOfMonthAllowsLeapDay() {
        assertEquals(29, BirthdayValidator.maxDayOfMonth(2))
        assertEquals(30, BirthdayValidator.maxDayOfMonth(4))
        assertEquals(31, BirthdayValidator.maxDayOfMonth(12))
    }

    @Test
    fun parseYearTreatsBlankAsUnknown() {
        assertNull(BirthdayValidator.parseYear("  ").getOrThrow())
        assertEquals(1995, BirthdayValidator.parseYear("1995").getOrThrow())
        assertTrue(BirthdayValidator.parseYear("x").isFailure)
    }
}
