package com.patika.getir_lite.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class BorderView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var lineOffset = 0f

    private val rect = RectF()
    private val radius = 16 * resources.displayMetrics.density

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#5d3ebc")
        strokeWidth = 1 * resources.displayMetrics.density
    }

    fun startAnimation() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener { animation ->
                lineOffset = animation.animatedValue as Float * width
                invalidate()
            }
            duration = 400
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = borderPaint.strokeWidth / 2
        rect.set(inset, inset, w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.clipRect(width - lineOffset, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, radius, radius, borderPaint)
        canvas.restore()
    }
}
