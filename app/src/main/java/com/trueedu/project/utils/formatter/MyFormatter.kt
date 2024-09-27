package com.trueedu.project.utils.formatter

interface MyFormatter {
    fun format(value: Int, withSign: Boolean = false): String
    fun format(value: Double, withSign: Boolean = false): String

    fun sign(value: Double): String {
        return when {
            value > 0.0 -> "+"
            else -> ""
        }
    }
    fun sign(value: Int) = sign(value.toDouble())
}
