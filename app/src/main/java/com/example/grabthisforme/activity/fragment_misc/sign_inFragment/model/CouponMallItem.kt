package com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model

data class CouponMallItem(
    /** 通用优惠券信息（组合关系，复用Coupon类的核心属性） */
    val coupon: Coupon,

    /** 兑换所需金币（商城专属：判断用户金币是否充足） */
    val needCoin: Int,

    /** 优惠券库存（商城专属：-1=无限库存，0=已售罄，>0=剩余库存） */
    val stock: Int = -1,

    /** 商城商品状态（商城专属：区分可兑换/已售罄） */
    val mallStatus: MallCouponStatus,

    /** 是否热门推荐（商城专属：用于商城热门标签展示） */
    val isHot: Boolean = false
){

    /**
     * 优惠券商城状态枚举（仅针对商城可兑换场景）
     */
    enum class MallCouponStatus {
        EXCHANGEABLE,
        SOLD_OUT
    }
    object CouponTestDataSingleton {
        /**
         * 获取优惠券商城测试数据（List<CouponMallItem>）
         * @return 商城可兑换的优惠券商品列表
         */
        fun getCouponMallTestData(): List<CouponMallItem> {
            // 1. 准备基础数据数组（用于循环随机取值，保证数据多样性）
            val couponTypes = arrayOf("全场通用", "食品专用", "家电专用", "数码专用", "美妆专用", "服饰专用", "母婴专用")
            val expireDescs = arrayOf(
                "有效期7天，订单满{}元可用，逾期自动失效",
                "有效期15天，订单满{}元可用，逾期自动失效",
                "有效期30天，订单满{}元可用，逾期自动失效",
                "有效期10天，订单满{}元可用，逾期自动失效",
                "有效期20天，订单满{}元可用，逾期自动失效"
            )
            val totalCount = 20 // 总数据项数

            return mutableListOf<CouponMallItem>().apply {
                // 2. 循环生成20项数据
                for (i in 0 until totalCount) {
                    // 构建唯一ID（从1开始，避免0）
                    val couponId = i + 1

                    // 3. 动态计算/随机分配各项属性（保证数据多样性，符合业务逻辑）
                    // 面额：按索引分段，生成不同档位的面额
                    val denomination = when (i % 5) {
                        0 -> 20.0f
                        1 -> 10.0f
                        2 -> 5.0f
                        3 -> 50.0f
                        4 -> 80.0f
                        else -> 15.0f
                    }

                    // 订单满减金额（与面额匹配，合理设置）
                    val minOrderAmount = (denomination * 5).toInt()

                    // 兑换所需金币（略低于面额对应的价值，合理设置）
                    val needCoin = (denomination * 4.5).toInt()

                    // 优惠券类型（从数组中循环取值）
                    val couponType = couponTypes[i % couponTypes.size]

                    // 有效期描述（从数组中循环取值，替换占位符）
                    val couponDesc = expireDescs[i % expireDescs.size].replace("{}", minOrderAmount.toString())

                    // 优惠券标题（拼接面额和满减金额）
                    val couponTitle = if (denomination == 5.0f || denomination == 10.0f) {
                        "无门槛${denomination}元优惠券"
                    } else {
                        "满${minOrderAmount}减${denomination}元优惠券"
                    }

                    // 热门状态（每4项出现1个热门，循环控制）
                    val isHot = (i % 4) == 0

                    // 商城状态（大部分可兑换，每5项出现1个已售罄，循环控制）
                    val mallStatus = if ((i % 5) == 2) {
                        MallCouponStatus.SOLD_OUT
                    } else {
                        MallCouponStatus.EXCHANGEABLE
                    }

                    // 库存（已售罄对应0，可兑换对应-1（无限库存）或100/50（有限库存））
                    val stock = when (mallStatus) {
                        MallCouponStatus.SOLD_OUT -> 0
                        MallCouponStatus.EXCHANGEABLE -> if ((i % 3) == 0) 100 else if ((i % 6) == 0) 50 else -1
                    }

                    // 4. 构建CouponMallItem并添加到列表
                    add(
                        CouponMallItem(
                            coupon = Coupon(
                                id = couponId,
                                title = couponTitle,
                                denomination = denomination,
                                type = couponType,
                                desc = couponDesc,
                                userStatus = Coupon.UserCouponStatus.UNUSED, // 商城场景占位，不影响使用
                                receiveTime = "",
                                expireTime = ""
                            ),
                            needCoin = needCoin,
                            stock = stock,
                            mallStatus = mallStatus,
                            isHot = isHot
                        )
                    )
                }
            }
        }

        /**
         * 获取用户已持有优惠券测试数据（List<Coupon>）
         * @return 用户已领取的优惠券列表
         */
        fun getUserCouponTestData(): List<Coupon> {
            return mutableListOf<Coupon>().apply {
                // 1. 满100减20（未使用、已领取、有效期内）
                add(
                    Coupon(
                        id = 1,
                        title = "满100减20优惠券",
                        denomination = 20.0f,
                        type = "全场通用",
                        desc = "有效期7天，订单满100元可用，逾期自动失效",
                        userStatus = Coupon.UserCouponStatus.UNUSED,
                        receiveTime = "2026-01-28 10:30:00",
                        expireTime = "2026-02-04 23:59:59"
                    )
                )

                // 2. 满200减50（已使用、已核销）
                add(
                    Coupon(
                        id = 4,
                        title = "满200减50优惠券",
                        denomination = 50.0f,
                        type = "家电专用",
                        desc = "有效期30天，订单满200元可用，逾期自动失效",
                        userStatus =Coupon.UserCouponStatus.USED,
                        receiveTime = "2026-01-01 15:20:00",
                        expireTime = "2026-01-31 23:59:59"
                    )
                )

                // 3. 无门槛5元（已过期、超出有效期）
                add(
                    Coupon(
                        id = 3,
                        title = "无门槛5元优惠券",
                        denomination = 5.0f,
                        type = "全场通用",
                        desc = "有效期3天，无订单金额限制，逾期自动失效",
                        userStatus = Coupon.UserCouponStatus.EXPIRED,
                        receiveTime = "2025-12-25 09:00:00",
                        expireTime = "2025-12-28 23:59:59"
                    )
                )
            }
        }
    }
}

