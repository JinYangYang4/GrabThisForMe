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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.communityFragment.custom.MaxLinesGridLayoutManager
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.PhotoPreviewDialog
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter.CommentRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui_model.PostDetailHeaderUiModel
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui_model.PostDetailStatsUiModel
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.viewModel.PostDetailViewModel
import com.example.grabthisforme.activity.fragment_misc.post_topic.adapter.ImagesRecyclerviewAdapter
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentPostDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostDetailFragment : Fragment() {
    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostDetailViewModel by viewModels()

    private var theCommentPosition: Int = -1
    private var theParentCommentId: Long = -1
    private var theBeCommenterId: Long = -1
    private var theParentReplyId: Long? = null

    private lateinit var commentAdapter: CommentRecyclerViewAdapter
    private lateinit var imagesAdapter: ImagesRecyclerviewAdapter

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

        initRecyclerViews()
        initInput()
        initObserve()
        initClickListeners()
        initScrollListener()

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

    private fun initRecyclerViews() {
        imagesAdapter = ImagesRecyclerviewAdapter { position ->
            val imageUris = viewModel.postHeaderUiModel.value?.imageUrls.orEmpty()
            if (imageUris.isEmpty()) return@ImagesRecyclerviewAdapter
            val initialIndex = position.coerceIn(0, imageUris.lastIndex)
            PhotoPreviewDialog
                .newInstance(imageUris, initialIndex)
                .show(childFragmentManager, "PhotoPreviewDialog")
        }
        binding.rvImagesGrid.apply {
            layoutManager = MaxLinesGridLayoutManager(context, 3, 4)
            adapter = imagesAdapter
        }

        commentAdapter = CommentRecyclerViewAdapter(
            onItemClick = { comment, position, commentId ->
                theCommentPosition = position
                theParentCommentId = commentId
                theBeCommenterId = comment.commenter?.id ?: -1L
                theParentReplyId = null
                inputActionType = InputActionType.REPLY_COMMENT
                viewModel.setInputVisible(true)
                showKeyboard(binding.etMessageInput)
            },
            onReplyItemClick = { reply, position, commentId ->
                theCommentPosition = position
                theParentCommentId = commentId
                theBeCommenterId = reply.commenter?.id ?: -1L
                theParentReplyId = reply.id
                inputActionType = InputActionType.REPLY_COMMENT
                viewModel.setInputVisible(true)
                binding.etMessageInput.setText("")
                viewModel.updateInputText("")
                showKeyboard(binding.etMessageInput)
            },
            onLoadMoreReply = { comment, _, visibleReplyCount ->
                viewModel.loadReplies(comment.id, visibleReplyCount)
            }
        )
        binding.rvComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
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

    private fun initClickListeners() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.llComment.setOnClickListener {
            inputActionType = InputActionType.POST_COMMENT
            resetReplyTarget()
            viewModel.setInputVisible(true)
            showKeyboard(binding.etMessageInput)
        }
        binding.tvSend.setOnClickListener {
            val sent = if (inputActionType == InputActionType.POST_COMMENT) {
                viewModel.submitComment()
            } else {
                viewModel.submitReply(
                    commentPosition = theCommentPosition,
                    parentCommentId = theParentCommentId,
                    beCommenterId = theBeCommenterId,
                    parentReplyId = theParentReplyId
                )
            }

            if (sent) {
                if (inputActionType == InputActionType.REPLY_COMMENT) {
                    commentAdapter.expandComment(theParentCommentId)
                } else {
                    scrollToComments()
                }
                resetReplyTarget()
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

        binding.tvSend.isEnabled = false
        binding.tvSend.alpha = 0.5f
    }

    private fun initScrollListener() {
        binding.nsvContent.setOnScrollChangeListener { _, _, _, _, scrollY ->
            val threshold = dp2px(60)
            val isCovered = scrollY >= threshold
            viewModel.setHeaderCovered(isCovered)

            val statsTopInScroll = binding.llStats.top - scrollY
            val shouldPin = statsTopInScroll <= 0
            binding.llPinnedStats.visibility = if (shouldPin) View.VISIBLE else View.GONE
            maybeLoadMoreComments()
        }
    }

    private fun scrollToComments() {
        binding.nsvContent.postDelayed({
            val targetY = binding.llStats.top
            binding.nsvContent.smoothScrollTo(0, targetY)
        }, 100)
    }

    private fun initObserve() {
        viewModel.commentList.observe(viewLifecycleOwner) { list ->
            commentAdapter.submitList(list) {
                binding.nsvContent.post {
                    maybeLoadMoreComments()
                }
            }
        }

        viewModel.postHeaderUiModel.observe(viewLifecycleOwner) { header ->
            renderPostHeader(header)
        }
        viewModel.postStatsUiModel.observe(viewLifecycleOwner) { stats ->
            renderPostStats(stats)
        }

        viewModel.canSend.observe(viewLifecycleOwner) { enabled ->
            binding.tvSend.isEnabled = enabled
            binding.tvSend.alpha = if (enabled) 1f else 0.5f
        }

        viewModel.loveIconRes.observe(viewLifecycleOwner) { isLove ->
            binding.ivLove.setImageResource(
                if (isLove) R.drawable.ic_love_selected else R.drawable.ic_unselected
            )
        }
    }

    private fun renderPostHeader(header: PostDetailHeaderUiModel) {
        binding.tvTopUsername.text = header.authorName
        binding.tvPostUsername.text = header.authorName
        binding.tvPostTime.text = header.timeText
        binding.tvPostContent.text = header.contentText

        Glide.with(this)
            .load(header.authorAvatarUrl)
            .placeholder(R.drawable.cat)
            .error(R.drawable.cat)
            .into(binding.ivPostAvatar)
        Glide.with(this)
            .load(header.authorAvatarUrl)
            .placeholder(R.drawable.cat)
            .error(R.drawable.cat)
            .into(binding.ivTopAvatar)

        val hasImages = header.imageUrls.isNotEmpty()
        binding.flImagesContainer.visibility = if (hasImages) View.VISIBLE else View.GONE
        if (hasImages) {
            val hiddenCount = (header.imageUrls.size - MAX_IMAGE_COUNT).coerceAtLeast(0)
            imagesAdapter.submitImages(header.imageUrls.take(MAX_IMAGE_COUNT), hiddenCount)
        } else {
            imagesAdapter.submitImages(emptyList(), 0)
        }
    }

    private fun renderPostStats(stats: PostDetailStatsUiModel) {
        binding.tvCommentCount.text = stats.commentText
        binding.tvLikeCountTop.text = stats.likeText
        binding.tvPinnedCommentCount.text = stats.commentText
        binding.tvPinnedLikeCount.text = stats.likeText
        binding.tvLoveCount.text = stats.likeCount.toString()
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

    private fun resetReplyTarget() {
        theCommentPosition = -1
        theParentCommentId = -1
        theBeCommenterId = -1
        theParentReplyId = null
    }

    private fun dp2px(dp: Int): Int {
        val density = requireContext().resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    private fun maybeLoadMoreComments() {
        val contentView = binding.nsvContent.getChildAt(0) ?: return
        val remainingHeight = contentView.bottom - (binding.nsvContent.height + binding.nsvContent.scrollY)
        if (remainingHeight <= dp2px(120)) {
            viewModel.loadMoreComments()
        }
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
