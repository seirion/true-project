package com.trueedu.project.utils


/**
 * 양수만 처리한다고 가정함
 */
fun getDigitInput(s: String, maxLength: Int = 12): Long {
    val r = s.filter(Char::isDigit)
        .dropWhile { it == '0' }
        .take(maxLength)

    return if (r.isEmpty()) 0L else r.toLong()
}
