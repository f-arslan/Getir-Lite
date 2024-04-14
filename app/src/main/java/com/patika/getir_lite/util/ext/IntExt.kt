package com.patika.getir_lite.util.ext

import com.patika.getir_lite.model.ItemActionType

fun Int.toItemActionType(): ItemActionType {
    return when (this) {
        0 -> ItemActionType.ONLY_PLUS
        1 -> ItemActionType.PLUS_DELETE
        else -> ItemActionType.PLUS_MINUS
    }
}
