package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.viewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.LocalSendStatus
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui_model.PostDetailHeaderUiModel
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui_model.PostDetailStatsUiModel
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui_model.buildPostDetailStatsUiModel
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui_model.toPostDetailHeaderUiModel
import com.example.grabthisforme.model.location.data.AmapLocationProvider
import com.example.grabthisforme.model.post.data.repository.PostRepository
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val amapLocationProvider: AmapLocationProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val COMMENT_FETCH_LIMIT = 20
        private const val REPLY_FETCH_LIMIT = 8
    }

    private val postId: String = savedStateHandle.get<String>("postId").orEmpty()

    private val _commentList = MutableLiveData<List<Comment>>(emptyList())
    val commentList: LiveData<List<Comment>> = _commentList

    private val _addLike = MutableLiveData(false)
    val addLike: LiveData<Boolean> get() = _addLike

    private val _postHeaderUiModel = MutableLiveData(PostDetailHeaderUiModel())
    val postHeaderUiModel: LiveData<PostDetailHeaderUiModel> = _postHeaderUiModel

    private val _postStatsUiModel = MutableLiveData(PostDetailStatsUiModel())
    val postStatsUiModel: LiveData<PostDetailStatsUiModel> = _postStatsUiModel

    private val _inputVisible = MutableLiveData(false)
    val inputVisible: LiveData<Boolean> get() = _inputVisible

    private val _headerCovered = MutableLiveData(false)
    val headerCovered: LiveData<Boolean> get() = _headerCovered

    private val _loveIconRes = MutableLiveData(false)
    val loveIconRes: LiveData<Boolean> get() = _loveIconRes

    private val _inputText = MutableLiveData("")
    val inputText: LiveData<String> get() = _inputText

    private val _canSend = MutableLiveData(false)
    val canSend: LiveData<Boolean> get() = _canSend

    private var isLoadingComments = false
    private var commentPagingExhausted = false
    private var cachedCommentProvince: String = ""

    private val fallbackUser = User(
        id = 1L,
        name = "匿名用户",
        headPic = ""
    )

    init {
        if (postId.isBlank()) {
            showMissingPostState()
        } else {
            observePost()
            observeComments()
            loadInitialComments()
            observeLikeState()
            preloadCommentProvince()
        }
    }

    fun toggleLike() {
        if (postId.isBlank()) return
        val targetLiked = !(_addLike.value ?: false)
        viewModelScope.launch {
            postRepository.setPostLiked(postId, targetLiked)
        }
    }

    fun setInputVisible(visible: Boolean) {
        _inputVisible.value = visible
    }

    fun setHeaderCovered(covered: Boolean) {
        _headerCovered.value = covered
    }

    fun updateInputText(value: String) {
        _inputText.value = value
        _canSend.value = value.trim().isNotEmpty()
    }

    fun clearInputText() {
        _inputText.value = ""
        _canSend.value = false
    }

    fun submitComment(): Boolean {
        val message = _inputText.value.orEmpty().trim()
        if (postId.isBlank() || message.isEmpty()) return false

        val pendingComment = Comment(
            id = System.currentTimeMillis(),
            time = System.currentTimeMillis(),
            message = message,
            imageUrls = emptyList(),
            commenter = userRepository.currentUser.value ?: fallbackUser,
            replies = emptyList(),
            replyCount = 0,
            commenterProvince = cachedCommentProvince,
            sendStatus = LocalSendStatus.SENDING
        )
        clearInputText()

        viewModelScope.launch {
            postRepository.addCommentLocal(postId, pendingComment)
            postRepository.addComment(postId, pendingComment)
                .onSuccess { remoteComment ->
                    postRepository.replaceCommentLocal(
                        postId = postId,
                        oldCommentId = pendingComment.id,
                        newComment = remoteComment.copy(sendStatus = LocalSendStatus.SUCCESS)
                    )
                }
                .onFailure {
                    postRepository.updateCommentLocal(
                        postId = postId,
                        comment = pendingComment.copy(sendStatus = LocalSendStatus.FAILED)
                    )
                }
        }
        return true
    }

    fun submitReply(
        commentPosition: Int,
        parentCommentId: Long,
        beCommenterId: Long,
        parentReplyId: Long? = null
    ): Boolean {
        val message = _inputText.value.orEmpty().trim()
        if (postId.isBlank() || message.isEmpty()) return false

        val replyTarget = resolveReplyTarget(
            commentPosition = commentPosition,
            parentCommentId = parentCommentId,
            beCommenterId = beCommenterId,
            parentReplyId = parentReplyId
        ) ?: return false

        val pendingReply = createReply(
            parentCommentId = parentCommentId,
            message = message,
            beCommenter = replyTarget.beCommenter,
            parentReplyId = replyTarget.parentReplyId
        )
        clearInputText()

        viewModelScope.launch {
            postRepository.addReplyLocal(
                postId = postId,
                parentCommentId = parentCommentId,
                reply = pendingReply
            )
            postRepository.addReply(
                postId = postId,
                parentCommentId = parentCommentId,
                reply = pendingReply,
                beCommenterId = replyTarget.beCommenterId
            ).onSuccess { remoteReply ->
                postRepository.replaceReplyLocal(
                    postId = postId,
                    parentCommentId = parentCommentId,
                    oldReplyId = pendingReply.id,
                    newReply = remoteReply.copy(sendStatus = LocalSendStatus.SUCCESS)
                )
            }.onFailure {
                postRepository.updateReplyLocal(
                    postId = postId,
                    parentCommentId = parentCommentId,
                    reply = pendingReply.copy(sendStatus = LocalSendStatus.FAILED)
                )
            }
        }
        return true
    }

    fun loadReplies(commentId: Long, targetVisibleCount: Int) {
        if (postId.isBlank() || commentId <= 0L) return

        viewModelScope.launch {
            var currentComment = _commentList.value.orEmpty().firstOrNull { it.id == commentId } ?: return@launch
            while (currentComment.replies.size < targetVisibleCount ||
                currentComment.replies.size - targetVisibleCount < REPLY_FETCH_LIMIT
            ) {
                val orderedReplies = currentComment.replies.sortedByDescending { it.time }
                val beforeTime = if (orderedReplies.isEmpty()) {
                    System.currentTimeMillis()
                } else {
                    orderedReplies.last().time
                }

                val replies = postRepository.getReplyPage(
                    postId = postId,
                    commentId = commentId,
                    limit = REPLY_FETCH_LIMIT,
                    beforeTime = beforeTime
                )
                if (replies.items.isEmpty()) {
                    break
                }
                currentComment = _commentList.value.orEmpty().firstOrNull { it.id == commentId } ?: break

                if (!replies.hasMore) {
                    break
                }
            }
        }
    }

    fun loadMoreComments() {
        if (postId.isBlank() || isLoadingComments || commentPagingExhausted) return

        viewModelScope.launch {
            isLoadingComments = true
            try {
                val currentComments = _commentList.value.orEmpty()
                val beforeTime = currentComments.lastOrNull()?.time?.minus(1L)
                    ?: (System.currentTimeMillis() + 1L)
                val comments = postRepository.getCommentPage(
                    postId = postId,
                    limit = COMMENT_FETCH_LIMIT,
                    beforeTime = beforeTime
                )
                val previousSize = currentComments.size
                val mergedSize = _commentList.value.orEmpty().size
                val addedCount = mergedSize - previousSize

                if (!comments.hasMore || addedCount <= 0) {
                    commentPagingExhausted = true
                }
            } finally {
                isLoadingComments = false
            }
        }
    }

    fun createReply(
        parentCommentId: Long,
        message: String,
        beCommenter: User? = null,
        parentReplyId: Long? = null
    ): Reply {
        return Reply(
            id = System.currentTimeMillis(),
            time = System.currentTimeMillis(),
            message = message.trim(),
            commenter = userRepository.currentUser.value ?: fallbackUser,
            beCommenter = beCommenter,
            imageUrls = emptyList(),
            parentCommentId = parentCommentId,
            parentReplyId = parentReplyId,
            sendStatus = LocalSendStatus.SENDING
        )
    }

    private fun resolveReplyTarget(
        commentPosition: Int,
        parentCommentId: Long,
        beCommenterId: Long,
        parentReplyId: Long?
    ): ReplyTarget? {
        if (beCommenterId <= 0L) return null

        val targetComment = _commentList.value.orEmpty().getOrNull(commentPosition) ?: return null
        if (targetComment.id != parentCommentId) return null

        return if (parentReplyId == null) {
            val targetUser = targetComment.commenter ?: return null
            if (targetUser.id != beCommenterId) return null
            ReplyTarget(
                beCommenterId = beCommenterId,
                beCommenter = targetUser,
                parentReplyId = null
            )
        } else {
            val targetReply = targetComment.replies.firstOrNull { it.id == parentReplyId } ?: return null
            val targetUser = targetReply.commenter ?: return null
            if (targetUser.id != beCommenterId) return null
            ReplyTarget(
                beCommenterId = beCommenterId,
                beCommenter = targetUser,
                parentReplyId = parentReplyId
            )
        }
    }

    private fun observePost() {
        viewModelScope.launch {
            postRepository.getPost(postId).collectLatest { post ->
                renderPost(post)
            }
        }
    }

    private fun observeComments() {
        viewModelScope.launch {
            postRepository.getCommentList(postId).collectLatest { comments ->
                _commentList.value = comments
                updatePostStats(
                    commentCount = maxOf(comments.size, _postStatsUiModel.value?.commentCount ?: 0)
                )
            }
        }
    }

    private fun loadInitialComments() {
        commentPagingExhausted = false
        loadMoreComments()
    }

    private fun observeLikeState() {
        viewModelScope.launch {
            postRepository.isPostLiked(postId).collectLatest { liked ->
                _addLike.value = liked
                _loveIconRes.value = liked
            }
        }
    }

    private fun preloadCommentProvince() {
        viewModelScope.launch {
            amapLocationProvider.getCurrentLocation()
                .onSuccess { location ->
                    cachedCommentProvince = location.provinceDisplayText
                }
        }
    }

    private fun renderPost(post: Post?) {
        if (post == null) {
            showMissingPostState()
            return
        }
        _postHeaderUiModel.value = post.toPostDetailHeaderUiModel()
        _postStatsUiModel.value = buildPostDetailStatsUiModel(
            likeCount = post.likeCount,
            commentCount = maxOf(post.commentCount, _postStatsUiModel.value?.commentCount ?: 0)
        )
    }

    private fun showMissingPostState() {
        _postHeaderUiModel.value = PostDetailHeaderUiModel()
        _postStatsUiModel.value = PostDetailStatsUiModel()
        _commentList.value = emptyList()
        _addLike.value = false
        _loveIconRes.value = false
    }

    private fun updatePostStats(
        likeCount: Int? = null,
        commentCount: Int? = null
    ) {
        val current = _postStatsUiModel.value ?: PostDetailStatsUiModel()
        _postStatsUiModel.value = buildPostDetailStatsUiModel(
            likeCount = likeCount ?: current.likeCount,
            commentCount = commentCount ?: current.commentCount
        )
    }

    private data class ReplyTarget(
        val beCommenterId: Long,
        val beCommenter: User,
        val parentReplyId: Long?
    )
}
