package com.example.grabthisforme.activity.communityFragment.viewmodel

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
class CommunityViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    val postList: LiveData<List<PostCardUiModel>> = postRepository.allPostList
        .map { posts -> posts.map { it.toPostCardUiModel() } }
        .asLiveData()
}
