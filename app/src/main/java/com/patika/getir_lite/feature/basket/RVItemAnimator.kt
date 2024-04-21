package com.patika.getir_lite.feature.basket

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

interface AnimationFinishListener {
    fun onAddFinished(item: RecyclerView.ViewHolder)
    fun onRemoveFinished(item: RecyclerView.ViewHolder)
}

class RVItemAnimator(private val animationFinishListener: AnimationFinishListener) :
    DefaultItemAnimator() {

    override fun onAddFinished(item: RecyclerView.ViewHolder?) {
        super.onAddFinished(item)
        item?.let { animationFinishListener.onAddFinished(it) }

    }

    override fun onRemoveFinished(item: RecyclerView.ViewHolder?) {
        super.onRemoveFinished(item)
        item?.let { animationFinishListener.onRemoveFinished(it) }
    }
}
