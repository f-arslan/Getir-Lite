package com.patika.getir_lite.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.patika.getir_lite.databinding.ItemActionCardHrBinding

class ActionCardHrView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val binding: ItemActionCardHrBinding

    init {
        binding = ItemActionCardHrBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
