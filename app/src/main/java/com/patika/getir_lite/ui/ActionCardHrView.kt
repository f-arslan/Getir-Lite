package com.patika.getir_lite.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.patika.getir_lite.R
import com.patika.getir_lite.model.ItemActionType.PLUS_DELETE
import com.patika.getir_lite.model.ItemActionType.PLUS_MINUS
import com.patika.getir_lite.util.ext.setVisibility
import com.patika.getir_lite.util.ext.toItemActionType

class ActionCardHrView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val btnMinus: ImageButton
    private val btnDelete: ImageButton
    private val btnAdd: ImageButton
    private val tvCount: TextView

    private var buttonContainerSize: Int =
        context.resources.getDimensionPixelSize(R.dimen.action_button_size)
    private var textContainerHeight: Int =
        context.resources.getDimensionPixelSize(R.dimen.action_text_height_detail)
    private var textContainerWidth: Int =
        context.resources.getDimensionPixelSize(R.dimen.action_text_width_detail)
    private var countTextSize = context.resources.getDimension(R.dimen.action_text_size_detail)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ActionCardHrView, 0, 0).apply {
            try {
                buttonContainerSize = getDimensionPixelSize(
                    R.styleable.ActionCardHrView_buttonContainerSize,
                    buttonContainerSize
                )
                textContainerHeight = getDimensionPixelSize(
                    R.styleable.ActionCardHrView_textContainerHeight,
                    textContainerHeight
                )
                textContainerWidth = getDimensionPixelSize(
                    R.styleable.ActionCardHrView_textContainerWidth,
                    textContainerWidth
                )
                countTextSize = getDimension(
                    R.styleable.ActionCardHrView_textSize,
                    resources.getDimension(R.dimen.action_text_size_detail)
                ) / resources.displayMetrics.scaledDensity
            } finally {
                recycle()
            }
        }

        LayoutInflater.from(context).inflate(R.layout.item_action_card_hr, this, true)

        radius = context.resources.getDimension(R.dimen.action_card_corner_radius)
        cardElevation = context.resources.getDimension(R.dimen.action_card_elevation)

        btnMinus = findViewById(R.id.btn_minus)
        btnDelete = findViewById(R.id.btn_delete)
        btnAdd = findViewById(R.id.btn_add)
        tvCount = findViewById(R.id.tv_item_count)
        setButtonSize(buttonContainerSize)
        setTextContainerSize(textContainerHeight, textContainerWidth)
        tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, countTextSize)
    }

    fun setOnMinusClickListener(listener: OnClickListener) {
        btnMinus.setOnClickListener(listener)
    }

    fun setOnDeleteClickListener(listener: OnClickListener) {
        btnDelete.setOnClickListener(listener)
    }

    fun setOnAddClickListener(listener: OnClickListener) {
        btnAdd.setOnClickListener(listener)
    }

    fun setCount(count: Int) {
        val actionType = count.toItemActionType()
        btnMinus.setVisibility(actionType == PLUS_MINUS)
        btnDelete.setVisibility(actionType == PLUS_DELETE)
        tvCount.text = count.toString()
    }

    private fun setTextContainerSize(textContainerHeight: Int, textContainerWidth: Int) {
        val params = tvCount.layoutParams
        params.height = textContainerHeight
        params.width = textContainerWidth
        tvCount.layoutParams = params
    }

    private fun setButtonSize(size: Int) {
        val params = btnMinus.layoutParams
        params.width = size
        params.height = size
        btnMinus.layoutParams = params
        btnDelete.layoutParams = params
        btnAdd.layoutParams = params
    }
}
