package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.mock

import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.dto.CommentDto
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.dto.ReplyDto
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.mapper.toDomain
import com.example.grabthisforme.model.user.domain.User

object PostDetailMockData {

    fun getMockCommentList(): List<Comment> {
        val mockUser1 = createMockUser(1001L, "Coder_Li", "https://xxx.com/avatar1.jpg")
        val mockUser2 = createMockUser(1002L, "Snack_Lover", "https://xxx.com/avatar2.jpg")
        val mockUser3 = createMockUser(1003L, "WallKing", "https://xxx.com/avatar3.jpg")
        val mockUser4 = createMockUser(1004L, "BingeWatcher", "https://xxx.com/avatar4.jpg")

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
                message = "This tablet looks pretty good. How much did you pay for it?",
                commenterId = mockUser2.id,
                commenterName = mockUser2.name,
                commenterAvatarUrl = mockUser2.headPic,
                replies = replyListForComment1
            ),
            CommentDto(
                id = 2L,
                time = System.currentTimeMillis() - 1_800_000,
                message = "I want the same model too.",
                commenterId = mockUser3.id,
                commenterName = mockUser3.name,
                commenterAvatarUrl = mockUser3.headPic
            ),
            CommentDto(
                id = 3L,
                time = System.currentTimeMillis() - 600_000,
                message = "I have the same one. Using it for half a year already.",
                commenterId = mockUser1.id,
                commenterName = mockUser1.name,
                commenterAvatarUrl = mockUser1.headPic,
                replies = replyListForComment3
            ),
            CommentDto(
                id = 4L,
                time = System.currentTimeMillis() - 500_000,
                message = "How is the smoothness of this tablet?",
                commenterId = mockUser4.id,
                commenterName = mockUser4.name,
                commenterAvatarUrl = mockUser4.headPic
            ),
            CommentDto(
                id = 5L,
                time = System.currentTimeMillis() - 400_000,
                message = "Need the specs: screen size, memory, and so on.",
                commenterId = mockUser2.id,
                commenterName = mockUser2.name,
                commenterAvatarUrl = mockUser2.headPic
            ),
            CommentDto(
                id = 6L,
                time = System.currentTimeMillis() - 300_000,
                message = "This one is great. Battery life lasts all day.",
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
                message = "Anyone used this for games?",
                commenterId = mockUser3.id,
                commenterName = mockUser3.name,
                commenterAvatarUrl = mockUser3.headPic
            ),
            CommentDto(
                id = 8L,
                time = System.currentTimeMillis() - 100_000,
                message = "Already bought it. Great value.",
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
                0 -> "I agree. Should have bought it earlier."
                1 -> "Reply @${targetCommenter.name}: my purchase price was 599."
                2 -> "I attached a screenshot. The smoothness is great."
                3 -> "Does this support a stylus?"
                else -> "Pretty good."
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
            headPic = headPicUrl
        )
    }
}
