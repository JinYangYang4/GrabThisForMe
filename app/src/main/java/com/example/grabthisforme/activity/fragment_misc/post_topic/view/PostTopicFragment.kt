package com.example.grabthisforme.activity.fragment_misc.post_topic.view

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.BottomSheetDialogPhoto
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.PhotoPreviewDialog
import com.example.grabthisforme.activity.fragment_misc.post_topic.adapter.ImagesRecyclerviewAdapter
import com.example.grabthisforme.activity.fragment_misc.post_topic.viewmodel.PostTopicActionType
import com.example.grabthisforme.activity.fragment_misc.post_topic.viewmodel.PostTopicViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentCreatePostBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostTopicFragment : Fragment() {
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostTopicViewModel by viewModels()
    private lateinit var imagesAdapter: ImagesRecyclerviewAdapter
    private var nestedScrollBaseBottomPadding = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeState()
        nestedScrollBaseBottomPadding = binding.nestedScrollView.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val systemBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val keyboardSpace = (imeBottom - systemBottom).coerceAtLeast(0)
            binding.nestedScrollView.setPadding(
                binding.nestedScrollView.paddingLeft,
                binding.nestedScrollView.paddingTop,
                binding.nestedScrollView.paddingRight,
                nestedScrollBaseBottomPadding + keyboardSpace
            )
            if (imeVisible && _binding != null) {
                binding.nestedScrollView.postDelayed({
                    scrollFocusedInputIntoView()
                }, KEYBOARD_SCROLL_DELAY_MS)
            }
            if (!imeVisible && _binding != null) {
                clearInputFocus()
            }
            insets
        }
    }

    private fun initView() {
        initImagesRV()
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.ivPostPic.setOnClickListener {
            showPhotoSelector()
        }
        binding.btnMySavedPostDraft.setOnClickListener {
            val draft = viewModel.getCurrentDraft()
            if (draft.content.isBlank() && draft.images.isEmpty()) {
                Toast.makeText(requireContext(), "当前还没有草稿内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            focusEditor()
        }
        binding.btnSavePostDraft.setOnClickListener {
            clearInputFocus()
            viewModel.saveDraft()
        }
        binding.btnPublishPost.setOnClickListener {
            clearInputFocus()
            viewModel.publishPost()
        }
        binding.itPostContent.doAfterTextChanged { editable ->
            viewModel.updateContent(editable?.toString().orEmpty())
            if (binding.itPostContent.hasFocus()) {
                binding.nestedScrollView.post {
                    scrollFocusedInputIntoView()
                }
            }
        }
        binding.itPostContent.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.nestedScrollView.postDelayed({
                    scrollFocusedInputIntoView()
                }, KEYBOARD_SCROLL_DELAY_MS)
            }
        }
        binding.llNested.setOnClickListener {
            clearInputFocus()
        }
        binding.cardEditor.setOnClickListener {
            focusEditor()
        }
        binding.tilPostContent.setOnClickListener {
            focusEditor()
        }
        binding.root.setOnClickListener {
            clearInputFocus()
        }
    }

    private fun initImagesRV() {
        imagesAdapter = ImagesRecyclerviewAdapter { position ->
            val imageUris = viewModel.selectedImages.value
                .orEmpty()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            if (imageUris.isEmpty()) return@ImagesRecyclerviewAdapter
            val initialIndex = position.coerceIn(0, imageUris.lastIndex)
            PhotoPreviewDialog
                .newInstance(imageUris, initialIndex)
                .show(childFragmentManager, "PhotoPreviewDialog")
        }
        binding.rvImages.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = imagesAdapter
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
        }
    }

    private fun observeState() {
        viewModel.selectedImages.observe(viewLifecycleOwner) { images ->
            if (images.isNotEmpty()) {
                binding.rvImages.visibility = View.VISIBLE
                imagesAdapter.submitImages(images, 0)
            } else {
                binding.rvImages.visibility = View.GONE
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            if (result.success && result.actionType == PostTopicActionType.PUBLISHED) {
                parentFragmentManager.popBackStack()
            }
        }

        viewModel.contentText.observe(viewLifecycleOwner) { content ->
            if (binding.itPostContent.text?.toString().orEmpty() != content) {
                binding.itPostContent.setText(content)
                binding.itPostContent.setSelection(content.length)
            }
        }
    }

    private fun showPhotoSelector() {
        val photoBottomSheet = BottomSheetDialogPhoto.newInstance(BottomSheetDialogPhoto.SELECT_UNLIMIT)
        photoBottomSheet.setOnPhotosSelectedListener(object : BottomSheetDialogPhoto.OnPhotosSelectedListener {
            override fun onPhotosSelected(photos: List<Uri>) {
                val imageUris = photos.map { it.toString() }
                if (imageUris.isEmpty()) return
                viewModel.updateSelectedImages(imageUris)
            }
        })
        photoBottomSheet.show(childFragmentManager, "PostTopicPhotoBottomSheet")
    }

    private fun focusEditor() {
        binding.itPostContent.requestFocus()
        binding.itPostContent.setSelection(binding.itPostContent.text?.length ?: 0)
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.itPostContent, InputMethodManager.SHOW_IMPLICIT)
        binding.nestedScrollView.postDelayed({
            scrollFocusedInputIntoView()
        }, KEYBOARD_SCROLL_DELAY_MS)
    }

    private fun scrollFocusedInputIntoView() {
        val currentFocus = requireActivity().currentFocus ?: return
        val scrollView = binding.nestedScrollView
        if (!isDescendantOf(currentFocus, scrollView)) return

        val focusedRect = when (currentFocus) {
            binding.itPostContent -> buildCursorRect(binding.itPostContent)
            else -> Rect().also { currentFocus.getDrawingRect(it) }
        }
        scrollView.offsetDescendantRectToMyCoords(currentFocus, focusedRect)

        val visibleTop = scrollView.scrollY
        val visibleBottom = visibleTop + scrollView.height - scrollView.paddingBottom
        val extraSpacing = KEYBOARD_FOCUS_SPACING_DP.dpToPx()

        when {
            focusedRect.bottom + extraSpacing > visibleBottom -> {
                val targetY = focusedRect.bottom - scrollView.height + scrollView.paddingBottom + extraSpacing
                scrollView.smoothScrollTo(0, targetY.coerceAtLeast(0))
            }
            focusedRect.top - extraSpacing < visibleTop -> {
                scrollView.smoothScrollTo(0, (focusedRect.top - extraSpacing).coerceAtLeast(0))
            }
        }
    }

    private fun buildCursorRect(textView: TextView): Rect {
        val layout = textView.layout ?: return Rect().also { textView.getDrawingRect(it) }
        val textLength = textView.text?.length ?: 0
        val selection = textView.selectionStart.coerceIn(0, textLength)
        val line = layout.getLineForOffset(selection)
        val horizontal = layout.getPrimaryHorizontal(selection).toInt()
        val halfWidth = CARET_TARGET_HALF_WIDTH_DP.dpToPx()

        return Rect(
            textView.totalPaddingLeft + horizontal - halfWidth,
            textView.totalPaddingTop + layout.getLineTop(line) - textView.scrollY,
            textView.totalPaddingLeft + horizontal + halfWidth,
            textView.totalPaddingTop + layout.getLineBottom(line) - textView.scrollY
        )
    }

    private fun isDescendantOf(child: View, parent: View): Boolean {
        var current: View? = child
        while (current != null) {
            if (current == parent) return true
            current = current.parent as? View
        }
        return false
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun clearInputFocus() {
        val currentFocus = requireActivity().currentFocus ?: return
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        currentFocus.clearFocus()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEYBOARD_SCROLL_DELAY_MS = 120L
        private const val KEYBOARD_FOCUS_SPACING_DP = 24
        private const val CARET_TARGET_HALF_WIDTH_DP = 12
    }
}
