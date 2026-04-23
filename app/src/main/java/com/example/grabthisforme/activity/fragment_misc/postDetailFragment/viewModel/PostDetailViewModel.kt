package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.model.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.model.Reply
import com.example.grabthisforme.model.user.User

class PostDetailViewModel : ViewModel() {
    private val _commentList = MutableLiveData<MutableList<Comment>>(mutableListOf())
    val commentList: LiveData<MutableList<Comment>> = _commentList
    private val _likeCount = MutableLiveData<Int>(0)
    private val _addLike = MutableLiveData<Boolean>(false)
    val addLike : LiveData<Boolean> get() = _addLike
    val likeCount: LiveData<Int> = _likeCount
    private val _inputVisible = MutableLiveData(false)
    val inputVisible: LiveData<Boolean> get() = _inputVisible
    private val _headerCovered = MutableLiveData(false)
    val headerCovered: LiveData<Boolean> get() = _headerCovered
    private val _postUserName = MutableLiveData("数码达人_小李")
    val postUserName: LiveData<String> get() = _postUserName
    private val _postTimeText = MutableLiveData("2小时前 发布于 二手数码社区")
    val postTimeText: LiveData<String> get() = _postTimeText
    private val _postContentText = MutableLiveData("新买的二手平板到了，成色99新，续航超给力，追剧一整天无压力，分享给有需要的小伙伴~")
    val postContentText: LiveData<String> get() = _postContentText
    private val _loveIconRes = MutableLiveData(false)
    val loveIconRes: LiveData<Boolean> get() = _loveIconRes
    private val _inputText = MutableLiveData("")
    val inputText: LiveData<String> get() = _inputText
    init {
        _commentList.value = Comment.MockDataUtils.getMockCommentList()
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
        val list = _commentList.value ?: mutableListOf()
        list.add(0,comment)
        _commentList.value = list
    }
    fun addReplyToComment(commentPosition: Int, reply: Reply) {
        val currentList = _commentList.value ?: mutableListOf()
        if (commentPosition < 0 || commentPosition >= currentList.size) {
            return
        }

        val oldComment = currentList[commentPosition]
        val newReplies = oldComment.replies?.toMutableList() ?: mutableListOf()
        newReplies.add(0,reply)

        val newComment = oldComment.copy(
            replies = newReplies,
            page = oldComment.page + 1,
            isExpanded = true
        )


        val newList = currentList.toMutableList()
        newList[commentPosition] = newComment

        _commentList.postValue(newList)
    }
    fun createReply(
        parentCommentId: Long,
        message: String,
        beCommenter: User? = null,
        parentReplyId: Long? = null
    ): Reply {
        val currentUser = User(id = 1, name = "用户1", headPic = "")
        return Reply(
            id = System.currentTimeMillis(), // 用时间戳作为唯一ID
            time = System.currentTimeMillis(), // 回复时间
            message = message.trim(), // 回复内容（去除首尾空格）
            commenter = currentUser, // 回复发布者
            beCommenter = beCommenter, // 被回复的用户
            imageUrls = mutableListOf(), // 回复图片（默认空列表）
            parentCommentId = parentCommentId, // 关联的评论ID（核心）
            parentReplyId = parentReplyId // 关联的父回复ID（二级回复用）
        )
    }
}