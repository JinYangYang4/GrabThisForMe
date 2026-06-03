package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.mock

import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.dto.CommentDto
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.dto.ReplyDto
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.mapper.toDomain
import com.example.grabthisforme.model.user.domain.User

object PostDetailMockData {

    fun getMockCommentList(): List<Comment> {
        val mockUser1 = createMockUser(1001L, "程序员小李", "https://xxx.com/avatar1.jpg")
        val mockUser2 = createMockUser(1002L, "零食爱好者", "https://xxx.com/avatar2.jpg")
        val mockUser3 = createMockUser(1003L, "墙王", "https://xxx.com/avatar3.jpg")
        val mockUser4 = createMockUser(1004L, "追剧达人", "https://xxx.com/avatar4.jpg")

        val replyListForComment3 = createMockReplyDtoList(
            targetCommenter = mockUser1,
            replyers = listOf(mockUser2, mockUser3, mockUser4, mockUser1),
            replyCount = 15
        )
        val replyListForComment1 = createMockReplyDtoList(
            targetCommenter = mockUser2,
            replyers = listOf(mockUser3, mockUser1),
            replyCount = 3
        )

        val commentDtos = listOf(
            CommentDto(
                id = 1L,
                time = System.currentTimeMillis() - 3_600_000,
                message = "这个平板看起来不错，你多少钱买的？",
                commenterId = mockUser2.id,
                commenterName = mockUser2.name,
                commenterAvatarUrl = mockUser2.headPic,
                replies = replyListForComment1
            ),
            CommentDto(
                id = 2L,
                time = System.currentTimeMillis() - 1_800_000,
                message = "我也想买同款。",
                commenterId = mockUser3.id,
                commenterName = mockUser3.name,
                commenterAvatarUrl = mockUser3.headPic
            ),
            CommentDto(
                id = 3L,
                time = System.currentTimeMillis() - 600_000,
                message = "我有同款，已经用了半年了。",
                commenterId = mockUser1.id,
                commenterName = mockUser1.name,
                commenterAvatarUrl = mockUser1.headPic,
                replies = replyListForComment3
            ),
            CommentDto(
                id = 4L,
                time = System.currentTimeMillis() - 500_000,
                message = "这款平板流畅度怎么样？",
                commenterId = mockUser4.id,
                commenterName = mockUser4.name,
                commenterAvatarUrl = mockUser4.headPic
            ),
            CommentDto(
                id = 5L,
                time = System.currentTimeMillis() - 400_000,
                message = "求配置：屏幕尺寸、内存等。",
                commenterId = mockUser2.id,
                commenterName = mockUser2.name,
                commenterAvatarUrl = mockUser2.headPic
            ),
            CommentDto(
                id = 6L,
                time = System.currentTimeMillis() - 300_000,
                message = "这款很棒，续航一整天。",
                commenterId = mockUser4.id,
                commenterName = mockUser4.name,
                commenterAvatarUrl = mockUser4.headPic,
                replies = createMockReplyDtoList(
                    targetCommenter = mockUser4,
                    replyers = listOf(mockUser1, mockUser2, mockUser3),
                    replyCount = 8
                )
            ),
            CommentDto(
                id = 7L,
                time = System.currentTimeMillis() - 200_000,
                message = "有人用这个打游戏吗？",
                commenterId = mockUser3.id,
                commenterName = mockUser3.name,
                commenterAvatarUrl = mockUser3.headPic
            ),
            CommentDto(
                id = 8L,
                time = System.currentTimeMillis() - 100_000,
                message = "已经买了，性价比很高。",
                commenterId = mockUser1.id,
                commenterName = mockUser1.name,
                commenterAvatarUrl = mockUser1.headPic,
                replies = createMockReplyDtoList(
                    targetCommenter = mockUser1,
                    replyers = listOf(mockUser2, mockUser4, mockUser3),
                    replyCount = 12
                )
            )
        )

        return commentDtos.map { it.toDomain() }
    }

    private fun createMockReplyDtoList(
        targetCommenter: User,
        replyers: List<User>,
        replyCount: Int
    ): List<ReplyDto> {
        val replyList = mutableListOf<ReplyDto>()
        for (index in 1..replyCount) {
            val randomReplyer = replyers[(Math.random() * replyers.size).toInt()]
            val replyMessage = when (index % 4) {
                0 -> "同意，早该买了。"
                1 -> "回复 @${targetCommenter.name}：我入手价是599。"
                2 -> "我贴了截图，流畅度很棒。"
                3 -> "支不支持手写笔？"
                else -> "挺好的。"
            }
            val replyImageUrls = if (index % 5 == 0) {
                listOf("https://xxx.com/reply_img$index.jpg")
            } else {
                emptyList()
            }
            replyList.add(
                ReplyDto(
                    id = 1000L + index,
                    time = System.currentTimeMillis() - (index * 10_000L),
                    message = replyMessage,
                    commenterId = randomReplyer.id,
                    commenterName = randomReplyer.name,
                    commenterAvatarUrl = randomReplyer.headPic,
                    beCommenterId = targetCommenter.id,
                    beCommenterName = targetCommenter.name,
                    beCommenterAvatarUrl = targetCommenter.headPic,
                    imageUrls = replyImageUrls
                )
            )
        }
        return replyList
    }

    private fun createMockUser(
        userId: Long,
        userName: String,
        headPicUrl: String
    ): User {
        return User(
            id = userId,
            name = userName,
            passwordHash = "",
            headPic = headPicUrl
        )
    }
}
