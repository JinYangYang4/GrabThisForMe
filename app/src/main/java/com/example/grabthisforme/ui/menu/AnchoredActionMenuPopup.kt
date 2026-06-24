package com.example.grabthisforme.ui.menu

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow

class AnchoredActionMenuPopup(
    context: Context
) {

    private val menuView = AnchoredActionMenuView(context)
    private val popupWindow = PopupWindow(
        menuView,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        true
    ).apply {
        isOutsideTouchable = true
        elevation = 0f
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun show(
        anchor: View,
        items: List<AnchoredActionMenuItem>,
        onItemClick: (AnchoredActionMenuItem) -> Unit
    ) {
        popupWindow.dismiss()

        menuView.submitItems(items)
        menuView.onItemClick = { item ->
            popupWindow.dismiss()
            onItemClick(item)
        }
        menuView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val popupWidth = menuView.measuredWidth
        val popupHeight = menuView.measuredHeight
        val screenWidth = anchor.resources.displayMetrics.widthPixels
        val screenHeight = anchor.resources.displayMetrics.heightPixels
        val anchorLocation = IntArray(2)
        anchor.getLocationOnScreen(anchorLocation)

        val anchorLeft = anchorLocation[0]
        val anchorTop = anchorLocation[1]
        val anchorCenterY = anchorTop + anchor.height / 2f

        val horizontalMargin = dp(anchor, 12)
        val tipOverlap = dp(anchor, 2)
        val arrowTipX = menuView.getArrowTipX()
        val desiredX = (anchorLeft - arrowTipX + tipOverlap).toInt()
        val popupX = desiredX.coerceIn(
            horizontalMargin,
            (screenWidth - popupWidth - horizontalMargin).coerceAtLeast(horizontalMargin)
        )
        val popupY = (anchorCenterY - popupHeight / 2f).toInt().coerceIn(
            horizontalMargin,
            (screenHeight - popupHeight - horizontalMargin).coerceAtLeast(horizontalMargin)
        )

        menuView.setArrowCenterY(anchorCenterY - popupY)

        val visibleFrame = Rect()
        anchor.rootView.getWindowVisibleDisplayFrame(visibleFrame)
        val finalX = popupX.coerceIn(
            visibleFrame.left + horizontalMargin,
            (visibleFrame.right - popupWidth - horizontalMargin).coerceAtLeast(visibleFrame.left + horizontalMargin)
        )
        val finalY = popupY.coerceIn(
            visibleFrame.top + horizontalMargin,
            (visibleFrame.bottom - popupHeight - horizontalMargin).coerceAtLeast(visibleFrame.top + horizontalMargin)
        )

        menuView.setArrowCenterY(anchorCenterY - finalY)

        popupWindow.showAtLocation(anchor.rootView, Gravity.NO_GRAVITY, finalX, finalY)
    }

    fun dismiss() {
        popupWindow.dismiss()
    }

    private fun dp(anchor: View, value: Int): Int {
        return (value * anchor.resources.displayMetrics.density).toInt()
    }
}
