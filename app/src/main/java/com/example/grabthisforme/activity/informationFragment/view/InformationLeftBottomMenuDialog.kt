package com.example.grabthisforme.activity.informationFragment.view

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.grabthisforme.R


class InformationLeftBottomMenuDialog: DialogFragment() {

    companion object {
        // 创建一个静态方法来传递目标视图的 ID
        fun newInstance(targetImageViewId: Int): InformationLeftBottomMenuDialog {
            val dialog = InformationLeftBottomMenuDialog()
            val args = Bundle()
            args.putInt("targetImageViewId", targetImageViewId)  // 将目标视图的 ID 传递给 Fragment
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 这里加载自定义的对话框布局
        return inflater.inflate(R.layout.information_dialog_left_bottom_menu, container, false)
    }
    override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            val window = dialog.window
            val params = window?.attributes

            // 获取传入的目标 ImageView 的 ID
            val targetImageViewId = arguments?.getInt("targetImageViewId") ?: return

            // 获取目标 ImageView
            val targetImageView: ImageView = requireActivity().findViewById(targetImageViewId)

            // 获取目标 ImageView 的位置
            val location = IntArray(2)
            targetImageView.getLocationOnScreen(location)  // 获取目标视图的位置 (x, y)
            Log.d("test11", "X : ${location[0]} Y : ${location[1]}")

            // 计算对话框的左下角位置
            var xPos = location[0]  // 水平位置：目标 ImageView 的 x 坐标
            var yPos = location[1] + targetImageView.height  // 垂直位置：目标 ImageView 的 y 坐标 + 目标 ImageView 的高度
            Log.d("test11", "Initial yPos : $yPos")

            // 获取屏幕高度和宽度
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels

            // 计算屏幕中心坐标
            val screenCenterY = screenHeight / 2
            val screenCenterX = screenWidth / 2

            // 计算目标视图到屏幕中心的 Y 距离
            val offsetY = location[1] - screenCenterY  // 负的距离，让对话框靠近视图
            yPos += offsetY


            val offsetX = location[0] - screenCenterX  // 负的距离，让对话框靠近视图
            xPos = offsetX

            Log.d("test11", "Adjusted xPos: $xPos, Adjusted yPos: $yPos")

            // 设置对话框的位置
            params?.gravity = Gravity.NO_GRAVITY  // 不使用默认的 Gravity
            params?.x = xPos  // 水平位置
            params?.y = yPos  // 垂直位置

            // 更新对话框的位置
            window?.attributes = params
        }
    }
}