package com.patika.getir_lite.util

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
        val position = parent.getChildAdapterPosition(view)
        val column = position % 2
        val resources = view.context.resources
        val spacing = resources.getDimensionPixelSize(R.dimen.margin_start_end)
        val spacingBottom = resources.getDimensionPixelSize(R.dimen.margin_bottom)
        val spanCount = 3
        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount

        if (position < spanCount) {
            outRect.top = spacing
        }
        outRect.bottom = spacingBottom
    }
}
