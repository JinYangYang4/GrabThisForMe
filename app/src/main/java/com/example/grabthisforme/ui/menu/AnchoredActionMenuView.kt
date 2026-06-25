package com.example.grabthisforme.ui.menu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.grabthisforme.R

class AnchoredActionMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val shadowBlur = dp(14f)
    private val shadowOffsetY = dp(3f)
    private val arrowWidth = dp(18f)
    private val arrowHeight = dp(10f)
    private val arrowSideGap = dp(3f)
    private val cornerRadius = dp(10f)
    private val arrowVerticalOffset = dp(20f)
    private val contentHorizontalPadding = dp(10f).toInt()
    private val contentVerticalPadding = dp(10f).toInt()
    private val dividerInset = dp(14f)
    private val rowMinWidth = dp(164f).toInt()

    private val shadowHorizontalInset = shadowBlur
    private val shadowTopInset = shadowBlur
    private val shadowBottomInset = shadowBlur + shadowOffsetY

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.FILL
        setShadowLayer(shadowBlur, 0f, shadowOffsetY, 0x22000000)
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE8DED3.toInt()
        style = Paint.Style.STROKE
        strokeWidth = dp(0f)
    }
    private val arrowPath = Path()

    private val itemsContainer = LinearLayout(context).apply {
        orientation = VERTICAL
    }

    private var arrowCenterY = 0f

    var onItemClick: ((AnchoredActionMenuItem) -> Unit)? = null

    init {
        orientation = VERTICAL
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setPadding(
            (shadowHorizontalInset + contentHorizontalPadding).toInt(),
            (shadowTopInset + contentVerticalPadding).toInt(),
            (shadowHorizontalInset + arrowSideGap + arrowHeight + contentHorizontalPadding).toInt(),
            (shadowBottomInset + contentVerticalPadding).toInt()
        )
        addView(
            itemsContainer,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        )
    }

    fun submitItems(items: List<AnchoredActionMenuItem>) {
        itemsContainer.removeAllViews()
        items.forEachIndexed { index, item ->
            itemsContainer.addView(buildItemView(item))
            if (index != items.lastIndex) {
                itemsContainer.addView(buildDivider())
            }
        }
    }

    fun setArrowCenterY(value: Float) {
        arrowCenterY = value
        invalidate()
    }

    fun getArrowTipX(): Float {
        return width - shadowHorizontalInset
    }

    override fun dispatchDraw(canvas: Canvas) {
        drawMenuBackground(canvas)
        super.dispatchDraw(canvas)
    }

    private fun drawMenuBackground(canvas: Canvas) {
        val left = shadowHorizontalInset
        val top = shadowTopInset
        val right = width - shadowHorizontalInset - arrowSideGap - arrowHeight
        val bottom = height - shadowBottomInset

        canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, fillPaint)
        canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, strokePaint)

        val clampedArrowCenterY = arrowCenterY.coerceIn(
            top + arrowVerticalOffset,
            bottom - arrowVerticalOffset
        )
        val arrowBaseX = right
        val arrowTipX = width - shadowHorizontalInset
        val halfArrowWidth = arrowWidth / 2f

        arrowPath.reset()
        arrowPath.moveTo(arrowBaseX, clampedArrowCenterY - halfArrowWidth)
        arrowPath.lineTo(arrowTipX, clampedArrowCenterY)
        arrowPath.lineTo(arrowBaseX, clampedArrowCenterY + halfArrowWidth)
        arrowPath.close()
        canvas.drawPath(arrowPath, fillPaint)
        canvas.drawPath(arrowPath, strokePaint)
    }

    private fun buildItemView(item: AnchoredActionMenuItem): View {
        return LinearLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            minimumWidth = rowMinWidth
            gravity = Gravity.CENTER_VERTICAL
            orientation = HORIZONTAL
            setPadding(dp(12f).toInt(), dp(10f).toInt(), dp(12f).toInt(), dp(10f).toInt())
            background = ContextCompat.getDrawable(context, R.drawable.bg_transparent_ripple_16)

            addView(
                FrameLayout(context).apply {
                    layoutParams = LayoutParams(dp(32f).toInt(), dp(32f).toInt())
                    background = ContextCompat.getDrawable(context, item.iconBackgroundRes)
                    addView(
                        ImageView(context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                dp(16f).toInt(),
                                dp(16f).toInt(),
                                Gravity.CENTER
                            )
                            setImageResource(item.iconRes)
                        }
                    )
                }
            )

            addView(
                TextView(context).apply {
                    layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                        marginStart = dp(10f).toInt()
                    }
                    text = item.title
                    setTextColor(0xFF2D3748.toInt())
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            )

            setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    private fun buildDivider(): View {
        return View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(0.5f).toInt().coerceAtLeast(1)).apply {
                marginStart = dividerInset.toInt()
                marginEnd = dividerInset.toInt()
            }
            alpha = 0.4f
            setBackgroundColor(0xFFD0D0D0.toInt())
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
