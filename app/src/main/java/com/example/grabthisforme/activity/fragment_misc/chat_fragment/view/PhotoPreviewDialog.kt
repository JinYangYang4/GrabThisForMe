package com.example.grabthisforme.activity.fragment_misc.chat_fragment.view

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.DialogPhotoPreviewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior


class PhotoPreviewDialog : DialogFragment() {
    private var _binding: DialogPhotoPreviewBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null

    companion object {
        private const val ARG_IMAGE_URI = "image_uri"

        fun newInstance(uri: String): PhotoPreviewDialog {
            val dialog = PhotoPreviewDialog()
            val args = Bundle()
            args.putString(ARG_IMAGE_URI, uri)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetStyle_black)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogPhotoPreviewBinding.inflate(inflater, container, false)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageUri = Uri.parse(arguments?.getString(ARG_IMAGE_URI))
        Glide.with(this)
            .load(imageUri)
            .fitCenter()
            .into(binding.ivPreview)


        binding.ivPreview.setOnClickListener { dismiss() }
        binding.btnClose.setOnClickListener { dismiss() }
    }
    @SuppressLint("ResourceAsColor")
    override fun onStart() {
        super.onStart()
        val dialog = getDialog()
        if (dialog != null && dialog.window != null) {
            val window = dialog.window
            window!!.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            val layoutParams = window!!.attributes
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            window.attributes = layoutParams
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.peekHeight = WindowManager.LayoutParams.MATCH_PARENT
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}