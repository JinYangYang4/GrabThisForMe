package com.example.grabthisforme.model.Post

import com.example.grabthisforme.model.user.User

// 说说
data class Post(
    val postId: String,
    val content: String,
    val images: List<String>? = null,
    val createTime: Long,
    val author: User,
    val likeCount: Int,
    val commentCount: Int
) {
    companion object {

        private var userList: List<User>? = null
        private var postList: List<Post>? = null

        fun getUserList(): List<User> {
            if (userList == null) {
                userList = listOf(
                    User(
                        name = "小明",
                        id = 30001L,
                        headPic = "avatar_xiaoming",
                        phone = "13800000001",
                        gender = 1,
                        isVip = true,
                        signature = "今天也要努力生活"
                    ),
                    User(
                        name = "小红",
                        id = 30002L,
                        headPic = "avatar_xiaohong",
                        phone = "13800000002",
                        gender = 0,
                        isVip = false,
                        signature = "喜欢拍照和旅行"
                    ),
                    User(
                        name = "阿强",
                        id = 30003L,
                        headPic = "avatar_aqiang",
                        phone = "13800000003",
                        gender = 1,
                        isVip = false,
                        signature = "代码改变世界"
                    )
                )
            }
            return userList!!
        }

        // ---------- 说说列表 ----------
        fun getPostList(): List<Post> {
            if (postList == null) {
                val users = getUserList()
                val now = System.currentTimeMillis()

                postList = listOf(
                    Post(
                        postId = "POST_${now - 100000}",
                        content = "今天阳光很好，心情也不错 ☀️",
                        images = listOf("post_img_1"),
                        createTime = now - 10 * 60 * 1000,
                        author = users[0],
                        likeCount = 12,
                        commentCount = 3
                    ),
                    Post(
                        postId = "POST_${now - 90000}",
                        content = "刚看完一部电影，真的很感动。",
                        images = null,
                        createTime = now - 30 * 60 * 1000,
                        author = users[1],
                        likeCount = 5,
                        commentCount = 1
                    ),
                    Post(
                        postId = "POST_${now - 80000}",
                        content = "加班到现在，程序员的日常 😵‍💫",
                        images = listOf("post_img_2", "post_img_3"),
                        createTime = now - 60 * 60 * 1000,
                        author = users[2],
                        likeCount = 23,
                        commentCount = 8
                    ),
                    Post(
                        postId = "POST_${now - 70000}",
                        content = "周末去爬山了，风景超棒！",
                        images = listOf("post_img_4"),
                        createTime = now - 2 * 60 * 60 * 1000,
                        author = users[0],
                        likeCount = 34,
                        commentCount = 6
                    ),
                    Post(
                        postId = "POST_${now - 60000}",
                        content = "最近在学 Kotlin，感觉越来越顺手了。",
                        images = null,
                        createTime = now - 3 * 60 * 60 * 1000,
                        author = users[2],
                        likeCount = 18,
                        commentCount = 4
                    ),
                    Post(
                        postId = "POST_${now - 50000}",
                        content = "分享一张今天拍的天空 🌤️",
                        images = listOf("post_img_5"),
                        createTime = now - 5 * 60 * 60 * 1000,
                        author = users[1],
                        likeCount = 42,
                        commentCount = 11
                    )
                )
            }
            return postList!!
        }
    }
}
