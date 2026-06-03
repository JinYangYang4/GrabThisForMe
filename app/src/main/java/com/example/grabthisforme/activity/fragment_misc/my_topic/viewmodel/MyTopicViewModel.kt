package com.example.grabthisforme.activity.fragment_misc.my_topic.viewmodel

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
class MyTopicViewModel @Inject constructor(
    postRepository: PostRepository
) : ViewModel() {
    val selfPosts: LiveData<List<PostCardUiModel>> = postRepository.myPostList
        .map { posts -> posts.map { it.toPostCardUiModel() } }
        .asLiveData()
}
