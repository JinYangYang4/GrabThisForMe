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
                "\u5168\u573a\u901a\u7528",
                "\u9910\u996e\u4e13\u7528",
                "\u8dd1\u817f\u4e13\u7528",
                "\u4e8c\u624b\u4e13\u7528"
            )
            val denominations = listOf(20.0f, 10.0f, 5.0f, 30.0f, 50.0f, 8.0f)

            return List(12) { index ->
                val denomination = denominations[index % denominations.size]
                val minOrderAmount = (denomination * 5).toInt()
                val noThreshold = denomination <= 10.0f
                val title = if (noThreshold) {
                    "\u65e0\u95e8\u69db${denomination.toInt()}\u5143\u5238"
                } else {
                    "\u6ee1${minOrderAmount}\u51cf${denomination.toInt()}\u5238"
                }
                val status = if (index % 5 == 2) MallCouponStatus.SOLD_OUT else MallCouponStatus.EXCHANGEABLE

                CouponMallItem(
                    coupon = Coupon(
                        id = index + 1,
                        title = title,
                        denomination = denomination,
                        type = types[index % types.size],
                        desc = if (noThreshold) "\u5c0f\u989d\u8ba2\u5355\u53ef\u76f4\u63a5\u4f7f\u7528" else "\u8ba2\u5355\u6ee1${minOrderAmount}\u5143\u53ef\u7528",
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
                    title = "\u6ee1100\u51cf20\u4f18\u60e0\u5238",
                    denomination = 20.0f,
                    type = "\u5168\u573a\u901a\u7528",
                    desc = "\u8ba2\u5355\u6ee1100\u5143\u53ef\u7528",
                    userStatus = Coupon.UserCouponStatus.UNUSED,
                    expireTime = "2026-06-07 23:59"
                ),
                Coupon(
                    id = 2,
                    title = "\u65e0\u95e8\u69db5\u5143\u5238",
                    denomination = 5.0f,
                    type = "\u5168\u573a\u901a\u7528",
                    desc = "\u6821\u56ed\u5c0f\u989d\u8ba2\u5355\u53ef\u7528",
                    userStatus = Coupon.UserCouponStatus.UNUSED,
                    expireTime = "2026-06-03 23:59"
                )
            )
        }
    }
}
