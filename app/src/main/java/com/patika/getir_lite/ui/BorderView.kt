package com.patika.getir_lite.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.patika.getir_lite.R

/**
 * A custom View that draws a border with rounded corners and includes an animation effect.
 * The border's appearance and animation are controlled through custom method.
 */
class BorderView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var lineOffset = 0f  // Tracks the animation progress

    private val rect = RectF()  // Defines the area inside which the border is drawn.
    private val radius = 16 * resources.displayMetrics.density
    private val primaryColor = resources.getColor(R.color.md_theme_light_primary, null)

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = primaryColor
        strokeWidth = 1 * resources.displayMetrics.density
    }

    /**
     * Starts a horizontal wipe animation for the border. The animation progresses from right to left.
     * This function triggers a redraw of the view at each step of the animation, updating the visible portion of the border.
     */
    fun startAnimation() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener { animation ->
                lineOffset = animation.animatedValue as Float * width
                invalidate()
            }
            duration = 250
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = borderPaint.strokeWidth / 2
        rect.set(inset, inset, w - inset, h - inset)  // Updates the rect to fit the new size with appropriate insets.
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.clipRect(width - lineOffset, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, radius, radius, borderPaint)
        canvas.restore()
    }
}
