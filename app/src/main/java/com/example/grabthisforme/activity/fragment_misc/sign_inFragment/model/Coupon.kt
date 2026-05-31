package com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model

data class Coupon(
    val id: Int,
    val title: String,
    val denomination: Float,
    val type: String,
    val desc: String,
    val userStatus: UserCouponStatus,
    val receiveTime: String = "",
    val expireTime: String = ""
) {
    enum class UserCouponStatus {
        UNUSED,
        USED,
        EXPIRED
    }
}
