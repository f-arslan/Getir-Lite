package com.patika.getir_lite.util.ext

import android.view.View

fun View.setVisibility(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}
