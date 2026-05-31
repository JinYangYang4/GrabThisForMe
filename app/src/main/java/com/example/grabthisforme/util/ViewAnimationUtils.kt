package com.example.grabthisforme.util

import android.view.View

object ViewAnimationUtils {

    /**
     * 为多个视图依次执行淡入 + 上滑的错列入场动画。
     *
     * 每个视图从透明 (alpha=0) 且向下偏移 [translationY] 像素开始，
     * 延迟 [startDelayInterval] 毫秒后以上一个视图逐个开始，最终停在原位。
     */
    @JvmStatic
    fun animateStaggeredEntrance(
        vararg views: View,
        duration: Long = 260L,
        startDelayInterval: Long = 55L,
        translationY: Float = 24f
    ) {
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = translationY
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(duration)
                .setStartDelay(index * startDelayInterval)
                .start()
        }
    }
}
