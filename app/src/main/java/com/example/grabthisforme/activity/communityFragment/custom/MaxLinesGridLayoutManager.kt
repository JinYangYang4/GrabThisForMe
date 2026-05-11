package com.example.grabthisforme.activity.communityFragment.custom

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil
import kotlin.math.min

class MaxLinesGridLayoutManager(
    context: Context,
    spanCount: Int,
    private val maxLines: Int
) : GridLayoutManager(context, spanCount) {

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)

        if (itemCount <= 0 || spanCount <= 0 || maxLines <= 0) return

        val firstView = try {
            recycler.getViewForPosition(0)
        } catch (_: Exception) {
            null
        } ?: return

        measureChildWithMargins(firstView, 0, 0)
        val childHeight = getDecoratedMeasuredHeight(firstView)
        recycler.recycleView(firstView)
        if (childHeight <= 0) return

        val requiredLines = ceil(itemCount / spanCount.toDouble()).toInt()
        val displayLines = min(requiredLines, maxLines)
        val maxHeight = (childHeight * displayLines) + paddingTop + paddingBottom

        val widthSize = View.MeasureSpec.getSize(widthSpec)
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val parentHeight = View.MeasureSpec.getSize(heightSpec)
        val finalHeight = if (heightMode == View.MeasureSpec.UNSPECIFIED) {
            maxHeight
        } else {
            min(parentHeight, maxHeight)
        }
        setMeasuredDimension(widthSize, finalHeight)
    }
}
