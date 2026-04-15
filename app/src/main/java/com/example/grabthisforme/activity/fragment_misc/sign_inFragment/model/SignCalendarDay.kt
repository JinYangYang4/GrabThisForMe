package com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model

data class SignCalendarDay(
    val day: Int,        // 日期(1-30)
    val isSigned: Boolean, // 是否已签到
    val isToday: Boolean,  // 是否是今日
    val reward: String     // 当日签到奖励
){
    object SignTestDataSingleton {
        fun getDefault30DaysSignData(): List<SignCalendarDay> {
            val signList = mutableListOf<SignCalendarDay>()
            for (day in 0..6) {
                val sign = SignCalendarDay(
                    day = day,
                    isSigned = day <= 5, // 前5天已签到
                    isToday = day == 6,  // 第6天为今日
                    reward = when {
                        day <= 5 -> "${10 + day % 5}金币" // 已签到的奖励标注「已领取」
                        day == 6 -> "优惠券" // 今日奖励突出展示
                        else -> "${10 + day % 6}金币" // 未来日期奖励随机波动
                    }
                )
                signList.add(sign)
            }
            return signList
        }
    }
}