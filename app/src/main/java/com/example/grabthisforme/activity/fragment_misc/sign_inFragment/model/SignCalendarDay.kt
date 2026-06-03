package com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model

data class SignCalendarDay(
    val day: Int,
    val isSigned: Boolean,
    val isToday: Boolean,
    val reward: String
) {
    object SignTestDataSingleton {
        fun getDefault30DaysSignData(): List<SignCalendarDay> {
            return (0..6).map { day ->
                SignCalendarDay(
                    day = day,
                    isSigned = day <= 5,
                    isToday = day == 6,
                    reward = when {
                        day <= 5 -> "${10 + day % 5}金币"
                        day == 6 -> "优惠券"
                        else -> "${10 + day % 6}金币"
                    }
                )
            }
        }
    }
}
