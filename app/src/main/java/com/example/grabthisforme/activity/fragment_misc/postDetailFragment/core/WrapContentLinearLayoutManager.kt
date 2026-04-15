package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.core

import android.view.View

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 解决嵌套RecyclerView使用wrap_content时测量失败的问题
 * 重写onMeasure，强制计算wrap_content下的真实高度
 */
class WrapContentLinearLayoutManager : LinearLayoutManager {
    // 构造方法1：代码中直接创建时调用
    constructor(context: Context) : super(context)
    // 构造方法2：XML中指定时调用（保留，避免报错）
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)
    // 构造方法3：支持XML属性（保留，避免报错）
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    /**
     * 核心重写方法：强制测量wrap_content下的真实高度
     */
    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        // 1. 处理wrap_content的高度测量（核心）
        val heightSpecMode = View.MeasureSpec.getMode(heightSpec)
        val heightSpecSize = View.MeasureSpec.getSize(heightSpec)
        // 如果是wrap_content模式（AT_MOST），重新计算真实高度
        if (heightSpecMode == View.MeasureSpec.AT_MOST) {
            var totalHeight = 0
            // 遍历所有可见的子Item，计算总高度
            for (i in 0 until itemCount) {
                val view = recycler.getViewForPosition(i)
                measureChild(view, widthSpec, heightSpec)
                val params = view.layoutParams as RecyclerView.LayoutParams
                totalHeight += view.measuredHeight + params.topMargin + params.bottomMargin
            }
            // 加上RV自身的padding
            totalHeight += paddingTop + paddingBottom
            // 重新构建测量规格，将计算出的真实高度传入
            val newHeightSpec = View.MeasureSpec.makeMeasureSpec(totalHeight, View.MeasureSpec.EXACTLY)
            // 调用父类方法，完成最终测量
            super.onMeasure(recycler, state, widthSpec, newHeightSpec)
        } else {
            // 非wrap_content模式，按原生逻辑处理
            super.onMeasure(recycler, state, widthSpec, heightSpec)
        }
    }
}