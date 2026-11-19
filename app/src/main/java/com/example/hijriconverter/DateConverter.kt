package com.example.hijriconverter

import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/**
 * منطق التحويل بين الميلادي والهجري (تقويم أم القرى) باستخدام java.time.
 * النطاق المدعوم هجرياً: 1300 – 1600.
 */
object DateConverter {

    const val HIJRI_MIN_YEAR = 1300
    const val HIJRI_MAX_YEAR = 1600

    val hijriMonths = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    val gregorianMonths = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    fun dayName(day: DayOfWeek): String = when (day) {
        DayOfWeek.SATURDAY -> "السبت"
        DayOfWeek.SUNDAY -> "الأحد"
        DayOfWeek.MONDAY -> "الاثنين"
        DayOfWeek.TUESDAY -> "الثلاثاء"
        DayOfWeek.WEDNESDAY -> "الأربعاء"
        DayOfWeek.THURSDAY -> "الخميس"
        DayOfWeek.FRIDAY -> "الجمعة"
    }

    data class Result(val gregorian: LocalDate, val hijri: HijrahDate) {
        val weekday: String get() = dayName(gregorian.dayOfWeek)
        val hijriDay: Int get() = hijri.get(ChronoField.DAY_OF_MONTH)
        val hijriMonth: Int get() = hijri.get(ChronoField.MONTH_OF_YEAR)
        val hijriYear: Int get() = hijri.get(ChronoField.YEAR)
        val gregorianText: String get() = formatGregorian(gregorian)
        val hijriText: String get() = formatHijri(hijri)
    }

    sealed interface Outcome {
        data class Ok(val result: Result) : Outcome
        data class Error(val message: String) : Outcome
    }

    fun formatGregorian(date: LocalDate): String =
        "${date.dayOfMonth} ${gregorianMonths[date.monthValue - 1]} ${date.year}م"

    fun formatHijri(date: HijrahDate): String {
        val d = date.get(ChronoField.DAY_OF_MONTH)
        val m = date.get(ChronoField.MONTH_OF_YEAR)
        val y = date.get(ChronoField.YEAR)
        return "$d ${hijriMonths[m - 1]} ${y}هـ"
    }

    fun today(): Result {
        val g = LocalDate.now()
        return Result(g, HijrahDate.from(g))
    }

    fun fromGregorian(year: Int, month: Int, day: Int): Outcome {
        val g = try {
            LocalDate.of(year, month, day)
        } catch (e: DateTimeException) {
            return Outcome.Error("التاريخ الميلادي غير صالح، تحقق من اليوم والشهر والسنة.")
        }
        return try {
            Outcome.Ok(Result(g, HijrahDate.from(g)))
        } catch (e: DateTimeException) {
            Outcome.Error("التاريخ خارج النطاق المدعوم (حوالي 1882م إلى 2174م).")
        }
    }

    fun fromHijri(year: Int, month: Int, day: Int): Outcome {
        if (year !in HIJRI_MIN_YEAR..HIJRI_MAX_YEAR) {
            return Outcome.Error("السنة الهجرية المدعومة من $HIJRI_MIN_YEAR إلى $HIJRI_MAX_YEAR.")
        }
        val h = try {
            HijrahDate.of(year, month, day)
        } catch (e: DateTimeException) {
            return Outcome.Error("التاريخ الهجري غير صالح، قد لا يحتوي هذا الشهر على هذا اليوم.")
        }
        return try {
            Outcome.Ok(Result(LocalDate.from(h), h))
        } catch (e: DateTimeException) {
            Outcome.Error("تعذّر التحويل لهذا التاريخ.")
        }
    }
}
