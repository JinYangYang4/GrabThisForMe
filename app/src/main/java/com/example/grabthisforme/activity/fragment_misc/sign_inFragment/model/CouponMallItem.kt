package com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model

data class CouponMallItem(
    val coupon: Coupon,
    val needCoin: Int,
    val stock: Int = -1,
    val mallStatus: MallCouponStatus,
    val isHot: Boolean = false
) {
    enum class MallCouponStatus {
        EXCHANGEABLE,
        SOLD_OUT
    }

    object CouponTestDataSingleton {
        fun getCouponMallTestData(): List<CouponMallItem> {
            val types = listOf(
                "全场通用",
                "餐饮专用",
                "跑腿专用",
                "二手专用"
            )
            val denominations = listOf(20.0f, 10.0f, 5.0f, 30.0f, 50.0f, 8.0f)

            return List(12) { index ->
                val denomination = denominations[index % denominations.size]
                val minOrderAmount = (denomination * 5).toInt()
                val noThreshold = denomination <= 10.0f
                val title = if (noThreshold) {
                    "无门槛${denomination.toInt()}元券"
                } else {
                    "满${minOrderAmount}减${denomination.toInt()}券"
                }
                val status = if (index % 5 == 2) MallCouponStatus.SOLD_OUT else MallCouponStatus.EXCHANGEABLE

                CouponMallItem(
                    coupon = Coupon(
                        id = index + 1,
                        title = title,
                        denomination = denomination,
                        type = types[index % types.size],
                        desc = if (noThreshold) "小额订单可直接使用" else "订单满${minOrderAmount}元可用",
                        userStatus = Coupon.UserCouponStatus.UNUSED
                    ),
                    needCoin = (denomination * 4).toInt(),
                    stock = if (status == MallCouponStatus.SOLD_OUT) 0 else 50 + index * 3,
                    mallStatus = status,
                    isHot = index % 4 == 0
                )
            }
        }

        fun getUserCouponTestData(): List<Coupon> {
            return listOf(
                Coupon(
                    id = 1,
                    title = "满100减20优惠券",
                    denomination = 20.0f,
                    type = "全场通用",
                    desc = "订单满100元可用",
                    userStatus = Coupon.UserCouponStatus.UNUSED,
                    expireTime = "2026-06-07 23:59"
                ),
                Coupon(
                    id = 2,
                    title = "无门槛5元券",
                    denomination = 5.0f,
                    type = "全场通用",
                    desc = "校园小额订单可用",
                    userStatus = Coupon.UserCouponStatus.UNUSED,
                    expireTime = "2026-06-03 23:59"
                )
            )
        }
    }
}
