package com.example.grabthisforme.ui.menu

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager

class BubbleArrowMenuPopup(
    context: Context
) {

    private val menuView = BubbleArrowMenuView(context)
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
        items: List<String>,
        onItemClick: (position: Int, title: String) -> Unit
    ) {
        menuView.orientation = LinearLayout.HORIZONTAL
        menuView.submitItems(items)
        menuView.onItemClick = { position, title ->
            popupWindow.dismiss()
            onItemClick(position, title)
        }
        menuView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val popupWidth = menuView.measuredWidth
        val xOffset = anchor.width / 2 - popupWidth / 2
        val yOffset = -anchor.height - menuView.measuredHeight / 2
        popupWindow.showAsDropDown(anchor, xOffset, yOffset, Gravity.START)
    }

    fun dismiss() {
        popupWindow.dismiss()
    }
}
