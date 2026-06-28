package dev.nanday.alternativefirecalculator.services

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

interface TimeProvider {
    fun getCurrentDate(): LocalDate
    fun getCurrentDateTimeString(): String
    fun daysInYear(year: Int): Int
}

class TimeProviderImpl : TimeProvider {

    override fun getCurrentDate(): LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    override fun getCurrentDateTimeString(): String {
        val nowInstant = Clock.System.now()
        val zone = TimeZone.currentSystemDefault()
        val localDateTime = nowInstant.toLocalDateTime(zone)

        val offset = zone.offsetAt(nowInstant)
        val offsetString = offset.toString()

        return "${localDateTime.date}T${localDateTime.time}$offsetString"
    }

    override fun daysInYear(year: Int): Int {
        val startOfYear = LocalDate(year, 1, 1)
        val startOfNextYear = LocalDate(year + 1, 1, 1)
        return startOfYear.daysUntil(startOfNextYear)
    }
}