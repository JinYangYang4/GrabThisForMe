package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.post.data.repository.PostRepository
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val postId: String = savedStateHandle.get<String>("postId").orEmpty()

    private val _commentList = MutableLiveData<List<Comment>>(emptyList())
    val commentList: LiveData<List<Comment>> = _commentList

    private val _likeCount = MutableLiveData(0)
    private val _addLike = MutableLiveData(false)
    val addLike: LiveData<Boolean> get() = _addLike
    val likeCount: LiveData<Int> = _likeCount

    private val _commentCount = MutableLiveData(0)
    val commentCount: LiveData<Int> = _commentCount

    private val _inputVisible = MutableLiveData(false)
    val inputVisible: LiveData<Boolean> get() = _inputVisible

    private val _headerCovered = MutableLiveData(false)
    val headerCovered: LiveData<Boolean> get() = _headerCovered

    private val _postUserName = MutableLiveData("Unknown user")
    val postUserName: LiveData<String> get() = _postUserName

    private val _postTimeText = MutableLiveData("")
    val postTimeText: LiveData<String> get() = _postTimeText

    private val _postContentText = MutableLiveData("Loading post...")
    val postContentText: LiveData<String> get() = _postContentText

    private val _postAvatarUrl = MutableLiveData("")
    val postAvatarUrl: LiveData<String> get() = _postAvatarUrl

    private val _postImageList = MutableLiveData<List<String>>(emptyList())
    val postImageList: LiveData<List<String>> get() = _postImageList

    private val _loveIconRes = MutableLiveData(false)
    val loveIconRes: LiveData<Boolean> get() = _loveIconRes

    private val _inputText = MutableLiveData("")
    val inputText: LiveData<String> get() = _inputText

    private val _canSend = MutableLiveData(false)
    val canSend: LiveData<Boolean> get() = _canSend

    private val fallbackUser = User(
        id = 1L,
        name = "User1",
        headPic = ""
    )

    init {
        if (postId.isBlank()) {
            showMissingPostState()
        } else {
            observePost()
            loadInitialComments()
            observeLikeState()
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

        val comment = Comment(
            id = System.currentTimeMillis(),
            time = System.currentTimeMillis(),
            message = message,
            imageUrls = emptyList(),
            commenter = userRepository.currentUser.value ?: fallbackUser,
            replies = emptyList()
        )
        addCommentLocal(comment)
        clearInputText()
        viewModelScope.launch {
            postRepository.addComment(postId, comment)
        }
        return true
    }

    fun submitReply(
        commentPosition: Int,
        parentCommentId: Long,
        beCommenter: User? = null
    ): Boolean {
        val message = _inputText.value.orEmpty().trim()
        if (postId.isBlank() || message.isEmpty()) return false

        val reply = createReply(
            parentCommentId = parentCommentId,
            message = message,
            beCommenter = beCommenter
        )
        addReplyLocal(commentPosition, reply)
        clearInputText()
        viewModelScope.launch {
            postRepository.addReply(postId, parentCommentId, reply)
        }
        return true
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

    private fun observePost() {
        viewModelScope.launch {
            postRepository.getPost(postId).collectLatest { post ->
                renderPost(post)
            }
        }
    }

    private fun loadInitialComments() {
        viewModelScope.launch {
            val comments = postRepository.getCommentListOnce(postId)
            _commentList.value = comments
            _commentCount.value = comments.size
        }
    }

    private fun observeLikeState() {
        viewModelScope.launch {
            postRepository.isPostLiked(postId).collectLatest { liked ->
                _addLike.value = liked
                _loveIconRes.value = liked
            }
        }
    }

    private fun addCommentLocal(comment: Comment) {
        val updatedList = listOf(comment) + _commentList.value.orEmpty()
        _commentList.value = updatedList
        _commentCount.value = updatedList.size
    }

    private fun addReplyLocal(commentPosition: Int, reply: Reply) {
        val currentList = _commentList.value.orEmpty()
        if (commentPosition !in currentList.indices) return

        val targetComment = currentList[commentPosition]
        val updatedComment = targetComment.copy(
            replies = listOf(reply) + targetComment.replies
        )
        val updatedList = currentList.toMutableList()
        updatedList[commentPosition] = updatedComment
        _commentList.value = updatedList
    }

    private fun renderPost(post: Post?) {
        if (post == null) {
            showMissingPostState()
            return
        }
        _postUserName.value = post.authorName.ifBlank { "匿名" }
        _postTimeText.value = formatPostTime(post.createTime)
        _postContentText.value = post.content
        _postAvatarUrl.value = post.authorAvatarUrl
        _postImageList.value = post.images.filter { it.isNotBlank() }
        _likeCount.value = post.likeCount
        if (_commentList.value.isNullOrEmpty()) {
            _commentCount.value = post.commentCount
        }
    }

    private fun showMissingPostState() {
        _postUserName.value = ""
        _postTimeText.value = ""
        _postContentText.value = ""
        _postAvatarUrl.value = ""
        _postImageList.value = emptyList()
        _commentList.value = emptyList()
        _likeCount.value = 0
        _commentCount.value = 0
        _addLike.value = false
        _loveIconRes.value = false
    }

    private fun formatPostTime(createTime: Long): String {
        if (createTime <= 0L) return ""
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return "发布于 ${formatter.format(Date(createTime))}"
    }
}
