package com.example.grabthisforme.activity.fragment_misc.my_love.viewmodel

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
class MyLoveViewModel @Inject constructor(
    userRepository: UserRepository,
    postRepository: PostRepository
) : ViewModel() {

    private val _likedPosts = MutableLiveData<List<Post>>(emptyList())
    val likedPosts: LiveData<List<Post>> = _likedPosts

    init {
        viewModelScope.launch {
            combine(
                userRepository.currentUser,
                postRepository.allPostList
            ) { currentUser, allPosts ->
                val likedPostIds = currentUser?.likedPostIds.orEmpty().distinct()
                if (likedPostIds.isEmpty()) {
                    emptyList()
                } else {
                    val postMap = allPosts.associateBy { it.postId }
                    likedPostIds.mapNotNull(postMap::get)
                }
            }.collectLatest { likedPosts ->
                _likedPosts.postValue(likedPosts)
            }
        }
    }
}
