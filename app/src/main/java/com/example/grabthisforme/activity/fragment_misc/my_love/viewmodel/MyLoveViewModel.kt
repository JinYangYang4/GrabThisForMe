package com.example.grabthisforme.activity.fragment_misc.my_love.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.ViewModel
import com.example.grabthisforme.activity.communityFragment.ui_model.PostCardUiModel
import com.example.grabthisforme.activity.communityFragment.ui_model.toPostCardUiModel
import com.example.grabthisforme.model.post.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MyLoveViewModel @Inject constructor(
    postRepository: PostRepository
) : ViewModel() {
    val likedPosts: LiveData<List<PostCardUiModel>> = postRepository.likedPostList
        .map { posts -> posts.map { it.toPostCardUiModel() } }
        .asLiveData()
}
