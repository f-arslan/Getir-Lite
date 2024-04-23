package com.patika.getir_lite.util.decor

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.patika.getir_lite.R

/**
 * It allows for equal horizontal spacing and negative top margin beyond the first row
 * to tightly pack the grid items.
 *
 * @param spanCount The number of columns in the grid layout.
 */
class GridSpacingItemDecoration(private val spanCount: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val resources = view.context.resources
        val spacing = resources.getDimensionPixelSize(R.dimen.margin_16)
        val position = parent.getChildAdapterPosition(view)
        val columnSize = spanCount
        if (position > 0) { // Ignore the first item, its horizontal RV
            if (position % columnSize == 1) {
                // First column
                outRect.left = spacing
            } else if (position % columnSize == 0) {
                // last column
                outRect.right = spacing / 2
            } else {
                // middle column
                outRect.left = spacing / 2
                outRect.right = spacing / 2
            }
        }
        if (position > spanCount) {
            // after the first row
            outRect.top = -spacing
        }
    }
}
