package com.example.grabthisforme.activity.fragment_misc.chat_fragment.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.adapter.PhotoPreviewPagerAdapter
import com.example.grabthisforme.databinding.DialogPhotoPreviewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior


class PhotoPreviewDialog : DialogFragment() {
    private var _binding: DialogPhotoPreviewBinding? = null
    private val binding get() = _binding!!
    private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null

    companion object {
        private const val ARG_IMAGE_URIS = "image_uris"
        private const val ARG_INITIAL_INDEX = "initial_index"

        fun newInstance(uri: String): PhotoPreviewDialog {
            return newInstance(listOf(uri))
        }

        fun newInstance(uris: List<String>, initialIndex: Int = 0): PhotoPreviewDialog {
            return PhotoPreviewDialog().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_IMAGE_URIS, ArrayList(uris))
                    putInt(ARG_INITIAL_INDEX, initialIndex)
                }
            }
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
        val imageUris = arguments
            ?.getStringArrayList(ARG_IMAGE_URIS)
            .orEmpty()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (imageUris.isEmpty()) {
            dismissAllowingStateLoss()
            return
        }

        val initialIndex = arguments
            ?.getInt(ARG_INITIAL_INDEX, 0)
            ?.coerceIn(0, imageUris.lastIndex)
            ?: 0

        binding.viewPager.adapter = PhotoPreviewPagerAdapter(imageUris) {
            dismiss()
        }
        binding.viewPager.setCurrentItem(initialIndex, false)
        updateCounter(initialIndex, imageUris.size)

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateCounter(position, imageUris.size)
            }
        }
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback!!)
        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun updateCounter(position: Int, total: Int) {
        binding.tvCounter.text = "${position + 1} / $total"
        binding.tvCounter.visibility = if (total > 1) View.VISIBLE else View.GONE
    }

    @SuppressLint("ResourceAsColor")
    override fun onStart() {
        super.onStart()
        val dialog = dialog ?: return
        val window = dialog.window ?: return
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        val layoutParams = window.attributes
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

    override fun onDestroyView() {
        pageChangeCallback?.let {
            binding.viewPager.unregisterOnPageChangeCallback(it)
        }
        binding.viewPager.adapter = null
        pageChangeCallback = null
        super.onDestroyView()
        _binding = null
    }
}
