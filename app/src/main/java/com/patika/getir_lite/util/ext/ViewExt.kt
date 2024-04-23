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

/**
 * Animates the visibility of a [View] based on a given price value, either showing or hiding
 * the view with a sliding animation. This function is designed to handle visibility transitions
 * for UI elements like a shopping basket, where visibility is dependent on the non-zero value
 * of the price.
 *
 * If the view should be visible and the price is greater than zero, it slides in from the right.
 * If the view should be hidden and the price is zero or less, it slides out to the right.
 *
 * @param price The [BigDecimal] price that determines the visibility of the view.
 * @param context The [Context] in which the view is running, needed to access resources.
 * @param textView The [TextView] to update with the formatted price when changes occur.
 *
 * Animations used:
 * - [R.anim.slide_in_from_right] for sliding in when the price is greater than zero.
 * - [R.anim.slide_out_to_right] for sliding out when the price is zero or less.
 */
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
