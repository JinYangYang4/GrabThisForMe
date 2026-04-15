package com.example.grabthisforme.model.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model.Coupon
import java.util.UUID

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: Long,
    val isCurrent: Boolean = false,
    val name: String,
    val headPic: String,
    val phone: String? = null,
    val email: String? = null,
    val gender: Int = 0,
    val createTime: Long = System.currentTimeMillis(),
    val isVip: Boolean = false,
    val signature: String? = null,
//    val coupon: List<Coupon>? = null,
//    val loveStoreId : List<Long>? = null
) {
    fun getInfoSummary(): String {
        return "用户ID：$id，昵称：$name，VIP状态：${if (isVip) "是" else "否"}"
    }

    fun isBasicInfoComplete(): Boolean {
        return name.isNotBlank() && headPic.isNotBlank()
    }
    companion object {
        fun createVirtualUsers(
            templateUser: User,
            count: Int,
            randomName: Boolean = true,
            randomVip: Boolean = false
        ): List<User> {
            if (count <= 0) return emptyList()

            val virtualUsers = mutableListOf<User>()
            var baseId = templateUser.id + 1

            repeat(count) { index ->
                val userName = if (randomName) {
                    val randomStr = UUID.randomUUID().toString().substring(0, 6)
                    "${templateUser.name}_$randomStr"
                } else {
                    "${templateUser.name}_虚拟${index + 1}"
                }
                val vipStatus = if (randomVip) (Math.random() > 0.7) else templateUser.isVip
                val virtualUser = templateUser.copy(
                    id = baseId++,
                    name = userName,
                    isVip = vipStatus,
                    phone = if (randomName) "138${(10000000..99999999).random()}" else templateUser.phone,
                    createTime = System.currentTimeMillis() // 重新生成创建时间
                )
                virtualUsers.add(virtualUser)
            }
            return virtualUsers
        }

        @Volatile
        private var mockUser: User? = null

        fun getVirtualUser(): User {
            if (mockUser == null) {
                synchronized(this) {
                    if (mockUser == null) {
                        // 构建默认的虚拟用户数据（可根据需求调整）
                        mockUser = User(
                            name = "测试用户_${UUID.randomUUID().toString().substring(0, 4)}",
                            id = 10000 + (1000..9999).random().toLong(), // 随机ID（10000-19999）
                            headPic = "https://example.com/avatar/${(1..10).random()}.png", // 占位头像URL
                            phone = "138${(10000000..99999999).random()}", // 随机手机号
                            email = "test_${
                                UUID.randomUUID().toString().substring(0, 6)
                            }@example.com", // 随机邮箱
                            gender = (0..1).random(), // 0=未知，1=男，2=女（这里随机0/1）
                            isVip = Math.random() > 0.5, // 50%概率为VIP
                            signature = "这是一个虚拟用户的个性签名~"
                        )
                    }
                }
            }
            return mockUser!!
        }
    }
}