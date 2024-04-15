package com.patika.getir_lite.util.decor

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.patika.getir_lite.R

class GridSpacingItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val resources = view.context.resources
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
        outRect.bottom = spacing
    }
}
