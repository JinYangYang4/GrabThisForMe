package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter.CommentRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.model.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.viewModel.PostDetailViewModel
import com.example.grabthisforme.databinding.FragmentPostDetailBinding
import com.example.grabthisforme.model.user.User
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PostDetailFragment : Fragment() {
    private var _binding: FragmentPostDetailBinding? = null
    private lateinit var viewModel : PostDetailViewModel
    private val binding get() = _binding!!
    private var isListenerActive = true
    private var the_commentPosition: Int = -1
    private var the_parentCommentId: Long = -1;
    enum class InputActionType {
        // 发布顶层评论
        POST_COMMENT,
        // 回复评论
        REPLY_COMMENT,
    }
    private var inputActionType: InputActionType = InputActionType.POST_COMMENT
    private var adapter = CommentRecyclerViewAdapter()

    private lateinit var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

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
        viewModel = ViewModelProvider(requireActivity()).get(PostDetailViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        initView()
        initObserve()
        initClickListeners()
        initRecyclerViewComment()
        initAppBarOffsetListener()


        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { view, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            viewModel.setInputVisible(imeVisible)

            insets
        }
        ViewCompat.setWindowInsetsAnimationCallback(
            binding.llInput,
            object : WindowInsetsAnimationCompat.Callback(
                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
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


    fun initRecyclerViewComment() {
        adapter = CommentRecyclerViewAdapter({reply, position, commentId ->
            the_commentPosition=position
            the_parentCommentId = commentId
            inputActionType = InputActionType.REPLY_COMMENT
            viewModel.setInputVisible(true)
            showKeyboard(binding.etMessageInput)
        },
        scrollListener = object : CommentRecyclerViewAdapter.OnCommentScrollListener {
            override fun onCommentCollapse(position: Int) {
                binding.rvComments.smoothScrollToPosition(position)
            }
        },
        onReplyItemClick = {reply, position, commentId ->
            inputActionType = InputActionType.REPLY_COMMENT
            viewModel.setInputVisible(true)
            showKeyboard(binding.etMessageInput)
        }
        )
        binding.rvComments.adapter = adapter
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter.submitList(viewModel.commentList.value)
    }


    private fun initAppBarOffsetListener() {
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val offsetAbs = Math.abs(verticalOffset)

            val avatarAreaHeight = dp2px(60)
            val isAvatarCovered = offsetAbs >= avatarAreaHeight
            val alpha = kotlin.math.min(Math.abs(offsetAbs.toFloat() - dp2px(60).toFloat()) / dp2px(60), 1f)
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
        return (dp * density + 0.5).toInt()
    }

    private fun initView() {
    }

    private fun initClickListeners() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.llComment.setOnClickListener {
            viewModel.setInputVisible(true)
            inputActionType = InputActionType.POST_COMMENT
            showKeyboard(binding.etMessageInput)
        }
        binding.tvSend.setOnClickListener {
            if (inputActionType == InputActionType.POST_COMMENT){
                Log.d("test11", "initClickListeners: ${inputActionType}")
                sendCommentToPostMan()
            }else{
                Log.d("test11", "initClickListeners: ${the_commentPosition}")
                sendReplyToComment(the_commentPosition,the_parentCommentId)
            }

        }
        var already_like = false
        binding.llLove.setOnClickListener {
             if (already_like){
                 viewModel.removeLike()
                 already_like = false
             }else{
                 viewModel.addLike()
                 already_like = true
             }
        }
        binding.llShare.setOnClickListener {
            val shareBottomSheet = PostShareBottomSheetDialogFragment.newInstance()
            shareBottomSheet.show(childFragmentManager, "PostShareBottomSheet")
        }
    }
    fun sendCommentToPostMan(){
        val newComment = Comment(
            id = System.currentTimeMillis(),
            time = System.currentTimeMillis(),
            message = (viewModel.inputText.value ?: "").trim(),
            imageUrls = mutableListOf(),
            commenter = User(id = 1, name = "用户1", headPic = ""),
            replies = mutableListOf(),
            isExpanded = false
        )
        viewModel.addComment(newComment)
        binding.etMessageInput.clearFocus()
        viewModel.clearInputText()
        hideKeyboard()
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
    fun sendReplyToComment(commentPosition: Int, parentCommentId: Long, beCommenter: User? = null) {
        // 1. 获取输入的回复内容
        val replyContent = (viewModel.inputText.value ?: "").trim()
        if (replyContent.isEmpty()) {
            Toast.makeText(requireContext(), "回复内容不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        val newReply = viewModel.createReply(
            parentCommentId = parentCommentId,
            message = replyContent,
            beCommenter = beCommenter
        )

        viewModel.addReplyToComment(commentPosition, newReply)

        binding.etMessageInput.clearFocus()
        viewModel.clearInputText()
        hideKeyboard()


    }
    fun initObserve(){
        viewModel.commentList.observe(viewLifecycleOwner){list ->
            val newList = list.toMutableList()
            adapter.submitList(newList)
            Log.d("test11", "initObserve: ")
        }
        viewModel.likeCount.observe(viewLifecycleOwner){count ->
            Log.d("test11", "initObserve: $count")
        }
        viewModel.loveIconRes.observe(viewLifecycleOwner){ isLove ->
            if (isLove){
                binding.ivLove.setImageResource(R.drawable.ic_love_selected)
            }else{
                binding.ivLove.setImageResource(R.drawable.ic_unselected)
            }
        }
    }

    private fun showKeyboard(view: View) {
        val context = view.context ?: return
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onStop() {
        super.onStop()
        if (::globalLayoutListener.isInitialized) {
            binding.root.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)

        }
        hideKeyboard()
        viewModel.setInputVisible(false)
    }

    override fun onResume() {
        super.onResume()
        isListenerActive = true
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::globalLayoutListener.isInitialized) {
            binding.root.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            isListenerActive = false
        }
        _binding = null
    }
}