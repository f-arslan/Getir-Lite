package com.patika.getir_lite.util.ext

import com.patika.getir_lite.model.ItemActionType
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

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
        0 -> ItemActionType.ONLY_PLUS
        1 -> ItemActionType.PLUS_DELETE
        else -> ItemActionType.PLUS_MINUS
    }
}
