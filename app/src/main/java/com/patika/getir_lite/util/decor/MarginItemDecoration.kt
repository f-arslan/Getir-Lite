package com.patika.getir_lite.util.decor

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.patika.getir_lite.R

class MarginItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount
        val margin = view.context.resources.getDimensionPixelSize(R.dimen.margin_start_end)

        if (position == 0) {
            outRect.left = margin
        }

        if (position == itemCount - 1) {
            outRect.right = margin
        }
    }
}
