package dev.nanday.alternativefirecalculator.utils

import kotlin.math.absoluteValue

/**
 * Formats a number with thousands separators (dots).
 * Example: 1000000 -> "1.000.000"
 */
fun formatThousands(amount: Long): String {
    val isNegative = amount < 0
    val s = amount.absoluteValue.toString()
    val builder = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        builder.append(s[i])
        count++
        if (count % 3 == 0 && i > 0) {
            builder.append('.')
        }
    }
    if (isNegative) builder.append('-')
    return builder.reverse().toString()
}
