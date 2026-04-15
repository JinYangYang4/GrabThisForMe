package com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model

/**
 * 通用优惠券类（仅描述优惠券本身，所有场景通用）
 * 适用于：用户已持有、订单抵扣、活动发放等场景
 */
data class Coupon(
    /** 优惠券唯一标识ID */
    val id: Int,

    /** 优惠券标题（如：满100减20、无门槛5元） */
    val title: String,

    /** 优惠券面额（如：20.0元、5.0元） */
    val denomination: Float,

    /** 优惠券适用类型（如：全场通用、食品专用、家电专用） */
    val type: String,

    /** 优惠券描述/使用规则（如：有效期7天、订单满100可用） */
    val desc: String,

    /** 优惠券用户状态（仅针对用户已持有场景） */
    val userStatus: UserCouponStatus,

    /** 领取时间（仅用户已持有场景有效，默认空字符串） */
    val receiveTime: String = "",
    val expireTime: String = ""
){
    enum class UserCouponStatus {
        UNUSED,
        USED,
        EXPIRED  //过期
    }
}

