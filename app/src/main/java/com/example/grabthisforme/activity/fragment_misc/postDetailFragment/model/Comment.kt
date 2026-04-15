package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.model

import com.example.grabthisforme.model.user.User

data class Comment(
    val id : Long,
    var time : Long,
    var message : String?= null,
    var imageUrls: MutableList<String>? = null,
    var commenter : User? = null,
    var replies: MutableList<Reply>? = null,
    var isExpanded: Boolean = false,
    var page: Int = 1,
    var hasMoreReply: Boolean = true
) {
    object MockDataUtils {

        fun getMockCommentList(): MutableList<Comment> {
            val mockUser1 = createMockUser(1001L, "数码达人_小李", "https://xxx.com/avatar1.jpg")
            val mockUser2 = createMockUser(1002L, "爱吃西瓜的喵", "https://xxx.com/avatar2.jpg")
            val mockUser3 = createMockUser(1003L, "隔壁老王", "https://xxx.com/avatar3.jpg", isVip = true)
            val mockUser4 = createMockUser(1004L, "追剧小能手", "https://xxx.com/avatar4.jpg") // 新增用户

            val replyListForComment3 = createMockReplyList(
                targetCommenter = mockUser1,
                replyers = listOf(mockUser2, mockUser3, mockUser4, mockUser1),
                replyCount = 15
            )
            val replyListForComment1 = createMockReplyList(
                targetCommenter = mockUser2,
                replyers = listOf(mockUser3, mockUser1),
                replyCount = 3
            )

            // 2. 构建评论列表（保留原有3条，新增5条，给指定评论添加回复）
            return mutableListOf(
                // 原有评论1：添加3条回复
                Comment(
                    id = 1L,
                    time = System.currentTimeMillis() - 3600000,
                    message = "这台平板看起来不错哦，多少米入的？",
                    commenter = mockUser2,
                    imageUrls = null,
                    replies = replyListForComment1, // 给评论1添加回复
                    hasMoreReply = false // 只有3条回复，无更多
                ),
                // 原有评论2：无回复
                Comment(
                    id = 2L,
                    time = System.currentTimeMillis() - 1800000,
                    message = "我也想入同款，求个购买渠道~",
                    commenter = mockUser3,
                    imageUrls = mutableListOf()
                ),
                // 原有评论3：添加15条回复，用于测试分页
                Comment(
                    id = 3L,
                    time = System.currentTimeMillis() - 600000,
                    message = "我有同款，用了半年，续航确实给力，附一张我的使用实拍~",
                    commenter = mockUser1,
                    imageUrls = mutableListOf(
                        "https://xxx.com/comment_img1.jpg",
                        "https://xxx.com/comment_img2.jpg"
                    ),
                    replies = replyListForComment3, // 给评论3添加15条回复
                    hasMoreReply = true // 模拟有更多回复（可后续扩展分页）
                ),
                // 新增评论4：无回复
                Comment(
                    id = 4L,
                    time = System.currentTimeMillis() - 500000,
                    message = "平板的流畅度怎么样？日常办公够用吗？",
                    commenter = mockUser4,
                    imageUrls = null
                ),
                // 新增评论5：无回复
                Comment(
                    id = 5L,
                    time = System.currentTimeMillis() - 400000,
                    message = "求个平板参数，比如屏幕尺寸、内存大小~",
                    commenter = mockUser2,
                    imageUrls = mutableListOf("https://xxx.com/comment_img3.jpg")
                ),
                // 新增评论6：添加8条回复（测试分页）
                Comment(
                    id = 6L,
                    time = System.currentTimeMillis() - 300000,
                    message = "我用这款平板追剧，续航能撑一天，太香了！",
                    commenter = mockUser4,
                    imageUrls = null,
                    replies = createMockReplyList(
                        targetCommenter = mockUser4,
                        replyers = listOf(mockUser1, mockUser2, mockUser3),
                        replyCount = 8
                    ),
                    hasMoreReply = false
                ),
                // 新增评论7：无回复
                Comment(
                    id = 7L,
                    time = System.currentTimeMillis() - 200000,
                    message = "有没有人用这款平板玩游戏？帧率怎么样？",
                    commenter = mockUser3,
                    imageUrls = mutableListOf()
                ),
                // 新增评论8：添加12条回复（测试分页）
                Comment(
                    id = 8L,
                    time = System.currentTimeMillis() - 100000,
                    message = "已入手，颜值很高，手感也不错~",
                    commenter = mockUser1,
                    imageUrls = mutableListOf("https://xxx.com/comment_img4.jpg"),
                    replies = createMockReplyList(
                        targetCommenter = mockUser1,
                        replyers = listOf(mockUser2, mockUser4, mockUser3),
                        replyCount = 12
                    ),
                    hasMoreReply = true
                )
            )
        }

        private fun createMockReplyList(
            targetCommenter: User,
            replyers: List<User>,
            replyCount: Int
        ): MutableList<Reply> {
            val replyList = mutableListOf<Reply>()
            for (i in 1..replyCount) {
                // 随机选取回复者
                val randomReplyer = replyers[(Math.random() * replyers.size).toInt()]
                // 构建回复内容（区分不同场景）
                val replyMessage = when (i % 4) {
                    0 -> "我也觉得不错，后悔没早点入手！"
                    1 -> "回复 @${targetCommenter.name}：我的入手价是2599，供参考~"
                    2 -> "附一张我的使用截图，流畅度拉满！"
                    3 -> "请问这款平板支持手写笔吗？"
                    else -> "不错不错，种草了！"
                }
                // 构建回复图片（部分回复带图片，模拟真实场景）
                val replyImageUrls = if (i % 5 == 0) {
                    mutableListOf("https://xxx.com/reply_img$i.jpg")
                } else {
                    null
                }
                // 添加到回复列表
                replyList.add(
                    Reply(
                        id = 1000L + i, // 回复唯一ID（避免重复）
                        time = System.currentTimeMillis() - (i * 10000), // 回复时间依次递减
                        message = replyMessage,
                        commenter = randomReplyer,
                        beCommenter = targetCommenter,
                        imageUrls = replyImageUrls
                    )
                )
            }
            return replyList
        }

        private fun createMockUser(
            userId: Long,
            userName: String,
            headPicUrl: String,
            isVip: Boolean = false
        ): User {
            return User(
                name = userName,
                id = userId,
                headPic = headPicUrl,
                isVip = isVip,
                signature = "分享生活，记录美好"
            )
        }
    }
}
