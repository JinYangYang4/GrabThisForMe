package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.mock.PostDetailMockData
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.user.domain.User

class PostDetailViewModel : ViewModel() {
    private val _commentList = MutableLiveData<List<Comment>>(emptyList())
    val commentList: LiveData<List<Comment>> = _commentList

    private val _likeCount = MutableLiveData(0)
    private val _addLike = MutableLiveData(false)
    val addLike: LiveData<Boolean> get() = _addLike
    val likeCount: LiveData<Int> = _likeCount

    private val _inputVisible = MutableLiveData(false)
    val inputVisible: LiveData<Boolean> get() = _inputVisible

    private val _headerCovered = MutableLiveData(false)
    val headerCovered: LiveData<Boolean> get() = _headerCovered

    private val _postUserName = MutableLiveData("DigitalCreator_Li")
    val postUserName: LiveData<String> get() = _postUserName

    private val _postTimeText = MutableLiveData("2 hours ago - Posted in second-hand digital community")
    val postTimeText: LiveData<String> get() = _postTimeText

    private val _postContentText = MutableLiveData(
        "The used tablet arrived today. It is in near-new condition, battery life is great, and it handles streaming all day without issues."
    )
    val postContentText: LiveData<String> get() = _postContentText

    private val _loveIconRes = MutableLiveData(false)
    val loveIconRes: LiveData<Boolean> get() = _loveIconRes

    private val _inputText = MutableLiveData("")
    val inputText: LiveData<String> get() = _inputText

    private val currentUser = User(
        id = 1L,
        name = "User1",
        headPic = ""
    )

    init {
        _commentList.value = PostDetailMockData.getMockCommentList()
    }

    fun addLike() {
        val current = _likeCount.value ?: 0
        _likeCount.value = current + 1
        _addLike.value = true
        _loveIconRes.value = true
    }

    fun removeLike() {
        val current = _likeCount.value ?: 0
        if (current > 0) {
            _likeCount.value = current - 1
            _addLike.value = false
            _loveIconRes.value = false
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
    }

    fun clearInputText() {
        _inputText.value = ""
    }

    fun addComment(comment: Comment) {
        val list = _commentList.value.orEmpty().toMutableList()
        list.add(0, comment)
        _commentList.value = list
    }

    fun addReplyToComment(commentPosition: Int, reply: Reply) {
        val currentList = _commentList.value.orEmpty()
        if (commentPosition < 0 || commentPosition >= currentList.size) {
            return
        }

        val oldComment = currentList[commentPosition]
        val newReplies = oldComment.replies.toMutableList()
        newReplies.add(0, reply)

        val newComment = oldComment.copy(
            replies = newReplies
        )

        val newList = currentList.toMutableList()
        newList[commentPosition] = newComment
        _commentList.value = newList
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
            commenter = currentUser,
            beCommenter = beCommenter,
            imageUrls = emptyList(),
            parentCommentId = parentCommentId,
            parentReplyId = parentReplyId
        )
    }
}
