package com.example.grabthisforme.model.user

import java.util.UUID

internal object UserSampleData {
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
            val virtualUser = User(
                id = baseId++,
                name = userName,
                headPic = templateUser.headPic,
                phone = if (randomName) "138${(10000000..99999999).random()}" else templateUser.phone,
                email = templateUser.email,
                gender = templateUser.gender,
                createTime = System.currentTimeMillis(),
                isVip = vipStatus,
                signature = templateUser.signature,
                isCurrent = templateUser.isCurrent,
                accountName = templateUser.accountName,
                passwordHash = templateUser.account.passwordHash,
                lastLoginTime = templateUser.account.lastLoginTime,
                setting = templateUser.setting
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
                    mockUser = User(
                        id = 10000 + (1000..9999).random().toLong(),
                        name = "测试用户_${UUID.randomUUID().toString().substring(0, 4)}",
                        headPic = "https://example.com/avatar/${(1..10).random()}.png",
                        phone = "138${(10000000..99999999).random()}",
                        email = "test_${UUID.randomUUID().toString().substring(0, 6)}@example.com",
                        gender = (0..1).random(),
                        isVip = Math.random() > 0.5,
                        signature = "这是一个虚拟用户的个性签名"
                    )
                }
            }
        }
        return mockUser!!
    }
}
