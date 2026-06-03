package com.example.grabthisforme.ui.liquidglass.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.example.grabthisforme.ui.liquidglass.utils.InteractiveHighlight
import com.kyant.backdrop.Backdrop

import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

@Composable
fun LiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    clipToShape: Boolean = false,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    content: @Composable RowScope.() -> Unit
) {
    val animationScope = rememberCoroutineScope()
    val shape = RoundedCornerShape(percent = 50)
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(
            animationScope = animationScope
        )
    }

    fun applyInteractiveTransform(size: IntSize) = Modifier.graphicsLayer {
        if (!isInteractive || size.width == 0 || size.height == 0) {
            return@graphicsLayer
        }
        val width = size.width.toFloat()
        val height = size.height.toFloat()
        val canvasSize = Size(width, height)
        val progress = interactiveHighlight.pressProgress
        val scale = lerp(1f, 1f + 1f.dp.toPx() / height, progress)

        val maxOffset = canvasSize.minDimension
        val initialDerivative = 0.05f
        val offset = interactiveHighlight.offset
        translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
        translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

        val maxDragScale = 4f.dp.toPx() / height
        val offsetAngle = atan2(offset.y, offset.x)
        scaleX =
            scale +
                maxDragScale * abs(cos(offsetAngle) * offset.x / canvasSize.maxDimension) *
                (width / height).fastCoerceAtMost(1f)
        scaleY =
            scale +
                maxDragScale * abs(sin(offsetAngle) * offset.y / canvasSize.maxDimension) *
                (height / width).fastCoerceAtMost(1f)
    }

    var buttonSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier
            .onSizeChanged { buttonSize = it }
            .then(applyInteractiveTransform(buttonSize))
            .clickable(
                interactionSource = null,
                indication = if (isInteractive) null else LocalIndication.current,
                role = Role.Button,
                onClick = onClick
            )
            .then(
                if (isInteractive) {
                    Modifier
                        .then(interactiveHighlight.gestureModifier)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier
                .then(
                    if (clipToShape) {
                        Modifier.graphicsLayer {
                            this.shape = shape
                            clip = true
                        }
                    } else {
                        Modifier
                    }
                )
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        vibrancy()
                        blur(2f.dp.toPx())
                        lens(12f.dp.toPx(), 24f.dp.toPx())
                    },
                    onDrawSurface = {
                        if (tint.isSpecified) {
                            drawRect(tint, blendMode = BlendMode.Hue)
                            drawRect(tint.copy(alpha = 0.75f))
                        }
                        if (surfaceColor.isSpecified) {
                            drawRect(surfaceColor)
                        }
                    }
                )
                .then(
                    if (isInteractive) {
                        interactiveHighlight.modifier
                    } else {
                        Modifier
                    }
                )
                .height(48f.dp)
                .padding(horizontal = 16f.dp),
            horizontalArrangement = Arrangement.spacedBy(8f.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
