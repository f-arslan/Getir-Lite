package com.patika.getir_lite.util.ext

import com.patika.getir_lite.model.ItemActionType
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats a [BigDecimal] value as a price string in a specific locale format.
 * This extension function is tailored for displaying monetary values in the format
 * used in Turkey, incorporating the Turkish Lira symbol and French locale number formatting conventions.
 *
 * Example:
 * - `1234.56.toBigDecimal().formatPrice()` returns "₺1.234,56"
 *
 * @return A string representation of the [BigDecimal] value formatted as a price with currency symbol.
 */
fun BigDecimal.formatPrice(): String {
    val symbols = DecimalFormatSymbols(Locale.FRANCE).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formatter = DecimalFormat("₺#,##0.00", symbols)
    return formatter.format(this)
}

fun Int.toItemActionType(): ItemActionType {
    return when (this) {
        0 -> ItemActionType.ONLY_PLUS_IDLE
        1 -> ItemActionType.PLUS_DELETE
        else -> ItemActionType.PLUS_MINUS
    }
}
