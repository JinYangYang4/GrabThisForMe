package com.example.grabthisforme.model.user

import java.util.UUID

data class User(
    val name: String,
    val id: Long,
    val headPic: String,
    val phone: String? = null,
    val email: String? = null,
    val gender: Int = 0,
    val createTime: Long = System.currentTimeMillis(),
    val isVip: Boolean = false,
    val signature: String? = null       // 个性签名
) {
    // 常用方法 - 获取用户信息摘要
    fun getInfoSummary(): String {
        return "用户ID：$id，昵称：$name，VIP状态：${if (isVip) "是" else "否"}"
    }

    // 判断用户是否完善基础信息
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
    }
}