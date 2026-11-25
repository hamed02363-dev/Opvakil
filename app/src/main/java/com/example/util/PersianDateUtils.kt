package com.example.util

import java.util.Calendar
import java.util.Locale

data class JalaliDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..31
)

object PersianDateUtils {

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    fun toPersianDigits(input: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '۰'..'۹' -> sb.append(('0'.code + (ch.code - '۰'.code)).toChar())
                in '٠'..'٩' -> sb.append(('0'.code + (ch.code - '٠'.code)).toChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun isJalaliLeapYear(jy: Int): Boolean {
        val breaks = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
            1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
        )
        var jp = breaks[0]
        var jump = 0
        for (i in 1 until breaks.size) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            jp = jm
        }
        var n = jy - jp
        if (jump - n < 6) n = n - jump + ((jump + 4) / 33) * 33
        var leap = ((n + 1) % 33 - 1) % 4
        if (leap == -1) leap = 4
        return leap == 0
    }

    fun daysInJalaliMonth(year: Int, month: Int): Int = when {
        month in 1..6 -> 31
        month in 7..11 -> 30
        month == 12 -> if (isJalaliLeapYear(year)) 30 else 29
        else -> 30
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gd + gdm[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jyAdj = jy + 1595
        var days = -355668 + (365 * jyAdj) + ((jyAdj / 33) * 8) +
            (((jyAdj % 33) + 3) / 4) + jd + if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            gy += 100 * (--days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val salA = intArrayOf(
            0, 31, if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28,
            31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        )
        var gm = 0
        while (gm < 13 && gd > salA[gm]) {
            gd -= salA[gm]
            gm++
        }
        return Triple(gy, gm, gd)
    }

    fun millisToJalali(millis: Long): JalaliDate {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun jalaliToMillis(year: Int, month: Int, day: Int, hour: Int = 9, minute: Int = 0): Long {
        val (gy, gm, gd) = jalaliToGregorian(year, month, day)
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, gy)
            set(Calendar.MONTH, gm - 1)
            set(Calendar.DAY_OF_MONTH, gd)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun formatShamsiShort(millis: Long?): String {
        if (millis == null || millis <= 0L) return "ثبت نشده"
        val j = millisToJalali(millis)
        val formatted = String.format(Locale.US, "%04d/%02d/%02d", j.year, j.month, j.day)
        return toPersianDigits(formatted)
    }

    fun formatShamsiLong(millis: Long?): String {
        if (millis == null || millis <= 0L) return "ثبت نشده"
        val j = millisToJalali(millis)
        val monthName = monthNames.getOrElse(j.month - 1) { "" }
        return toPersianDigits("${j.day} $monthName ${j.year}")
    }

    fun formatShamsiDateTime(millis: Long?): String {
        if (millis == null || millis <= 0L) return "ثبت نشده"
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val timeStr = String.format(Locale.US, "%02d:%02d", hour, minute)
        return toPersianDigits("${formatShamsiLong(millis)} - ساعت $timeStr")
    }

    fun daysUntil(targetMillis: Long?): Long? {
        if (targetMillis == null || targetMillis <= 0L) return null
        val diff = targetMillis - System.currentTimeMillis()
        return diff / (1000L * 60L * 60L * 24L)
    }

    fun relativeDaysLabel(targetMillis: Long?): String {
        if (targetMillis == null || targetMillis <= 0L) return "بدون تاریخ"
        val days = daysUntil(targetMillis) ?: return "بدون تاریخ"
        return when {
            days < 0 -> toPersianDigits("${-days} روز گذشته")
            days == 0L -> "امروز"
            days == 1L -> "فردا"
            else -> toPersianDigits("$days روز آینده")
        }
    }
}
