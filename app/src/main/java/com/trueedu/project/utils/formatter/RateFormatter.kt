package com.trueedu.project.utils.formatter

import java.text.NumberFormat
import java.util.Locale

class RateFormatter(
    decimalPlaces: Int = 2
): MyFormatter {
    private val numberInstance = getNumberFormatInstance(decimalPlaces)

    override fun format(value: Int, withSign: Boolean): String {
        val sign = if (withSign) sign(value) else ""
        return sign + numberInstance.format(value) + "%"
    }

    override fun format(value: Long, withSign: Boolean): String {
        val sign = if (withSign) sign(value) else ""
        return sign + numberInstance.format(value) + "%"
    }

    override fun format(value: Double, withSign: Boolean): String {
        val sign = if (withSign) sign(value) else ""
        return sign + numberInstance.format(value) + "%"
    }

    private fun getNumberFormatInstance(decimalPlaces: Int): NumberFormat {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        numberFormat.maximumFractionDigits = decimalPlaces
        numberFormat.minimumFractionDigits = decimalPlaces
        return numberFormat
    }
}
