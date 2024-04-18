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
        val spacing = resources.getDimensionPixelSize(R.dimen.margin_16)
        val position = parent.getChildAdapterPosition(view)
        val columnSize = 3
        if (position > 0) {
            if (position % columnSize == 1) {
                outRect.left = spacing
            } else if (position % columnSize == 0) {
                outRect.right = spacing / 2
            } else {
                outRect.left = spacing / 2
                outRect.right = spacing / 2
            }
        }
        if (position > 3) {
            outRect.top = -spacing
        }
    }
}
