package com.example.grabthisforme.activity.fragment_misc.my_topic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.post.data.repository.PostRepository
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.user.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyTopicViewModel @Inject constructor(
    userRepository: UserRepository,
    postRepository: PostRepository
) : ViewModel() {

    private val _selfPosts = MutableLiveData<List<Post>>(emptyList())
    val selfPosts: LiveData<List<Post>> = _selfPosts

    init {
        viewModelScope.launch {
            combine(
                userRepository.currentUser,
                postRepository.allPostList
            ) { currentUser, allPosts ->
                val selfPostIds = currentUser?.selfPosts.orEmpty().distinct()
                if (selfPostIds.isEmpty()) {
                    emptyList()
                } else {
                    val postMap = allPosts.associateBy { it.postId }
                    selfPostIds.mapNotNull(postMap::get)
                }
            }.collectLatest { posts ->
                _selfPosts.postValue(posts)
            }
        }
    }
}
