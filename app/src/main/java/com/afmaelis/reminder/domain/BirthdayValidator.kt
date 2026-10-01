package com.afmaelis.reminder.domain

import java.time.LocalDate
import java.time.Month
import java.time.Year

enum class NameError { BLANK }

enum class DateError { INVALID_MONTH, INVALID_DAY }

enum class YearError { NOT_A_NUMBER, OUT_OF_RANGE, IN_FUTURE, NOT_A_LEAP_YEAR }

data class ValidationResult(
    val nameError: NameError? = null,
    val dateError: DateError? = null,
    val yearError: YearError? = null,
) {
    val isValid: Boolean get() = nameError == null && dateError == null && yearError == null
}

object BirthdayValidator {

    const val MIN_YEAR = 1900

    /** Longest the month can be (leap year), so 29 February is always selectable. */
    fun maxDayOfMonth(month: Int): Int = Month.of(month).maxLength()

    /** Parses the optional year field: blank means "unknown". Returns null when not a number. */
    fun parseYear(yearText: String): Result<Int?> {
        val trimmed = yearText.trim()
        if (trimmed.isEmpty()) return Result.success(null)
        return trimmed.toIntOrNull()?.let { Result.success(it) }
            ?: Result.failure(NumberFormatException(trimmed))
    }

    fun validate(name: String, month: Int, day: Int, yearText: String, today: LocalDate): ValidationResult {
        val nameError = if (name.isBlank()) NameError.BLANK else null

        val dateError = when {
            month !in 1..12 -> DateError.INVALID_MONTH
            day < 1 || day > maxDayOfMonth(month) -> DateError.INVALID_DAY
            else -> null
        }

        val year = parseYear(yearText).getOrElse {
            return ValidationResult(nameError, dateError, YearError.NOT_A_NUMBER)
        }
        val yearError = when {
            year == null -> null
            year < MIN_YEAR || year > today.year -> YearError.OUT_OF_RANGE
            dateError != null -> null
            month == 2 && day == 29 && !Year.isLeap(year.toLong()) -> YearError.NOT_A_LEAP_YEAR
            LocalDate.of(year, month, day).isAfter(today) -> YearError.IN_FUTURE
            else -> null
        }

        return ValidationResult(nameError, dateError, yearError)
    }
}
