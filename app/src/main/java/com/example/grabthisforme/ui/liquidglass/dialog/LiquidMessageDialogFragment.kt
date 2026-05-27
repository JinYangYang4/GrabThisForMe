package com.example.grabthisforme.ui.liquidglass.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.grabthisforme.R
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight

class LiquidMessageDialogFragment : DialogFragment() {

    private val titleText: String
        get() = requireArguments().getString(ARG_TITLE).orEmpty()

    private val messageText: String
        get() = requireArguments().getString(ARG_MESSAGE).orEmpty()

    private val positiveText: String
        get() = requireArguments().getString(ARG_POSITIVE_TEXT).orEmpty()

    private val negativeText: String
        get() = requireArguments().getString(ARG_NEGATIVE_TEXT).orEmpty()

    private val requestKey: String
        get() = requireArguments().getString(ARG_REQUEST_KEY).orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent_NoTitleBar)
        isCancelable = requireArguments().getBoolean(ARG_CANCELABLE, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.38f)),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidDialogContent(
                        title = titleText,
                        message = messageText,
                        positiveText = positiveText,
                        negativeText = negativeText,
                        onNegativeClick = {
                            dispatchResult(confirmed = false)
                            dismiss()
                        },
                        onPositiveClick = {
                            dispatchResult(confirmed = true)
                            dismiss()
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun dispatchResult(confirmed: Boolean) {
        if (requestKey.isBlank()) return
        parentFragmentManager.setFragmentResult(
            requestKey,
            bundleOf(
                RESULT_CONFIRMED to confirmed,
                RESULT_TITLE to titleText,
                RESULT_MESSAGE to messageText
            )
        )
    }

    @Composable
    private fun LiquidDialogContent(
        title: String,
        message: String,
        positiveText: String,
        negativeText: String,
        onNegativeClick: () -> Unit,
        onPositiveClick: () -> Unit
    ) {
        val backdrop = rememberLayerBackdrop()
        val isLightTheme = !isSystemInDarkTheme()
        val containerColor =
            if (isLightTheme) Color(0xFFFAFAFA).copy(0.9f)
            else Color(0xFF121212).copy(0.4f)
        val contentColor = if (isLightTheme) Color.Black else Color.White
        val accentColor =
            if (isLightTheme) Color(0xFF0088FF)
            else Color(0xFF0091FF)


        Column(
            Modifier
                .padding(40.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedCornerShape(24.dp) },
                    effects = {
                        colorControls(
                            brightness = if (isLightTheme) 0.2f else 0f,
                            saturation = 1.5f
                        )
                        blur(if (isLightTheme) 16.dp.toPx() else 8.dp.toPx())
                        lens(24.dp.toPx(), 48.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    onDrawSurface = { drawRect(containerColor) }
                )
                .fillMaxWidth()
        ) {
            BasicText(
                title,
                Modifier.padding(28.dp, 24.dp, 28.dp, 12.dp),
                style = TextStyle(contentColor, 24.sp, FontWeight.Medium)
            )

            BasicText(
                message,
                Modifier
                    .then(
                        if (isLightTheme) {
                            Modifier
                        } else {
                            Modifier.graphicsLayer(blendMode = BlendMode.Plus)
                        }
                    )
                    .padding(24.dp, 12.dp, 24.dp, 12.dp),
                style = TextStyle(contentColor.copy(0.68f), 15.sp),
                maxLines = 5
            )

            Row(
                Modifier
                    .padding(24.dp, 12.dp, 24.dp, 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(containerColor.copy(0.2f))
                        .clickable(onClick = onNegativeClick)
                        .height(48.dp)
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicText(
                        negativeText,
                        style = TextStyle(contentColor, 16.sp)
                    )
                }
                Row(
                    Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(accentColor)
                        .clickable(onClick = onPositiveClick)
                        .height(48.dp)
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicText(
                        positiveText,
                        style = TextStyle(Color.White, 16.sp)
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "LiquidMessageDialog"
        const val DEFAULT_REQUEST_KEY = "liquid_message_dialog_request_key"
        const val RESULT_CONFIRMED = "liquid_message_dialog_result_confirmed"
        const val RESULT_TITLE = "liquid_message_dialog_result_title"
        const val RESULT_MESSAGE = "liquid_message_dialog_result_message"

        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_POSITIVE_TEXT = "arg_positive_text"
        private const val ARG_NEGATIVE_TEXT = "arg_negative_text"
        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val ARG_CANCELABLE = "arg_cancelable"

        fun newInstance(
            title: String,
            message: String,
            positiveText: String = "确定",
            negativeText: String = "取消",
            requestKey: String = DEFAULT_REQUEST_KEY,
            cancelable: Boolean = true
        ): LiquidMessageDialogFragment {
            return LiquidMessageDialogFragment().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_MESSAGE to message,
                    ARG_POSITIVE_TEXT to positiveText,
                    ARG_NEGATIVE_TEXT to negativeText,
                    ARG_REQUEST_KEY to requestKey,
                    ARG_CANCELABLE to cancelable
                )
            }
        }

        fun show(
            fragmentManager: FragmentManager,
            title: String,
            message: String,
            positiveText: String = "确定",
            negativeText: String = "取消",
            requestKey: String = DEFAULT_REQUEST_KEY,
            cancelable: Boolean = true
        ) {
            newInstance(
                title = title,
                message = message,
                positiveText = positiveText,
                negativeText = negativeText,
                requestKey = requestKey,
                cancelable = cancelable
            ).show(fragmentManager, TAG)
        }
    }
}
