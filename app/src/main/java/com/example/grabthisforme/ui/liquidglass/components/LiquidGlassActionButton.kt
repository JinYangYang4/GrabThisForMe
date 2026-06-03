package com.example.grabthisforme.ui.liquidglass.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun LiquidGlassActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    clipToShape: Boolean = true,
    tint: Color = Color.Blue.copy(alpha = 0.12f),
    surfaceColor: Color = Color.Cyan.copy(alpha = 0.5f),
    textColor: Color = Color.White
) {
    val backdrop = rememberLayerBackdrop()
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        clipToShape = clipToShape,
        tint = tint,
        surfaceColor = surfaceColor
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = textColor,
                fontSize = 15.sp
            )
        )
    }
}
