package com.example.grabthisforme.model.post.data.network.api

import com.example.grabthisforme.model.network.ApiResponse
import com.example.grabthisforme.model.post.data.network.dto.CreateCommentRequest
import com.example.grabthisforme.model.post.data.network.dto.CreatePostRequest
import com.example.grabthisforme.model.post.data.network.dto.CreateReplyRequest
import com.example.grabthisforme.model.post.data.network.dto.PageResponseDto
import com.example.grabthisforme.model.post.data.network.dto.PostDetailDto
import com.example.grabthisforme.model.post.data.network.dto.PostDto
import com.example.grabthisforme.model.post.data.network.dto.PostCommentDto
import com.example.grabthisforme.model.post.data.network.dto.PostReplyDto
import com.example.grabthisforme.model.post.data.network.dto.SetPostLikedRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi{
    @GET("api/posts")
    suspend fun listPosts(): ApiResponse<List<PostDto>>

    @GET("api/posts/{postId}")
    suspend fun getPost(@Path("postId")postId : String): ApiResponse<PostDetailDto>

    @GET("api/posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ApiResponse<PageResponseDto<PostCommentDto>>

    @GET("api/posts/{postId}/comments/{commentId}/replies")
    suspend fun getReplies(
        @Path("postId") postId: String,
        @Path("commentId") commentId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ApiResponse<PageResponseDto<PostReplyDto>>

    @POST("api/posts")
    suspend fun createPost(@Body request: CreatePostRequest): ApiResponse<PostDetailDto>

    @POST("api/posts/{postId}/like")
    suspend fun setPostLiked(
        @Path("postId") postId: String,
        @Body request: SetPostLikedRequest
    ): ApiResponse<Boolean>

    @POST("api/posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body request: CreateCommentRequest
    ): ApiResponse<PostCommentDto>

    @POST("api/posts/{postId}/replies")
    suspend fun addReply(
        @Path("postId") postId: String,
        @Body request: CreateReplyRequest
    ): ApiResponse<PostReplyDto>
}
