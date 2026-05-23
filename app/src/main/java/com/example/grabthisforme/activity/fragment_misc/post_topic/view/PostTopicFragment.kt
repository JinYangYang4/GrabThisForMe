package com.example.grabthisforme.activity.fragment_misc.post_topic.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
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
    private lateinit var imagesAdapter : ImagesRecyclerviewAdapter

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
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
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
        }
        binding.llNested.setOnClickListener {
            clearInputFocus()
        }
        binding.tilPostContent.setOnClickListener {
            binding.itPostContent.requestFocus()
            val inputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(binding.itPostContent, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.root.setOnClickListener {
            clearInputFocus()
        }
    }
    private fun initImagesRV(){
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
            if (images.size>0){
                binding.rvImages.visibility = View.VISIBLE
                imagesAdapter.submitList(images)
            }else{
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
}
