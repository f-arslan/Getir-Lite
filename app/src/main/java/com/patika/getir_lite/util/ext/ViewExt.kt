package com.patika.getir_lite.util.ext

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.patika.getir_lite.R
import java.math.BigDecimal

fun View.setVisibility(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.animateBasketVisibility(price: BigDecimal, context: Context, textView: TextView) {
    val shouldBeVisible = price > BigDecimal.ZERO
    val currentVisibility = visibility == View.VISIBLE

    if (shouldBeVisible != currentVisibility) {
        val animation = if (shouldBeVisible) {
            visibility = View.VISIBLE
            textView.text = price.formatPrice()
            AnimationUtils.loadAnimation(context, R.anim.slide_in_from_right)
        } else {
            AnimationUtils.loadAnimation(context, R.anim.slide_out_to_right).apply {
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) = Unit
                    override fun onAnimationEnd(animation: Animation?) {
                        this@animateBasketVisibility.visibility = View.GONE
                        textView.text = price.formatPrice()
                    }

                    override fun onAnimationRepeat(animation: Animation?) = Unit
                })
            }
        }
        startAnimation(animation)
    } else {
        textView.text = price.formatPrice()
    }
}
