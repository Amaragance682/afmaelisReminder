package com.afmaelis.reminder.domain

import java.time.LocalDate
import java.time.Year
import java.time.temporal.ChronoUnit

/** The fields of a birthday that the date rules need; implemented by the Room entity. */
interface BirthdayInfo {
    val name: String
    val month: Int
    val day: Int
    val year: Int?
}

data class UpcomingBirthday<T : BirthdayInfo>(
    val birthday: T,
    val nextOccurrence: LocalDate,
    val daysUntil: Int,
    val ageTurning: Int?,
) {
    val isToday: Boolean get() = daysUntil == 0
}

object BirthdayCalculator {

    /** The birthday's date in [year]; 29 February falls on 28 February in non-leap years. */
    fun occurrenceIn(year: Int, month: Int, day: Int): LocalDate {
        val adjustedDay = if (month == 2 && day == 29 && !Year.isLeap(year.toLong())) 28 else day
        return LocalDate.of(year, month, adjustedDay)
    }

    fun nextOccurrence(month: Int, day: Int, today: LocalDate): LocalDate {
        val thisYear = occurrenceIn(today.year, month, day)
        return if (!thisYear.isBefore(today)) thisYear else occurrenceIn(today.year + 1, month, day)
    }

    fun daysUntil(month: Int, day: Int, today: LocalDate): Int =
        ChronoUnit.DAYS.between(today, nextOccurrence(month, day, today)).toInt()

    fun isToday(month: Int, day: Int, today: LocalDate): Boolean =
        nextOccurrence(month, day, today) == today

    fun ageTurning(birthday: BirthdayInfo, today: LocalDate): Int? =
        birthday.year?.let { nextOccurrence(birthday.month, birthday.day, today).year - it }

    fun <T : BirthdayInfo> upcoming(birthdays: List<T>, today: LocalDate): List<UpcomingBirthday<T>> =
        birthdays
            .map { b ->
                val next = nextOccurrence(b.month, b.day, today)
                UpcomingBirthday(
                    birthday = b,
                    nextOccurrence = next,
                    daysUntil = ChronoUnit.DAYS.between(today, next).toInt(),
                    ageTurning = b.year?.let { next.year - it },
                )
            }
            .sortedWith(
                compareBy<UpcomingBirthday<T>> { it.daysUntil }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.birthday.name },
            )

    /** All birthdays whose occurrence in [date]'s year is exactly [date], sorted by name. */
    fun <T : BirthdayInfo> birthdaysOn(birthdays: List<T>, date: LocalDate): List<T> =
        birthdays
            .filter { occurrenceIn(date.year, it.month, it.day) == date }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
}
