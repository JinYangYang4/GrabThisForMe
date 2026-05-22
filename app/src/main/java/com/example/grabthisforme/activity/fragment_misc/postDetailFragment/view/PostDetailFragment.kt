package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.view

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.communityFragment.custom.MaxLinesGridLayoutManager
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter.CommentRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.viewModel.PostDetailViewModel
import com.example.grabthisforme.activity.fragment_misc.post_topic.adapter.ImagesRecyclerviewAdapter
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentPostDetailBinding
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostDetailFragment : Fragment() {
    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostDetailViewModel by viewModels()

    private var theCommentPosition: Int = -1
    private var theParentCommentId: Long = -1
    private lateinit var commentAdapter: CommentRecyclerViewAdapter
    private lateinit var postImagesAdapter: ImagesRecyclerviewAdapter

    enum class InputActionType {
        POST_COMMENT,
        REPLY_COMMENT,
    }

    private var inputActionType: InputActionType = InputActionType.POST_COMMENT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        initView()
        initRecyclerViewComment()
        initInput()
        initObserve()
        initClickListeners()
        initAppBarOffsetListener()

        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            viewModel.setInputVisible(imeVisible)
            insets
        }
        ViewCompat.setWindowInsetsAnimationCallback(
            binding.llInput,
            object : WindowInsetsAnimationCompat.Callback(
                DISPATCH_MODE_CONTINUE_ON_SUBTREE
            ) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
                    binding.llInput.translationY = -imeInsets.bottom.toFloat()
                    return insets
                }
            }
        )
    }

    private fun initView() {
        postImagesAdapter = ImagesRecyclerviewAdapter { }
        binding.rvPostImages.apply {
            adapter = postImagesAdapter
            layoutManager = MaxLinesGridLayoutManager(requireContext(), 3, 4)
            isNestedScrollingEnabled = false
        }
        binding.tvSend.isEnabled = false
        binding.tvSend.alpha = 0.5f
    }

    private fun initRecyclerViewComment() {
        commentAdapter = CommentRecyclerViewAdapter(
            onItemClick = { _, position, commentId ->
                theCommentPosition = position
                theParentCommentId = commentId
                inputActionType = InputActionType.REPLY_COMMENT
                viewModel.setInputVisible(true)
                showKeyboard(binding.etMessageInput)
            },
            scrollListener = object : CommentRecyclerViewAdapter.OnCommentScrollListener {
                override fun onCommentCollapse(position: Int) {
                    binding.rvComments.smoothScrollToPosition(position)
                }
            },
            onReplyItemClick = { reply, position, commentId ->
                theCommentPosition = position
                theParentCommentId = commentId
                inputActionType = InputActionType.REPLY_COMMENT
                viewModel.setInputVisible(true)
                binding.etMessageInput.setText("")
                viewModel.updateInputText("")
                showKeyboard(binding.etMessageInput)
            }
        )
        binding.rvComments.adapter = commentAdapter
        binding.rvComments.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        commentAdapter.submitList(viewModel.commentList.value)
    }

    private fun initInput() {
        binding.etMessageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateInputText(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun initAppBarOffsetListener() {
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val offsetAbs = kotlin.math.abs(verticalOffset)
            val avatarAreaHeight = dp2px(60)
            val isAvatarCovered = offsetAbs >= avatarAreaHeight
            val alpha = kotlin.math.min(
                kotlin.math.abs(verticalOffset.toFloat() - dp2px(60).toFloat()) / dp2px(60),
                1f
            )
            viewModel.setHeaderCovered(isAvatarCovered)
            if (isAvatarCovered) {
                binding.llHead.alpha = alpha
            } else {
                binding.tvPageTitle.alpha = alpha
            }
        })
    }

    private fun dp2px(dp: Int): Int {
        val density = requireContext().resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    private fun initClickListeners() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.llComment.setOnClickListener {
            inputActionType = InputActionType.POST_COMMENT
            viewModel.setInputVisible(true)
            showKeyboard(binding.etMessageInput)
        }
        binding.tvSend.setOnClickListener {
            val sent = if (inputActionType == InputActionType.POST_COMMENT) {
                viewModel.submitComment()
            } else {
                viewModel.submitReply(theCommentPosition, theParentCommentId)
            }

            if (sent) {
                if (inputActionType == InputActionType.REPLY_COMMENT) {
                    commentAdapter.expandComment(theParentCommentId)
                } else {
                    scrollCommentsToTop()
                }
                binding.etMessageInput.clearFocus()
                hideKeyboard()
            }
        }
        binding.llLove.setOnClickListener {
            viewModel.toggleLike()
        }
        binding.llShare.setOnClickListener {
            val shareBottomSheet = PostShareBottomSheetDialogFragment.newInstance()
            shareBottomSheet.show(childFragmentManager, "PostShareBottomSheet")
        }
    }

    private fun scrollCommentsToTop() {
        lifecycleScope.launch {
            delay(100)
            val layoutManager = binding.rvComments.layoutManager as LinearLayoutManager
            val smoothScroller = object : LinearSmoothScroller(requireContext()) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }
            }
            smoothScroller.targetPosition = 0
            layoutManager.startSmoothScroll(smoothScroller)
        }
    }

    private fun initObserve() {
        viewModel.commentList.observe(viewLifecycleOwner) { list ->
            commentAdapter.submitList(list)
            binding.tvCommentCount.text = "评论 ${viewModel.commentCount.value ?: list.size}"
        }
        viewModel.commentCount.observe(viewLifecycleOwner) { count ->
            binding.tvCommentCount.text = "评论 $count"
        }
        viewModel.canSend.observe(viewLifecycleOwner) { enabled ->
            binding.tvSend.isEnabled = enabled
            binding.tvSend.alpha = if (enabled) 1f else 0.5f
        }
        viewModel.loveIconRes.observe(viewLifecycleOwner) { isLove ->
            if (isLove) {
                binding.ivLove.setImageResource(R.drawable.ic_love_selected)
            } else {
                binding.ivLove.setImageResource(R.drawable.ic_unselected)
            }
        }
        viewModel.postAvatarUrl.observe(viewLifecycleOwner) { avatarUrl ->
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivPostAvatar)
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivTopAvatar)
        }
        viewModel.postImageList.observe(viewLifecycleOwner) { images ->
            val visibleImages = images.take(MAX_IMAGE_COUNT)
            val hiddenCount = images.size - visibleImages.size
            binding.rvPostImages.visibility =
                if (visibleImages.isEmpty()) View.GONE else View.VISIBLE
            postImagesAdapter.submitImages(visibleImages, hiddenCount)
        }
    }

    private fun showKeyboard(view: View) {
        val context = view.context ?: return
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
        viewModel.setInputVisible(false)
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
        private const val MAX_IMAGE_COUNT = 12
    }
}
