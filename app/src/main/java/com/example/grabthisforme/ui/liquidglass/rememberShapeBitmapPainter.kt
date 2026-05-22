package com.example.grabthisforme.ui.liquidglass

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberShapeBitmapPainter(@DrawableRes resId: Int, width: Int = Int.MIN_VALUE, height: Int = Int.MIN_VALUE): BitmapPainter {
    val context = LocalContext.current
    val bitmap = remember(resId, width, height) {
        drawableToBitmap(context, resId, width, height)
    }
    return BitmapPainter(bitmap.asImageBitmap())
}

fun drawableToBitmap(
    context: android.content.Context,
    @DrawableRes resId: Int,
    width: Int = Int.MIN_VALUE,
    height: Int = Int.MIN_VALUE
): Bitmap {
    val drawable = context.resources.getDrawable(resId, null).mutate()

    val w = if (width != Int.MIN_VALUE) width else drawable.intrinsicWidth.let {
        if (it > 0) it else 100 // 没有固有尺寸时给一个默认值
    }
    val h = if (height != Int.MIN_VALUE) height else drawable.intrinsicHeight.let {
        if (it > 0) it else 100
    }

    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
