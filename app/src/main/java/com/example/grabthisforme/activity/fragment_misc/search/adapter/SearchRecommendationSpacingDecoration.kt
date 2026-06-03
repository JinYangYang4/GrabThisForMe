package com.example.grabthisforme.activity.fragment_misc.search.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SearchRecommendationSpacingDecoration(
    private val spanCount: Int,
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        outRect.left = spacing / 2
        outRect.right =  spacing / 2
        outRect.top = if (position < spanCount) 0 else spacing
    }
}
