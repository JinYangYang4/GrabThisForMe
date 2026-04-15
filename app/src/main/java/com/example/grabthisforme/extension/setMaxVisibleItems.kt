package com.example.grabthisforme.extension

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setMaxVisibleItems(maxVisibleItems: Int) {
    val layoutManager = LinearLayoutManager(context)
    this.layoutManager = layoutManager

    post {
        if (adapter == null || adapter?.itemCount == 0) return@post
        val firstView = layoutManager.getChildAt(0) ?: return@post
        val itemHeight = firstView.height
        val maxHeight = itemHeight * maxVisibleItems
        val totalHeight = itemHeight * adapter!!.itemCount
        val finalHeight = if (totalHeight > maxHeight) maxHeight else totalHeight

        layoutParams.height = finalHeight
        requestLayout()
    }
}