package com.patika.getir_lite.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue

class ProductImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    fun setImageSize(sizeDp: Int) {
        val sizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, sizeDp.toFloat(), resources.displayMetrics
        ).toInt()

        val layoutParams = layoutParams.apply {
            width = sizePx
            height = sizeDp
        }
        this.layoutParams = layoutParams
    }
}
