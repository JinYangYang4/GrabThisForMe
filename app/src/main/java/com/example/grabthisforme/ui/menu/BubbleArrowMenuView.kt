package com.example.grabthisforme.ui.menu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.grabthisforme.R

class BubbleArrowMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val shadowBlur = dp(12f)
    private val shadowOffsetY = dp(3f)
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.FILL
        setShadowLayer(shadowBlur, 0f, shadowOffsetY, 0x22000000)
    }
    private val arrowPath = Path()

    private val arrowWidth = dp(16f)
    private val arrowHeight = dp(9f)
    private val cornerRadius = dp(4f)
    private val shadowHorizontalInset = shadowBlur
    private val shadowTopInset = shadowBlur
    private val shadowBottomInset = shadowBlur + shadowOffsetY
    private val horizontalPadding = dp(4f).toInt()
    private val itemVerticalPadding = dp(2f).toInt()
    private val itemHorizontalPadding = dp(4f).toInt()

    var onItemClick: ((position: Int, title: String) -> Unit)? = null

    init {
        orientation = VERTICAL
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setPadding(
            (shadowHorizontalInset + horizontalPadding).toInt(),
            (shadowTopInset + horizontalPadding).toInt(),
            (shadowHorizontalInset + horizontalPadding).toInt(),
            (shadowBottomInset + arrowHeight + dp(8f)).toInt()
        )
    }

    fun submitItems(items: List<String>) {
        removeAllViews()
        items.forEachIndexed { index, title ->
            addView(buildItemView(index, title))
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        drawBubbleBackground(canvas)
        super.dispatchDraw(canvas)
    }

    private fun drawBubbleBackground(canvas: Canvas) {
        val left = shadowHorizontalInset
        val top = shadowTopInset
        val right = width - shadowHorizontalInset
        val bodyBottom = height - shadowBottomInset - arrowHeight
        canvas.drawRoundRect(
            left,
            top,
            right,
            bodyBottom,
            cornerRadius,
            cornerRadius,
            bubblePaint
        )

        val arrowCenterX = width / 2f
        val arrowTipY = height - shadowBottomInset
        arrowPath.reset()
        arrowPath.moveTo(arrowCenterX - arrowWidth / 2f, bodyBottom)
        arrowPath.lineTo(arrowCenterX, arrowTipY)
        arrowPath.lineTo(arrowCenterX + arrowWidth / 2f, bodyBottom)
        arrowPath.close()
        canvas.drawPath(arrowPath, bubblePaint)
    }

    private fun buildItemView(index: Int, title: String): View {
        return TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER
            setPadding(itemHorizontalPadding, itemVerticalPadding, itemHorizontalPadding, itemVerticalPadding)
            text = title
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.black))
            background = ContextCompat.getDrawable(context, R.drawable.bg_transparent_ripple_16)
            setOnClickListener {
                onItemClick?.invoke(index, title)
            }
        }
    }

    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }
}
