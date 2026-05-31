package com.example.grabthisforme.util

import android.graphics.Rect
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView

/**
 * 封装键盘避让 + 焦点输入框自动滚动的逻辑，可在任意带 NestedScrollView 的表单 Fragment 中复用。
 *
 * 用法：
 * 1. 在 onViewCreated 中创建实例并调用 [setup]
 * 2. 在 onDestroyView 中调用 [teardown]
 * 3. 如需在其他时机手动滚动到焦点（如文本变更后），可调用 [scrollToFocused]
 *
 * @param rootView Fragment 根视图，用于设置 WindowInsets 监听
 * @param scrollView 表单所在的 NestedScrollView
 * @param density 屏幕密度，来自 resources.displayMetrics.density
 * @param onImeHidden 键盘收起时的回调，通常用于清理焦点
 * @param focusRectProvider 可选。为焦点 View 生成 target rect，默认使用 getDrawingRect。
 *                          若焦点是大段文本编辑器，建议返回光标所在行区域。
 */
class KeyboardScrollHelper(
    private val rootView: View,
    private val scrollView: NestedScrollView,
    private val density: Float,
    private val onImeHidden: (() -> Unit)? = null,
    private val focusRectProvider: ((View) -> Rect)? = null
) {
    private var baseBottomPadding = 0
    private var isSetup = false

    fun setup() {
        if (isSetup) return
        baseBottomPadding = scrollView.paddingBottom
        isSetup = true

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val systemBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val keyboardSpace = (imeBottom - systemBottom).coerceAtLeast(0)

            scrollView.setPadding(
                scrollView.paddingLeft,
                scrollView.paddingTop,
                scrollView.paddingRight,
                baseBottomPadding + keyboardSpace
            )

            if (imeVisible) {
                scrollView.postDelayed(
                    { scrollToFocused() },
                    KEYBOARD_SCROLL_DELAY_MS
                )
            } else {
                onImeHidden?.invoke()
            }
            insets
        }
    }

    fun teardown() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView, null)
        isSetup = false
    }

    /**
     * 将当前焦点输入框滚动到可见区域。键盘弹起时会自动调用；Fragment 也可在文本变更等时机手动调用。
     */
    fun scrollToFocused() {
        val currentFocus = scrollView.findFocus() ?: return
        if (!isDescendantOf(currentFocus, scrollView)) return

        val focusedRect = if (focusRectProvider != null) {
            focusRectProvider(currentFocus)
        } else {
            Rect().also { currentFocus.getDrawingRect(it) }
        }
        scrollView.offsetDescendantRectToMyCoords(currentFocus, focusedRect)

        val visibleTop = scrollView.scrollY
        val visibleBottom = visibleTop + scrollView.height - scrollView.paddingBottom
        val extraSpacing = (KEYBOARD_FOCUS_SPACING_DP * density).toInt()

        when {
            focusedRect.bottom + extraSpacing > visibleBottom -> {
                val targetY =
                    focusedRect.bottom - scrollView.height + scrollView.paddingBottom + extraSpacing
                scrollView.smoothScrollTo(0, targetY.coerceAtLeast(0))
            }
            focusedRect.top - extraSpacing < visibleTop -> {
                scrollView.smoothScrollTo(0, (focusedRect.top - extraSpacing).coerceAtLeast(0))
            }
        }
    }

    private fun isDescendantOf(child: View, parent: View): Boolean {
        var current: View? = child
        while (current != null) {
            if (current == parent) return true
            current = current.parent as? View
        }
        return false
    }

    companion object {
        private const val KEYBOARD_SCROLL_DELAY_MS = 120L
        private const val KEYBOARD_FOCUS_SPACING_DP = 24
    }
}
