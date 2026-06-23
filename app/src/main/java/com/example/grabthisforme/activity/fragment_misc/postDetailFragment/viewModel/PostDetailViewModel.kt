package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
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
            commenterProvince = cachedCommentProvince
        )
        addCommentLocal(pendingComment)
        clearInputText()

        viewModelScope.launch {
            postRepository.addComment(postId, pendingComment)
                .onSuccess { remoteComment ->
                    replaceComment(pendingComment.id, remoteComment)
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
        addReplyLocal(commentPosition, pendingReply)
        clearInputText()

        viewModelScope.launch {
            postRepository.addReply(
                postId = postId,
                parentCommentId = parentCommentId,
                reply = pendingReply,
                beCommenterId = replyTarget.beCommenterId
            ).onSuccess { remoteReply ->
                replaceReply(parentCommentId, pendingReply.id, remoteReply)
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

                appendCommentReplies(commentId, replies.items)
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
                Log.d("test11", "loadMoreComments: ${comments.items.size}")

                val previousSize = currentComments.size
                if (comments.items.isNotEmpty()) {
                    appendComments(comments.items)
                }
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
            parentReplyId = parentReplyId
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

    private fun addCommentLocal(comment: Comment) {
        val updatedList = listOf(comment) + _commentList.value.orEmpty()
        _commentList.value = updatedList
        updatePostStats(commentCount = maxOf(updatedList.size, (_postStatsUiModel.value?.commentCount ?: 0) + 1))
    }

    private fun replaceComment(oldCommentId: Long, newComment: Comment) {
        val updatedList = _commentList.value.orEmpty().map { comment ->
            if (comment.id == oldCommentId) newComment else comment
        }
        _commentList.value = updatedList
    }

    private fun addReplyLocal(commentPosition: Int, reply: Reply) {
        val currentList = _commentList.value.orEmpty()
        if (commentPosition !in currentList.indices) return

        val targetComment = currentList[commentPosition]
        val updatedReplies = listOf(reply) + targetComment.replies
        val updatedComment = targetComment.copy(
            replies = updatedReplies,
            replyCount = maxOf(targetComment.replyCount, updatedReplies.size)
        )
        val updatedList = currentList.toMutableList()
        updatedList[commentPosition] = updatedComment
        _commentList.value = updatedList
    }

    private fun replaceReply(parentCommentId: Long, oldReplyId: Long, newReply: Reply) {
        val updatedList = _commentList.value.orEmpty().map { comment ->
            if (comment.id != parentCommentId) {
                comment
            } else {
                val updatedReplies = comment.replies.map { reply ->
                    if (reply.id == oldReplyId) newReply else reply
                }
                comment.copy(
                    replies = updatedReplies,
                    replyCount = maxOf(comment.replyCount, updatedReplies.size)
                )
            }
        }
        _commentList.value = updatedList
    }

    private fun appendCommentReplies(commentId: Long, replies: List<Reply>) {
        val updatedList = _commentList.value.orEmpty().map { comment ->
            if (comment.id != commentId) {
                comment
            } else {
                val mergedReplies = (comment.replies + replies)
                    .distinctBy { it.id }
                    .sortedByDescending { it.time }
                comment.copy(
                    replies = mergedReplies,
                    replyCount = maxOf(comment.replyCount, mergedReplies.size)
                )
            }
        }
        _commentList.value = updatedList
    }

    private fun appendComments(comments: List<Comment>) {
        val mergedComments = (_commentList.value.orEmpty() + comments)
            .distinctBy { it.id }
            .sortedByDescending { it.time }
        _commentList.value = mergedComments
        updatePostStats(commentCount = maxOf(mergedComments.size, _postStatsUiModel.value?.commentCount ?: 0))
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
