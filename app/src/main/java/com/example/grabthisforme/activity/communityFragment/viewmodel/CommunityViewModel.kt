package com.example.grabthisforme.activity.communityFragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.post.data.repository.PostRepository
import com.example.grabthisforme.model.post.domain.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    private val _postList = MutableLiveData<List<Post>>(emptyList())
    val postList: LiveData<List<Post>> get() = _postList

    init {
        viewModelScope.launch {
            postRepository.allPostList.collectLatest { posts ->
                _postList.value = posts
            }
        }
    }
}
