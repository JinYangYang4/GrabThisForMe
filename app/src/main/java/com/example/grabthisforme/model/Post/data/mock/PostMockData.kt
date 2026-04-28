package com.example.grabthisforme.model.post.data.mock

import com.example.grabthisforme.model.post.data.dto.PostDto
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.post.mapper.toDomain
import com.example.grabthisforme.model.user.domain.User

object PostMockData {

    fun getPostList(): List<Post> {
        val users = listOf(
            createMockUser(30001L, "XiaoMing", "avatar_xiaoming"),
            createMockUser(30002L, "XiaoHong", "avatar_xiaohong"),
            createMockUser(30003L, "AQiang", "avatar_aqiang")
        )

        val now = System.currentTimeMillis()
        val postDtos = listOf(
            PostDto(
                postId = "POST_${now - 100000}",
                content = "Sunny day, feeling good.",
                images = listOf("post_img_1"),
                createTime = now - 10 * 60 * 1000,
                authorId = users[0].id,
                authorName = users[0].name,
                authorAvatarUrl = users[0].headPic,
                likeCount = 12,
                commentCount = 3
            ),
            PostDto(
                postId = "POST_${now - 90000}",
                content = "Finished watching a great movie.",
                images = emptyList(),
                createTime = now - 30 * 60 * 1000,
                authorId = users[1].id,
                authorName = users[1].name,
                authorAvatarUrl = users[1].headPic,
                likeCount = 5,
                commentCount = 1
            ),
            PostDto(
                postId = "POST_${now - 80000}",
                content = "Coding after work, still going strong.",
                images = listOf("post_img_2", "post_img_3"),
                createTime = now - 60 * 60 * 1000,
                authorId = users[2].id,
                authorName = users[2].name,
                authorAvatarUrl = users[2].headPic,
                likeCount = 23,
                commentCount = 8
            ),
            PostDto(
                postId = "POST_${now - 70000}",
                content = "Weekend hike was amazing.",
                images = listOf("post_img_4"),
                createTime = now - 2 * 60 * 60 * 1000,
                authorId = users[0].id,
                authorName = users[0].name,
                authorAvatarUrl = users[0].headPic,
                likeCount = 34,
                commentCount = 6
            ),
            PostDto(
                postId = "POST_${now - 60000}",
                content = "Learning Kotlin feels smoother every day.",
                images = emptyList(),
                createTime = now - 3 * 60 * 60 * 1000,
                authorId = users[2].id,
                authorName = users[2].name,
                authorAvatarUrl = users[2].headPic,
                likeCount = 18,
                commentCount = 4
            ),
            PostDto(
                postId = "POST_${now - 50000}",
                content = "Sharing today's sky photo.",
                images = listOf("post_img_5"),
                createTime = now - 5 * 60 * 60 * 1000,
                authorId = users[1].id,
                authorName = users[1].name,
                authorAvatarUrl = users[1].headPic,
                likeCount = 42,
                commentCount = 11
            )
        )

        return postDtos.map { it.toDomain() }
    }

    private fun createMockUser(
        userId: Long,
        userName: String,
        avatar: String
    ): User {
        return User(
            id = userId,
            name = userName,
            headPic = avatar
        )
    }
}
